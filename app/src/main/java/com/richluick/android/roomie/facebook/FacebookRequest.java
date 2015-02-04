package com.richluick.android.roomie.facebook;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * This class stores the Facebook Session and user info and handles all the different requests
 * performed by Roomie
 */
public class FacebookRequest {

    private String mCurrentUserId;
    private Context mContext;
    private Session mSession;

    public FacebookRequest(Context context) {
        this.mContext = context;
        getCurrentFacebookUser();
        mSession = Session.getActiveSession();
    }

    public Boolean isLoggedIn() {
        return Session.getActiveSession().isOpened();
    }

    /**
     * This method sends a data request to the facebook api server and retrieves current users id
     */
    private void getCurrentFacebookUser() {
        final Session session = Session.getActiveSession();
        if (isLoggedIn()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // If the response is successful
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            mCurrentUserId = user.getId();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

    public Bitmap getProfilePicture() {
         return null;
    }
}
