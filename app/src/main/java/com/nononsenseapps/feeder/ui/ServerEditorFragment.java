package com.nononsenseapps.feeder.ui;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.AuthHelper;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.FloatLabelLayout;

import java.io.IOException;

/**
 * Fragment allowing the user to configure the server settings.
 */
public class ServerEditorFragment extends DialogFragment implements
        AccountManagerCallback<Bundle> {
    private static final long ANIMATION_DURATION_OUT = 100;
    private static final long ANIMATION_DURATION_IN = 150;

    private View mFrameUserPass;
    private TextView mServerText;
    private TextView mUserText;
    private TextView mPassText;
    private SwitchCompat mGoogleSwitch;
    private FloatLabelLayout mServerLabel;
    private FloatLabelLayout mUserLabel;
    private FloatLabelLayout mPassLabel;
    private LinearLayout mAccountList;
    private CheckedTextView mSelectedAccountView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_server_editor, container, false);

        mFrameUserPass = root.findViewById(R.id.frame_userpass);
        mServerText = (TextView) root.findViewById(R.id.server_text);
        mServerLabel = ((FloatLabelLayout) mServerText.getParent());
        mServerText.setText(PrefUtils.getServerUrl(getActivity()));

        mUserText = (TextView) root.findViewById(R.id.user_text);
        mUserLabel = ((FloatLabelLayout) mUserText.getParent());
        mUserText.setText(PrefUtils.getUsername(getActivity(), ""));
        mPassText = (TextView) root.findViewById(R.id.password_text);
        mPassLabel = ((FloatLabelLayout) mPassText.getParent());
        mPassText.setText(PrefUtils.getPassword(getActivity(), ""));

        mAccountList = (LinearLayout) root.findViewById(R.id.account_list);
        populateAccountList();

        mGoogleSwitch = (SwitchCompat) root.findViewById(R.id.switch_google);
        mGoogleSwitch.setSwitchPadding((int) getResources().getDimension(R.dimen.switch_padding));
        mGoogleSwitch.setChecked(PrefUtils.getUseGoogleAccount(getActivity()));
        mGoogleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAccountList();
                    hideUserPassFrame();
                } else {
                    hideAccountList();
                    showUserPassFrame();
                }
            }
        });
        // Do it separately without animation
        if (PrefUtils.getUseGoogleAccount(getActivity())) {
            mAccountList.setVisibility(View.VISIBLE);
            mFrameUserPass.setVisibility(View.GONE);
        } else {
            mAccountList.setVisibility(View.GONE);
            mFrameUserPass.setVisibility(View.VISIBLE);
        }

        root.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        root.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        return root;
    }

    private void onOkClicked() {
        // don't do shit unless fields are valid and filled in
        if (!validateFields()) {
            return;
        }

        // Save current
        PrefUtils.setServerUrl(getActivity(), mServerText.getText().toString());
        PrefUtils.setUseGoogleAccount(getActivity(), mGoogleSwitch.isChecked());
        if (mGoogleSwitch.isChecked()) {
            PrefUtils.setUsername(getActivity(), mSelectedAccountView.getText().toString());
            PrefUtils.setPassword(getActivity(), null);
        } else {
            PrefUtils.setUsername(getActivity(), mUserText.getText().toString());
            PrefUtils.setPassword(getActivity(), mPassText.getText().toString());
        }

        // TODO clear database and request sync with new settings

        // And close
        dismiss();
    }

    /**
     *
     * @return true if all visible fields are OK to save
     */
    private boolean validateFields() {
        boolean result = true;
        // Server url first
        // TODO use string resources
        boolean serverValid = mServerText.getText().length() > 0;
        result &= serverValid;
        if (!serverValid) {
            mServerLabel.showError("Missing URL");
        }

        // User pass, or account
        if (mGoogleSwitch.isChecked()) {
            // Need to have selected an account
            result &= mSelectedAccountView != null;
        } else {
            // Check if user has typed anything
            boolean userValid = mUserText.getText().length() > 0;
            result &= userValid;
            if (!userValid) {
                mUserLabel.showError("Missing username");
            }

            // Check if user has typed anything
            boolean passValid = mPassText.getText().length() > 0;
            result &= passValid;
            if (!passValid) {
                mPassLabel.showError("Missing password");
            }
        }

        // Returned total AND-result
        return result;
    }

    private void showUserPassFrame() {
        mFrameUserPass.setVisibility(View.VISIBLE);
        mFrameUserPass.setAlpha(0f);
        mFrameUserPass.setTranslationX(mFrameUserPass.getWidth());
        mFrameUserPass.animate()
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator(0.7f))
                .translationX(0f)
                .setDuration(ANIMATION_DURATION_IN)
                .setListener(null).start();
    }

    private void hideUserPassFrame() {
        mFrameUserPass.setAlpha(1f);
        mFrameUserPass.setTranslationX(0f);
        mFrameUserPass.animate()
                .alpha(0f)
                .translationX(mFrameUserPass.getWidth())
                .setInterpolator(new AccelerateInterpolator(0.6f))
                .setDuration(ANIMATION_DURATION_OUT)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFrameUserPass.setVisibility(View.GONE);
                    }
                }).start();
    }

    private void showAccountList() {
        mAccountList.setVisibility(View.VISIBLE);
        mAccountList.setAlpha(0f);
        mAccountList.setTranslationX(-mAccountList.getWidth());
        mAccountList.animate()
                .alpha(1f)
                .translationX(0f)
                .setInterpolator(new DecelerateInterpolator(0.7f))
                .setDuration(ANIMATION_DURATION_IN)
                .setListener(null).start();
    }

    private void hideAccountList() {
        mAccountList.setAlpha(1f);
        mAccountList.setTranslationX(0f);
        mAccountList.animate()
                .alpha(0f)
                .translationX(-mAccountList.getWidth())
                .setInterpolator(new AccelerateInterpolator(0.6f))
                .setDuration(ANIMATION_DURATION_OUT)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAccountList.setVisibility(View.GONE);
                    }
                }).start();
    }

    private void populateAccountList() {
        final String currentAccountName = PrefUtils.getUsername(getActivity(), null);
        final Account[] accounts = AccountManager.get(getActivity())
                .getAccountsByType(AuthHelper.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length == 0) {
            // TODO add empty view here
        }
        for (final Account account : accounts) {
            final CheckedTextView itemView = (CheckedTextView) LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_single_choice,
                    mAccountList, false);
            itemView.setText(account.name);
            if (account.name.equals(currentAccountName)) {
                itemView.setChecked(true);
                mSelectedAccountView = itemView;
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedAccountView != null) {
                        mSelectedAccountView.setChecked(false);
                    }
                    itemView.setChecked(true);
                    mSelectedAccountView = itemView;
                    // Request user's permission
                    AccountManager.get(getActivity()).getAuthToken(account,
                            AuthHelper.SCOPE, null, getActivity(), ServerEditorFragment.this, null);
                    // work continues in callback, method run()
                }
            });
            mAccountList.addView(itemView);
        }
    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        boolean valid = false;
        try {
            String token = future.getResult().getString(
                    AccountManager.KEY_AUTHTOKEN);
            if (token != null && !token.isEmpty()) {
                // Valid token
                valid = true;
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

        // Not valid, un-check account
        if (!valid && mSelectedAccountView != null) {
            mSelectedAccountView.setChecked(false);
            mSelectedAccountView = null;
        }
    }
}
