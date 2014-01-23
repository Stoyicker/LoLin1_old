package org.jorge.lolin1.io.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import org.jorge.lolin1.R;
import org.jorge.lolin1.utils.Utils;
import org.jorge.lolin1.utils.feeds.news.NewsEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This file is part of LoLin1.
 * <p/>
 * LoLin1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * LoLin1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with LoLin1. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by JorgeAntonio on 04/01/14.
 */
public class NewsToSQLiteBridge extends SQLiteOpenHelper {

    public static final String LOLNEWS_FEED_HOST = "http://feed43.com/", LOLNEWS_FEED_EXTENSION =
            ".xml", NEWS_KEY_ID = "ARTICLE_ID", NEWS_KEY_TITLE = "ARTICLE_TITLE", NEWS_KEY_BLOB =
            "ARTICLE_BLOB",
            NEWS_KEY_URL = "ARTICLE_URL", NEWS_KEY_DESC = "ARTICLE_DESC", NEWS_KEY_IMG_URL =
            "ARTICLE_IMG_URL";
    private static NewsToSQLiteBridge singleton;
    private static Context mContext;

    private NewsToSQLiteBridge(Context _context) {
        super(_context, Utils.getString(_context, "db_name", "LoLin1_DB"), null, 1);
    }

    public static void setup(Context _context) {
        if (singleton == null) {
            singleton = new NewsToSQLiteBridge(_context);
            mContext = _context;
        }
    }

    public static NewsToSQLiteBridge getSingleton() {
        if (singleton == null) {
            throw new RuntimeException(
                    "Use method NewsToSQLiteBridge.setup(Context _context) at least once before calling this method.");
        }
        return singleton;
    }

    public static String getTableName(Context context) {
        String prefix =
                Utils.getString(context, "news_euw_en", "http://feed43.com/lolnews_euw_en.xml")
                        .replaceAll(NewsToSQLiteBridge.LOLNEWS_FEED_HOST, "")
                        .replaceAll(NewsToSQLiteBridge.LOLNEWS_FEED_EXTENSION, "")
                        .replaceAll("_(.*)", "") + "_";
        String ret, server, lang, langSimplified;
        final String ERROR = "PREF_NOT_FOUND", defaultTableName = "EUW_EN", defaultLanguage =
                "ENGLISH";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        server = preferences.getString(Utils.getString(context, "pref_title_server", "nvm"), ERROR);
        lang = preferences.getString(Utils.getString(context, "pref_title_lang", "nvm"), ERROR);

        if (server.contentEquals(ERROR) || lang.contentEquals(ERROR)) {
            ret = prefix + defaultTableName;
        }
        else {
            langSimplified = Utils.getStringArray(context, "langs_simplified",
                    new String[]{defaultLanguage})[new ArrayList<>(
                    Arrays.asList(Utils.getStringArray(context, "langs",
                            new String[]{defaultLanguage})))
                    .indexOf(lang)];
            ret = prefix + server + "_" + langSimplified;
        }

        return ret.toUpperCase();
    }

