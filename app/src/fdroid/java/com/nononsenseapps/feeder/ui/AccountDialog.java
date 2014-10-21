package com.nononsenseapps.feeder.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.nononsenseapps.feeder.R;


public class AccountDialog extends DialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle args) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.select_account);

    builder.setMessage("Non google-login not implemented yet :(");
    return builder.create();
  }
}