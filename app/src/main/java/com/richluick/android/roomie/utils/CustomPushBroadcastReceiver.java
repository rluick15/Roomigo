package com.richluick.android.roomie.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.richluick.android.roomie.ui.activities.MessagingActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomPushBroadcastReceiver extends BroadcastReceiver {
    public CustomPushBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle data = intent.getExtras();
        if(data != null) {
            String jsonData = data.getString("com.parse.Data");
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String type = jsonObject.getString("type");
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");

                Intent myIntent = new Intent(context, MessagingActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myIntent.putExtra(Constants.RECIPIENT_ID, id);
                myIntent.putExtra(Constants.RECIPIENT_NAME, name);
                context.startActivity(myIntent);

                if (type.equals("PushMessage")) {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
