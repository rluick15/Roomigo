package com.richluick.android.roomie.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.richluick.android.roomie.R;

public class YesNoRadioGroup extends RelativeLayout implements CompoundButton.OnCheckedChangeListener {

    private CheckBox yesBox;
    private CheckBox noBox;
    private Boolean booleanValue; //the value (true, false, or null)

    public YesNoRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.yes_no_radio_group, this);

        yesBox = (CheckBox) findViewById(R.id.yesCheckBox);
        noBox = (CheckBox) findViewById(R.id.noCheckBox);

        yesBox.setOnCheckedChangeListener(this);
        noBox.setOnCheckedChangeListener(this);
    }

    /**
     * This method is called when the activity is created and sets the previously selected
     * values of the radiogroups based upon the users saved profile. This is only used for Yes/No
     * questions
     *
     * @param field This is the boolean value of the questions being checked(true=yes, false=no)
     */
    public void setCheckedItems(Boolean field) {
        setBooleanValue(field); //set the starting value of the field

        if(field != null) {
            if(field) {
                yesBox.setChecked(true);
            }
            else {
                noBox.setChecked(true);
            }
        }
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    private void setBooleanValue(Boolean value) {
        booleanValue = value;
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        if(v == yesBox) {
            if (isChecked) {
                if (noBox.isChecked()) {
                    noBox.setChecked(false);
                }
                booleanValue = true;
            }
            else {
                booleanValue = null;
            }
        }
        else if(v == noBox) {
            if(isChecked) {
                if (yesBox.isChecked()) {
                    yesBox.setChecked(false);
                }
                booleanValue = false;
            }
            else {
                booleanValue = null;
            }
        }
    }
}
