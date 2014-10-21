package com.nononsenseapps.feeder.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.AuthHelper;

import java.io.IOException;


public class AccountDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_account);
        final Account[] accounts = AccountManager.get(getActivity())
                .getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        final int size = accounts.length;
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = accounts[i].name;
        }
        // Could add a clear alternative here
        builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Stuff to do when the account is selected by the user
                accountSelected(accounts[which]);
            }
        });
        return builder.create();
    }

    /**
     * Called from the activity, since that one builds the dialog
     *
     * @param account
     */
    public void accountSelected(Account account) {
        if (account != null) {
            //new TokenGetter().execute(account.name);
            new GetTokenTask(getActivity(), account.name, AuthHelper.SCOPE)
                    .execute();
        }
    }

    public class GetTokenTask extends AsyncTask<Void, Void, String> {
        public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
        public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR =
                1002;
        private static final String TAG = "TokenInfoTask";
        protected String mScope;
        protected String mEmail;
        protected Activity mActivity;

        public GetTokenTask(Activity activity, String email, String scope) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = email;
        }

        @Override
        protected String doInBackground(Void... params) {
            String token = null;

            try {
                token = fetchToken();
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }

            Log.d(TAG, "Token: " + token);

            if (token != null) {
                PreferenceManager.getDefaultSharedPreferences(mActivity).edit()
                        .putString(AuthHelper.KEY_ACCOUNT, mEmail).commit();
                RssContentProvider.RequestSync(mActivity);
            }

            return token;
        }

        /**
         * Get an authentication token if one is not available. If the error is
         * not recoverable then it displays the error message on parent activity.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (GooglePlayServicesAvailabilityException playEx) {
                // GooglePlayServices.apk is either old, disabled, or not present.
                //mActivity.showErrorDialog(playEx.getConnectionStatusCode());
                Log.e(TAG, "PlayServices code " +
                           playEx.getConnectionStatusCode());
            } catch (UserRecoverableAuthException userRecoverableException) {
                // Unable to authenticate, but the user can fix this.
                // Forward the user to the appropriate activity.
                mActivity.startActivityForResult(
                        userRecoverableException.getIntent(),
                        REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
            } catch (GoogleAuthException fatalException) {
                onError("Unrecoverable error " + fatalException.getMessage(),
                        fatalException);
            }
            return null;
        }

        protected void onError(String msg, Exception e) {
            if (e != null) {
                Log.e(TAG, "Exception: ", e);
            }
        }
    }
}