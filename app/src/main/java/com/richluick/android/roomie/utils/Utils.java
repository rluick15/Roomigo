package com.richluick.android.roomie.utils;

import android.content.Context;
import android.content.Intent;

import com.richluick.android.roomie.ui.activities.LoginActivity;

/**
 * Utility methods class
 */
public class Utils {

    /**
     * This method navigates back to the login screen if the user logs out
     *
     * @param context the context of the activity the user is logging out from
     */
    public static void navigateToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
