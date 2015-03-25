package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.widgets.ToggleableRadioButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends BaseActivity implements ToggleableRadioButton.UnCheckListener,
        View.OnClickListener {

    @InjectView(R.id.discoveryCheckBox) ToggleableRadioButton mDiscoveryCheckBox;
    @InjectView(R.id.privacyText) TextView mPrivacyButton;
    @InjectView(R.id.termText) TextView mTermsButton;
    @InjectView(R.id.logoutText) TextView mLogoutButton;
    @InjectView(R.id.deleteAccountText) TextView mDeleteAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        mDiscoveryCheckBox.setUncheckListener(this);

        mPrivacyButton.setOnClickListener(this);
        mTermsButton.setOnClickListener(this);
        mLogoutButton.setOnClickListener(this);
        mDeleteAccountButton.setOnClickListener(this);
    }

    @Override
    public void onUnchecked(View v) {

    }

    @Override
    public void onClick(View v) {
        if(v == mPrivacyButton) {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
        }
        else if(v == mTermsButton) {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
        }
        else if(v == mDeleteAccountButton) {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
        }
        else if(v == mLogoutButton) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.dialog_title_logout))
                    .content(getString(R.string.dialog_content_logout))
                    .positiveText(getString(R.string.dialog_positive_logout))
                    .negativeText(getString(R.string.dialog_negative))
                    .negativeColorRes(R.color.primary_text)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            ParseFacebookUtils.getSession().closeAndClearTokenInformation();
                            ParseUser.logOut();

                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                        }
                    })
                    .show();
        }
    }
}
