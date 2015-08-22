package com.richluick.android.roomie.login;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.IntentFactory;
import com.richluick.android.roomie.utils.constants.Constants;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        TextView titleText = (TextView) findViewById(R.id.appTitleTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        titleText.setTypeface(font);
        titleText.setShadowLayer(10, 0, 0, Color.BLACK);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(ParseUser.getCurrentUser() != null) {
                if (ParseUser.getCurrentUser().isAuthenticated()) {
                    if ((Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD)) {
                        IntentFactory.pickIntent(LauncherActivity.this, IntentFactory.MAIN_ACTIVITY, true, R.anim.fade_in, R.anim.fade_out);
                    } else {
                        IntentFactory.pickIntent(LauncherActivity.this, IntentFactory.ONBOARD, true, R.anim.fade_in, R.anim.fade_out);
                    }
                }
            }
            else {
                IntentFactory.pickIntent(LauncherActivity.this, IntentFactory.LOGIN, true, R.anim.fade_in, R.anim.fade_out);
            }
        }, 2000);
    }
}
