package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseFacebookUtils;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private Boolean mDiscoverable;
    private Boolean mGeneralNot;
    private Boolean mMessageNot;
    private Boolean mConnectionNot;
    private ParseUser mCurrentUser;

    @InjectView(R.id.discoveryCheckBox) CheckBox mDiscoveryCheckBox;
    @InjectView(R.id.privacyText) TextView mPrivacyButton;
    @InjectView(R.id.termText) TextView mTermsButton;
    @InjectView(R.id.logoutText) TextView mLogoutButton;
    @InjectView(R.id.deleteAccountText) TextView mDeleteAccountButton;
    @InjectView(R.id.generalCheckBox) CheckBox mGeneralNotifications;
    @InjectView(R.id.messageCheckBox) CheckBox mMessageNotifications;
    @InjectView(R.id.connectionCheckBox) CheckBox mConnectionNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        mCurrentUser = ParseUser.getCurrentUser();

        mDiscoveryCheckBox.setOnCheckedChangeListener(this);
        mGeneralNotifications.setOnCheckedChangeListener(this);
        mMessageNotifications.setOnCheckedChangeListener(this);
        mConnectionNotifications.setOnCheckedChangeListener(this);

        mDiscoverable = (Boolean) mCurrentUser.get(Constants.DISCOVERABLE);
        if(mDiscoverable == null) {
            mDiscoverable = true;
            mCurrentUser.put(Constants.DISCOVERABLE, mDiscoverable);
            mCurrentUser.saveInBackground();
        }

        mGeneralNot = (Boolean) mCurrentUser.get(Constants.GENERAL_NOTIFICATIONS);
        if(mGeneralNot == null) {
            mGeneralNot = true;
            mCurrentUser.put(Constants.GENERAL_NOTIFICATIONS, mGeneralNot);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.GENERAL_PUSH);
        }

        mMessageNot = (Boolean) mCurrentUser.get(Constants.MESSAGE_NOTIFICATIONS);
        if(mGeneralNot == null) {
            mMessageNot = true;
            mCurrentUser.put(Constants.MESSAGE_NOTIFICATIONS, mGeneralNot);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.MESSAGE_PUSH);
        }

        mConnectionNot = (Boolean) mCurrentUser.get(Constants.CONNECTION_NOTIFICATIONS);
        if(mGeneralNot == null) {
            mConnectionNot = true;
            mCurrentUser.put(Constants.CONNECTION_NOTIFICATIONS, mConnectionNot);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.CONNECTION_PUSH);
        }

        setChecks(mDiscoverable, mDiscoveryCheckBox);
        setChecks(mGeneralNot, mGeneralNotifications);
        setChecks(mMessageNot, mMessageNotifications);
        setChecks(mConnectionNot, mConnectionNotifications);

        mPrivacyButton.setOnClickListener(this);
        mTermsButton.setOnClickListener(this);
        mLogoutButton.setOnClickListener(this);
        mDeleteAccountButton.setOnClickListener(this);
    }

    private void setChecks(Boolean field, CheckBox checkBox) {
        if(field) {
            checkBox.setChecked(true);
        }
        else {
            checkBox.setChecked(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        if(v == mDiscoveryCheckBox) {
            if(isChecked) {
                mDiscoverable = true;
            }
            else {
                mDiscoverable = false;
            }
            mCurrentUser.put(Constants.DISCOVERABLE, mDiscoverable);
            mCurrentUser.saveInBackground();
        }
        else if(v == mGeneralNotifications) {
            if(isChecked) {
                mGeneralNot = true;
                ParsePush.subscribeInBackground(Constants.GENERAL_PUSH);
            }
            else {
                mGeneralNot = false;
                ParsePush.unsubscribeInBackground(Constants.GENERAL_PUSH);
            }
            mCurrentUser.put(Constants.GENERAL_NOTIFICATIONS, mGeneralNot);
            mCurrentUser.saveInBackground();
        }
        else if(v == mMessageNotifications) {
            if(isChecked) {
                mMessageNot = true;
                ParsePush.subscribeInBackground(Constants.MESSAGE_PUSH);
            }
            else {
                mMessageNot = false;
                ParsePush.unsubscribeInBackground(Constants.MESSAGE_PUSH);
            }
            mCurrentUser.put(Constants.MESSAGE_NOTIFICATIONS, mMessageNot);
            mCurrentUser.saveInBackground();
        }
        else if(v == mConnectionNotifications) {
            if(isChecked) {
                mConnectionNot = true;
                ParsePush.subscribeInBackground(Constants.CONNECTION_PUSH);
            }
            else {
                mConnectionNot = false;
                ParsePush.unsubscribeInBackground(Constants.CONNECTION_PUSH);
            }
            mCurrentUser.put(Constants.CONNECTION_NOTIFICATIONS, mConnectionNot);
            mCurrentUser.saveInBackground();
        }
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
