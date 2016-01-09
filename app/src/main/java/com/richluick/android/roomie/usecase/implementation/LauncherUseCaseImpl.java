package com.richluick.android.roomie.usecase.implementation;

import com.parse.ParseUser;
import com.richluick.android.roomie.home.MainActivity;
import com.richluick.android.roomie.login.LoginActivity;
import com.richluick.android.roomie.login.OnBoardActivity;
import com.richluick.android.roomie.usecase.UseCase;
import com.richluick.android.roomie.usecase.UseCaseCallback;
import com.richluick.android.roomie.utils.constants.Constants;

/**
 * Created by rluic on 1/7/2016.
 */
public class LauncherUseCaseImpl implements UseCase {

    @Override
    public void execute(UseCaseCallback callback) {
        if(ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().isAuthenticated()) {
                if ((Boolean) ParseUser.getCurrentUser().get(Constants.ALREADY_ONBOARD)) {
                    callback.onCompleted(MainActivity.class);
                } else {
                    callback.onCompleted(OnBoardActivity.class);
                }
            }
        }
        else {
            callback.onCompleted(LoginActivity.class);
        }
    }
}
