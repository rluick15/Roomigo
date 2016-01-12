package com.richluick.android.roomie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.richluick.android.roomie.settings.EditProfileActivity;
import com.richluick.android.roomie.activities.LoginActivity;
import com.richluick.android.roomie.home.MainActivity;
import com.richluick.android.roomie.messaging.MessagingActivity;
import com.richluick.android.roomie.activities.OnBoardActivity;
import com.richluick.android.roomie.settings.SettingsActivity;
import com.richluick.android.roomie.utils.constants.Constants;

/**
 * A factory class for choosing intents as well as some utility method for more specific intents
 */
public class IntentFactory {

    //todo: this was a frivolous exercise. We probably dont need this class. It adds too much abstraction

    public static final String LOGIN = "login";
    public static final String ONBOARD = "onBoard";
    public static final String MAIN_ACTIVITY = "mainActivity";
    public static final String SETTINGS = "settings";
    public static final String EDIT_PROFILE = "editProfile";
    public static final String SHARE = "share";
    public static final String BROWSER = "browser";
    public static final String MESSAGING = "messaging";
    public static final String IMAGE_GALLERY = "imageGallery";

    //Bundle Constants
    public static final String SHARE_EMAIL = "shareEmail";
    public static final String SHARE_SUBJECT = "shareSubject";
    public static final String SHARE_TEXT = "shareText";
    public static final String R_ID = "rId";
    public static final String R_NAME = "rName";
    public static final String R_OBJECT_ID = "rObjectId";

    public static void pickIntent(Context context, String intentKey) {
        pickIntent(context, intentKey, "");
    }

    public static void pickIntent(Context context, String intentKey, Bundle b) {
        pickIntent(context, intentKey, false, b);
    }

    public static void pickIntent(Context context, String intentKey, Boolean newTask, Bundle b) {
        pickIntent(context, intentKey, newTask, 0, 0, "", b);
    }

    public static void pickIntent(Context context, String intentKey, String url) {
        pickIntent(context, intentKey, false, 0, 0, url, null);
    }

    public static void pickIntent(Context context, String intentKey, Boolean newTask, int inAnim, int outAnim) {
        pickIntent(context, intentKey, newTask, inAnim, outAnim, "", null);
    }

    //todo: add javadocs
    public static void pickIntent(Context context, String intentKey, Boolean newTask, int inAnim,
                                  int outAnim, String url, Bundle b) {
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
            case SHARE:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/intent");

                //get the text from the bundle
                if(b != null) {
                    if(b.containsKey(SHARE_EMAIL)) {
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{b.getString(SHARE_EMAIL)});
                    }
                    if(b.containsKey(SHARE_SUBJECT)) {
                        intent.putExtra(Intent.EXTRA_SUBJECT, b.getString(SHARE_SUBJECT));
                    }
                    if(b.containsKey(SHARE_TEXT)) {
                        intent.putExtra(Intent.EXTRA_TEXT, b.getString(SHARE_TEXT));
                    }
                }
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
            case MESSAGING:
                intent = new Intent(context, MessagingActivity.class);

                if(b != null) {
                    if(b.containsKey(R_ID)) {
                        intent.putExtra(Constants.RECIPIENT_ID, b.getString(R_ID));
                    }
                    if(b.containsKey(R_NAME)) {
                        intent.putExtra(Constants.RECIPIENT_NAME, b.getString(R_NAME));
                    }
                    if(b.containsKey(R_OBJECT_ID)) {
                        intent.putExtra(Constants.OBJECT_ID, b.getString(R_OBJECT_ID));
                    }
                }

                break;
            case IMAGE_GALLERY:
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                break;
        }

        if(newTask && intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        //start activity for result for Image Gallery, otherwise startactivty
        if(intentKey.equals(IMAGE_GALLERY)) {
            ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        } else {
            context.startActivity(intent);
        }

        //if animation present, set pending transition
        if (inAnim != 0 && outAnim != 0) {
            ((Activity) context).overridePendingTransition(inAnim, outAnim);
        }
    }
}