    public static final Bitmap getArticleBitmap(Context context, byte[] blob,
                                                final String imageLinkCallbackURL) {
        Bitmap ret = null;
        if (blob == null) {
            if (Utils.isInternetReachable(context)) {
                try {
                    ret = BitmapFactory
                            .decodeStream(
                                    new URL(imageLinkCallbackURL).openConnection()
                                            .getInputStream());
                }
                catch (IOException e) {
                    ret = null;
                }
                ByteArrayOutputStream blobOS = new ByteArrayOutputStream();
                ret.compress(Bitmap.CompressFormat.PNG, 0, blobOS);
                byte[] bmpAsByteArray = blobOS.toByteArray();
                NewsToSQLiteBridge.getSingleton()
                        .updateArticleBlob(bmpAsByteArray, imageLinkCallbackURL);
                return ret;
            }
        }
        else {
            ret = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return ret;
    }

    public byte[] getArticleBlob(String imageUrl) {
        byte[] ret;
        String tableName = getTableName(mContext);
        SQLiteDatabase db = getReadableDatabase();
        Cursor result;

        db.beginTransaction();
        result = db.query(tableName, new String[]{NEWS_KEY_BLOB},
                NEWS_KEY_IMG_URL + "='" + imageUrl.replaceAll("http://", "httpxxx") + "'",
                null, null, null, null, null);
        result.moveToFirst();
        try {
            ret = result.getBlob(0);
        }
        catch (IndexOutOfBoundsException ex) { //Meaning there's no blob stored yet
            ret = null;
        }
        result.close();

        db.setTransactionSuccessful();
        db.endTransaction();
        return ret;
    }

    public final ArrayList<NewsEntry> getNews() {
        return getFilteredNews(null);
    }

    public final ArrayList<NewsEntry> getNewNews(int alreadyShown) {
        return getFilteredNews(NEWS_KEY_ID + " > " + alreadyShown);
    }

    private final ArrayList<NewsEntry> getFilteredNews(String whereClause) {

        ArrayList<NewsEntry> ret = new ArrayList<>();
        String[] fields =
                new String[]{NEWS_KEY_BLOB, NEWS_KEY_IMG_URL, NEWS_KEY_URL, NEWS_KEY_TITLE,
                        NEWS_KEY_DESC};
        ArrayList<String> fieldsAsList = new ArrayList<>(Arrays.asList(fields));

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        String tableName = getTableName(mContext);
        Cursor result =
                db.query(tableName, fields, whereClause, null, null, null, NEWS_KEY_ID + " DESC");
        StringBuilder data = new StringBuilder("");
        final String separator = NewsEntry.getSEPARATOR();
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            for (String x : fieldsAsList) {
                if (!x.contentEquals(NEWS_KEY_BLOB)) {
                    if (x.contentEquals(NEWS_KEY_URL)) {
                        data.append(
                                result.getString(result.getColumnIndex(x))
                                        .replaceAll("httpxxx", "http://"))
                                .append(separator);
                    }
                    else {
                        data.append(result.getString(result.getColumnIndex(x))).append(separator);
                    }
                }
            }
            ret.add(new NewsEntry(data.toString(), result.getBlob(0)));
            data = new StringBuilder("");
        }
        result.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return ret;
    }

    public final void updateArticleBlob(byte[] blob, String imgUrl) {
        SQLiteDatabase db = getWritableDatabase();
        String tableName = getTableName(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(NEWS_KEY_BLOB, blob);

        db.beginTransaction();
        int rowsUpdated = db.update(tableName, contentValues,
                NEWS_KEY_IMG_URL + " = '" + imgUrl.replaceAll("http://", "httpxxx") + "'", null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * @param values {@link android.content.ContentValues} The values to insert. The most recent article is the latest one
     * @return The rowID of the newly inserted row, or -1 if any error happens
     */
    public long insertArticle(ContentValues values) {
        long ret;
        SQLiteDatabase db = getWritableDatabase();
        String tableName = getTableName(mContext);
        db.beginTransaction();
        ret = db.insert(tableName, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        return ret;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        HashSet<String> tableNames = new HashSet<>();
        ArrayList<String> langsInThisServer, servers = new ArrayList<>(
                Arrays.asList(mContext.getResources().getStringArray(R.array.servers)));
        String prefix =
                Utils.getString(mContext, "news_euw_en", "http://feed43.com/lolnews_euw_en.xml")
                        .replaceAll(LOLNEWS_FEED_HOST, "").replaceAll(LOLNEWS_FEED_EXTENSION, "")
                        .replaceAll("_(.*)", "") + "_";

        for (String x : servers) {
            langsInThisServer = new ArrayList<>();
            for (String y : Utils
                    .getStringArray(mContext, "lang_" + x.toLowerCase() + "_simplified",
                            new String[]{""})) {
                langsInThisServer.add((prefix + x + "_" + y).toUpperCase());
            }
            tableNames.addAll(langsInThisServer);
        }

        sqLiteDatabase.beginTransaction();
        for (Iterator<String> it = tableNames.iterator(); it.hasNext(); ) {
            String tableName = it.next();
            sqLiteDatabase.execSQL(("CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    NEWS_KEY_ID + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                    NEWS_KEY_TITLE + " TEXT NOT NULL ON CONFLICT FAIL, " +
                    NEWS_KEY_BLOB + " BLOB, " +
                    NEWS_KEY_URL + " TEXT NOT NULL ON CONFLICT FAIL UNIQUE ON CONFLICT IGNORE, " +
                    NEWS_KEY_DESC + " TEXT, " +
                    NEWS_KEY_IMG_URL + " TEXT NOT NULL ON CONFLICT FAIL " +
                    ")").toUpperCase());
        }

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //At the moment no further database versions are planned.
    }
}