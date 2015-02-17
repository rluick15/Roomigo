package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;

/**
 * This is a custom activity containing the options bar menu code. All the other activities
 * extend this class to inheit the same action bar
 */
public class BaseActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout) {
            ParseFacebookUtils.getSession().closeAndClearTokenInformation();
            ParseUser.logOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
