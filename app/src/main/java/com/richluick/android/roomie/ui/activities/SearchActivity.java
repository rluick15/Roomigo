package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.fragments.RoomieFragment;
import com.richluick.android.roomie.utils.Constants;

import java.util.List;

public class SearchActivity extends ActionBarActivity implements View.OnClickListener {

    private ParseUser mCurrentUser;
    private Button mAcceptButton;
    private Button mRejectButton;
    private ParseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.title_roommate_search));
        setContentView(R.layout.activity_search);

        mCurrentUser = ParseUser.getCurrentUser();

        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);
        mAcceptButton.setOnClickListener(this);
        mRejectButton.setOnClickListener(this);

       roomieQuery();
    }

    @Override
    public void onClick(View v) {
        if(v == mAcceptButton) {
            roomieRequestQuery();
            roomieQuery();
        }
        else if(v == mRejectButton){
            roomieQuery();
        }
    }

    /**
     * This method performs the ParseQuery and returns a new "Roomie" user object each time the user
     * either accepts or rejects the previous "Roomie" user object. It then displays the object in
     * the RoomieFragment
     */
    private void roomieQuery() {
        //todo: subquery checking if users are already in a relation
        ParseGeoPoint userLocation = (ParseGeoPoint) mCurrentUser.get(Constants.GEOPOINT);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
        query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(parseUsers.isEmpty()) {
                    //todo: handle empty list
                }
                else {
                    mUser = parseUsers.get(0);

                    String name = (String) mUser.get(Constants.NAME);
                    String age = (String) mUser.get(Constants.AGE);
                    String location = (String) mUser.get(Constants.LOCATION);
                    String aboutMe = (String) mUser.get(Constants.ABOUT_ME);
                    Boolean hasRoom = (Boolean) mUser.get(Constants.HAS_ROOM);
                    ParseFile profImage = (ParseFile) mUser.get(Constants.PROFILE_IMAGE);

                    RoomieFragment fragment = new RoomieFragment(hasRoom, aboutMe, location, name,
                            profImage, age);
                    getFragmentManager().beginTransaction().add(R.id.roomieFrag, fragment).commit();
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
                    if (parseObjects.isEmpty()) {
                        ParseObject request = new ParseObject(Constants.ROOMIE_REQUEST);
                        request.put(Constants.SENDER, mCurrentUser);
                        request.put(Constants.RECEIVER, mUser);
                        request.saveInBackground();
                    }
                    else {
                        parseObjects.get(0).deleteInBackground();

                        ParseObject relation = new ParseObject(Constants.RELATION);
                        relation.put(Constants.USER1, mCurrentUser);
                        relation.put(Constants.USER2, mUser);
                        relation.saveInBackground();
                    }
                }
                else {
                    //todo: handle errors!!
                }
            }
        });
    }
}
