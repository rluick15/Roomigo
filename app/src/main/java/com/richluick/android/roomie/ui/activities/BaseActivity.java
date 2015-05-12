package com.richluick.android.roomie.ui.activities;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.richluick.android.roomie.R;

/**
 * This is a custom activity containing the options bar menu code. All the other activities
 * extend this class to inheit the same action bar
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
