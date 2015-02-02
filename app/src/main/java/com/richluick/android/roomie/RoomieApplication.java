package com.richluick.android.roomie;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.richluick.android.roomie.utils.Constants;

/**
 * The application class for Roomie
 */
public class RoomieApplication extends Application {

    public void onCreate() {
        Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_KEY);

        ParseUser user = new ParseUser();
        user.setUsername("my name");
        user.setPassword("my pass");
        user.setEmail("email@example.com");

// other fields can be set just like with ParseObject
        user.put("phone", "650-555-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                } else {
                }
            }
        });
    }

}
