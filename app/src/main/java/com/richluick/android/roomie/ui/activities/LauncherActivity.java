package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.IntentUtils;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);

        TextView titleText = (TextView) findViewById(R.id.appTitleTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        titleText.setTypeface(font);
        titleText.setShadowLayer(10, 0, 0, Color.BLACK);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ParseUser.getCurrentUser() != null) {
                    if (ParseUser.getCurrentUser().isAuthenticated()) {
                        if (IntentUtils.checkIfAlreadyOnBoarded()) {
                            IntentUtils.mainIntent(LauncherActivity.this);
                        } else {
                            IntentUtils.onBoardIntent(LauncherActivity.this);
                        }
                    }
                }
                else {
                   Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
                }
            }
        }, 2000);
    }
}
