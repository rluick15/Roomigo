package com.richluick.android.roomie.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.FindCallback;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.ui.adapters.MessageAdapter;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.MessageService;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    //todo: exit out if other user deletes connection

    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();
    private MessageAdapter messageAdapter;
    private String mRecipientName;
    private String mRelationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        ((RoomieApplication) getApplication()).getTracker(RoomieApplication.TrackerName.APP_TRACKER);

        //bind the messaging service
        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra(Constants.RECIPIENT_ID);
        mRecipientName = intent.getStringExtra(Constants.RECIPIENT_NAME);
        mRelationId = intent.getStringExtra(Constants.OBJECT_ID);
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        getSupportActionBar().setTitle(mRecipientName);

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        //set the message adapter to the listview
        ListView messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
        messageQuery();

        //listen for a click on the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ConnectionDetector.getInstance(MessagingActivity.this).isConnected()) {
                    //show a toast if there is no con nection
                    Toast.makeText(MessagingActivity.this, MessagingActivity.this.getString(R.string.no_connection),
                            Toast.LENGTH_SHORT).show();
                } else { //send the message and clear the edit text if there is a connection
                    messageBody = messageBodyField.getText().toString();
                    messageService.sendMessage(recipientId, messageBody);
                    messageBodyField.setText("");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    /**
     * This method is called when the messaging activity is opened. It queries the parse backend
     * for all previous messages and displayes them in the adapter
     */
    private void messageQuery() {
        String[] userIds = {currentUserId, recipientId};

        //query all the previous message and display them in the list view
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.PARSE_MESSAGE);
        query.whereContainedIn(Constants.SENDER_ID, Arrays.asList(userIds));
        query.whereContainedIn(Constants.ID_RECIPIENT, Arrays.asList(userIds));
        query.orderByAscending(Constants.CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message =
                                new WritableMessage(messageList.get(i).get(Constants.ID_RECIPIENT).toString(),
                                        messageList.get(i).get(Constants.MESSAGE_TEXT).toString());

                        Format formatter = new SimpleDateFormat("MM/dd HH:mm"); //add the date to the adapter
                        message.addHeader(Constants.DATE, formatter.format(messageList.get(i).getCreatedAt()));

                        //add the message as either incoming or outgoing
                        if (messageList.get(i).get(Constants.SENDER_ID).toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, mRecipientName);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING, mRecipientName);
                        }
                    }
                }
            }
        });
    }

    //unbind the service when the activity is destroyed
    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        messageService.removeMessageClientListener(messageClientListener);

        super.onDestroy();
    }

    /*
     * When the service is connected add the service connection and the message listener
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    /*
     * The message listener for the Sinch Messaging service.
     */
    private class MyMessageClientListener implements MessageClientListener {
        //Notify the user if their message failed to send
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MessagingActivity.this,
                    getString(R.string.toast_message_send_failed), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            if (message.getSenderId().equals(recipientId)) {
                WritableMessage writableMessage =
                        new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                Format formatter = new SimpleDateFormat("MM/dd HH:mm");
                writableMessage.addHeader(Constants.DATE, formatter.format(new Date()));
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, mRecipientName);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            final WritableMessage writableMessage =
                    new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
            Format formatter = new SimpleDateFormat("MM/dd HH:mm");
            writableMessage.addHeader(Constants.DATE, formatter.format(new Date()));

            //only add message to parse database if it doesn't already exist there
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.PARSE_MESSAGE);
            query.whereEqualTo(Constants.SINCH_ID, message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject(Constants.PARSE_MESSAGE);
                            parseMessage.put(Constants.SENDER_ID, currentUserId);
                            parseMessage.put(Constants.ID_RECIPIENT, writableMessage.getRecipientIds().get(0));
                            parseMessage.put(Constants.MESSAGE_TEXT, writableMessage.getTextBody());
                            parseMessage.put(Constants.SINCH_ID, writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING,
                                    mRecipientName);
                        }
                    }
                }
            });

            try {
                sendPushNotification();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Do you want to notify your user when the message is delivered?
        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }

    /**
     * This method handles the push notifications sent when a message is sent. The chat is opened
     * when the notification is clicked on
     */
    private void sendPushNotification() throws JSONException {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereEqualTo(Constants.USER_ID, recipientId);
        query.whereEqualTo(Constants.CHANNELS, Constants.MESSAGE_PUSH);

        JSONObject data = new JSONObject();
        data.put(Constants.PUSH_ALERT, "You have a message from " +
                ParseUser.getCurrentUser().get(Constants.NAME) + "!");
        data.put(Constants.PUSH_ID, ParseUser.getCurrentUser().getObjectId());
        data.put(Constants.PUSH_NAME, ParseUser.getCurrentUser().get(Constants.NAME));

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setData(data);
        push.sendInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_messaging, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_delete) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.dialog_title_delete_connection))
                    .content(getString(R.string.dialog_content_delete_connection))
                    .positiveText(getString(R.string.dialog_positive_delete_connection))
                    .negativeText(getString(R.string.dialog_negative))
                    .negativeColorRes(R.color.primary_text)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            ParseObject.createWithoutData(Constants.RELATION, mRelationId).deleteEventually();

                            Intent intent = new Intent(MessagingActivity.this, ChatActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                        }
                    })
                    .show();
        }
        else if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
