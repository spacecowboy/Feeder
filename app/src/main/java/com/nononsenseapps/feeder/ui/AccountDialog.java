package com.nononsenseapps.feeder.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.AuthHelper;

import java.io.IOException;


public class
        AccountDialog extends DialogFragment implements
        AccountManagerCallback<Bundle> {

    private Account mAccount = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_account);
        final Account[] accounts = AccountManager.get(getActivity())
                .getAccountsByType(AuthHelper.GOOGLE_ACCOUNT_TYPE);
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
            this.mAccount = account;
            // Request user's permission
            AccountManager.get(getActivity()).getAuthToken(account,
                    AuthHelper.SCOPE, null, getActivity(), this, null);
            // work continues in callback, method run()
        }
    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        try {
            String token = future.getResult().getString(
                    AccountManager.KEY_AUTHTOKEN);
            if (token != null && !token.isEmpty() && mAccount != null) {
                // Valid token
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString(AuthHelper.KEY_ACCOUNT, mAccount.name).commit();
                // Request sync
                RssContentProvider.RequestSync(getActivity());
            }
        } catch (OperationCanceledException e) {
            // if the request was canceled for any reason
        }
        catch (AuthenticatorException e) {
            // if there was an error communicating with the authenticator or
            // if the authenticator returned an invalid response
        }
        catch (IOException e) {
            // if the authenticator returned an error response that
            // indicates that it encountered an IOException while
            // communicating with the authentication server
        }
    }
}