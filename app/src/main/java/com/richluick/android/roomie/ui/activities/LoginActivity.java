package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.ParseFacebookUtils;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.IntentFactory;

import java.util.Arrays;

public class LoginActivity extends Activity {

    private TextView mPrivacyTermsText;

    //span constants
    private static final int privacyStart = 37;
    private static final int privacyEnd = 51;
    private static final int termsStart = 57;
    private static final int termsEnd = 69;
    private static final String agreementNormal = "By logging in, you agree to Roomigo's\nPrivacy Policy and Terms of Use.";
    private static final String agreementLarge = "By logging in, you agree to Roomigo's Privacy Policy and Terms of Use.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        mPrivacyTermsText = (TextView) findViewById(R.id.privacyTerms);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK; //get the screen size of the device

        SpannableString privacyTermsLink;

        //set a different spannable string for the Terms and Privacy depending on screen size
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            privacyTermsLink = new SpannableString(agreementNormal);
        }
        else {
            privacyTermsLink = new SpannableString(agreementLarge);
        }

        //set the span that says "[rivacy policy" and "terms of use" as clickable to open pages
        ClickableSpan privacySpan = new ClickableSpan() { //privacy policy link
            @Override
            public void onClick(View widget) {
                IntentFactory.pickIntent(LoginActivity.this, IntentFactory.BROWSER, Constants.PRIVACY_POLICY);
            }
        };
        ClickableSpan termsSpan = new ClickableSpan() { //terms of service link
            @Override
            public void onClick(View widget) {
                IntentFactory.pickIntent(LoginActivity.this, IntentFactory.BROWSER, Constants.TERMS_OF_USE);
            }
        };

        //set the correct span positions for each link and set the text to the textview
        privacyTermsLink.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        privacyTermsLink.setSpan(termsSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mPrivacyTermsText.setText(privacyTermsLink);
        mPrivacyTermsText.setMovementMethod(LinkMovementMethod.getInstance());

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> ParseFacebookUtils.logInWithReadPermissionsInBackground(
                LoginActivity.this, Arrays.asList("user_birthday", "email"),
                (user, e) -> {
                    if (user == null) {
                        if (!ConnectionDetector.getInstance(LoginActivity.this).isConnected()) {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.toast_error_request), Toast.LENGTH_LONG).show();
                        }
                    } else if (user.isNew()) {
                        user.put(Constants.ALREADY_ONBOARD, false);
                        user.saveInBackground();

                        IntentFactory.pickIntent(LoginActivity.this, IntentFactory.ONBOARD, true, R.anim.slide_in_right, R.anim.hold);
                    } else {
                        if (IntentFactory.checkIfAlreadyOnBoarded()) {
                            IntentFactory.pickIntent(LoginActivity.this, IntentFactory.MAIN_ACTIVITY, true, R.anim.slide_in_right, R.anim.hold);
                        } else {
                            IntentFactory.pickIntent(LoginActivity.this, IntentFactory.ONBOARD, true, R.anim.slide_in_right, R.anim.hold);
                        }
                    }
                }));

        TextView titleText = (TextView) findViewById(R.id.appTitleTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        titleText.setTypeface(font);
        titleText.setShadowLayer(10, 0, 0, Color.BLACK);
    }

    @Override
     protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
