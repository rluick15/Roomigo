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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
            Request request = Request.newMeRequest(mSession, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // If the response is successful
                    if (mSession == Session.getActiveSession()) {
                        if (user != null) {
                            String currentUserId = user.getId();
                            String name = user.getFirstName();
                            String birthday = user.getBirthday();
                            String age = getAge(birthday);
                            String gender = user.asMap().get(Constants.GENDER).toString();

                            mParseUser.put(Constants.NAME, name);
                            mParseUser.put(Constants.AGE, age);
                            mParseUser.put(Constants.GENDER, gender);
                            mParseUser.saveInBackground();

                            SharedPreferences pref =
                                    mContext.getSharedPreferences(ParseUser.getCurrentUser().getUsername(),
                                            Context.MODE_PRIVATE);
                            SharedPreferences.Editor ed = pref.edit();
                            ed.putString(Constants.FACEBOOK_USER_ID, currentUserId);
                            ed.apply();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ageString;
    }

    /**
     * This method  returns the current facebook user Id set in the shared prefs on Login
     *
     * @return String the id of the current facebook user
     */
    public String getCurrentFacebookUserId() {
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
        String currentUserId = getCurrentFacebookUserId();

        if(currentUserId != null) {
            URL imageURL = new URL("https://graph.facebook.com/" + currentUserId + "/picture?type=large");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        }

        return bitmap;
    }
}
