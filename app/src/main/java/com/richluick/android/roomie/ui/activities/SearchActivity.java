package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.fragments.RoomieFragment;
import com.richluick.android.roomie.utils.Constants;

import java.util.List;

public class SearchActivity extends ActionBarActivity implements RoomieFragment.OnFragmentInteractionListener,
        View.OnClickListener {

    private ParseUser mCurrentUser;
    private Button mAcceptButton;
    private Button mRejectButton;

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
                    RoomieFragment fragment = new RoomieFragment();
                    getFragmentManager().beginTransaction().add(R.id.roomieFrag, fragment).commit();
                }
            }
        });
    }
}
