package com.nononsenseapps.feeder.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.Spanned;

public class NothingLoader extends AsyncTaskLoader<Spanned> {
    public NothingLoader(Context context) {
        super(context);
    }

    @Override
    public Spanned loadInBackground() {
        return null;
    }
}
