package com.richluick.android.roomie.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.richluick.android.roomie.BaseActivity;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.activities.OnBoardActivity;
import com.richluick.android.roomie.home.connections.ChatsFragment;
import com.richluick.android.roomie.home.connections.ConnectionsList;
import com.richluick.android.roomie.home.search.SearchFragment;
import com.richluick.android.roomie.utils.IntentFactory;
import com.richluick.android.roomie.utils.constants.Constants;
import com.sromku.simple.fb.SimpleFacebook;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    private ParseUser mCurrentUser;
    private ImageLoader loader;
    private SimpleFacebook mSimpleFacebook;

    //for nav drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mPreviousPosition = 0; //default nav drawer selected position
    private MainActivityData mainData;
    private SearchFragment mSearchFragment;

    @Bind(R.id.navList) ListView mNavList;
    @Bind(R.id.navProfImage) ImageView mNavProfImageField;
    @Bind(R.id.navName) TextView mNavNameField;

    //todo: go here on General push notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

        //start by loading the SearchFragment
        mSearchFragment = new SearchFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mSearchFragment)
                .addToBackStack(null)
                .commit();

        getDataFromServer();
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
        SharedPreferences prefs = getSharedPreferences(mCurrentUser.getObjectId(), MODE_PRIVATE);

        //todo:settings activity always triggering this
        if(prefs.getBoolean(Constants.PROFILE_UPDATED, false)) {
            mSearchFragment.showProgressLayout();
            getDataFromServer();
            prefs.edit().putBoolean(Constants.PROFILE_UPDATED, false).apply();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this); //stop Analytics
    }

    /*
     * This method retrieves a series of RxJava Observables and executes them in order to retrieve
     * the current user's data from Parse. This includes the current connections list, and general
     * data. Called onCreate and if the user chanes his preferences, onResume
     */
    private void getDataFromServer() {
        //todo: add timeout for long network calls
        ConnectionsList.getInstance(this).getConnectionsFromParse(mCurrentUser)
            .delay(3, TimeUnit.SECONDS)
            .map(s -> mCurrentUser.getParseFile(Constants.PROFILE_IMAGE).getUrl())
            .flatMap(s -> mainData.getPictureFromUrl(s))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Bitmap>() {
                @Override
                public void onCompleted() {
                    Intent intent = new Intent(MainActivity.this, OnBoardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

                    if (mCurrentUser.get(Constants.NAME) != null) {
                        mNavNameField.setText((String) mCurrentUser.get(Constants.NAME));
                    }

                    mSearchFragment.setupActivity();
                }

                @Override
                public void onError(Throwable e) {
                    setupNavDrawer();
                    e.printStackTrace();
                    mSearchFragment.setEmptyView();
                }

                @Override
                public void onNext(Bitmap bitmap) {
                    mNavProfImageField.setImageBitmap(bitmap);
                }
            });
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

        mNavList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
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
                            .replace(R.id.container, new SearchFragment())
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
                            .replace(R.id.container, new ChatsFragment())
                            .addToBackStack(null)
                            .commit();
                    mNavList.setItemChecked(position, true);
                    mPreviousPosition = position;
                    if (mDrawerLayout != null) { //close nav drawer
                        mDrawerLayout.closeDrawers();
                    }
                    break;

                case 2: //share
                    Bundle b = new Bundle();
                    b.putString(IntentFactory.SHARE_TEXT, getString(R.string.share_text));
                    b.putString(IntentFactory.SHARE_SUBJECT, getString(R.string.share_subject));
                    IntentFactory.pickIntent(MainActivity.this, IntentFactory.SHARE, b);
                    mNavList.setItemChecked(mPreviousPosition, true);
                    break;

                case 3: //feedback
                    Bundle bundle = new Bundle();
                    bundle.putString(IntentFactory.SHARE_SUBJECT, getString(R.string.help_feedback));
                    bundle.putString(IntentFactory.SHARE_EMAIL, Constants.ROOMIGO_EMAIL);
                    IntentFactory.pickIntent(MainActivity.this, IntentFactory.SHARE, bundle);
                    mNavList.setItemChecked(mPreviousPosition, true);
                    break;

                case 4: //settings
                    IntentFactory.pickIntent(MainActivity.this, IntentFactory.SETTINGS, false, R.anim.slide_in_right, R.anim.hold);
                    mNavList.setItemChecked(mPreviousPosition, true);
                    break;
            }
        });

        //set onclick for NavHeader
        findViewById(R.id.navHeader).setOnClickListener((View v) -> {
            IntentFactory.pickIntent(MainActivity.this, IntentFactory.EDIT_PROFILE, false, R.anim.slide_in_right, R.anim.hold);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

