package com.richluick.android.roomie.ui.activities;

import android.os.Bundle;
import android.view.View;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.widgets.ToggleableRadioButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends BaseActivity implements ToggleableRadioButton.UnCheckListener {

    @InjectView(R.id.discoveryCheckBox) ToggleableRadioButton mDiscoveryCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        mDiscoveryCheckBox.setUncheckListener(this);
    }

    @Override
    public void onUnchecked(View v) {

    }
}
