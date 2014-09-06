package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.nononsenseapps.feeder.db.RssContentProvider;

import java.io.IOException;

public class SyncHelper {

    public static final String KEY_ACCOUNT = "key_account";
    public static final String SCOPE =
            "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    static final String TAG = "SyncHelper";

    public static String getSavedAccountName(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SyncHelper.KEY_ACCOUNT, null);
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
        try {
            return "Bearer " +
                   GoogleAuthUtil.getTokenWithNotification(context, accountName,
                           SCOPE, null);
        } catch (UserRecoverableNotifiedException userRecoverableException) {
            // Unable to authenticate, but the user can fix this.
            Log.e(TAG, "Could not fetch token: " +
                       userRecoverableException.getMessage());
        } catch (GoogleAuthException fatalException) {
            Log.e(TAG, "Unrecoverable error " + fatalException.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public static Account getAccount(final Context context,
            final String accountName) {
        final AccountManager manager = AccountManager.get(context);
        Account[] accounts =
                manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }
}