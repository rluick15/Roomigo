package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;

public class EditProfileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);

        ParseUser currentUser = ParseUser.getCurrentUser();

        AutoCompleteTextView locationField = (AutoCompleteTextView) findViewById(R.id.locationField);
        RadioGroup genderPrefGroup = (RadioGroup) findViewById(R.id.genderGroup);
        RadioGroup haveRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);
        EditText aboutMeField = (EditText) findViewById(R.id.aboutMe);

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
    }
}
