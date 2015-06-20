package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.ui.adapters.NavListAdapter;
import com.richluick.android.roomie.ui.fragments.ChatsFragment;
import com.richluick.android.roomie.ui.fragments.SearchFragment;
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

public class MainActivity extends BaseActivity {

    private ParseUser mCurrentUser;
    private Boolean mConnected = true;
    private ImageLoader loader;
    private ParseFile mProfImage;

    //for nav drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mPreviousPosition = 0; //default nav drawer selected position

    @InjectView(R.id.navList) ListView mNavList;
    @InjectView(R.id.navProfImage) ImageView mNavProfImageField;
    @InjectView(R.id.navName) TextView mNavNameField;

    //todo:add progress bar indicators for profile progress
    //todo: go here on General notification
    //todo: get user emails
    //todo: delay a few secondes while finding matches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        loader = ImageLoader.getInstance(); //get the ImageLoader instance

        getDataFromNetwork(); //todo: observe when this finishes, then load search fragment

        setupNavDrawer();
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
            loader.displayImage(profImage.getUrl(), mNavProfImageField);
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
                mNavNameField.setText(username);
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
                loader.displayImage(mProfImage.getUrl(), mNavProfImageField);
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
        //todo:get email
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
                            mNavProfImageField);
                }

                if (name != null) { //display the username from facebook
                    mNavNameField.setText(name);
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

    /**
     * This method handles everything required to setup the nav drawer in Main Activity
     */
    private void setupNavDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {}

            public void onDrawerClosed(View view) {}
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //NavItems to add to the drawer list.
        NavItem search = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_search, null), getString(R.string.search_for_roommates));
        NavItem chat = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_chat, null), getString(R.string.button_chats));
        NavItem settings = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_settings, null), getString(R.string.action_settings));
        NavItem share = new NavItem(getResources()
                .getDrawable(R.drawable.ic_share, null), getString(R.string.action_share));
        NavItem feedback = new NavItem(getResources()
                .getDrawable(R.drawable.ic_action_help, null), getString(R.string.action_help));

        //list with nav items. Add items to list in desired order
        ArrayList<NavItem> navItems = new ArrayList<>();
        navItems.add(search);
        navItems.add(chat);
        navItems.add(share);
        navItems.add(feedback);
        navItems.add(settings);

        NavListAdapter adapter = new NavListAdapter(this, navItems); //adapter to display items
        mNavList.setAdapter(adapter);
        mNavList.setItemChecked(0, true); //Search is selected by default

        mNavList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //close the nav drawer if the user selects the same item as previously
                if (position == mPreviousPosition) {
                    if (mDrawerLayout != null) { //close nav drawer
                        mDrawerLayout.closeDrawers();
                    }
                    return;
                }

                switch (position) {
                    case 0: //search
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new SearchFragment(MainActivity.this))
                                .addToBackStack(null)
                                .commit();
                        mNavList.setItemChecked(position, true);
                        mPreviousPosition = position;
                        if (mDrawerLayout != null) { //close nav drawer
                            mDrawerLayout.closeDrawers();
                        }
                        break;

                    case 1: //chat
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new ChatsFragment(MainActivity.this))
                                .addToBackStack(null)
                                .commit();
                        mNavList.setItemChecked(position, true);
                        mPreviousPosition = position;
                        if (mDrawerLayout != null) { //close nav drawer
                            mDrawerLayout.closeDrawers();
                        }
                        break;

                    case 2: //share
                        //create a share intent
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                        startActivity(shareIntent);
                        mNavList.setItemChecked(mPreviousPosition, true);
                        break;

                    case 3: //feedback
                        //create an email intent
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/intent");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.ROOMIGO_EMAIL});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Help/Feedback");
                        startActivity(intent);
                        mNavList.setItemChecked(mPreviousPosition, true);
                        break;

                    case 4: //settings
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                        mNavList.setItemChecked(mPreviousPosition, true);
                        break;
                }
            }
        });

        //set onclick for NavHeader
        RelativeLayout navHeader = (RelativeLayout) findViewById(R.id.navHeader);
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
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

        if(id == R.id.action_refresh) {
            getDataFromNetwork();
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

