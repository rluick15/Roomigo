package com.richluick.android.roomie.data;

import android.content.Context;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * This object holds the current user friend list and updates it as the add or remove relations
 */
public class ConnectionsList {

    //todo:update list on add/remove conenction

    //singleton fields
    private static ConnectionsList instance;
    private Context context;

    private ArrayList<ParseUser> mConnectionList = new ArrayList<>(); //list with ParseUsers
    private ArrayList<String> mConnectionIdList = new ArrayList<>(); //list with objectIds
    private ArrayList<String> mPendingConnectionList = new ArrayList<>();
    private ParseUser mCurrentUser;

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
    public Observable<String> getConnectionsFromParse(ParseUser currentUser) {
        mCurrentUser = currentUser;
        mCurrentUser.fetchIfNeededInBackground();

        //check if the current user is eiter User1 or User2 in the list of relation objectsParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.RELATION);
        return Observable.create(subscriber -> {
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
            relationQuery.findInBackground((parseObjects, e) -> {
                if(e == null) {
                    mConnectionList.clear();
                    mConnectionIdList.clear();

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
                        mConnectionList.add(user);
                        mConnectionIdList.add(user.getObjectId());
                    }
                }
                else {
                    subscriber.onError(e);
                }

                if(!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            });
        });

    }

    public void getPendingConnectionsFromParse() {}

    public ArrayList<ParseUser> getConnectionList() {
        return mConnectionList;
    }

    public ArrayList<String> getConnectionIdList() {
        return mConnectionIdList;
    }

    public ArrayList<String> getPendingConnectionList() {
        return mPendingConnectionList; //todo:for later
    }

    //todo: if a connection request already exists, dont send another one
    /**
     * This method is called when the user accepts a Roomie card. It first checks if the other user
     * has already sent a RoomieRequest via a parse query. If so, then a relation is established
     * between the two users and the connection list is updated. If not, then a RoomieRequest
     * is sent to the other user
     */
    public void connectionRequest(final ParseUser currentUser, final ParseUser user) {
        ParseQuery<ParseObject> requestQuery = ParseQuery.getQuery(Constants.ROOMIE_REQUEST);
        requestQuery.whereEqualTo(Constants.SENDER, user);
        requestQuery.whereEqualTo(Constants.RECEIVER, currentUser);
        requestQuery.findInBackground((parseObjects, e) -> {
            if (e == null) {

                if (parseObjects.isEmpty()) { //send a request to the other user
                    ParseObject request = new ParseObject(Constants.ROOMIE_REQUEST);
                    request.put(Constants.SENDER, currentUser);
                    request.put(Constants.RECEIVER, user);
                    request.saveInBackground();
                }
                else { //add a relation if a request is waiting for the current ser
                    for(int i = 0; i < parseObjects.size(); i++) {
                        parseObjects.get(i).deleteInBackground(); //delete all pending requests
                    }

                    //create a new relation object on parse
                    ParseObject relation = new ParseObject(Constants.RELATION);
                    relation.put(Constants.USER1, currentUser);
                    relation.put(Constants.USER2, user);
                    relation.saveInBackground(e1 -> getConnectionsFromParse(currentUser));

                    try { //send the push notifications to both users
                        new ParsePushNotification().sendConnectionPushNotification(currentUser, user);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            else {
                e.printStackTrace();
            }
        });
    }

}