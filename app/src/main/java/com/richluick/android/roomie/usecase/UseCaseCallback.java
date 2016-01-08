package com.richluick.android.roomie.usecase;

/**
 * Created by rluic on 1/7/2016.
 */
public interface UseCaseCallback<I> {

    void onCompleted(I result);
}
