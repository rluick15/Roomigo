package com.richluick.android.roomie.data;

import android.content.Context;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * This object holds the current user friend list and updates it as the add or remove relations
 */
public class ConnectionsList {

    //todo:update list on add/remove conenction

    //singleton fields
    private static ConnectionsList instance;
    private Context context;

    private ArrayList<String> mConnectionList = new ArrayList<>();
    private ArrayList<String> mPendingConnectionList = new ArrayList<>();
    private ParseUser mCurrentUser;
    private ConnectionsLoadedListener connectionsLoadedListener;

    public ConnectionsList(Context context){
        this.context = context;
    }

    //setup class as a singleton
    public static ConnectionsList getInstance(Context ctx) {
        if(instance == null) {
            instance = new ConnectionsList(ctx);
        }
        return instance;
    }

    /**
     * This method is called when the user accepts or rejects a Roomie card. It creates a list
     * of users that the current user is already in a relation with and adds them to a list. It
     * uses that list to exclude those users from the query
     */
    public void getConnectionsFromParse(ParseUser currentUser, ConnectionsLoadedListener listener) {
        mCurrentUser = currentUser;
        mCurrentUser.fetchIfNeededInBackground();
        connectionsLoadedListener = listener;

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
                    mConnectionList.clear();

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
                        mConnectionList.add(user.getObjectId());
                    }

                    connectionsLoadedListener.onConnectionsLoaded();
                }
            }
        });
    }

    public void getPendingConnectionsFromParse() {

    }

    public ArrayList<String> getConnectionList() {
        return mConnectionList;
    }

    public ArrayList<String> getPendingConnectionList() {
        return mPendingConnectionList;
    }

    public void addConnection(String userId) {
        mConnectionList.add(userId);
        mCurrentUser.saveInBackground();
    }

    public void removeConnection(String userId) {
        if(mConnectionList.contains(userId)) {
            mConnectionList.remove(userId);
        }
        mCurrentUser.saveInBackground();
    }

    public void blockConnection() {
        //todo: for later
    }

    /*
     * The listener interface for this class
     */
    public interface ConnectionsLoadedListener {
        void onConnectionsLoaded();
    }

}