package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.data.ConnectionsList;
import com.richluick.android.roomie.data.MainActivityData;
import com.richluick.android.roomie.data.SearchResults;
import com.richluick.android.roomie.ui.adapters.NavListAdapter;
import com.richluick.android.roomie.ui.fragments.ChatsFragment;
import com.richluick.android.roomie.ui.fragments.SearchFragment;
import com.richluick.android.roomie.ui.objects.NavItem;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity implements MainActivityData.MainDataListener,
        ConnectionsList.ConnectionsLoadedListener {

    private ParseUser mCurrentUser;
    private ImageLoader loader;
    private SimpleFacebook mSimpleFacebook;

    //for nav drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mPreviousPosition = 0; //default nav drawer selected position
    private MainActivityData mainData;

    @InjectView(R.id.navList) ListView mNavList;
    @InjectView(R.id.navProfImage) ImageView mNavProfImageField;
    @InjectView(R.id.navName) TextView mNavNameField;
    @InjectView(R.id.progressBar) ProgressBar mProgress;
    @InjectView(R.id.loadingText) TextView mLoadingText;

    //todo: go here on General push notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgress.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);

        //Google Analytics
        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        mCurrentUser = ParseUser.getCurrentUser(); //get the current user
        mSimpleFacebook = SimpleFacebook.getInstance(this); //get an instance of the Simple Facebook library
        loader = ImageLoader.getInstance(); //get the ImageLoader instance
        mainData = new MainActivityData(); //get the MainActivityData object

        //get the users email if not in the database
        if (mCurrentUser != null && mCurrentUser.get(Constants.EMAIL) == null) {
            mainData.getFacebookEmail(mCurrentUser, mSimpleFacebook);
        }

        setDefaultSettings();

        //delay 3s for effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setupNavDrawer();
                callDataIfConnected();
            }
        }, 3000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this); //Google Analytics setup
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser.fetchIfNeededInBackground();

        //if prof pic has been changed, reload
        ParseFile profImage = mCurrentUser.getParseFile(Constants.PROFILE_IMAGE);
        if (profImage != null) {
            loader.displayImage(profImage.getUrl(), mNavProfImageField);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this); //stop Analytics
    }

    /**
     * This method checks the connection and then calls the MainActivityData class to retrieve
     * the information to populate the views on the MainActivity
     */
    private void callDataIfConnected() {
        if (!ConnectionDetector.getInstance(this).isConnected()) { //check the connection
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
        }
        else {
            //get the connections list from Parse and move forward once it is retrieved
            ConnectionsList.getInstance(this).getConnectionsFromParse(mCurrentUser, this);
        }
    }

    //the listener callback for when the connection list is loaded
    @Override
    public void onConnectionsLoaded() {
        mainData.getDataFromNetwork(this, mCurrentUser, mSimpleFacebook, this);
    }

    //The listener callback for when the main data is loaded
    @Override
    public void onDataLoadedListener(String profURL, String username) {
        mProgress.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);

        if (username != null) {
            mNavNameField.setText(username);
        }

        if(profURL != null) {
            loader.displayImage(profURL, mNavProfImageField, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null) {
                        mainData.saveImageToParse(loadedImage); //save the image to Parse backend
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        }

        //start by loading the SearchFragment
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SearchFragment(MainActivity.this))
                .addToBackStack(null)
                .commit();
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

        if(id == R.id.action_refresh) { //refresh the page
            callDataIfConnected();
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

