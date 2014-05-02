package org.jorge.lolin1.ui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import org.jorge.lolin1.R;
import org.jorge.lolin1.func.auth.LoLin1Authenticator;
import org.jorge.lolin1.ui.frags.AcceptCredentialsFragment;
import org.jorge.lolin1.ui.frags.AccountCredentialsComponentFragment;

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
 * Created by JorgeAntonio on 01/05/2014.
 */
public class LoLChatAccountAuthenticationActivity extends Activity implements
        AcceptCredentialsFragment.AcceptCredentialsListener,
        AccountCredentialsComponentFragment.AccountCredentialsComponentListener {

    public static final String KEY_REALM = "REALM";
    public static final String KEY_NEW_ACCOUNT = "NEW_ACCOUNT";
    public static final String KEY_RESPONSE = "RESPONSE";
    private AccountCredentialsComponentFragment USERNAME_COMPONENT_FRAGMENT,
            PASSWORD_COMPONENT_FRAGMENT;
    private AcceptCredentialsFragment ACCEPT_CREDENTIALS_FRAGMENT;

    @Override
    public void onFieldUpdated() {
        ACCEPT_CREDENTIALS_FRAGMENT.setEnabled(
                !TextUtils.isEmpty(USERNAME_COMPONENT_FRAGMENT.getContents()) &&
                        !TextUtils.isEmpty(PASSWORD_COMPONENT_FRAGMENT.getContents())
        );
    }

    public enum AccountType {
        CHAT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);

        getActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        USERNAME_COMPONENT_FRAGMENT =
                (AccountCredentialsComponentFragment) getFragmentManager()
                        .findFragmentById(R.id.username_fragment);
        PASSWORD_COMPONENT_FRAGMENT =
                (AccountCredentialsComponentFragment) getFragmentManager()
                        .findFragmentById(R.id.password_fragment);
        ACCEPT_CREDENTIALS_FRAGMENT =
                (AcceptCredentialsFragment) getFragmentManager()
                        .findFragmentById(R.id.accept_credentials);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up button
                finish();
                return Boolean.TRUE;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCredentialsAccepted() {
        CharSequence username = USERNAME_COMPONENT_FRAGMENT.getContents(), password =
                PASSWORD_COMPONENT_FRAGMENT.getContents(), authToken =
                username + LoLin1Authenticator.TOKEN_GENERATION_JOINT + password;
        final Intent res = new Intent();
        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountType.CHAT);
        finishLogin(res);
    }

    private void finishLogin(Intent intent) {
        AccountManager accountManager = AccountManager.get(getBaseContext());
        String[] processedToken =
                intent.getStringArrayExtra(AccountManager.KEY_AUTHTOKEN).toString()
                        .split(LoLin1Authenticator.TOKEN_GENERATION_JOINT);
        String accountName = processedToken[0];
        String accountPassword = processedToken[1];
        final Account account =
                new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        if (getIntent().getBooleanExtra(KEY_NEW_ACCOUNT, false)) {
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            accountManager.addAccountExplicitly(account, accountPassword, null);
            accountManager.setAuthToken(account, "none", authToken);
        }
        else {
            accountManager.setPassword(account, accountPassword);
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
