package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.fragments.RoomieFragment;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private ParseUser mCurrentUser;
    private Button mAcceptButton;
    private Button mRejectButton;
    private ParseUser mUser;
    private List<String> mCurrentRelations;
    private CardView mCardView;
    private Animation mSlideOutRight;
    private Animation mSlideOutLeft;
    private Animation mExpandIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ConnectionDetector detector = new ConnectionDetector(this);
        if(!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            return;
        }

        mCurrentUser = ParseUser.getCurrentUser();

        setAnimations();
        previousRelationQuery();

        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);
        mAcceptButton.setOnClickListener(this);
        mRejectButton.setOnClickListener(this);
    }

    /**
     * This method sets the animations and listeners for the card animations used in this activity
     */
    private void setAnimations() {
        mCardView = (CardView) findViewById(R.id.roomieFrag);

        mExpandIn = AnimationUtils.loadAnimation(this, R.anim.card_expand_in);

        mSlideOutRight = AnimationUtils.loadAnimation(this, R.anim.card_slide_out_right);
        mSlideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mCardView.startAnimation(mExpandIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.card_slide_out_left);
        mSlideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mCardView.startAnimation(mExpandIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    public void onClick(View v) {

        if(v == mAcceptButton) {
            mCardView.startAnimation(mSlideOutLeft);
            roomieRequestQuery();
        }
        else if(v == mRejectButton){
            mCardView.startAnimation(mSlideOutRight);
            previousRelationQuery();
        }
    }

    /**
     * This method performs the ParseQuery and returns a new "Roomie" user object each time the user
     * either accepts or rejects the previous "Roomie" user object. It then displays the object in
     * the RoomieFragment
     */
    private void roomieQuery() {
        ParseGeoPoint userLocation = (ParseGeoPoint) mCurrentUser.get(Constants.GEOPOINT);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
        //query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.whereNotContainedIn(Constants.OBJECT_ID, mCurrentRelations);

        if((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.MALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.MALE);
        }
        else if((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.FEMALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.FEMALE);
        }

        if(String.valueOf(mCurrentUser.get(Constants.HAS_ROOM)).equals(Constants.TRUE)) {
            query.whereEqualTo(Constants.HAS_ROOM, false);
        }

        int count = 0;
        try {
            //todo: eliminate twice in a row results
            count = query.count();
        } catch (ParseException ignored) {}
        query.setSkip((int) Math.floor(Math.random() * count));

        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(e == null) {
                    if (!parseUsers.isEmpty() && parseUsers != null) {
                        mAcceptButton.setEnabled(true);
                        mRejectButton.setEnabled(true);

                        mUser = parseUsers.get(0);

                        String name = (String) mUser.get(Constants.NAME);
                        String age = (String) mUser.get(Constants.AGE);
                        String location = (String) mUser.get(Constants.LOCATION);
                        String aboutMe = (String) mUser.get(Constants.ABOUT_ME);
                        Boolean hasRoom = (Boolean) mUser.get(Constants.HAS_ROOM);
                        ParseFile profImage = (ParseFile) mUser.get(Constants.PROFILE_IMAGE);

                        RoomieFragment fragment = new RoomieFragment(hasRoom, aboutMe, location, name,
                                profImage, age);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.roomieFrag, fragment)
                                .commit();
                    }
                    else {
                        //todo: handle empty list
                        RoomieFragment fragment = (RoomieFragment) getFragmentManager().findFragmentById(R.id.roomieFrag);
                        if (fragment != null) {
                            getFragmentManager().beginTransaction().remove(fragment).commit();
                        }

                        mAcceptButton.setEnabled(false);
                        mRejectButton.setEnabled(false);
                    }
                }
            }
        });
    }

    /**
     * This method is called when the user accepts or rejects a Roomie card. It creates a list
     * of users that the current user is already in a relation with and adds them to a list. It
     * uses that list to exclude those users from the query
     */
    private void previousRelationQuery() {
        mCurrentRelations = new ArrayList<>();

        ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.RELATION);
        query1.whereEqualTo(Constants.USER1, mCurrentUser);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Constants.RELATION);
        query2.whereEqualTo(Constants.USER2, mCurrentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> relationQuery = ParseQuery.or(queries);
        relationQuery.include(Constants.USER1);
        relationQuery.include(Constants.USER2);
        relationQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                mCurrentRelations.clear();

                for(int i = 0; i < parseObjects.size(); i++) {
                    ParseUser user1 = (ParseUser) parseObjects.get(i).get(Constants.USER1);
                    String userId = user1.getObjectId();

                    ParseUser user;

                    if(userId.equals(mCurrentUser.getObjectId())) {
                        user = (ParseUser) parseObjects.get(i).get(Constants.USER2);
                    }
                    else {
                        user = (ParseUser) parseObjects.get(i).get(Constants.USER1);
                    }
                    mCurrentRelations.add(user.getObjectId());
                }

                roomieQuery();
            }
        });
    }

    /**
     * This method is called when the user accepts a Roomie card. It first checks if the other user
     * has already sent a RoomieRequest via a parse query. If so, then a relation is established
     * between the two users. If not, then a RoomieRequest is sent to the other user
     */
    private void roomieRequestQuery() {
        ParseQuery<ParseObject> requestQuery = ParseQuery.getQuery(Constants.ROOMIE_REQUEST);
        requestQuery.whereEqualTo(Constants.SENDER, mUser);
        requestQuery.whereEqualTo(Constants.RECEIVER, mCurrentUser);
        requestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.isEmpty()) {
                        ParseObject request = new ParseObject(Constants.ROOMIE_REQUEST);
                        request.put(Constants.SENDER, mCurrentUser);
                        request.put(Constants.RECEIVER, mUser);
                        request.saveInBackground();

                        previousRelationQuery();
                    }
                    else {
                        for(int i = 0; i < parseObjects.size(); i++) {
                            parseObjects.get(0).deleteInBackground();
                        }

                        ParseObject relation = new ParseObject(Constants.RELATION);
                        relation.put(Constants.USER1, mCurrentUser);
                        relation.put(Constants.USER2, mUser);
                        relation.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                previousRelationQuery();
                            }
                        });

                        try {
                            sendPushNotification();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                else {
                    //todo: handle errors!!
                }
            }
        });
    }

    /**
     * This method handles the push notifications sent when a connection is made. Push1 goes to
     * the other user while Push2 goes to the current user. Both open the chat when clicked on.
     */
    private void sendPushNotification() throws JSONException {
        ParseQuery<ParseInstallation> query1 = ParseInstallation.getQuery();
        query1.whereEqualTo(Constants.USER_ID, mUser.getObjectId());

        JSONObject data1 = new JSONObject();
        data1.put(Constants.PUSH_ALERT, getString(R.string.message_new_connection));
        data1.put(Constants.PUSH_ID, mCurrentUser.getObjectId());
        data1.put(Constants.PUSH_NAME, mCurrentUser.get(Constants.NAME));

        ParsePush push1 = new ParsePush();
        push1.setQuery(query1);
        push1.setData(data1);
        push1.sendInBackground();

        ParseQuery<ParseInstallation> query2 = ParseInstallation.getQuery();
        query2.whereEqualTo(Constants.USER_ID, mCurrentUser.getObjectId());

        JSONObject data2 = new JSONObject();
        data2.put(Constants.PUSH_ALERT, getString(R.string.message_new_connection));
        data2.put(Constants.PUSH_ID, mUser.getObjectId());
        data2.put(Constants.PUSH_NAME, mUser.get(Constants.NAME));

        ParsePush push2 = new ParsePush();
        push2.setQuery(query2);
        push2.setData(data2);
        push2.sendInBackground();
    }
}
