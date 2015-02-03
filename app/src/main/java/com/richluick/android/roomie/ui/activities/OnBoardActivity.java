package com.richluick.android.roomie.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.richluick.android.roomie.R;

public class OnBoardActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private CheckBox mYesBox;
    private CheckBox mNoBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_on_board);

//        mYesBox = (CheckBox) findViewById(R.id.yesCheckBox);
//        mNoBox = (CheckBox) findViewById(R.id.noCheckBox);
//
//        mYesBox.setOnCheckedChangeListener(this);
//        mNoBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.yesCheckBox:
                mNoBox.setChecked(false);
                break;
            case R.id.noCheckBox:
                mYesBox.setChecked(false);
                mNoBox.setChecked(true);
                break;
        }
    }
}
