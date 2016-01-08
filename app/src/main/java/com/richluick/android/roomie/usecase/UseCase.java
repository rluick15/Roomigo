package com.richluick.android.roomie.usecase;

import rx.Observer;

/**
 * Created by rluic on 1/7/2016.
 */
public interface UseCase {

    void execute(UseCaseCallback callback);

}
