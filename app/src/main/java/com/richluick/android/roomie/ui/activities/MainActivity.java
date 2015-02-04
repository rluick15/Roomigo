package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.facebook.FacebookRequest;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookRequest request = new FacebookRequest(this);
    }
}
