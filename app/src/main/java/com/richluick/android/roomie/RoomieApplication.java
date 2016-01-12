package com.richluick.android.roomie;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.richluick.android.roomie.home.connections.ConnectionsList;
import com.richluick.android.roomie.home.search.SearchResults;
import com.richluick.android.roomie.utils.constants.ApiKeys;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.constants.Constants;

import java.util.HashMap;

/**
 * The application class for Roomie
 */
public class RoomieApplication extends Application {

    private static final String PROPERTY_ID = "UA-62279592-1";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    public void onCreate() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(this, ApiKeys.APPLICATION_ID, ApiKeys.CLIENT_KEY);
        ParseFacebookUtils.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        //Initialize ImageLoader Singleton
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        //Intialize ConnectionDetector singleton
        ConnectionDetector.getInstance(getApplicationContext());

        //Initialize ConnectionsList singleton
        ConnectionsList.getInstance(getApplicationContext());

        //Initialize SearchResults singleton
        SearchResults.getInstance(getApplicationContext());
    }

    public static void updateParseInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(Constants.USER_ID, ParseUser.getCurrentUser().getObjectId());
        installation.saveInBackground();
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER)
                        ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER)
                        ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}
