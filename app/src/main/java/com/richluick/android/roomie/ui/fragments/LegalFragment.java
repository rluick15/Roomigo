package com.richluick.android.roomie.ui.fragments;


import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.richluick.android.roomie.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LegalFragment extends DialogFragment {

    @InjectView(R.id.butterKnifeField) TextView butterKnifeField;
    @InjectView(R.id.simpleFacebookField) TextView simpleFacebookField;
    @InjectView(R.id.circleImageField) TextView circleImageField;
    @InjectView(R.id.uilField) TextView uilField;
    @InjectView(R.id.retroLambdaField) TextView retroLambdaField;
    @InjectView(R.id.materialDialogsField) TextView materialDialogsField;
    @InjectView(R.id.swipeCardsField) TextView swipeCardsField;

    public LegalFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_legal, container, false);
        ButterKnife.inject(this, v);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        butterKnifeField.setText(Html.fromHtml(getString(R.string.legal_butter_knife)));
        simpleFacebookField.setText(Html.fromHtml(getString(R.string.legal_simple_facebook)));
        circleImageField.setText(Html.fromHtml(getString(R.string.legal_circle_image)));
        uilField.setText(Html.fromHtml(getString(R.string.legal_uil)));
        retroLambdaField.setText(Html.fromHtml(getString(R.string.legal_retrolambda)));
        materialDialogsField.setText(Html.fromHtml(getString(R.string.legal_material_dialogs)));
        swipeCardsField.setText(Html.fromHtml(getString(R.string.legal_swipecards)));

        return v;
    }
}
