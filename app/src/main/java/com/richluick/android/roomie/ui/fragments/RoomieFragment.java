package com.richluick.android.roomie.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseFile;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.RoomieApplication;

import butterknife.ButterKnife;

/**
 *
 */
public class RoomieFragment extends Fragment {

    private ImageView mProfImageField;
    private ParseFile mProfImage;
    private String mName;
    private String mLocation;
    private String mAboutMe;
    private Boolean mHasRoom;
    private Boolean mSmokes;
    private Boolean mDrinks;
    private Boolean mPets;
    private String mAge;
    private TextView mNameField;
    private TextView mLocationField;
    private TextView mAboutMeTitle;
    private TextView mAboutMeField;
    private TextView mHasRoomField;
    private TextView mSmokesField;
    private TextView mDrinksField;
    private TextView mPetsField;
    private ProgressBar mProgressBar;
    private ViewFlipper mViewFlipper;

    public RoomieFragment() {} // Required empty public constructorred empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomie, container, false);
        ButterKnife.inject(getActivity());

        mProfImageField = (ImageView) view.findViewById(R.id.profImage);
        mNameField = (TextView) view.findViewById(R.id.nameField);
        mLocationField = (TextView) view.findViewById(R.id.locationField);
        mAboutMeTitle = (TextView) view.findViewById(R.id.aboutMeText);
        mAboutMeField = (TextView) view.findViewById(R.id.aboutMeField);
        mHasRoomField = (TextView) view.findViewById(R.id.hasRoomField);
        mSmokesField = (TextView) view.findViewById(R.id.smokesField);
        mDrinksField = (TextView) view.findViewById(R.id.drinksField);
        mPetsField = (TextView) view.findViewById(R.id.petField);
        mProgressBar = (ProgressBar) view.findViewById(R.id.imageProgressBar);
        mViewFlipper = (ViewFlipper) view.findViewById(R.id.flipper);

        return view;
    }

    //the following methods are the setters for the info for the fragment

    public void setProfImage(ParseFile profImage) {
        mProfImage = profImage;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public void setAboutMe(String aboutMe) {
        mAboutMe = aboutMe;
    }

    public void setHasRoom(Boolean hasRoom) {
        mHasRoom = hasRoom;
    }

    public void setSmokes(Boolean smokes) {
        mSmokes = smokes;
    }

    public void setDrinks(Boolean drinks) {
        mDrinks = drinks;
    }

    public void setPets(Boolean pets) {
        mPets = pets;
    }

    public void setAge(String age) {
        mAge = age;
    }

    /*
     * This metod is called once all the variables are reset and it then sets the filed with
     * the new variable for the new Roomie Card
     */
    public void setFields() {
        mNameField.setText(mName + ", " + mAge);
        mLocationField.setText(mLocation);
        mAboutMeTitle.setText("About " + mName);
        mAboutMeField.setText(mAboutMe);

        mSmokesField.setText("");
        mDrinksField.setText("");
        mPetsField.setText("");

        setYesNoFields(mSmokes, mSmokesField);
        setYesNoFields(mDrinks, mDrinksField);
        setYesNoFields(mPets, mPetsField);
        setYesNoFields(mHasRoom, mHasRoomField);

        if(mProfImage != null) {
            ImageLoader loader = ImageLoader.getInstance();
            loader.displayImage(mProfImage.getUrl(), mProfImageField, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    mViewFlipper.setDisplayedChild(0);
                    mViewFlipper.setAutoStart(true);
                    mViewFlipper.setFlipInterval(4000);
                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_left));
                    mViewFlipper.startFlipping();
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                }
            });
        }
    }

    /*
     * This metod is called once when a new card is being shown. It sets all the previous fields
     * to blank until the new fields are loaded
     */
    public void resetFields() {
        mProgressBar.setVisibility(View.VISIBLE);

        mProfImageField.setImageDrawable(null);
        mNameField.setText("");
        mLocationField.setText("");
        mAboutMeTitle.setText("");
        mAboutMeField.setText("");

        setYesNoFields(null, mSmokesField);
        setYesNoFields(null, mDrinksField);
        setYesNoFields(null, mPetsField);
        setYesNoFields(null, mHasRoomField);
    }

    private void setYesNoFields(Boolean field, TextView view) {
        if(field != null) {
            if(field) {
                view.setText(getString(R.string.yes));
            }
            else {
                view.setText(getString(R.string.no));
            }
        }
        else {
            view.setText("");
        }
    }
}
