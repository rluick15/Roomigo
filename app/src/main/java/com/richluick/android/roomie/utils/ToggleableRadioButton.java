package com.richluick.android.roomie.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * This is a custom widget which allows a radiobutton to be delselected when it is clicked
 */
public class ToggleableRadioButton extends RadioButton {
    public ToggleableRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleableRadioButton(Context context) {
        super(context);
    }

    @Override
    public void toggle() {
        if(isChecked()) {
            if(getParent() instanceof RadioGroup) {
                ((RadioGroup)getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }

    public interface UnCheckListener {
        public void onUnchecked();
    }

    private UnCheckListener mUncheckListener;

    public void setUncheckListener(UnCheckListener uncheckListener) {
        this.mUncheckListener = uncheckListener;
    }
}