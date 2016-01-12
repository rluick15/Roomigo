package com.richluick.android.roomie.presenter.views;

import com.richluick.android.roomie.presenter.IView;

/**
 * Created by rluic on 1/11/2016.
 */
public interface OnBoardView extends IView {

    void setHasRoom(boolean value);

    void setGenderPref(String pref);

    void activateSubmitButton();
}
