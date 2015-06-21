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
import butterknife.InjectView;

/**
 *
 */
public class RoomieFragment extends Fragment {

    private ParseFile mProfImage;
    private ParseFile mProfImage2;
    private ParseFile mProfImage3;
    private ParseFile mProfImage4;
    private String mName;
    private String mLocation;
    private String mAboutMe;
    private Boolean mHasRoom;
    private Boolean mSmokes;
    private Boolean mDrinks;
    private Boolean mPets;
    private String mAge;
    private String mMinPrice;
    private String mMaxPrice;

    private ImageLoader loader;

    @InjectView(R.id.profImage) ImageView mProfImageField;
    @InjectView(R.id.profImage2) ImageView mProfImageField2;
    @InjectView(R.id.profImage3) ImageView mProfImageField3;
    @InjectView(R.id.profImage4) ImageView mProfImageField4;
    @InjectView(R.id.nameField) TextView mNameField;
    @InjectView(R.id.locationField) TextView mLocationField;
    @InjectView(R.id.aboutMeText) TextView mAboutMeTitle;
    @InjectView(R.id.aboutMeField) TextView mAboutMeField;
    @InjectView(R.id.hasRoomField) TextView mHasRoomField;
    @InjectView(R.id.smokesField) TextView mSmokesField;
    @InjectView(R.id.drinksField) TextView mDrinksField;
    @InjectView(R.id.petField) TextView mPetsField;
    @InjectView(R.id.priceField) TextView mPriceField;
    @InjectView(R.id.imageProgressBar) ProgressBar mProgressBar;
    @InjectView(R.id.flipper) ViewFlipper mViewFlipper;

    public RoomieFragment() {} // Required empty public constructorred empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_roomie, container, false);
        ButterKnife.inject(this, view);
        loader = ImageLoader.getInstance();
        return view;
    }

    //the following methods are the setters for the info for the fragment

    public void setProfImage(ParseFile profImage) {
        mProfImage = profImage;
    }

    public void setProfImage2(ParseFile profImage) {
        mProfImage2 = profImage;
    }

    public void setProfImage3(ParseFile profImage) {
        mProfImage3 = profImage;
    }

    public void setProfImage4(ParseFile profImage) {
        mProfImage4 = profImage;
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

    public void setMinPrice(String minPrice) {
        mMinPrice = minPrice;
    }

    public void setMaxPrice(String maxPrice) {
        mMaxPrice = maxPrice;
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

        //check if not null
        if(mMinPrice != null && mMaxPrice != null) {
            //if both price field are empty, set to N/A
            if (mMinPrice.equals("") && mMaxPrice.equals("")) {
                mPriceField.setText(" N/A");
            }
            else if(mMinPrice.equals("") && !mMaxPrice.equals("")) { //no min Price
                mPriceField.setText(" N/A to $" + mMaxPrice);
            }
            else if(!mMinPrice.equals("") && mMaxPrice.equals("")) { //no max price
                mPriceField.setText(" $" + mMinPrice + " to N/A");
            }
            else { //max and min price
                mPriceField.setText(" $" + mMinPrice + " to $" + mMaxPrice);
            }
        }
        else {
            mPriceField.setText(" N/A");
        }

        mSmokesField.setText("");
        mDrinksField.setText("");
        mPetsField.setText("");

        setYesNoFields(mSmokes, mSmokesField);
        setYesNoFields(mDrinks, mDrinksField);
        setYesNoFields(mPets, mPetsField);
        setYesNoFields(mHasRoom, mHasRoomField);

        //load the secondary images or remove them from the flipper if unavailable
        if(mProfImage2 != null) {
            loader.displayImage(mProfImage2.getUrl(), mProfImageField2);
        }
        else {
            mViewFlipper.removeView(mProfImageField2);
        }
        if(mProfImage3 != null) {
            loader.displayImage(mProfImage3.getUrl(), mProfImageField3);
        }
        else {
            mViewFlipper.removeView(mProfImageField3);
        }
        if(mProfImage4 != null) {
            loader.displayImage(mProfImage4.getUrl(), mProfImageField4);
        }
        else {
            mViewFlipper.removeView(mProfImageField4);
        }

        //first imageview with listener
        if(mProfImage != null) {
            loader.displayImage(mProfImage.getUrl(), mProfImageField, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    mViewFlipper.stopFlipping();
                    mViewFlipper.setInAnimation(null);
                    mViewFlipper.setOutAnimation(null);
                    mViewFlipper.setDisplayedChild(0);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    if(!(mViewFlipper.getChildCount() == 1)) { //dont do animation if only one view present
                        mViewFlipper.setAutoStart(true);
                        mViewFlipper.setFlipInterval(4000);
                        mViewFlipper.startFlipping();
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_left));
                    }
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
        mProfImageField2.setImageDrawable(null);
        mProfImageField3.setImageDrawable(null);
        mProfImageField4.setImageDrawable(null);
        mNameField.setText("");
        mLocationField.setText("");
        mAboutMeTitle.setText("");
        mAboutMeField.setText("");
        mPriceField.setText("");

        //re-add the removed fields. If not removed, remove first then re-add
        mViewFlipper.removeView(mProfImageField2);
        mViewFlipper.removeView(mProfImageField3);
        mViewFlipper.removeView(mProfImageField4);
        mViewFlipper.addView(mProfImageField2);
        mViewFlipper.addView(mProfImageField3);
        mViewFlipper.addView(mProfImageField4);

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
