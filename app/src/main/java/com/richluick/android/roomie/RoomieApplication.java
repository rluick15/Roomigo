package com.richluick.android.roomie;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

/**
 * The application class for Roomie
 */
public class RoomieApplication extends Application {

    public void onCreate() {
        Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_KEY);
        ParseFacebookUtils.initialize(Constants.FACEBOOK_APP_ID);

        ParseInstallation.getCurrentInstallation().saveInBackground();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }

    public static void updateParseInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(Constants.USER_ID, ParseUser.getCurrentUser().getObjectId());
        installation.saveInBackground();
    }
}
