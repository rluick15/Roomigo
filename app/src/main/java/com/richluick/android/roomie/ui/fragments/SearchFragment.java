package com.richluick.android.roomie.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.data.ConnectionsList;
import com.richluick.android.roomie.data.SearchResults;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Observer;
import rx.android.view.ViewObservable;

/**
 * Fragment containing the search results being displayed to the user
 */
public class SearchFragment extends Fragment implements View.OnClickListener {

    private ParseUser mCurrentUser;
    private ParseUser mUser;
    private List<String> mCurrentRelations;
    private Animation mSlideOutRight;
    private Animation mSlideOutLeft;
    private Animation mExpandIn;
    private List<String> mIndices = new ArrayList<>();
    private SearchResults mSearchResults;

    @InjectView(R.id.acceptButton) Button mAcceptButton;
    @InjectView(R.id.rejectButton) Button mRejectButton;
    @InjectView(R.id.emptyView) TextView mEmptyView;
    @InjectView(R.id.undiscoverableView) TextView mUndiscoverable;
    @InjectView(R.id.loadingLayout) RelativeLayout mLoadingLayout;
    @InjectView(R.id.roomieCard) com.richluick.android.roomie.ui.views.SwipeableCards mRoomieCard;


    public SearchFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, v);

        mLoadingLayout.setVisibility(View.VISIBLE);
        mEmptyView.setOnClickListener(this);
        mUndiscoverable.setOnClickListener(this);
        mCurrentUser = ParseUser.getCurrentUser();
        mSearchResults = SearchResults.getInstance(getActivity());
        setAnimations();

        return v;
    }

    /**
     * This method continues the setup of the activity. It is called onCreate and also if the user
     * decides to refresh the activity either after no search results or a connection error
     */
    public void setupActivity() {
        //check if user has selected/deslected discoverable and proceed from there
        Boolean discoverable = (Boolean) ParseUser.getCurrentUser().get(Constants.DISCOVERABLE);
        if(!discoverable) {
            mUndiscoverable.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);
            mRoomieCard.setVisibility(View.GONE);
            return;
        }

        //get the search results from Parse
        //Get the results from parse and after that is done, display the first result only
        mSearchResults.getSearchResultsFromParse(mCurrentUser)
            .flatMap(parseUsers -> Observable.from(parseUsers))
            .first()
            .subscribe(new Observer<ParseUser>() {
                @Override
                public void onCompleted() {
                    mLoadingLayout.setVisibility(View.GONE);
                    setUserResult();
                }

                @Override
                public void onError(Throwable e) {
                    setEmptyView();
                }

                @Override
                public void onNext(ParseUser parseUser) {
                    mUser = parseUser;
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
            mRoomieCard.startAnimation(mSlideOutLeft);
            ConnectionsList.getInstance(getActivity()).connectionRequest(mCurrentUser, mUser);
            mUser = mSearchResults.getSearchResult();
            setUserResult();
        }
        else if(v == mRejectButton){
            mRoomieCard.startAnimation(mSlideOutRight);
            mUser = mSearchResults.getSearchResult();
            setUserResult();
        }
        else if(v == mEmptyView || v == mUndiscoverable) {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            mUndiscoverable.setVisibility(View.GONE);

            //a delay animation for the progress bar if the user clicks refresh
            new Handler().postDelayed(SearchFragment.this::setupActivity, 1000);
        }
    }

    /**
     * This method gets and displays a new search result object in the RoomieFragment
     */
    private void setUserResult() {
        if(mUser != null) {
            mAcceptButton.setEnabled(true);
            mRejectButton.setEnabled(true);

            if (mRoomieCard.getVisibility() == View.GONE) { //show the card if hidden
                mRoomieCard.setVisibility(View.VISIBLE);
            }

            //todo: user callback
            mRoomieCard.setUser(mUser);
            mRoomieCard.startAnimation(mExpandIn);
        }
        else { //no results
            setEmptyView();
        }
    }

    /**
     * This method sets the search view to empty if either there are no search results or if there
     * is no connection available
     */
    public void setEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
        mRoomieCard.setVisibility(View.GONE);

        mAcceptButton.setEnabled(false);
        mRejectButton.setEnabled(false);
    }

    /**
     * This method is called from the MainActivity when the user updates their profile. It hides
     * the card and displays the loading bar
     */
    public void showProgressLayout() {
        if(mLoadingLayout != null) {
            mLoadingLayout.setVisibility(View.VISIBLE);
        }

        if(mRoomieCard != null) {
            mRoomieCard.setVisibility(View.GONE);
        }

        if(mUndiscoverable != null) {
            mUndiscoverable.setVisibility(View.GONE);
        }

        if(mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
