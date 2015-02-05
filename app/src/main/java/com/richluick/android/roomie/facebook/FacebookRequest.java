package com.richluick.android.roomie.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

import java.io.IOException;
import java.net.URL;

/**
 * This class stores the Facebook Session and user info and handles all the different requests
 * performed by Roomie
 */
public class FacebookRequest {

    private Context mContext;
    private Session mSession;

    public FacebookRequest(Context context) {
        this.mContext = context;
        mSession = Session.getActiveSession();
    }

    /**
     * This method  checks if the user is logged in through facebook to help prevent null
     * pointers
     */
    private Boolean isLoggedIn() {
        return Session.getActiveSession().isOpened();
    }

    /**
     * This method sends a data request to the facebook api server and retrieves current users id.
     * It then sets it into the shared preferences for later use
     */
    public void setCurrentFacebookUser() {
        final Session session = Session.getActiveSession();
        if (isLoggedIn()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // If the response is successful
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            String currentUserId = user.getId();

                            SharedPreferences pref = mContext.getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor ed = pref.edit();
                            ed.putString(Constants.FACEBOOK_USER_ID, currentUserId);
                            ed.commit();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

    /**
     * This method  returns the current facebook user Id set in the shared prefs on Login
     *
     * @return String the id of the current facebook user
     */
    private String getCurrentFacebookUser() {
        SharedPreferences pref = mContext.getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
                Context.MODE_PRIVATE);
        return pref.getString(Constants.FACEBOOK_USER_ID, null);
    }

    /**
     * This method  sends a request to the facebook server to return the profile picture of the
     * current user
     *
     * @return Bitmap the bitmap of the users profile pic
     */
    public Bitmap getProfilePicture() throws IOException {
        Bitmap bitmap = null;
        String currentUserId = getCurrentFacebookUser();

        if(currentUserId != null) {
            URL imageURL = new URL("https://graph.facebook.com/" + currentUserId + "/picture?type=large");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        }

        return bitmap;
    }
}
