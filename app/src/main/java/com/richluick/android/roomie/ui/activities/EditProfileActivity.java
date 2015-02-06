package com.richluick.android.roomie.ui.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;

import java.io.IOException;
import java.util.List;

public class EditProfileActivity extends ActionBarActivity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private Double mLat;
    private Double mLng;
    private String mPlace;
    private ParseUser mCurrentUser;
    private EditText mAboutMeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);

        mCurrentUser = ParseUser.getCurrentUser();

        mAboutMeField = (EditText) findViewById(R.id.aboutMe);
        RadioGroup genderPrefGroup = (RadioGroup) findViewById(R.id.genderGroup);
        RadioGroup haveRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);

        AutoCompleteTextView locationField = (AutoCompleteTextView) findViewById(R.id.locationField);
        LocationAutocompleteUtil.setAutoCompleteAdapter(this, locationField);
        locationField.setOnItemClickListener(this);

        genderPrefGroup.setOnCheckedChangeListener(this);
        haveRoomGroup.setOnCheckedChangeListener(this);

        String location = (String) mCurrentUser.get(Constants.LOCATION);
        String genderPref = (String) mCurrentUser.get(Constants.GENDER_PREF);
        Boolean hasRoom = (Boolean) mCurrentUser.get(Constants.HAS_ROOM);
        String aboutMeText = (String) mCurrentUser.get(Constants.ABOUT_ME);

        locationField.setText(location);
        mAboutMeField.setText(aboutMeText);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPlace = (String) parent.getItemAtPosition(position);

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(mPlace, 1);
            if(addresses.size() > 0) {
                mLat = addresses.get(0).getLatitude();
                mLng = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(mLat == null) {
            Toast.makeText(EditProfileActivity.this,
                    getString(R.string.toast_valid_location), Toast.LENGTH_LONG).show();
        }
        else {
            mCurrentUser.put(Constants.LOCATION, mPlace);
            mCurrentUser.put(Constants.GENDER_PREF, mGenderPref);
            mCurrentUser.put(Constants.HAS_ROOM, mHasRoom);
            mCurrentUser.put(Constants.ABOUT_ME, mAboutMeField.getText().toString());
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(EditProfileActivity.this, getString(R.string.toast_profile_updated),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(EditProfileActivity.this, getString(R.string.toast_error_request),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
