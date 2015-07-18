package com.richluick.android.roomie.ui.views;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;
import com.richluick.android.roomie.utils.LinearRegression;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by dionysis_lorentzos on 5/8/14
 * for package com.lorentzos.swipecards
 * and project Swipe cards.
 * Use with caution dinausaurs might appear!
 *
 * Additions and customizations done by Rich Luick
 */
public class SwipeableCards extends CardView {

    private static final int INVALID_POINTER_ID = -1;

    private ParseUser mUser;

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

    private float objectX;
    private float objectY;
    private int objectH;
    private int objectW;
    private int parentWidth;
    //private SwipeListener mSwipeListener;
    private float halfWidth;
    private float BASE_ROTATION_DEGREES = 15.f;

    private float aPosX;
    private float aPosY;
    private float aDownTouchX;
    private float aDownTouchY;

    private int mActivePointerId = INVALID_POINTER_ID;
    private View frame = null;

    private final int TOUCH_ABOVE = 0;
    private final int TOUCH_BELOW = 1;
    private int touchPosition;
    private boolean isAnimationRunning = false;
    private float MAX_COS = (float) Math.cos(Math.toRadians(45));
    private float ROTATION_DEGREES = 15.f;

    public SwipeableCards(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.swipeable_card, this, true);
        ButterKnife.inject(this);

        loader = ImageLoader.getInstance();

//        frame = getRootView();
//        this.objectX = frame.getX();
//        this.objectY = frame.getY();
//        this.objectH = frame.getHeight();
//        this.objectW = frame.getWidth();
//        this.halfWidth = objectW / 2f;
//        this.parentWidth = ((ViewGroup) frame.getParent()).getWidth();
    }

    public void setUser(ParseUser parseUser) {
        mUser = parseUser;

        resetFields();

        mProfImage = (ParseFile) mUser.get(Constants.PROFILE_IMAGE);
        mProfImage2 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE2);
        mProfImage3 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE3);
        mProfImage4 = (ParseFile) mUser.get(Constants.PROFILE_IMAGE4);
        mName = (String) mUser.get(Constants.NAME);
        mLocation = (String) mUser.get(Constants.LOCATION);
        mAboutMe = (String) mUser.get(Constants.ABOUT_ME);
        mHasRoom = (Boolean) mUser.get(Constants.HAS_ROOM);
        mSmokes = (Boolean) mUser.get(Constants.SMOKES);
        mDrinks = (Boolean) mUser.get(Constants.DRINKS);
        mPets = (Boolean) mUser.get(Constants.DRINKS);
        mAge = (String) mUser.get(Constants.AGE);
        mMaxPrice = (String) mUser.get(Constants.MAX_PRICE);
        mMinPrice = (String) mUser.get(Constants.MAX_PRICE);
        setFields();
    }

    public ParseUser getUser() {
        return mUser;
    }

    /*
     * This metod is called once all the variables are reset and it then sets the filed with
     * the new variable for the new Roomie Card
     */
    private void setFields() {
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
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.card_slide_out_left));
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
    private void resetFields() {
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
                view.setText(getContext().getString(R.string.yes));
            }
            else {
                view.setText(getContext().getString(R.string.no));
            }
        }
        else {
            view.setText("");
        }
    }

