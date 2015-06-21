package com.richluick.android.roomie.data;

import android.content.Context;
import android.view.View;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class queries the users search reults based on location and returns them one at a time
 */
public class SearchResults {

    private int counter = 0; //iterates through the array
    private ArrayList<ParseUser> searchResults = new ArrayList<>();
    private ParseUser mCurrentUser;
    private Context mContext;

    public void getSearchResultsFromParse(Context context, ParseUser currentUser) {
        mContext = context;

        ParseGeoPoint userLocation = (ParseGeoPoint) mCurrentUser.get(Constants.GEOPOINT);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
        query.whereNotEqualTo(Constants.OBJECT_ID, mCurrentUser.getObjectId());
        query.whereNotEqualTo(Constants.DISCOVERABLE, false);
        query.whereNotContainedIn(Constants.OBJECT_ID,
                ConnectionsList.getInstance(mContext).getConnectionList());

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

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    if (parseUsers != null) {
                        searchResults = (ArrayList<ParseUser>) parseUsers;
                        Collections.shuffle(searchResults);
                    }
                }
            }
        });
    }

    public ParseUser getSearchResult() {
        ParseUser user = searchResults.get(counter);
        counter++;

        if(counter == searchResults.size()) {
            counter = 0;
            return null;
        }
        else {
            return user;
        }
    }
}
