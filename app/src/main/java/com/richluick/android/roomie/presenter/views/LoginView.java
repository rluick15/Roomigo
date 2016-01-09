package com.richluick.android.roomie.presenter.views;

import com.richluick.android.roomie.presenter.IView;

/**
 * Created by rluic on 1/7/2016.
 */
public interface LoginView extends IView {

    void onNewUser();

    void onFullUser();

    void onPartialUser();
}
