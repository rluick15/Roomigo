package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity implements ImageLoadingListener {

    private ParseUser mCurrentUser;
    private Boolean mConnected = true;
    private ImageLoader loader;
    private ParseFile mProfImage;

    @InjectView(R.id.imageProgressBar) ProgressBar mImageProgressBar;
    @InjectView(R.id.nameProgressBar) ProgressBar mNameProgressBar;
    @InjectView(R.id.profImage) ImageView mProfPicField;
    @InjectView(R.id.nameField) TextView mUsernameField;

    //todo:add progress bar indicators for profile progress
    //todo: go here on General notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        loader = ImageLoader.getInstance(); //get the ImageLoader instance

        getDataFromNetwork();

        //setup the Main page buttons
        RelativeLayout profileButton = (RelativeLayout) findViewById(R.id.profileSplace);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

            }
        });

        RelativeLayout searchButton = (RelativeLayout) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.expand_in_search, R.anim.hold);
            }
        });

        RelativeLayout chatButton = (RelativeLayout) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.expand_in_chat, R.anim.hold);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if prof pic has been changed, reload
        ParseFile profImage = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE);
        if (profImage != null) {
            loader.displayImage(profImage.getUrl(), mProfPicField);
        }

        //if connection was false before leaving the activity, reset the fields
        if(!mConnected) {
            getDataFromNetwork();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /**
     * This method first checks the connection and then sets the profile image and the username by
     * getting the data from either Facebook or Parse. It is called either during onCreate or if
     * the user clicks refresh in the menu
     */
    private void getDataFromNetwork() {
        mCurrentUser = ParseUser.getCurrentUser();
        mCurrentUser.fetchIfNeededInBackground();

        if(mCurrentUser != null) {//set the username field if ParseUser is not null
            String username = (String) mCurrentUser.get(Constants.NAME);

            if (username != null) {
                mUsernameField.setText(username);
            }
        }

        if (!ConnectionDetector.getInstance(this).isConnected()) { //check the connection
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            mConnected = false;
        }
        else { //proceed to set prof pic and settings if connection is active
            mConnected = true;

            setDefaultSettings();

            String username = (String) mCurrentUser.get(Constants.NAME);
            mProfImage = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE);

            //todo: take into account edge cases
            if (username == null && mProfImage == null) {
                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) {
                    facebookRequest();
                }
            }
            else {
                if (mProfImage != null) {
                    loader.displayImage(mProfImage.getUrl(), mProfPicField, MainActivity.this);
                }
            }
        }
    }

    /**
     * This method sets the default settings for discoverable and notification settings the first
     * time the user logs in
     */
    private void setDefaultSettings() {
        Boolean discoverable = (Boolean) mCurrentUser.get(Constants.DISCOVERABLE);
        if(discoverable == null) {
            mCurrentUser.put(Constants.DISCOVERABLE, true);
            mCurrentUser.saveInBackground();
        }

        Boolean generalNot = (Boolean) mCurrentUser.get(Constants.GENERAL_NOTIFICATIONS);
        if(generalNot == null) {
            mCurrentUser.put(Constants.GENERAL_NOTIFICATIONS, true);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.GENERAL_PUSH);
        }

        Boolean messageNot = (Boolean) mCurrentUser.get(Constants.MESSAGE_NOTIFICATIONS);
        if(messageNot == null) {
            mCurrentUser.put(Constants.MESSAGE_NOTIFICATIONS, true);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.MESSAGE_PUSH);
        }

        Boolean connectionNot = (Boolean) mCurrentUser.get(Constants.CONNECTION_NOTIFICATIONS);
        if(connectionNot == null) {
            mCurrentUser.put(Constants.CONNECTION_NOTIFICATIONS, true);
            mCurrentUser.saveInBackground();
            ParsePush.subscribeInBackground(Constants.CONNECTION_PUSH);
        }
    }

    /**
     * This method contains the facebook request and also sets the users info to parse as well as
     * setting the ui elements
     */
    private void facebookRequest() {
        SimpleFacebook simpleFacebook = SimpleFacebook.getInstance(this);
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.GENDER)
                .add(Profile.Properties.BIRTHDAY)
                .add(Profile.Properties.ID)
                .build();

        simpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile response) {
                String id = response.getId();
                String name = response.getFirstName();
                String gender = response.getGender();
                String birthday = response.getBirthday();
                String age = getAge(birthday);

                mCurrentUser.put(Constants.NAME, name);
                mCurrentUser.put(Constants.AGE, age);
                mCurrentUser.put(Constants.GENDER, gender);
                mCurrentUser.saveInBackground();

                if(id != null) {
                    loader.displayImage("https://graph.facebook.com/" + id + "/picture?type=large",
                            mProfPicField, MainActivity.this);
                }

                if (name != null) {
                    mUsernameField.setText(name);
                    mNameProgressBar.setVisibility(View.INVISIBLE);
                }
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

    /**
     * These next 4 methods are from the ImageLoadingListener Interface
     */
    @Override
    public void onLoadingStarted(String s, View view) {
        mImageProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingFailed(String s, View view, FailReason failReason) {
        mImageProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
        mImageProgressBar.setVisibility(View.INVISIBLE);

        if(!s.equals(mProfImage.getUrl())) {
            saveImageToParse(bitmap);
        }
    }

    @Override
    public void onLoadingCancelled(String s, View view) {
        mImageProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * This helper method takes the result from the Facebook prof pic request and converts it to a
     * byte array and then to a Parse file and then uploads it to parse
     *
     * @param bitmap the bitmap image
     */
    private void saveImageToParse(Bitmap bitmap) {
        //convert bitmap to byte array and upload to Parse
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        //save the bitmap to parse
        final ParseFile file = new ParseFile(Constants.PROFILE_IMAGE_FILE, byteArray);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mCurrentUser.put(Constants.PROFILE_IMAGE, file);
                    mCurrentUser.saveInBackground();
                    mCurrentUser.fetchIfNeededInBackground();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
        }
        else if(id == R.id.action_refresh) {
            getDataFromNetwork();
        }

        return super.onOptionsItemSelected(item);
    }
}

