package com.richluick.android.roomie.presenter.implementations;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.parse.ParseUser;
import com.richluick.android.roomie.presenter.Presenter;
import com.richluick.android.roomie.presenter.views.LoginView;
import com.richluick.android.roomie.usecase.UseCaseCallback;
import com.richluick.android.roomie.usecase.callbacks.LoginUseCaseCallback;
import com.richluick.android.roomie.usecase.implementation.LoginUseCaseImpl;
import com.richluick.android.roomie.utils.constants.Constants;

/**
 * Created by rluic on 1/8/2016.
 */
public class LoginPresenterImpl implements Presenter<LoginView>, LoginUseCaseCallback<ParseUser> {

    LoginView mLoginView;
    LoginUseCaseImpl mLoginUseCase;

    public LoginPresenterImpl(Activity activity) {
        mLoginUseCase = new LoginUseCaseImpl(activity);
    }

    @Override
    public void setView(@NonNull LoginView view) {
        mLoginView = view;
    }

    public void loginUser() {
        mLoginUseCase.execute(this);
    }

    @Override
    public void onCompleted(ParseUser result) {
        if (result == null) {
            mLoginView.onError();
        } else {
            if ((Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD)) {
                mLoginView.onFullUser();
            } else {
                mLoginView.onPartialUser();
            }
        }
    }

    @Override
    public void onNewUserSaved() {
        mLoginView.onNewUser();
    }
}
