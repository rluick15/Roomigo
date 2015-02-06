package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.richluick.android.roomie.R;

public class SearchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.title_roommate_search));
        setContentView(R.layout.activity_search);
    }
}
