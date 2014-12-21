package com.nononsenseapps.feeder.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.util.PasswordUtils;
import com.nononsenseapps.feeder.util.PrefUtils;
import com.nononsenseapps.feeder.views.FloatLabelLayout;

/**
 * Fragment allowing the user to configure the server settings.
 */
public class ServerEditorFragment extends DialogFragment {
    private static final long ANIMATION_DURATION_OUT = 100;
    private static final long ANIMATION_DURATION_IN = 150;

    private View mFrameUserPass;
    private TextView mServerText;
    private TextView mUserText;
    private TextView mPassText;
    private SwitchCompat mAccountSwitch;
    private FloatLabelLayout mServerLabel;
    private FloatLabelLayout mUserLabel;
    private FloatLabelLayout mPassLabel;

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
        // Do NOT fill in the user's hash. Might override stored password!
        mPassText.setText("");

        mAccountSwitch = (SwitchCompat) root.findViewById(R.id.switch_account);
        mAccountSwitch.setSwitchPadding((int) getResources().getDimension(R.dimen.switch_padding));
        mAccountSwitch.setChecked(PrefUtils.getUseAccount(getActivity()));
        mAccountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableUserPassFrame(isChecked);
            }
        });
        // Do it separately without animation
        enableUserPassFrame(PrefUtils.getUseAccount(getActivity()));

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

        // TODO handle resizing the window

        return root;
    }


    private void onOkClicked() {
        // don't do shit unless fields are valid and filled in
        if (!validateFields()) {
            return;
        }

        // Save current
        PrefUtils.setServerUrl(getActivity(), mServerText.getText().toString());
        PrefUtils.setUseAccount(getActivity(), mAccountSwitch.isChecked());
        if (mAccountSwitch.isChecked()) {
            PrefUtils.setUsername(getActivity(), mUserText.getText().toString());
            // Actually store a salted-hashed version of the password
            PrefUtils.setPassword(getActivity(),
                    PasswordUtils.getSaltedHashedPassword(mPassText.getText().toString()));
        }

        // And close
        dismiss();
    }

    /**
     * @return true if all visible fields are OK to save
     */
    private boolean validateFields() {
        // Server url first
        // TODO use string resources
        boolean serverValid = mServerText.getText().length() > 0;
        boolean result = serverValid;
        if (!serverValid) {
            mServerLabel.showError("Missing URL");
        }

        // User pass, or account
        if (mAccountSwitch.isChecked()) {
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

    private void enableUserPassFrame(final boolean enable) {
        mUserText.setEnabled(enable);
        mUserText.setText("");
        mPassText.setEnabled(enable);
        mPassText.setText("");
        if (enable) {
            mPassLabel.showError("Missing password");
            mUserLabel.showError("Missing username");
        } else {
            mPassLabel.showError("");
            mUserLabel.showError("");
        }
    }
}
