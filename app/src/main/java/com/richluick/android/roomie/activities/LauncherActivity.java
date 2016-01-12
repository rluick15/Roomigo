package com.richluick.android.roomie.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.presenter.implementations.LauncherPresenterImpl;
import com.richluick.android.roomie.presenter.views.LauncherView;

public class LauncherActivity extends Activity implements LauncherView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        TextView titleText = (TextView) findViewById(R.id.appTitleTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        titleText.setTypeface(font);
        titleText.setShadowLayer(10, 0, 0, Color.BLACK);

        new Handler().postDelayed(() -> {
            LauncherPresenterImpl presenter = new LauncherPresenterImpl();
            presenter.setView(LauncherActivity.this);
            presenter.launchActivity();
        }, 2000);
    }

    @Override
    public void onError() {
        //leave empty here. used to satisfy interface requirements
    }

    @Override
    public void launchNewActivity(Activity activity) {
        Intent intent = new Intent(this, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
