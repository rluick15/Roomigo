package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;

/**
 * This is a custom activity containing the options bar menu code. All the other activities
 * extend this class to inheit the same action bar
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.dialog_title_logout))
                    .content(getString(R.string.dialog_content_logout))
                    .positiveText(getString(R.string.dialog_positive_logout))
                    .negativeText(getString(R.string.dialog_negative_logout))
                    .negativeColorRes(R.color.primary_text)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            ParseFacebookUtils.getSession().closeAndClearTokenInformation();
                            ParseUser.logOut();

                            Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                        }
                    })
                    .show();
        }
        else if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
