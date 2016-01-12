package com.richluick.android.roomie.usecase.implementation;

import android.app.Activity;
import android.content.Context;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.android.roomie.usecase.UseCase;
import com.richluick.android.roomie.usecase.UseCaseCallback;
import com.richluick.android.roomie.usecase.callbacks.LoginUseCaseCallback;
import com.richluick.android.roomie.utils.constants.Constants;

import java.util.Arrays;

/**
 * Created by rluic on 1/8/2016.
 */
public class LoginUseCaseImpl implements UseCase, LogInCallback, SaveCallback {

    LoginUseCaseCallback useCaseCallback;
    Activity mActivity;

    public LoginUseCaseImpl(Activity activity) {
        this.mActivity = activity;
    }

    //todo: get context here. look at Dagger 2 injection
    @Override
    public void execute(UseCaseCallback callback) {
        if(mActivity.isFinishing()) {
            useCaseCallback.onCompleted(null); //return an error
        } else {
            useCaseCallback = (LoginUseCaseCallback) callback;
            ParseFacebookUtils.logInWithReadPermissionsInBackground(mActivity,
                    Arrays.asList("user_birthday", "email"), this);
        }
    }

    @Override
    public void done(ParseUser parseUser, ParseException e) {
        if (parseUser.isNew()) {
            parseUser.put(Constants.ALREADY_ONBOARD, false);
            parseUser.saveInBackground(this);
        } else {
            useCaseCallback.onCompleted(parseUser);
        }
    }

    @Override
    public void done(ParseException e) {
        useCaseCallback.onNewUserSaved();
    }
}
