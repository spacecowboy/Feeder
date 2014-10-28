package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.preference.PreferenceManager;

import java.io.IOException;

public class AuthHelper {

    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final String KEY_ACCOUNT = "key_account";
    public static final String SCOPE =
            "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    static final String TAG = "SyncHelper";

    public static String getSavedAccountName(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(AuthHelper.KEY_ACCOUNT, null);
    }

    public static String getAuthToken(final Context context) {
        final String accountName = getSavedAccountName(context);
        if (accountName == null || accountName.isEmpty()) {
            return null;
        }

        return getAuthToken(context, accountName);
    }

    /**
     * Only use this in a background thread
     */
    public static String getAuthToken(final Context context,
            final String accountName) {
        String authToken = "";
        try {
            // Might be invalid in the cache
            AccountManager accountManager = AccountManager.get(context);
            authToken = accountManager.blockingGetAuthToken(getAccount(context, accountName),
                    SCOPE, true);
            accountManager.invalidateAuthToken(GOOGLE_ACCOUNT_TYPE, authToken);

            authToken = accountManager.blockingGetAuthToken(getAccount(context, accountName),
                    SCOPE, true);

            return "Bearer " + authToken;
        }
        catch (OperationCanceledException ignored) {
        }
        catch (AuthenticatorException ignored) {
        }
        catch (IOException ignored) {
        }
        return null;
    }

    public static Account getSavedAccount(final Context context) {
        return getAccount(context, getSavedAccountName(context));
    }

    public static Account getAccount(final Context context,
            final String accountName) {
        final AccountManager manager = AccountManager.get(context);
        Account[] accounts =
                manager.getAccountsByType(GOOGLE_ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }
}