//    public boolean onTouch(View view, MotionEvent event) {
//        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                Toast.makeText(getContext(), "DOWN!", Toast.LENGTH_LONG).show();
//                // from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
//                // Save the ID of this pointer
//
//                mActivePointerId = event.getPointerId(0);
//                float x = 0;
//                float y = 0;
//                boolean success = false;
//                try {
//                    x = event.getX(mActivePointerId);
//                    y = event.getY(mActivePointerId);
//                    success = true;
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }
//                if (success) {
//                    // Remember where we started
//                    aDownTouchX = x;
//                    aDownTouchY = y;
//                    //to prevent an initial jump of the magnifier, aposX and aPosY must
//                    //have the values from the magnifier frame
//                    if (aPosX == 0) {
//                        aPosX = frame.getX();
//                    }
//                    if (aPosY == 0) {
//                        aPosY = frame.getY();
//                    }
//
//                    if (y < objectH / 2) {
//                        touchPosition = TOUCH_ABOVE;
//                    } else {
//                        touchPosition = TOUCH_BELOW;
//                    }
//                }
//
//                view.getParent().requestDisallowInterceptTouchEvent(true);
//                break;
//
//            case MotionEvent.ACTION_UP:
//                mActivePointerId = INVALID_POINTER_ID;
//                resetCardViewOnStack();
//                view.getParent().requestDisallowInterceptTouchEvent(false);
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                break;
//
//            case MotionEvent.ACTION_POINTER_UP:
//                // Extract the index of the pointer that left the touch sensor
//                final int pointerIndex = (event.getAction() &
//                        MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//                final int pointerId = event.getPointerId(pointerIndex);
//                if (pointerId == mActivePointerId) {
//                    // This was our active pointer going up. Choose a new
//                    // active pointer and adjust accordingly.
//                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//                    mActivePointerId = event.getPointerId(newPointerIndex);
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//
//                // Find the index of the active pointer and fetch its position
//                final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
//                final float xMove = event.getX(pointerIndexMove);
//                final float yMove = event.getY(pointerIndexMove);
//
//                //from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
//                // Calculate the distance moved
//                final float dx = xMove - aDownTouchX;
//                final float dy = yMove - aDownTouchY;
//
//
//                // Move the frame
//                aPosX += dx;
//                aPosY += dy;
//
//                // calculate the rotation degrees
//                float distobjectX = aPosX - objectX;
//                float rotation = BASE_ROTATION_DEGREES * 2.f * distobjectX / parentWidth;
//                if (touchPosition == TOUCH_BELOW) {
//                    rotation = -rotation;
//                }
//
//                //in this area would be code for doing something with the view as the frame moves.
//                frame.setX(aPosX);
//                frame.setY(aPosY);
//                frame.setRotation(rotation);
//                mSwipeListener.onScroll(getScrollProgressPercent());
//                break;
//
//            case MotionEvent.ACTION_CANCEL: {
//                mActivePointerId = INVALID_POINTER_ID;
//                view.getParent().requestDisallowInterceptTouchEvent(false);
//                break;
//            }
//        }
//
//        return true;
//    }
//
//    private float getScrollProgressPercent() {
//        if (movedBeyondLeftBorder()) {
//            return -1f;
//        } else if (movedBeyondRightBorder()) {
//            return 1f;
//        } else {
//            float zeroToOneValue = (aPosX + halfWidth - leftBorder()) / (rightBorder() - leftBorder());
//            return zeroToOneValue * 2f - 1f;
//        }
//    }
//
//    private boolean resetCardViewOnStack() {
//        if (movedBeyondLeftBorder()) {
//            // Left Swipe
//            onSelected(true, getExitPoint(-objectW), 100);
//            mSwipeListener.onScroll(-1.0f);
//        } else if (movedBeyondRightBorder()) {
//            // Right Swipe
//            onSelected(false, getExitPoint(parentWidth), 100);
//            mSwipeListener.onScroll(1.0f);
//        } else {
//            float abslMoveDistance = Math.abs(aPosX - objectX);
//            aPosX = 0;
//            aPosY = 0;
//            aDownTouchX = 0;
//            aDownTouchY = 0;
//            frame.animate()
//                    .setDuration(200)
//                    .setInterpolator(new OvershootInterpolator(1.5f))
//                    .x(objectX)
//                    .y(objectY)
//                    .rotation(0);
//            mSwipeListener.onScroll(0.0f);
//            if (abslMoveDistance < 4.0) {
//                mSwipeListener.onClick(mUser);
//            }
//        }
//        return false;
//    }
//
//    private boolean movedBeyondLeftBorder() {
//        return aPosX + halfWidth < leftBorder();
//    }
//
//    private boolean movedBeyondRightBorder() {
//        return aPosX + halfWidth > rightBorder();
//    }
//
//
//    public float leftBorder() {
//        return parentWidth / 4.f;
//    }
//
//    public float rightBorder() {
//        return 3 * parentWidth / 4.f;
//    }
//
//    public void onSelected(final boolean isLeft, float exitY, long duration) {
//
//        isAnimationRunning = true;
//        float exitX;
//        if (isLeft) {
//            exitX = -objectW - getRotationWidthOffset();
//        } else {
//            exitX = parentWidth + getRotationWidthOffset();
//        }
//
//        this.frame.animate()
//                .setDuration(duration)
//                .setInterpolator(new AccelerateInterpolator())
//                .x(exitX)
//                .y(exitY)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        if (isLeft) {
//                            mSwipeListener.onCardExited();
//                            mSwipeListener.leftExit(mUser);
//                        } else {
//                            mSwipeListener.onCardExited();
//                            mSwipeListener.rightExit(mUser);
//                        }
//                        isAnimationRunning = false;
//                    }
//                })
//                .rotation(getExitRotation(isLeft));
//    }
//
//
//    /**
//     * Starts a default left exit animation.
//     */
//    public void selectLeft() {
//        if (!isAnimationRunning)
//            onSelected(true, objectY, 200);
//    }
//
//    /**
//     * Starts a default right exit animation.
//     */
//    public void selectRight() {
//        if (!isAnimationRunning)
//            onSelected(false, objectY, 200);
//    }
//
//
//    private float getExitPoint(int exitXPoint) {
//        float[] x = new float[2];
//        x[0] = objectX;
//        x[1] = aPosX;
//
//        float[] y = new float[2];
//        y[0] = objectY;
//        y[1] = aPosY;
//
//        LinearRegression regression = new LinearRegression(x, y);
//
//        //Your typical y = ax+b linear regression
//        return (float) regression.slope() * exitXPoint + (float) regression.intercept();
//    }
//
//    private float getExitRotation(boolean isLeft) {
//        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentWidth - objectX) / parentWidth;
//        if (touchPosition == TOUCH_BELOW) {
//            rotation = -rotation;
//        }
//        if (isLeft) {
//            rotation = -rotation;
//        }
//        return rotation;
//    }
//
//
//    /**
//     * When the object rotates it's width becomes bigger.
//     * The maximum width is at 45 degrees.
//     * <p/>
//     * The below method calculates the width offset of the rotation.
//     */
//    private float getRotationWidthOffset() {
//        return objectW / MAX_COS - objectW;
//    }
//
//
//    public void setRotationDegrees(float degrees) {
//        this.BASE_ROTATION_DEGREES = degrees;
//    }
//
//    public boolean isTouching() {
//        return this.mActivePointerId != INVALID_POINTER_ID;
//    }
//
//    public PointF getLastPoint() {
//        return new PointF(this.aPosX, this.aPosY);
//    }
//
//    protected interface SwipeListener {
//        void onCardExited();
//        void leftExit(ParseUser user);
//        void rightExit(ParseUser user);
//        void onClick(ParseUser user);
//        void onScroll(float scrollProgressPercent);
//    }
}
