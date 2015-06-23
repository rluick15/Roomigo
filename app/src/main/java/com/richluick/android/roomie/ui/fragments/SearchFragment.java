package com.richluick.android.roomie.ui.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.data.ConnectionsList;
import com.richluick.android.roomie.data.SearchResults;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements View.OnClickListener {

    private Context mContext;
    private ParseUser mCurrentUser;
    private ParseUser mUser;
    private List<String> mCurrentRelations;
    private Animation mSlideOutRight;
    private Animation mSlideOutLeft;
    private Animation mExpandIn;
    private List<String> mIndices = new ArrayList<>();
    private RoomieFragment mRoomieFragment;

    @InjectView(R.id.acceptButton) Button mAcceptButton;
    @InjectView(R.id.rejectButton) Button mRejectButton;
    @InjectView(R.id.emptyView) TextView mEmptyView;
    @InjectView(R.id.undiscoverableView) TextView mUndiscoverable;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.roomieFrag) CardView mCardView;


    public SearchFragment() {
        // Required empty public constructor
    }

    public SearchFragment(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, v);

        mProgressBar.setVisibility(View.VISIBLE);

        mEmptyView.setOnClickListener(this);

        if (checkConnection()) {
            setEmptyView();
        }
        else {
            setupActivity();
        }

        return v;
    }

    /**
     * This method continues the setup of the activity. It is called onCreate and also if the user
     * decides to refresh the activity either after no search results or a connection error
     */
    private void setupActivity() {
        //check if user has selected/deslected discoverable and proceed from there
        Boolean discoverable = (Boolean) ParseUser.getCurrentUser().get(Constants.DISCOVERABLE);
        if(!discoverable) {
            mUndiscoverable.setVisibility(View.VISIBLE);
            mCardView.setVisibility(View.GONE);
            return;
        }

        //build the Roomie card fragment for displaying info
        mRoomieFragment = new RoomieFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.roomieFrag, mRoomieFragment)
                .commit();
        getChildFragmentManager().executePendingTransactions();

        mCurrentUser = ParseUser.getCurrentUser();

        //get the search results from Parse
        SearchResults.getInstance(mContext).getSearchResultsFromParse(mCurrentUser,
                new SearchResults.ResultsLoadedListener() {
                    @Override
                    public void onResultsLoaded() {
                        mProgressBar.setVisibility(View.GONE);
                        setAnimations();
                        setUserResult();
                    }
                });

        mAcceptButton.setOnClickListener(this);
        mRejectButton.setOnClickListener(this);
    }

    /**
     * This method sets the animations and listeners for the card animations used in this activity
     */
    private void setAnimations() {
        mExpandIn = AnimationUtils.loadAnimation(getActivity(), R.anim.card_expand_in);

        mSlideOutRight = AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_right);
        mSlideOutRight.setFillAfter(true);

        mSlideOutLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.card_slide_out_left);
        mSlideOutLeft.setFillAfter(true);
    }

    @Override
    public void onClick(View v) {
        if(v == mAcceptButton) {
            mCardView.startAnimation(mSlideOutLeft);
            ConnectionsList.getInstance(mContext).connectionRequest(mCurrentUser, mUser);
            setUserResult();
        }
        else if(v == mRejectButton){
            mCardView.startAnimation(mSlideOutRight);
            setUserResult();
        }
        else if(v == mEmptyView) {
            mProgressBar.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            //a little delay animation for the progress bar if the user clicks refresh
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupActivity();
                }
            }, 1000);
        }
    }

    /**
     * This method gets and displays a new search result object in the RoomieFragment
     */
    private void setUserResult() {
        mUser = SearchResults.getInstance(mContext).getSearchResult();

        if(mUser != null) {
            mAcceptButton.setEnabled(true);
            mRejectButton.setEnabled(true);

            if (mCardView.getVisibility() == View.GONE) { //show the card if hidden
                mCardView.setVisibility(View.VISIBLE);
            }

            mRoomieFragment.resetFields();

            mCardView.startAnimation(mExpandIn);

            //set the RoomieFragment fields for the user
            mRoomieFragment.setName((String) mUser.get(Constants.NAME));
            mRoomieFragment.setAge((String) mUser.get(Constants.AGE));
            mRoomieFragment.setLocation((String) mUser.get(Constants.LOCATION));
            mRoomieFragment.setAboutMe((String) mUser.get(Constants.ABOUT_ME));
            mRoomieFragment.setHasRoom((Boolean) mUser.get(Constants.HAS_ROOM));
            mRoomieFragment.setProfImage((ParseFile) mUser.get(Constants.PROFILE_IMAGE));
            mRoomieFragment.setProfImage2((ParseFile) mUser.get(Constants.PROFILE_IMAGE2));
            mRoomieFragment.setProfImage3((ParseFile) mUser.get(Constants.PROFILE_IMAGE3));
            mRoomieFragment.setProfImage4((ParseFile) mUser.get(Constants.PROFILE_IMAGE4));
            mRoomieFragment.setSmokes((Boolean) mUser.get(Constants.SMOKES));
            mRoomieFragment.setDrinks((Boolean) mUser.get(Constants.DRINKS));
            mRoomieFragment.setPets((Boolean) mUser.get(Constants.PETS));
            mRoomieFragment.setMaxPrice((String) mUser.get(Constants.MAX_PRICE));
            mRoomieFragment.setMinPrice((String) mUser.get(Constants.MIN_PRICE));
            mRoomieFragment.setFields();
        }
        else { //no results
            setEmptyView();
        }
    }

    /**
     * This method checks if the device is connected to the internet and sets the empty view if not
     */
    private boolean checkConnection() {
        if(!ConnectionDetector.getInstance(mContext).isConnected()) {
            Toast.makeText(mContext, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    /**
     * This method sets the search view to empty if either there are no search results or if there
     * is no connection available
     */
    private void setEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
        mCardView.setVisibility(View.GONE);

        mAcceptButton.setEnabled(false);
        mRejectButton.setEnabled(false);
    }


}
