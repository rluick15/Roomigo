package com.richluick.android.roomie.utils.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.richluick.android.roomie.utils.constants.Constants;
import com.richluick.android.roomie.utils.IntentFactory;

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

                Bundle b = new Bundle();
                b.putString(IntentFactory.R_NAME, name);
                b.putString(IntentFactory.R_ID, id);
                IntentFactory.pickIntent(context, IntentFactory.MESSAGING, true, b);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
