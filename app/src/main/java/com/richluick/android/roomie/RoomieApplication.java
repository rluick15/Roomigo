package com.richluick.android.roomie;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.richluick.android.roomie.utils.Constants;

/**
 * The application class for Roomie
 */
public class RoomieApplication extends Application {

    public void onCreate() {
        Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_KEY);
        ParseFacebookUtils.initialize(Constants.FACEBOOK_APP_ID);
    }

}
