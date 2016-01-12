package com.richluick.android.roomie.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.BaseActivity;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.constants.Constants;
import com.richluick.android.roomie.utils.IntentFactory;

import java.util.ArrayList;
import java.util.List;

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
    @InjectView(R.id.legalText) TextView mLegalButton;
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

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        mCurrentUser = ParseUser.getCurrentUser();
        if(mCurrentUser != null) {
            mDiscoverable = (Boolean) mCurrentUser.get(Constants.DISCOVERABLE);
            mGeneralNot = (Boolean) mCurrentUser.get(Constants.GENERAL_NOTIFICATIONS);
            mMessageNot = (Boolean) mCurrentUser.get(Constants.MESSAGE_NOTIFICATIONS);
            mConnectionNot = (Boolean) mCurrentUser.get(Constants.CONNECTION_NOTIFICATIONS);

            setChecks(mDiscoverable, mDiscoveryCheckBox);
            setChecks(mGeneralNot, mGeneralNotifications);
            setChecks(mMessageNot, mMessageNotifications);
            setChecks(mConnectionNot, mConnectionNotifications);
        }

        mDiscoveryCheckBox.setOnCheckedChangeListener(this);
        mGeneralNotifications.setOnCheckedChangeListener(this);
        mMessageNotifications.setOnCheckedChangeListener(this);
        mConnectionNotifications.setOnCheckedChangeListener(this);

        mPrivacyButton.setOnClickListener(this);
        mTermsButton.setOnClickListener(this);
        mLegalButton.setOnClickListener(this);
        mLogoutButton.setOnClickListener(this);
        mDeleteAccountButton.setOnClickListener(this);
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
        if(ConnectionDetector.getInstance(this).isConnected()) {
            if (v == mDiscoveryCheckBox) {
                mDiscoverable = isChecked;
                mCurrentUser.put(Constants.DISCOVERABLE, mDiscoverable);

                mCurrentUser.saveInBackground(e ->
                    getSharedPreferences(mCurrentUser.getObjectId(), MODE_PRIVATE)
                        .edit().putBoolean(Constants.PROFILE_UPDATED, true).apply());
            } else if (v == mGeneralNotifications) {
                if (isChecked) {
                    mGeneralNot = true;
                    ParsePush.subscribeInBackground(Constants.GENERAL_PUSH);
                } else {
                    mGeneralNot = false;
                    ParsePush.unsubscribeInBackground(Constants.GENERAL_PUSH);
                }

                mCurrentUser.put(Constants.GENERAL_NOTIFICATIONS, mGeneralNot);
                mCurrentUser.saveInBackground();
            } else if (v == mMessageNotifications) {
                if (isChecked) {
                    mMessageNot = true;
                    ParsePush.subscribeInBackground(Constants.MESSAGE_PUSH);
                } else {
                    mMessageNot = false;
                    ParsePush.unsubscribeInBackground(Constants.MESSAGE_PUSH);
                }

                mCurrentUser.put(Constants.MESSAGE_NOTIFICATIONS, mMessageNot);
                mCurrentUser.saveInBackground();
            } else if (v == mConnectionNotifications) {
                if (isChecked) {
                    mConnectionNot = true;
                    ParsePush.subscribeInBackground(Constants.CONNECTION_PUSH);
                } else {
                    mConnectionNot = false;
                    ParsePush.unsubscribeInBackground(Constants.CONNECTION_PUSH);
                }

                mCurrentUser.put(Constants.CONNECTION_NOTIFICATIONS, mConnectionNot);
                mCurrentUser.saveInBackground();
                mCurrentUser.fetchInBackground();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mPrivacyButton) {
            IntentFactory.pickIntent(SettingsActivity.this, IntentFactory.BROWSER, Constants.PRIVACY_POLICY);
        }
        else if(v == mTermsButton) {
            IntentFactory.pickIntent(SettingsActivity.this, IntentFactory.BROWSER, Constants.TERMS_OF_USE);
        }
        else if(v == mLegalButton) {
            LegalFragment legalFragment = new LegalFragment();
            legalFragment.show(getFragmentManager(), "Legal Fragment");
        }
        else if(v == mDeleteAccountButton) {
            new MaterialDialog.Builder(this)
                    .title("Delete Account")
                    .content("Delete your account?")
                    .positiveText("DELETE")
                    .negativeText(getString(R.string.dialog_negative))
                    .negativeColorRes(R.color.primary_text)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            deleteAccount();
                        }
                    })
                    .show();
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
                            ParseUser.logOut();
                            IntentFactory.pickIntent(SettingsActivity.this, IntentFactory.LOGIN,
                                    true, R.anim.slide_in_right, R.anim.hold);
                        }
                    })
                    .show();
        }
    }

    /*
     * This helper method is called when the user opts to delete their account. It deltes the user
     * account in the background and also deletes all conections associated with that user
     */
    private void deleteAccount() {
        //check if current user is USER1 in any relations
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.RELATION);
        query1.whereEqualTo(Constants.USER1, mCurrentUser);

        //check if current user is USER2 in any relations
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Constants.RELATION);
        query2.whereEqualTo(Constants.USER2, mCurrentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>(); //queries go in a list
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> relationQuery = ParseQuery.or(queries);
        relationQuery.include(Constants.USER1);
        relationQuery.include(Constants.USER2);
        relationQuery.findInBackground((parseObjects, e) -> {
            if (e == null) {
                for (int i = 0; i < parseObjects.size(); i++) {
                    parseObjects.get(i).deleteInBackground();
                }
            }
        });

        //delete the current user in the background and go to Login on completion
        mCurrentUser.deleteInBackground(e -> {
            Toast.makeText(SettingsActivity.this, "Account Deleted!", Toast.LENGTH_LONG).show();

            //log user out of Facebook when account is deleted
            ParseUser.logOut();

            IntentFactory.pickIntent(SettingsActivity.this, IntentFactory.LOGIN, true, R.anim.slide_in_right, R.anim.hold);
        });
    }
}