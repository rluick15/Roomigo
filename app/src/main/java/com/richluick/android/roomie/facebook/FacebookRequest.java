package com.richluick.android.roomie.facebook;

import android.content.Context;

import com.facebook.Session;
import com.parse.ParseUser;

/**
 * This class stores the Facebook Session and user info and handles all the different requests
 * performed by Roomie. It also integrates the requests with the Parse Account for consistancy
 */
public class FacebookRequest {

    private Context mContext;
    private Session mSession;
    private ParseUser mParseUser;

    public FacebookRequest(Context context) {
        this.mContext = context;
        mSession = Session.getActiveSession();
        mParseUser = ParseUser.getCurrentUser();
    }

    /**
     * This method  checks if the user is logged in through facebook to help prevent null
     * pointers
     */
    public Boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        return (session != null && session.isOpened());
    }

    /**
     * This method sends a data request to the facebook api server and retrieves current users id
     * and the username.
     * It then sets them into the shared preferences for later use
     */
    public void setCurrentFacebookUser() {

        if (isLoggedIn()) {
//            Request request = Request.newMeRequest(mSession, new Request.GraphUserCallback() {
//                @Override
//                public void onCompleted(GraphUser user, Response response) {
//                    // If the response is successful
//                    if (mSession == Session.getActiveSession()) {
//                        if (user != null) {
//                            String currentUserId = user.getId();
//                            String name = user.getFirstName();
//                            String birthday = user.getBirthday();
//                            String age = getAge(birthday);
//                            String gender = user.asMap().get(Constants.GENDER).toString();
//
//                            mParseUser.put(Constants.NAME, name);
//                            mParseUser.put(Constants.AGE, age);
//                            mParseUser.put(Constants.GENDER, gender);
//                            mParseUser.saveInBackground();
//
//                            SharedPreferences pref =
//                                    mContext.getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
//                                            Context.MODE_PRIVATE);
//                            SharedPreferences.Editor ed = pref.edit();
//                            ed.putString(Constants.FACEBOOK_USER_ID, currentUserId);
//                            ed.apply();
//                        }
//                    }
//                }
//            });
//            Request.executeBatchAsync(request);
        }
    }
}
