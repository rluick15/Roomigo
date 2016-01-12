package com.richluick.android.roomie.presenter.implementations;

import android.support.annotation.NonNull;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.presenter.Presenter;
import com.richluick.android.roomie.presenter.views.LauncherView;
import com.richluick.android.roomie.presenter.views.OnBoardView;
import com.richluick.android.roomie.usecase.implementation.OnBoardUseCaseImpl;
import com.richluick.android.roomie.utils.constants.Constants;

/**
 * Created by rluic on 1/11/2016.
 */
public class OnBoardPresenterImpl implements Presenter<OnBoardView> {

    private OnBoardUseCaseImpl onBoardUseCase;
    private OnBoardView onBoardView;

    public OnBoardPresenterImpl() {
        onBoardUseCase = new OnBoardUseCaseImpl();
    }

    @Override
    public void setView(@NonNull OnBoardView view) {
        onBoardView = view;
    }

    public void updateChecks(int checkedId) {
        switch (checkedId) {
            //gnder pref radio group
            case R.id.maleCheckBox:
                mGenderPref = Constants.MALE;
                break;
            case R.id.femaleCheckBox:
                mGenderPref = Constants.FEMALE;
                break;
            case R.id.bothCheckBox:
                mGenderPref = Constants.BOTH;
                break;

            //has room radio group
            case R.id.yesCheckBox:
                onBoardView.setHasRoom(true);
                break;
            case R.id.noCheckBox:
                onBoardView.setHasRoom(false);
                break;
        }

        //enable the button when all items are selected
        if (mGenderGroup.getCheckedRadioButtonId() != -1 &&
                mHasRoomGroup.getCheckedRadioButtonId() != -1 && mLat != null) {
            mSetPrefButton.setEnabled(true);
        }
    }
}
