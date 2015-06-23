package com.richluick.android.roomie.data;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class handles all push notifications done through parse
 */
public class ParsePushNotification {

    private static final String connectionPushMessage = "You have a new connection!";

    //todo: put messaging notifications in here
    //todo: push sends twice

    /**
     * This method handles the push notifications sent when a connection is made. Push1 goes to
     * the other user while Push2 goes to the current user. Both open the chat when clicked on.
     */
    public void sendConnectionPushNotification(ParseUser currentUser, ParseUser user) throws JSONException {
        //send the push notification to the other user
        ParseQuery<ParseInstallation> query1 = ParseInstallation.getQuery();
        query1.whereEqualTo(Constants.USER_ID, user.getObjectId());
        query1.whereEqualTo(Constants.CHANNELS, Constants.CONNECTION_PUSH);

        JSONObject data1 = new JSONObject();
        data1.put(Constants.PUSH_ALERT, connectionPushMessage);
        data1.put(Constants.PUSH_ID, currentUser.getObjectId());
        data1.put(Constants.PUSH_NAME, currentUser.get(Constants.NAME));

        ParsePush push1 = new ParsePush();
        push1.setQuery(query1);
        push1.setData(data1);
        push1.sendInBackground();

        //if the current user has selected to recieve connection notifications, send to them as well
        Boolean sendToCurrentUser = (Boolean) currentUser.get(Constants.CONNECTION_NOTIFICATIONS);
        if(sendToCurrentUser) {
            ParseQuery<ParseInstallation> query2 = ParseInstallation.getQuery();
            query2.whereEqualTo(Constants.USER_ID, currentUser.getObjectId());

            JSONObject data2 = new JSONObject();
            data2.put(Constants.PUSH_ALERT, connectionPushMessage);
            data2.put(Constants.PUSH_ID, user.getObjectId());
            data2.put(Constants.PUSH_NAME, user.get(Constants.NAME));

            ParsePush push2 = new ParsePush();
            push2.setQuery(query2);
            push2.setData(data2);
            push2.sendInBackground();
        }
    }
}
