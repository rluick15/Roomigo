package com.richluick.android.roomie.ui.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;

import java.io.IOException;
import java.util.List;

public class EditProfileActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private Double mLat;
    private Double mLng;
    private String mPlace;
    private ParseUser mCurrentUser;
    private EditText mAboutMeField;
    private String mLocation;
    private AutoCompleteTextView mLocationField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);

        mCurrentUser = ParseUser.getCurrentUser();

        mAboutMeField = (EditText) findViewById(R.id.aboutMe);
        RadioGroup genderPrefGroup = (RadioGroup) findViewById(R.id.genderGroup);
        RadioGroup haveRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);
        mLocationField = (AutoCompleteTextView) findViewById(R.id.locationField);

        genderPrefGroup.setOnCheckedChangeListener(this);
        haveRoomGroup.setOnCheckedChangeListener(this);

        mLocation = (String) mCurrentUser.get(Constants.LOCATION);
        String genderPref = (String) mCurrentUser.get(Constants.GENDER_PREF);
        Boolean hasRoom = (Boolean) mCurrentUser.get(Constants.HAS_ROOM);
        String aboutMeText = (String) mCurrentUser.get(Constants.ABOUT_ME);

        mLocationField.setText(mLocation);
        mAboutMeField.setText(aboutMeText);

        LocationAutocompleteUtil.setAutoCompleteAdapter(this, mLocationField);
        mLocationField.setOnItemClickListener(this);
        mLocationField.setListSelection(0);

        Button updateProfileButtom = (Button) findViewById(R.id.updateProfButton);
        updateProfileButtom.setOnClickListener(this);

        switch (genderPref) {
            case Constants.MALE:
                genderPrefGroup.check(R.id.maleCheckBox);
                break;
            case Constants.FEMALE:
                genderPrefGroup.check(R.id.femaleCheckBox);
                break;
            case Constants.BOTH:
                genderPrefGroup.check(R.id.bothCheckBox);
                break;
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

    /**
     * This method  handles saving the new parse user when the user selects to update the profile
     */
    @Override
    public void onClick(View v) {
        if(mLat == null && !mLocation.equals(mLocationField.getText().toString())) {
            Toast.makeText(EditProfileActivity.this,
                    getString(R.string.toast_valid_location), Toast.LENGTH_SHORT).show();
        }
        else {
            if(mPlace != null) {
                ParseGeoPoint geoPoint = new ParseGeoPoint(mLat, mLng);
                mCurrentUser.put(Constants.LOCATION, mPlace);
                mCurrentUser.put(Constants.GEOPOINT, geoPoint);
            }
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
