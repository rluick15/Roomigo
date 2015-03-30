package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.fragments.RoomieFragment;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchActivity extends BaseActivity implements View.OnClickListener {

    //todo: display view if user is not discoverable

    private ParseUser mCurrentUser;
    private ParseUser mUser;
    private List<String> mCurrentRelations;
    private Animation mSlideOutRight;
    private Animation mSlideOutLeft;
    private Animation mExpandIn;
    private List<String> mIndices = new ArrayList<>();
    private RoomieFragment mRoomieFragment;

    @InjectView(R.id.acceptButton) Button mAcceptButton;
    @InjectView(R.id.rejectButton) Button mRejectButton;
    @InjectView(R.id.emptyView) TextView mEmptyView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.roomieFrag) CardView mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);

        ConnectionDetector detector = new ConnectionDetector(this);
        if(!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            return;
        }

        mEmptyView.setOnClickListener(this);

        mRoomieFragment = new RoomieFragment(); //initialize the fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.roomieFrag, mRoomieFragment)
                .commit();

        mCurrentUser = ParseUser.getCurrentUser();

        setAnimations();
        previousRelationQuery();

        mAcceptButton.setOnClickListener(this);
        mRejectButton.setOnClickListener(this);
    }

    /**
     * This method sets the animations and listeners for the card animations used in this activity
     */
    private void setAnimations() {
        mExpandIn = AnimationUtils.loadAnimation(this, R.anim.card_expand_in);

        mSlideOutRight = AnimationUtils.loadAnimation(this, R.anim.card_slide_out_right);
        mSlideOutRight.setFillAfter(true);

        mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.card_slide_out_left);
        mSlideOutLeft.setFillAfter(true);
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
        else if(v == mEmptyView) {
            mProgressBar.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   mProgressBar.setVisibility(View.GONE);
                   previousRelationQuery();
                }
            }, 1000);
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
        query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.whereNotEqualTo(Constants.DISCOVERABLE, false);
        query.whereNotContainedIn(Constants.OBJECT_ID, mCurrentRelations);

        if ((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.MALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.MALE);
        } else if ((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.FEMALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.FEMALE);
        }

        if (String.valueOf(mCurrentUser.get(Constants.HAS_ROOM)).equals(Constants.TRUE)) {
            query.whereEqualTo(Constants.HAS_ROOM, false);
        }

        int count = 0;
        try {
            count = query.count();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mIndices.size() == count) {
            mIndices.clear();
        }

        int random = 0;
        if (count != 0) {
            Boolean check = false;
            while (!check) {
                random = (int) Math.floor(Math.random() * count);
                if (!mIndices.contains(String.valueOf(random))) {
                    check = true;
                    mIndices.add(String.valueOf(random));
                }
            }
        }

        query.setSkip(random);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    if (!parseUsers.isEmpty() && parseUsers != null) {
                        mAcceptButton.setEnabled(true);
                        mRejectButton.setEnabled(true);

                        mUser = parseUsers.get(0);

                        String name = (String) mUser.get(Constants.NAME);
                        String age = (String) mUser.get(Constants.AGE);
                        String location = (String) mUser.get(Constants.LOCATION);
                        String aboutMe = (String) mUser.get(Constants.ABOUT_ME);
                        Boolean hasRoom = (Boolean) mUser.get(Constants.HAS_ROOM);
                        Boolean smokes = (Boolean) mUser.get(Constants.SMOKES);
                        Boolean drinks = (Boolean) mUser.get(Constants.DRINKS);
                        Boolean pets = (Boolean) mUser.get(Constants.PETS);
                        ParseFile profImage = (ParseFile) mUser.get(Constants.PROFILE_IMAGE);

                        if (mCardView.getVisibility() == View.GONE) {
                            mCardView.setVisibility(View.VISIBLE);
                        }

                        mCardView.startAnimation(mExpandIn);

                        mRoomieFragment.setName(name);
                        mRoomieFragment.setAge(age);
                        mRoomieFragment.setLocation(location);
                        mRoomieFragment.setAboutMe(aboutMe);
                        mRoomieFragment.setHasRoom(hasRoom);
                        mRoomieFragment.setProfImage(profImage);
                        mRoomieFragment.setSmokes(smokes);
                        mRoomieFragment.setDrinks(drinks);
                        mRoomieFragment.setPets(pets);
                        mRoomieFragment.setFields();
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                        mCardView.setVisibility(View.GONE);

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
                if(e == null) {
                    mCurrentRelations.clear();

                    for (int i = 0; i < parseObjects.size(); i++) {
                        ParseUser user1 = (ParseUser) parseObjects.get(i).get(Constants.USER1);
                        String userId = user1.getObjectId();

                        ParseUser user;

                        if (userId.equals(mCurrentUser.getObjectId())) {
                            user = (ParseUser) parseObjects.get(i).get(Constants.USER2);
                        } else {
                            user = (ParseUser) parseObjects.get(i).get(Constants.USER1);
                        }
                        mCurrentRelations.add(user.getObjectId());
                    }

                    roomieQuery();
                }
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
                    previousRelationQuery();

                    if (parseObjects.isEmpty()) {
                        ParseObject request = new ParseObject(Constants.ROOMIE_REQUEST);
                        request.put(Constants.SENDER, mCurrentUser);
                        request.put(Constants.RECEIVER, mUser);
                        request.saveInBackground();
                    }
                    else {
                        for(int i = 0; i < parseObjects.size(); i++) {
                            parseObjects.get(i).deleteInBackground();
                        }

                        ParseObject relation = new ParseObject(Constants.RELATION);
                        relation.put(Constants.USER1, mCurrentUser);
                        relation.put(Constants.USER2, mUser);
                        relation.saveInBackground();

                        try {
                            sendPushNotification();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                else {
                    e.printStackTrace();
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
