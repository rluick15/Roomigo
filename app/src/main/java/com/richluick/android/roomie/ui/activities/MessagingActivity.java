package com.richluick.android.roomie.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.adapters.MessageAdapter;
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

import java.util.Arrays;
import java.util.List;

public class MessagingActivity extends BaseActivity {

    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra(Constants.RECIPIENT_ID);
        String recipientName = intent.getStringExtra(Constants.RECIPIENT_NAME);
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        getSupportActionBar().setTitle(recipientName);

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        ListView messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
        messageQuery();

        //listen for a click on the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageBody = messageBodyField.getText().toString();
                messageService.sendMessage(recipientId, messageBody);
                messageBodyField.setText("");
            }
        });
    }

    /**
     * This method is called when the messaging activity is opened. It queries the parse backend
     * for all previous messages and displayes them in the adapter
     */
    private void messageQuery() {
        String[] userIds = {currentUserId, recipientId};
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
                        if (messageList.get(i).get(Constants.SENDER_ID).toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
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
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            final WritableMessage writableMessage =
                    new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

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

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
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
}
