package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.fragments.RoomieFragment;
import com.richluick.android.roomie.utils.Constants;

import java.util.List;

public class SearchActivity extends ActionBarActivity implements RoomieFragment.OnFragmentInteractionListener {

    private ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.title_roommate_search));
        setContentView(R.layout.activity_search);

        mCurrentUser = ParseUser.getCurrentUser();

        roomieQuery();

//        RoomieFragment fragment = new RoomieFragment();
//        getFragmentManager().beginTransaction().add(R.id.roomieFrag, fragment).commit();
    }

    private void roomieQuery() {
        ParseGeoPoint userLocation = (ParseGeoPoint) mCurrentUser.get(Constants.GEOPOINT);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
        query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(parseUsers.isEmpty()) {
                    //todo: handle empty list
                }
                else {

                }
            }
        });
    }
}
