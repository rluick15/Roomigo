package com.richluick.android.roomie.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.ParseFacebookUtils;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.home.MainActivity;
import com.richluick.android.roomie.presenter.implementations.LoginPresenterImpl;
import com.richluick.android.roomie.presenter.views.LoginView;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.IntentFactory;
import com.richluick.android.roomie.utils.constants.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity implements LoginView {

    @Bind(R.id.privacyTerms)
    TextView privacyTermsText;
    @Bind(R.id.appTitleTextView)
    TextView titleText;

    private LoginPresenterImpl loginPresenter;

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
        ButterKnife.bind(this);

        //todo: maybe inherit presenter object
        loginPresenter = new LoginPresenterImpl(this);
        loginPresenter.setView(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

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
        privacyTermsText.setText(privacyTermsLink);
        privacyTermsText.setMovementMethod(LinkMovementMethod.getInstance());

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

    @OnClick(R.id.loginButton)
    public void submit(View view) {
        loginPresenter.loginUser();
    }

    @Override
    public void onNewUser() {
        Intent intent = new Intent(this, OnBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    @Override
    public void onFullUser() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    @Override
    public void onPartialUser() {
        Intent intent = new Intent(this, OnBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    @Override
    public void onError() {
        if (!ConnectionDetector.getInstance(LoginActivity.this).isConnected()) {
            Toast.makeText(LoginActivity.this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.toast_error_request), Toast.LENGTH_LONG).show();
        }
    }
}
