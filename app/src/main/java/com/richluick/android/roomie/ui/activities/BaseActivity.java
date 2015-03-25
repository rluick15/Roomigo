package com.richluick.android.roomie.ui.activities;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.richluick.android.roomie.R;

/**
 * This is a custom activity containing the options bar menu code. All the other activities
 * extend this class to inheit the same action bar
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
