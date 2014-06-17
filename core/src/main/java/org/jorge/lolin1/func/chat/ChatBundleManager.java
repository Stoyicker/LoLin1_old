package org.jorge.lolin1.func.chat;

import android.os.Bundle;
import android.os.Parcelable;

import com.github.theholywaffle.lolchatapi.wrapper.Friend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 * Created by Jorge Antonio Diaz-Benito Soriano on 04/05/2014.
 */
public abstract class ChatBundleManager {

    private static final Map<Friend, Bundle> BUNDLES = new HashMap<>();
    public static final String KEY_MESSAGE_ARRAY = "LOL_CHAT_MESSAGES";

    public static Bundle getBundleByFriend(Friend f) {
        return BUNDLES.containsKey(f) ? BUNDLES.get(f) : Bundle.EMPTY;
    }

    public static void addMessageToFriendChat(ChatMessageWrapper msg, Friend chatSubject) {
        Bundle currentBundle = getBundleByFriend(chatSubject);
        ArrayList<Parcelable> messages;
        if (currentBundle == null) {
            currentBundle = new Bundle();
            messages = new ArrayList<>();
        } else {
            messages = currentBundle.getParcelableArrayList(KEY_MESSAGE_ARRAY);
        }
        messages.add(msg);
        currentBundle.putParcelableArrayList(KEY_MESSAGE_ARRAY, messages);
        BUNDLES.put(chatSubject, currentBundle);
    }
}
