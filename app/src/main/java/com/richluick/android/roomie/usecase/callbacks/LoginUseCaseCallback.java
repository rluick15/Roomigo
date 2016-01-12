package com.richluick.android.roomie.usecase.callbacks;

import com.richluick.android.roomie.usecase.UseCaseCallback;

/**
 * Created by rluic on 1/11/2016.
 */
public interface LoginUseCaseCallback<I> extends UseCaseCallback<I> {

    void onNewUserSaved();

}
