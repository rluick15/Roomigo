package com.richluick.android.roomie.data;

import android.content.Context;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * This class queries the users search results based on location and returns them one at a time
 */
public class SearchResults {

    //singleton fields
    private static SearchResults instance;
    private Context context;

    private int counter = 1; //iterates through the array
    private ArrayList<ParseUser> searchResults = new ArrayList<>();

    public SearchResults(Context context){
        this.context = context;
    }

    //setup class as a singleton
    public static SearchResults getInstance(Context ctx) {
        if(instance == null) {
            instance = new SearchResults(ctx);
        }
        return instance;
    }

    public Observable<ArrayList<ParseUser>> getSearchResultsFromParse(ParseUser currentUser) {
        currentUser.fetchInBackground();

        return Observable.create(subscriber -> {
            ParseGeoPoint userLocation = (ParseGeoPoint) currentUser.get(Constants.GEOPOINT);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereWithinMiles(Constants.GEOPOINT, userLocation, 10);
            query.whereNotEqualTo(Constants.OBJECT_ID, currentUser.getObjectId());
            query.whereNotEqualTo(Constants.DISCOVERABLE, false);
            query.whereNotContainedIn(Constants.OBJECT_ID,
                    ConnectionsList.getInstance(context).getConnectionIdList());

            //filter query by gender preference if user selects so
            if ((currentUser.get(Constants.GENDER_PREF)).equals(Constants.MALE)) {
                query.whereEqualTo(Constants.GENDER, Constants.MALE);
            } else if ((currentUser.get(Constants.GENDER_PREF)).equals(Constants.FEMALE)) {
                query.whereEqualTo(Constants.GENDER, Constants.FEMALE);
            }

            //if user has a room, only show others who are looking for a room
            if (String.valueOf(currentUser.get(Constants.HAS_ROOM)).equals(Constants.TRUE)) {
                query.whereEqualTo(Constants.HAS_ROOM, false);
            }

            query.findInBackground((List<ParseUser> parseUsers, ParseException e) -> {
                if (e == null) {
                    if (parseUsers != null) {
                        searchResults = (ArrayList<ParseUser>) parseUsers;
                        Collections.shuffle(searchResults); //randomize the results
                        subscriber.onNext(searchResults);
                    }
                }

                if(!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            });
        });

    }

    /*
     * this method iterates through the results and returns them one at a time until the list is
     * empty
     */
    public ParseUser getSearchResult() {
        if(searchResults.isEmpty()) { //if empty, return
            return null;
        }

        if(counter == searchResults.size()) { //return null at list end
            counter = 1;
            return null;
        }
        else {
            ParseUser user = searchResults.get(counter);
            counter++;
            return user;
        }
    }
}
