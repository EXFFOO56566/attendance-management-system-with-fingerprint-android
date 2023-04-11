package com.example.attendacewithfingerprint;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends com.example.attendacewithfingerprint.BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.aboutVersion)).setText(BuildConfig.VERSION_NAME); // set version from build gradle
    }


}