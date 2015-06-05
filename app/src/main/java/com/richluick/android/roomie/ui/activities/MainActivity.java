package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.richluick.android.roomie.ui.adapters.NavAdapter;
import com.richluick.android.roomie.ui.objects.NavItem;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @InjectView(R.id.navList) ListView mNavList;

    //todo:add progress bar indicators for profile progress
    //todo: go here on General notification
    //todo: get user emails

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        loader = ImageLoader.getInstance(); //get the ImageLoader instance

        getDataFromNetwork();

        setupNavDrawer();

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
        GoogleAnalytics.getInstance(this).reportActivityStart(this); //Google Analytics setup
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
        GoogleAnalytics.getInstance(this).reportActivityStop(this); //stop Analytics
    }

    /**
     * This method first checks the connection and then sets the profile image and the username by
     * getting the data from either Facebook or Parse. It is called either during onCreate or if
     * the user clicks refresh in the menu
     */
    private void getDataFromNetwork() {
        mCurrentUser = ParseUser.getCurrentUser();
        mCurrentUser.fetchInBackground();

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

            //ParseObject yes =

            String username = (String) mCurrentUser.get(Constants.NAME);
            mProfImage = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE);

            //todo: take into account edge cases
            //if prof pic is null then request from facebook. Should only be on the first login
            if (mProfImage == null) {
                setDefaultSettings();

                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) { //check if session opened properly
                    facebookRequest();
                }
            }
            else { //get the prof pic from parse
                loader.displayImage(mProfImage.getUrl(), mProfPicField, MainActivity.this);
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
        //get simple facebook and add the user properties we are looking to retrieve
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

                if(id != null) { //display the profile image from facebook
                    loader.displayImage("https://graph.facebook.com/" + id + "/picture?type=large",
                            mProfPicField, MainActivity.this);
                }

                if (name != null) { //display the username from facebook
                    mUsernameField.setText(name);
                    mNameProgressBar.setVisibility(View.INVISIBLE);
                }
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
                getDataFromNetwork();
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

        if(mProfImage == null) {
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

    private void setupNavDrawer() {
        NavItem settings = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_settings, null), getString(R.string.action_settings));
        NavItem share = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_share, null), getString(R.string.action_share));
//        NavItem feedback = new NavItem(getResources()
//                .getDrawable(R.drawable.ic_action_settings, null), getString(R.string.action_settings));

        ArrayList<NavItem> navItems = new ArrayList<>();
        navItems.add(settings);
        navItems.add(share);

        NavAdapter adapter = new NavAdapter(this, navItems);
        mNavList.setAdapter(adapter);
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
        else if(id == R.id.action_share) { //launch a share intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

