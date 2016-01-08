package com.richluick.android.roomie.presenter;

import android.support.annotation.NonNull;

/**
 * Created by rluic on 1/7/2016.
 */
public interface Presenter<T extends IView> {

    void setView(@NonNull T view);

}
