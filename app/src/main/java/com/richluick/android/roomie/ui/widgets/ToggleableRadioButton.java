package com.richluick.android.roomie.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
                if(mUncheckListener != null) {
                    mUncheckListener.onUnchecked(this);
                }
            }
        } else {
            setChecked(true);
        }
    }

    public interface UnCheckListener {
        public void onUnchecked(View v);
    }

    private UnCheckListener mUncheckListener;

    public void setUncheckListener(UnCheckListener uncheckListener) {
        this.mUncheckListener = uncheckListener;
    }
}