package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.content.Context;
import android.preference.PreferenceManager;

public class AuthHelper {

  public static final String KEY_ACCOUNT = "key_account";
  public static final String SCOPE =
      "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
  static final String TAG = "SyncHelper";

  public static String getAuthToken(final Context context) {
    final String accountName = getSavedAccountName(context);
    if (accountName == null || accountName.isEmpty()) {
      return null;
    }

    return getAuthToken(context, accountName);
  }

  public static String getSavedAccountName(final Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(AuthHelper.KEY_ACCOUNT, null);
  }

  /**
   * Only use this in a background thread
   */
  public static String getAuthToken(final Context context,
      final String accountName) {
    // Not available without play services
    return null;
  }

  public static Account getSavedAccount(final Context context) {
    return getAccount(context, getSavedAccountName(context));
  }

  public static Account getAccount(final Context context,
      final String accountName) {
    // Not available without play services
    return null;
  }
}