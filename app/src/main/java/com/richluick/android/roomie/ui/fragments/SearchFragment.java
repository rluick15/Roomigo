package com.richluick.android.roomie.ui.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements View.OnClickListener {

    private Context mContext;
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
    @InjectView(R.id.undiscoverableView) TextView mUndiscoverable;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.roomieFrag) CardView mCardView;


    public SearchFragment() {
        // Required empty public constructor
    }

    public SearchFragment(Context ctx) {
        this.mContext = ctx;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, v);

        mEmptyView.setOnClickListener(this);

        //if (checkConnection()) return; //check internet connection

        setupActivity();

        return v;
    }

    /**
     * This method continues the setup of the activity. It is called onCreate and also if the user
     * decides to refresh the activity either after no search results or a connection error
     */
    private void setupActivity() {
        //check if user has selected/deslected discoverable and proceed from there
        Boolean discoverable = (Boolean) ParseUser.getCurrentUser().get(Constants.DISCOVERABLE);
        if(!discoverable) {
            mUndiscoverable.setVisibility(View.VISIBLE);
            mCardView.setVisibility(View.GONE);
            return;
        }

        //build the Roomie card fragment for displaying info
        mRoomieFragment = new RoomieFragment();
        getChildFragmentManager().beginTransaction()
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
        mExpandIn = AnimationUtils.loadAnimation(getActivity(), R.anim.card_expand_in);

        mSlideOutRight = AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_right);
        mSlideOutRight.setFillAfter(true);

        mSlideOutLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_left);
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

            //a little delay animation for the progress bar if the user clicks refresh
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    setupActivity();
                }
            }, 1000);
        }
    }

    /**
     * This method is called when the user accepts or rejects a Roomie card. It creates a list
     * of users that the current user is already in a relation with and adds them to a list. It
     * uses that list to exclude those users from the query
     */
    private void previousRelationQuery() {
        if (checkConnection()) return;

        mCurrentRelations = new ArrayList<>(); //list to store all current relations

        //check if the current user is eiter User1 or User2 in the list of relation objects
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

                    //check if the returned user row is already a relation of the current user
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
        if (checkConnection()) return;

        ParseQuery<ParseObject> requestQuery = ParseQuery.getQuery(Constants.ROOMIE_REQUEST);
        requestQuery.whereEqualTo(Constants.SENDER, mUser);
        requestQuery.whereEqualTo(Constants.RECEIVER, mCurrentUser);
        requestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    previousRelationQuery(); //start the next query at the same time

                    if (parseObjects.isEmpty()) { //send a request to the other user
                        ParseObject request = new ParseObject(Constants.ROOMIE_REQUEST);
                        request.put(Constants.SENDER, mCurrentUser);
                        request.put(Constants.RECEIVER, mUser);
                        request.saveInBackground();
                    }
                    else { //add a relation if a request is waiting for the current ser
                        for(int i = 0; i < parseObjects.size(); i++) {
                            parseObjects.get(i).deleteInBackground(); //delete all pending requests
                        }

                        //create a new relation object on parse
                        ParseObject relation = new ParseObject(Constants.RELATION);
                        relation.put(Constants.USER1, mCurrentUser);
                        relation.put(Constants.USER2, mUser);
                        relation.saveInBackground();

                        try { //send the push notifications to both users
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
        //send the push notification to the other user
        ParseQuery<ParseInstallation> query1 = ParseInstallation.getQuery();
        query1.whereEqualTo(Constants.USER_ID, mUser.getObjectId());
        query1.whereEqualTo(Constants.CHANNELS, Constants.CONNECTION_PUSH);

        JSONObject data1 = new JSONObject();
        data1.put(Constants.PUSH_ALERT, getString(R.string.message_new_connection));
        data1.put(Constants.PUSH_ID, mCurrentUser.getObjectId());
        data1.put(Constants.PUSH_NAME, mCurrentUser.get(Constants.NAME));

        ParsePush push1 = new ParsePush();
        push1.setQuery(query1);
        push1.setData(data1);
        push1.sendInBackground();

        //if the current user has selected to recieve connection notifications, send to them as well
        Boolean sendToCurrentUser = (Boolean) mCurrentUser.get(Constants.CONNECTION_NOTIFICATIONS);
        if(sendToCurrentUser) {
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

    /**
     * This method performs the ParseQuery and returns a new "Roomie" user object each time the user
     * either accepts or rejects the previous "Roomie" user object. It then displays the object in
     * the RoomieFragment
     */
    private void roomieQuery() {
        if (checkConnection()) return;

        ParseGeoPoint userLocation = (ParseGeoPoint) mCurrentUser.get(Constants.GEOPOINT);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
        query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.whereNotEqualTo(Constants.DISCOVERABLE, false);
        query.whereNotContainedIn(Constants.OBJECT_ID, mCurrentRelations);

        //filter query by gender preference if user selects so
        if ((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.MALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.MALE);
        } else if ((mCurrentUser.get(Constants.GENDER_PREF)).equals(Constants.FEMALE)) {
            query.whereEqualTo(Constants.GENDER, Constants.FEMALE);
        }

        //if user has a room, only show others who are looking for a room
        if (String.valueOf(mCurrentUser.get(Constants.HAS_ROOM)).equals(Constants.TRUE)) {
            query.whereEqualTo(Constants.HAS_ROOM, false);
        }

        //create a count to prevent repeated users from showing up twice in a row
        //will repeat the list when the end is reached if the user gets there
        int count = 0;
        try {
            count = query.count();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mIndices.size() == count) {
            //todo: change message? or no repeats ever again?
            mIndices.clear();
            setEmptyView(); //set the empty view and return to prevent repeats
            return;
        }

        //get a random row to show in the database. Row is added to a list to prevent a repeat
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
                        String minPrice = (String) mUser.get(Constants.MIN_PRICE);
                        String maxPrice = (String) mUser.get(Constants.MAX_PRICE);
                        Boolean hasRoom = (Boolean) mUser.get(Constants.HAS_ROOM);
                        Boolean smokes = (Boolean) mUser.get(Constants.SMOKES);
                        Boolean drinks = (Boolean) mUser.get(Constants.DRINKS);
                        Boolean pets = (Boolean) mUser.get(Constants.PETS);
                        ParseFile profImage = (ParseFile) mUser.get(Constants.PROFILE_IMAGE);
                        ParseFile profImage2 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE2);
                        ParseFile profImage3 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE3);
                        ParseFile profImage4 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE4);

                        if (mCardView.getVisibility() == View.GONE) { //show the card if hidden
                            mCardView.setVisibility(View.VISIBLE);
                        }

                        mRoomieFragment.resetFields();

                        mCardView.startAnimation(mExpandIn);

                        //set the RoomieFragment fields for the user
                        mRoomieFragment.setName(name);
                        mRoomieFragment.setAge(age);
                        mRoomieFragment.setLocation(location);
                        mRoomieFragment.setAboutMe(aboutMe);
                        mRoomieFragment.setHasRoom(hasRoom);
                        mRoomieFragment.setProfImage(profImage);
                        mRoomieFragment.setProfImage2(profImage2);
                        mRoomieFragment.setProfImage3(profImage3);
                        mRoomieFragment.setProfImage4(profImage4);
                        mRoomieFragment.setSmokes(smokes);
                        mRoomieFragment.setDrinks(drinks);
                        mRoomieFragment.setPets(pets);
                        mRoomieFragment.setMaxPrice(maxPrice);
                        mRoomieFragment.setMinPrice(minPrice);
                        mRoomieFragment.setFields();
                    }
                    else { //no results
                        setEmptyView();
                    }
                }
            }
        });
    }

    /**
     * This method checks if the device is connected to the internet and sets the empty view if not
     */
    private boolean checkConnection() {
        if(!ConnectionDetector.getInstance(mContext).isConnected()) {
            Toast.makeText(mContext, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

            setEmptyView();

            return true;
        }
        return false;
    }

    /**
     * This method sets the search view to empty if either there are no search results or if there
     * is no connection available
     */
    private void setEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
        mCardView.setVisibility(View.GONE);

        mAcceptButton.setEnabled(false);
        mRejectButton.setEnabled(false);
    }


}
