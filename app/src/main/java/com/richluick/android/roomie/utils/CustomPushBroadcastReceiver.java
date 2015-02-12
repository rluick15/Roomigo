package com.richluick.android.roomie.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.richluick.android.roomie.ui.activities.ChatActivity;

public class CustomPushBroadcastReceiver extends BroadcastReceiver {
    public CustomPushBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, ChatActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}
