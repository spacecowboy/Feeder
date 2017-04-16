package com.nononsenseapps.feeder.ui;

import android.os.Bundle;
import android.widget.TextView;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.util.FileLog;


public class DebugLogActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_log);
    }

    @Override
    public void onResume() {
        FileLog log = FileLog.singleton.getInstance(this);

        ((TextView) findViewById(android.R.id.text1)).setText(log.getLog());

        super.onResume();
    }
}
