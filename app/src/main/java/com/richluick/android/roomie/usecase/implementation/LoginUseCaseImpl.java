package com.richluick.android.roomie.usecase.implementation;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.richluick.android.roomie.usecase.UseCase;
import com.richluick.android.roomie.usecase.UseCaseCallback;

import java.util.Arrays;

/**
 * Created by rluic on 1/8/2016.
 */
public class LoginUseCaseImpl implements UseCase, LogInCallback {

    UseCaseCallback useCaseCallback;
    //todo: get context here. look at Dagger 2 injection
    @Override
    public void execute(UseCaseCallback callback) {
        useCaseCallback = callback;
        ParseFacebookUtils.logInWithReadPermissionsInBackground(, Arrays.asList("user_birthday", "email"), this);
    }

    @Override
    public void done(ParseUser parseUser, ParseException e) {
        useCaseCallback.onCompleted(parseUser);
    }
}
