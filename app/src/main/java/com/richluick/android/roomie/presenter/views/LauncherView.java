package com.richluick.android.roomie.presenter.views;

import android.app.Activity;

import com.richluick.android.roomie.presenter.IView;

/**
 * Created by rluic on 1/7/2016.
 */
public interface LauncherView extends IView {

    void launchNewActivity(Activity activity);
}
