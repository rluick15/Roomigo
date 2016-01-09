package com.richluick.android.roomie.presenter.implementations;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.presenter.Presenter;
import com.richluick.android.roomie.presenter.views.LoginView;
import com.richluick.android.roomie.usecase.UseCaseCallback;
import com.richluick.android.roomie.usecase.implementation.LoginUseCaseImpl;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.IntentFactory;
import com.richluick.android.roomie.utils.constants.Constants;

/**
 * Created by rluic on 1/8/2016.
 */
public class LoginPresenterImpl implements Presenter<LoginView>, UseCaseCallback<ParseUser> {

    LoginView mLoginView;
    LoginUseCaseImpl mLoginUseCase;

    public LoginPresenterImpl() {
        mLoginUseCase = new LoginUseCaseImpl();
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
        } else if (result.isNew()) {
            result.put(Constants.ALREADY_ONBOARD, false);
            result.saveInBackground();
            mLoginView.onNewUser();
        } else {
            if ((Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD)) {
                mLoginView.onFullUser();
            } else {
                mLoginView.onPartialUser();
            }
        }
    }
}
