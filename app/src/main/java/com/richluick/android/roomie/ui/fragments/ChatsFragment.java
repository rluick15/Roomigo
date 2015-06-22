package com.richluick.android.roomie.ui.fragments;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.data.ConnectionsList;
import com.richluick.android.roomie.ui.activities.MessagingActivity;
import com.richluick.android.roomie.ui.adapters.ChatListAdapter;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This fragment displays the list of current connections fpr the current user and allows them to
 * click on a list item to open the corresponding chat
 */
public class ChatsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ParseUser mCurrentUser;
    private ChatListAdapter mAdapter;
    private ArrayList<ParseUser> mChats;
    private Context mContext;

    @InjectView(R.id.chatList)
    ListView mListView;
    @InjectView(R.id.emptyView) TextView mEmptyView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    public ChatsFragment() {
        // Required empty public constructor
    }

    public ChatsFragment(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.inject(this, v);

        executeQuery();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Check the connection
        if(!ConnectionDetector.getInstance(mContext).isConnected()) {
            Toast.makeText(mContext, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
        }
    }

    /*
         * This method sets up and executes the query. It is called onCreate and if the user decides to
         * refrest after a connection error
         */
    private void executeQuery() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCurrentUser = ParseUser.getCurrentUser();
        mListView.setOnItemClickListener(this);

        //Check the connection
        if(!ConnectionDetector.getInstance(mContext).isConnected()) {
            Toast.makeText(mContext, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            mListView.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
        else {
            mListView.setVisibility(View.VISIBLE);

            mChats = ConnectionsList.getInstance(mContext).getConnectionList();

            mProgressBar.setVisibility(View.GONE);

            if (mChats.isEmpty() || mChats == null) { //set empty view
                mEmptyView.setVisibility(View.VISIBLE);
            } else { //set list adapter to returned relations
                mEmptyView.setVisibility(View.GONE);
                mAdapter = new ChatListAdapter(mContext, mChats);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParseUser user = mChats.get(position);

        //go to selected chat activity
        Intent intent = new Intent(mContext, MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, user.getObjectId());
        intent.putExtra(Constants.RECIPIENT_NAME, (String) user.get(Constants.NAME));
        intent.putExtra(Constants.OBJECT_ID, user.getObjectId());
        startActivity(intent);
        //overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

}
