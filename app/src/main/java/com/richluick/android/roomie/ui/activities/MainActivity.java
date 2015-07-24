package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseFile;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.data.ConnectionsList;
import com.richluick.android.roomie.data.MainActivityData;
import com.richluick.android.roomie.ui.adapters.NavListAdapter;
import com.richluick.android.roomie.ui.fragments.ChatsFragment;
import com.richluick.android.roomie.ui.fragments.SearchFragment;
import com.richluick.android.roomie.ui.objects.NavItem;
import com.richluick.android.roomie.utils.Constants;
import com.sromku.simple.fb.SimpleFacebook;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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

    @InjectView(R.id.navList) ListView mNavList;
    @InjectView(R.id.navProfImage) ImageView mNavProfImageField;
    @InjectView(R.id.navName) TextView mNavNameField;

    //todo: go here on General push notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

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
        setupNavDrawer();

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
        ConnectionsList.getInstance(this).getConnectionsFromParse(mCurrentUser)
            .delay(3, TimeUnit.SECONDS)
            .flatMap(s -> mainData.getDataFromNetwork(this, mCurrentUser, mSimpleFacebook))
            .flatMap(s -> mainData.getPictureFromUrl(s))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Bitmap>() {
                @Override
                public void onCompleted() {
                    if (mCurrentUser.get(Constants.NAME) != null) {
                        mNavNameField.setText((String) mCurrentUser.get(Constants.NAME));
                    }
                    mSearchFragment.setupActivity();
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    mSearchFragment.setEmptyView();
                }

                @Override
                public void onNext(Bitmap bitmap) {
                    mNavProfImageField.setImageBitmap(bitmap);
                    mainData.saveImageToParse(bitmap); //save the image to Parse backend
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
        });

        //set onclick for NavHeader
        findViewById(R.id.navHeader).setOnClickListener((View v) -> {
            startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

