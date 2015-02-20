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
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.facebook.FacebookRequest;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.MessageService;

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
                        if (checkIfAlreadyOnBoarded()) {
                            mainIntent();
                        } else {
                            onBoardIntent();
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

    /**
     * This method  checks if the user has already gone through the onboard process and either
     * sends them to onboarding if they have not or skips that step and brings them to the
     * Main Activity
     *
     * @return Boolean true or false depending on if the user has onboarded or not
     */
    private Boolean checkIfAlreadyOnBoarded() {
        return (Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD);
    }

    /**
     * This method  sends the user to onboarding using an intent and also set the current facebook
     * user id in the shared preferences.
     */
    private void onBoardIntent() {
        new FacebookRequest(this).setCurrentFacebookUser(); //sets the user to shared prefs

        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        startService(serviceIntent);

        RoomieApplication.updateParseInstallation();

        Intent intent = new Intent(this, OnBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * This method  sends the user to the main activity using an intent
     */
    private void mainIntent() {
        new FacebookRequest(this).setCurrentFacebookUser(); //sets the user to shared prefs

        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        startService(serviceIntent);

        RoomieApplication.updateParseInstallation();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
