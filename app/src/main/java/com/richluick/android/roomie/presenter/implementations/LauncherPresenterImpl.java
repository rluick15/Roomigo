package com.richluick.android.roomie.presenter.implementations;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.richluick.android.roomie.presenter.Presenter;
import com.richluick.android.roomie.presenter.views.LauncherView;
import com.richluick.android.roomie.usecase.UseCaseCallback;
import com.richluick.android.roomie.usecase.implementation.LauncherUseCaseImpl;

/**
 * Created by rluic on 1/7/2016.
 */
public class LauncherPresenterImpl implements Presenter<LauncherView>, UseCaseCallback<Activity> {

    LauncherView mLauncherView;
    LauncherUseCaseImpl mLauncherUseCase;

    public LauncherPresenterImpl() {
        mLauncherUseCase = new LauncherUseCaseImpl();
    }

    @Override
    public void setView(@NonNull LauncherView view) {
        mLauncherView = view;
    }

    public void launchActivity() {
        mLauncherUseCase.execute(this);
    }

    @Override
    public void onCompleted(Activity result) {
        mLauncherView.launchNewActivity(result);
    }
}
