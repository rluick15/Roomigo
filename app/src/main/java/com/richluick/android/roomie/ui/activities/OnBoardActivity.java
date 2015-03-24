package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class OnBoardActivity extends Activity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private Double mLat;
    private Double mLng;
    private String mPlace;

    @InjectView(R.id.genderGroup) RadioGroup mGenderGroup;
    @InjectView(R.id.haveRoomGroup) RadioGroup mHasRoomGroup;
    @InjectView(R.id.cancelButton) Button mCancelButton;
    @InjectView(R.id.submitButton) Button mSetPrefButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);
        ButterKnife.inject(this);

        AutoCompleteTextView placesField = (AutoCompleteTextView) findViewById(R.id.locationField);
        placesField.setOnItemClickListener(this);
        LocationAutocompleteUtil.setAutoCompleteAdapter(this, placesField);

        mGenderGroup.setOnCheckedChangeListener(this);
        mHasRoomGroup.setOnCheckedChangeListener(this);
        mCancelButton.setOnClickListener(this);
        mSetPrefButton.setOnClickListener(this);
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

        if (mGenderGroup.getCheckedRadioButtonId() != -1 &&
                mHasRoomGroup.getCheckedRadioButtonId() != -1 && mLat != null) {
            mSetPrefButton.setEnabled(true);
        }
    }

    /**
     * This method gets the location the user selects and extracts the coordinates from it
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPlace = (String) parent.getItemAtPosition(position);

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(mPlace, 1);
            if (addresses.size() > 0) {
                mLat = addresses.get(0).getLatitude();
                mLng = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mGenderGroup.getCheckedRadioButtonId() != -1 &&
                mHasRoomGroup.getCheckedRadioButtonId() != -1 && mLat != null) {
            mSetPrefButton.setEnabled(true);
        }
    }

    /**
     * This method  handles saving the new parse user when the user selects to finish onboarding
     */
    @Override
    public void onClick(View v) {
        if (v == mSetPrefButton) {
            ParseUser.getCurrentUser().put(Constants.ALREADY_ONBOARD, true);
            ParseUser.getCurrentUser().saveInBackground();

            ParseGeoPoint geoPoint = new ParseGeoPoint(mLat, mLng);

            ParseUser user = ParseUser.getCurrentUser();
            user.put(Constants.LOCATION, mPlace);
            user.put(Constants.GEOPOINT, geoPoint);
            user.put(Constants.GENDER_PREF, mGenderPref);
            user.put(Constants.HAS_ROOM, mHasRoom);
            user.put(Constants.ABOUT_ME, "");
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_account_created),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(OnBoardActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                    } else {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_error_request),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else if (v == mCancelButton) {
            Intent intent = new Intent(OnBoardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in_quick, R.anim.slide_out_right);
        }
    }
}
