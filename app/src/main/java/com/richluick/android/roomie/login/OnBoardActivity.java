package com.richluick.android.roomie.login;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.home.MainActivityData;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.constants.Constants;
import com.richluick.android.roomie.utils.IntentFactory;
import com.richluick.android.roomie.utils.places.LocationAutocompleteUtil;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OnBoardActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private String mGenderPref;
    private Boolean mHasRoom;
    private Double mLat;
    private Double mLng;
    private String mPlace;
    private ParseUser mCurrentUser;
    private MainActivityData mainData;

    @InjectView(R.id.genderGroup) RadioGroup mGenderGroup;
    @InjectView(R.id.haveRoomGroup) RadioGroup mHasRoomGroup;
    @InjectView(R.id.cancelButton) Button mCancelButton;
    @InjectView(R.id.submitButton) Button mSetPrefButton;
    @InjectView(R.id.locationField) AutoCompleteTextView mPlacesField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);
        ButterKnife.inject(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        mainData = new MainActivityData(); //get the MainActivityData object

        //set the adapter for the autocomplete text view
        mPlacesField.setOnItemClickListener(this);
        LocationAutocompleteUtil.setAutoCompleteAdapter(this, mPlacesField);

        //set the listeners for the radio groups
        mGenderGroup.setOnCheckedChangeListener(this);
        mHasRoomGroup.setOnCheckedChangeListener(this);
        mCancelButton.setOnClickListener(this);
        mSetPrefButton.setOnClickListener(this);
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

    /**
     * This method handles the check responses for the radio groups for setting preferences.
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            //gnder pref radio group
            case R.id.maleCheckBox:
                mGenderPref = Constants.MALE;
                break;
            case R.id.femaleCheckBox:
                mGenderPref = Constants.FEMALE;
                break;
            case R.id.bothCheckBox:
                mGenderPref = Constants.BOTH;
                break;

            //has room radio group
            case R.id.yesCheckBox:
                mHasRoom = true;
                break;
            case R.id.noCheckBox:
                mHasRoom = false;
                break;
        }

        //enable the button when all items are selected
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
        mPlace = (String) parent.getItemAtPosition(position); //get the place object

        //get the lat and lng from the selected place object
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

        //enable the button when all items are selected
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
            //first check the connection before proceeding
            if(!ConnectionDetector.getInstance(this).isConnected()) {
                Toast.makeText(OnBoardActivity.this, getString(R.string.no_connection),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                ParseGeoPoint geoPoint = new ParseGeoPoint(mLat, mLng);

                //save the new user in the background and go to the Main Activity
                mCurrentUser = ParseUser.getCurrentUser();
                mCurrentUser.put(Constants.ALREADY_ONBOARD, true);
                mCurrentUser.put(Constants.LOCATION, mPlace);
                mCurrentUser.put(Constants.GEOPOINT, geoPoint);
                mCurrentUser.put(Constants.GENDER_PREF, mGenderPref);
                mCurrentUser.put(Constants.HAS_ROOM, mHasRoom);
                mCurrentUser.put(Constants.ABOUT_ME, "");
                mCurrentUser.saveInBackground(e -> {
                    if (e == null) {
                        if (AccessToken.getCurrentAccessToken() != null) { //check if session opened properly
                            facebookRequest();
                        } else {
                            Toast.makeText(OnBoardActivity.this, getString(R.string.toast_error_request),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(OnBoardActivity.this, getString(R.string.toast_error_request),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else if (v == mCancelButton) { //go back to the Login screen
            IntentFactory.pickIntent(OnBoardActivity.this, IntentFactory.LOGIN, true, R.anim.fade_in_quick, R.anim.slide_out_right);
        }
    }

    /**
     * This method handles the request to facebook to retrieve the users account into
     */
    private void facebookRequest() {
        //get simple facebook and add the user properties we are looking to retrieve
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.GENDER)
                .add(Profile.Properties.BIRTHDAY)
                .add(Profile.Properties.ID)
                .add(Profile.Properties.EMAIL)
                .build();

        SimpleFacebook.getInstance(this).getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile response) {
                String id = response.getId();
                String name = response.getFirstName();
                String gender = response.getGender();
                String birthday = response.getBirthday();
                String email = response.getEmail();
                String age = getAge(birthday);

                mCurrentUser.put(Constants.NAME, name);
                mCurrentUser.put(Constants.AGE, age);
                mCurrentUser.put(Constants.GENDER, gender);
                mCurrentUser.put(Constants.EMAIL, email);
                mCurrentUser.saveInBackground(e -> {
                    mainData.getPictureFromUrl("https://graph.facebook.com/" + id + "/picture?type=large")
                        .flatMap(bitmap -> mainData.saveImageToParse(bitmap))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            Toast.makeText(OnBoardActivity.this, getString(R.string.toast_account_created),
                                    Toast.LENGTH_SHORT).show();

                            IntentFactory.pickIntent(OnBoardActivity.this, IntentFactory.MAIN_ACTIVITY, true, R.anim.slide_in_right, R.anim.hold);
                        });
                });
            }

           /*
            * Ocassionally an Exception is thrown because the facebook session has been temporarily
            * disconnected. This is an issue with parse and facebook. If this happens, refresh the
            * page by calling the getDataFromNetwork() method and attempt to retrieve the facebook
            * info again.
            */
            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                facebookRequest();
            }
        });
    }

    /**
     * This method takes a string birthday and calculates the age of the person from it
     *
     * @param birthday the birthdate in string form
     */
    private String getAge(String birthday) {
        Date yourDate;
        String ageString = null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy");
            yourDate = parser.parse(birthday);
            Calendar dob = Calendar.getInstance();
            dob.setTime(yourDate);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            Integer ageInt = age;
            ageString = ageInt.toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return ageString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_onboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_share) { //launch a share intent
            Bundle b = new Bundle();
            b.putString(IntentFactory.SHARE_TEXT, getString(R.string.share_text));
            b.putString(IntentFactory.SHARE_SUBJECT, getString(R.string.share_subject));
            IntentFactory.pickIntent(OnBoardActivity.this, IntentFactory.SHARE, b);
        }

        return super.onOptionsItemSelected(item);
    }
}
