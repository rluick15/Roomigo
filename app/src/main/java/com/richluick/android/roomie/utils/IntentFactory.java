package com.richluick.android.roomie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;
import com.richluick.android.roomie.ui.activities.EditProfileActivity;
import com.richluick.android.roomie.ui.activities.LoginActivity;
import com.richluick.android.roomie.ui.activities.MainActivity;
import com.richluick.android.roomie.ui.activities.MessagingActivity;
import com.richluick.android.roomie.ui.activities.OnBoardActivity;
import com.richluick.android.roomie.ui.activities.SettingsActivity;

/**
 * A factory class for choosing intents as well as some utility method for more specific intents
 */
public class IntentFactory {

    public static final String LOGIN = "login";
    public static final String ONBOARD = "onBoard";
    public static final String MAIN_ACTIVITY = "mainActivity";
    public static final String SETTINGS = "settings";
    public static final String EDIT_PROFILE = "editProfile";
    public static final String SHARE = "share";
    public static final String SHARE_FEEDBACK = "shareFeedback";
    public static final String BROWSER = "browser";

    /**
     * This method  checks if the user has already gone through the onboard process and either
     * sends them to onboarding if they have not or skips that step and brings them to the
     * Main Activity
     *
     * @return Boolean true or false depending on if the user has onboarded or not
     */
    //todo: why is this here??
    public static Boolean checkIfAlreadyOnBoarded() {
        return (Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD);
    }

    public static void reportUserIntent(Context context, String message, String userId) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/intent");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.ROOMIGO_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Report User: " + userId);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(intent);
    }

    public static void imageGalleryIntent(Context context) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    public static void messagingIntent(Context context, ParseUser user) {
        Intent intent = new Intent(context, MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, user.getObjectId());
        intent.putExtra(Constants.RECIPIENT_NAME, (String) user.get(Constants.NAME));
        intent.putExtra(Constants.OBJECT_ID, user.getObjectId());
        context.startActivity(intent);
        //overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    public static void pushNotificationIntent(Context context, String id, String name) {
        Intent myIntent = new Intent(context, MessagingActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(Constants.RECIPIENT_ID, id);
        myIntent.putExtra(Constants.RECIPIENT_NAME, name);
        context.startActivity(myIntent);
    }

    public static void pickIntent(Context context, String intentKey, String url) {
        pickIntent(context, intentKey, false, 0, 0, url);
    }

    public static void pickIntent(Context context, String intentKey, Boolean newTask) {
        pickIntent(context, intentKey, newTask, 0, 0);
    }

    public static void pickIntent(Context context, String intentKey, Boolean newTask, int inAnim, int outAnim) {
        pickIntent(context, intentKey, newTask, inAnim, outAnim, "");
    }

    public static void pickIntent(Context context, String intentKey, Boolean newTask, int inAnim, int outAnim, String url) {
        Intent intent = null;

        switch (intentKey) {
            case MAIN_ACTIVITY:
                intent = new Intent(context, MainActivity.class);
                break;
            case ONBOARD:
                intent = new Intent(context, OnBoardActivity.class);
                break;
            case LOGIN:
                intent = new Intent(context, LoginActivity.class);
                break;
            case SHARE_FEEDBACK:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/intent");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.ROOMIGO_EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Help/Feedback");
                break;
            case SHARE:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/intent");
                intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text));
                break;
            case SETTINGS:
                intent = new Intent(context, SettingsActivity.class);
                break;
            case EDIT_PROFILE:
                intent = new Intent(context, EditProfileActivity.class);
                break;
            case BROWSER:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                break;
        }

        if(newTask && intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        context.startActivity(intent);
        if (inAnim != 0 && outAnim != 0) {
            ((Activity) context).overridePendingTransition(inAnim, outAnim);
        }
    }
}
