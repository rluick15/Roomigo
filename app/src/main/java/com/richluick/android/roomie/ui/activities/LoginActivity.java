package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.IntentUtils;

import java.util.Arrays;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.BIRTHDAY),
                        LoginActivity.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.toast_error_request), Toast.LENGTH_LONG).show();
                        }
                        else if (user.isNew()) {
                            user.put(Constants.ALREADY_ONBOARD, false);
                            user.saveInBackground();

                            IntentUtils.onBoardIntent(LoginActivity.this);
                        }
                        else {
                            if(IntentUtils.checkIfAlreadyOnBoarded()) {
                                IntentUtils.mainIntent(LoginActivity.this);
                            }
                            else {
                                IntentUtils.onBoardIntent(LoginActivity.this);
                            }
                        }
                    }
                });
            }
        });

        TextView titleText = (TextView) findViewById(R.id.appTitleTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        titleText.setTypeface(font);
        titleText.setShadowLayer(10, 0, 0, Color.BLACK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
}
