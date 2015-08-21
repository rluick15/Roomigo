package com.richluick.android.roomie.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.richluick.android.roomie.ui.activities.MessagingActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomPushBroadcastReceiver extends BroadcastReceiver {

    public CustomPushBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if(data != null) {
            String jsonData = data.getString(Constants.PARSE_DATA);
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String id = jsonObject.getString(Constants.PUSH_ID);
                String name = jsonObject.getString(Constants.PUSH_NAME);

                IntentFactory.pushNotificationIntent(context, id, name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
