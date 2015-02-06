package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;

public class EditProfileActivity extends ActionBarActivity implements RadioGroup.OnCheckedChangeListener{

    private String mGenderPref;
    private Boolean mHasRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);

        ParseUser currentUser = ParseUser.getCurrentUser();

        AutoCompleteTextView locationField = (AutoCompleteTextView) findViewById(R.id.locationField);
        EditText aboutMeField = (EditText) findViewById(R.id.aboutMe);
        RadioGroup genderPrefGroup = (RadioGroup) findViewById(R.id.genderGroup);
        RadioGroup haveRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);

        genderPrefGroup.setOnCheckedChangeListener(this);
        haveRoomGroup.setOnCheckedChangeListener(this);

        String location = (String) currentUser.get(Constants.LOCATION);
        String genderPref = (String) currentUser.get(Constants.GENDER_PREF);
        Boolean hasRoom = (Boolean) currentUser.get(Constants.HAS_ROOM);
        String aboutMeText = (String) currentUser.get(Constants.ABOUT_ME);

        locationField.setText(location);
        aboutMeField.setText(aboutMeText);

        if(genderPref.equals(Constants.MALE)) {
            genderPrefGroup.check(R.id.maleCheckBox);
        }
        else if(genderPref.equals(Constants.FEMALE)) {
            genderPrefGroup.check(R.id.femaleCheckBox);
        }
        else if(genderPref.equals(Constants.BOTH)) {
            genderPrefGroup.check(R.id.bothCheckBox);
        }

        if(hasRoom) {
            haveRoomGroup.check(R.id.yesCheckBox);
        }
        else {
            haveRoomGroup.check(R.id.noCheckBox);
        }

        LocationAutocompleteUtil.setAutoCompleteAdapter(this, locationField);
    }

    /**
     * This method handles the check responses for the radio groups for setting preferences.
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.maleCheckBox:
                mGenderPref = Constants.MALE;
                break;
            case R.id.femaleCheckBox:
                mGenderPref = Constants.FEMALE;
                break;
            case R.id.bothCheckBox:
                mGenderPref = Constants.BOTH;
                break;

            case R.id.yesCheckBox:
                mHasRoom = true;
                break;
            case R.id.noCheckBox:
                mHasRoom = false;
                break;
        }
    }
}
