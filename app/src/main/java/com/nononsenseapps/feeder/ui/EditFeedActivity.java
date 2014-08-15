package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;

public class EditFeedActivity extends Activity {

    public static final String SHOULD_FINISH_BACK = "SHOULD_FINISH_BACK";
    private boolean mShouldFinishBack = false;
    private EditText mTextUrl;
    private EditText mTextTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        mTextUrl = (EditText) findViewById(R.id.feed_url);
        mTextTitle = (EditText) findViewById(R.id.feed_title);

        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // TODO error checking and stuff like that
                ContentValues values = new ContentValues();

                values.put(FeedSQL.COL_TITLE, mTextTitle.getText().toString());
                values.put(FeedSQL.COL_URL, mTextUrl.getText().toString());
                getContentResolver()
                        .insert(RssContentProvider.URI_FEEDS, values);
                finish();
            }
        });

        Intent i = getIntent();
        if (i != null) {
            mShouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false);

            if (i.getDataString() != null) {
                mTextUrl.setText(i.getDataString());
            } else if (i.hasExtra(Intent.EXTRA_TEXT)) {
                mTextUrl.setText(i.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }

    private void setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.session_details_floating_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.session_details_floating_height);
        params.alpha = 1;
        params.dimAmount = 0.7f;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    private boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            // isFloatingWindow flag is not defined in theme
            return false;
        }
        return (floatingWindowFlag.data != 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.edit_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home && mShouldFinishBack) {
            // Was launched from inside app, should just go back
            // Action bar handles other cases.
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
