package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class OnBoardActivity extends Activity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private ArrayAdapter<String> adapter;
    private Double mLat;
    private Double mLng;
    private RadioGroup mGenderGroup;
    private RadioGroup mHasRoomGroup;
    private String mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_on_board);

        AutoCompleteTextView placesField = (AutoCompleteTextView) findViewById(R.id.locationField);
        placesField.setOnItemClickListener(this);
        LocationAutocompleteUtil.setAutoCompleteAdapter(this, placesField);

        mGenderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        mHasRoomGroup = (RadioGroup) findViewById(R.id.haveRoomGroup);

        mGenderGroup.setOnCheckedChangeListener(this);
        mHasRoomGroup.setOnCheckedChangeListener(this);

        Button setPrefButton = (Button) findViewById(R.id.submitButton);
        setPrefButton.setOnClickListener(this);
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
            if(addresses.size() > 0) {
                mLat = addresses.get(0).getLatitude();
                mLng = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method  handles saving the new parse user when the user selects to finish onboarding
     */
    @Override
    public void onClick(View v) {
        if(mGenderGroup.getCheckedRadioButtonId() == -1 ||
                mHasRoomGroup.getCheckedRadioButtonId() == -1 || mLat == null) {
            Toast.makeText(OnBoardActivity.this, getString(R.string.toast_empty_fields), Toast.LENGTH_LONG).show();
        }
        else {
            SharedPreferences pref = getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean(Constants.ALREADY_ONBOARD, true);
            ed.commit();

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
                    }
                    else {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_error_request),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
