package com.richluick.android.roomie.ui.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.widgets.ToggleableRadioButton;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LocationAutocompleteUtil;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EditProfileActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener, ToggleableRadioButton.UnCheckListener,
        CompoundButton.OnCheckedChangeListener{

    private String mGenderPref;
    private Boolean mHasRoom;
    private Boolean mSmokes;
    private Boolean mDrinks;
    private Boolean mPets;
    private Double mLat;
    private Double mLng;
    private String mPlace;
    private ParseUser mCurrentUser;
    private String mLocation;

    @InjectView(R.id.genderGroup) RadioGroup genderPrefGroup;
    @InjectView(R.id.haveRoomGroup) RadioGroup haveRoomGroup;
    @InjectView(R.id.locationField) AutoCompleteTextView locationField;
    @InjectView(R.id.aboutMe) EditText aboutMeField;
    @InjectView(R.id.yesDrinkCheckBox) CheckBox yesDrink;
    @InjectView(R.id.noDrinkCheckBox) CheckBox noDrink;
    @InjectView(R.id.yesSmokeCheckBox) CheckBox yesSmoke;
    @InjectView(R.id.noSmokeCheckBox) CheckBox noSmoke;
    @InjectView(R.id.yesPetCheckBox) CheckBox yesPet;
    @InjectView(R.id.noPetCheckBox) CheckBox noPet;
    @InjectView(R.id.updateProfButton) ImageButton updateProfileButtom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.action_bar_my_profile));
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.inject(this);

        mCurrentUser = ParseUser.getCurrentUser();

        genderPrefGroup.setOnCheckedChangeListener(this);
        haveRoomGroup.setOnCheckedChangeListener(this);
        updateProfileButtom.setOnClickListener(this);

        yesSmoke.setOnCheckedChangeListener(this);
        noSmoke.setOnCheckedChangeListener(this);
        yesDrink.setOnCheckedChangeListener(this);
        noDrink.setOnCheckedChangeListener(this);
        yesPet.setOnCheckedChangeListener(this);
        noPet.setOnCheckedChangeListener(this);

        mLocation = (String) mCurrentUser.get(Constants.LOCATION);
        mGenderPref = (String) mCurrentUser.get(Constants.GENDER_PREF);
        mHasRoom = (Boolean) mCurrentUser.get(Constants.HAS_ROOM);
        mSmokes = (Boolean) mCurrentUser.get(Constants.SMOKES);
        mDrinks = (Boolean) mCurrentUser.get(Constants.DRINKS);
        mPets = (Boolean) mCurrentUser.get(Constants.PETS);
        String aboutMeText = (String) mCurrentUser.get(Constants.ABOUT_ME);

        locationField.setText(mLocation);
        aboutMeField.setText(aboutMeText);

        LocationAutocompleteUtil.setAutoCompleteAdapter(this, locationField);
        locationField.setOnItemClickListener(this);
        locationField.setListSelection(0);

        switch (mGenderPref) {
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

        if(mHasRoom) {
            haveRoomGroup.check(R.id.yesCheckBox);
        }
        else {
            haveRoomGroup.check(R.id.noCheckBox);
        }

        setCheckedItems(mSmokes, yesSmoke, noSmoke);
        setCheckedItems(mDrinks, yesDrink, noDrink);
        setCheckedItems(mPets, yesPet, noPet);
    }

    /**
     * This method is called when the activity is created and sets the previously selected
     * values of the radiogroups based upon the users saved profile. This is only used for Yes/No
     * questions
     *
     * @param field This is the boolean value of the questions being checked(true=yes, false=no)
     * @param yes the "yes" checkbox
     * @param no the "no" checkbox
     */
    private void setCheckedItems(Boolean field, CheckBox yes, CheckBox no) {
        if(field != null) {
            if(field) {
                yes.setChecked(true);
            }
            else {
                no.setChecked(true);
            }
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

            case R.id.yesDrinkCheckBox:
                mDrinks = true;
                break;
            case R.id.noDrinkCheckBox:
                mDrinks = false;
                break;

            case R.id.yesPetCheckBox:
                mPets = true;
                break;
            case R.id.noPetCheckBox:
                mPets = false;
                break;
        }
    }

    /**
     * This method handles the check responses for the yes/no questions
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        if(v == yesSmoke) {
            if (isChecked) {
                if (noSmoke.isChecked()) {
                    noSmoke.setChecked(false);
                }
                mSmokes = true;
            }
            else {
                mSmokes = null;
            }
        }
        else if(v == noSmoke) {
            if(isChecked) {
                if (yesSmoke.isChecked()) {
                    yesSmoke.setChecked(false);
                }
                mSmokes = false;
            }
            else {
                mSmokes = null;
            }
        }

        if(v == yesDrink) {
            if (isChecked) {
                if (noDrink.isChecked()) {
                    noDrink.setChecked(false);
                }
                mDrinks = true;
            }
            else {
                mDrinks = null;
            }
        }
        else if(v == noDrink) {
            if(isChecked) {
                if (yesDrink.isChecked()) {
                    yesDrink.setChecked(false);
                }
                mDrinks = false;
            }
            else {
                mDrinks = null;
            }
        }

        if(v == yesPet) {
            if (isChecked) {
                if (noPet.isChecked()) {
                    noPet.setChecked(false);
                }
                mPets = true;
            }
            else {
                mPets = null;
            }
        }
        else if(v == noPet) {
            if(isChecked) {
                if (yesPet.isChecked()) {
                    yesPet.setChecked(false);
                }
                mPets = false;
            }
            else {
                mPets = null;
            }
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
        if(v == updateProfileButtom) {
            if (mLat == null && !mLocation.equals(locationField.getText().toString())) {
                Toast.makeText(EditProfileActivity.this,
                        getString(R.string.toast_valid_location), Toast.LENGTH_SHORT).show();
            } else {
                if (mPlace != null) {
                    ParseGeoPoint geoPoint = new ParseGeoPoint(mLat, mLng);
                    mCurrentUser.put(Constants.LOCATION, mPlace);
                    mCurrentUser.put(Constants.GEOPOINT, geoPoint);
                }

                mCurrentUser.put(Constants.GENDER_PREF, mGenderPref);
                mCurrentUser.put(Constants.HAS_ROOM, mHasRoom);
                mCurrentUser.put(Constants.ABOUT_ME, aboutMeField.getText().toString());

                saveYesNoFields(mSmokes, Constants.SMOKES);
                saveYesNoFields(mDrinks, Constants.DRINKS);
                saveYesNoFields(mPets, Constants.PETS);

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

    /**
     * This method is called when the user decides to save his profile. It handles whether or not
     * to save a new value of remove the old value from all the yes or no fields
     *
     * @param field This is the boolean value of the fields being saved(true=yes, false=no)
     * @param fieldKey the Parse key of the field to save
     */
    private void saveYesNoFields(Boolean field, String fieldKey) {
        if(field != null) {
            mCurrentUser.put(fieldKey, field);
        }
        else {
            mCurrentUser.remove(fieldKey);
        }
    }

    @Override
    public void onUnchecked(View v) {
        if(v == yesDrink || v == noDrink) {
            mDrinks = null;
        }
        else if(v == yesSmoke || v == noSmoke) {
            mSmokes = null;
        }
        else if(v == yesPet || v == noPet) {
            mPets = null;
        }
    }


}
