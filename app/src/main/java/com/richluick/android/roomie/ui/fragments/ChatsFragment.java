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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.activities.MessagingActivity;
import com.richluick.android.roomie.ui.adapters.ChatListAdapter;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ParseUser mCurrentUser;
    private ChatListAdapter mAdapter;
    private List<ParseObject> mChats;
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
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        else {
            mListView.setVisibility(View.VISIBLE);

            //Query relations where current user is either User1 or User2
            ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.RELATION);
            query1.whereEqualTo(Constants.USER1, mCurrentUser);

            ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Constants.RELATION);
            query2.whereEqualTo(Constants.USER2, mCurrentUser);

            List<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(query1);
            queries.add(query2);

            ParseQuery<ParseObject> query = ParseQuery.or(queries);
            query.include(Constants.USER1);
            query.include(Constants.USER2);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    mProgressBar.setVisibility(View.GONE);

                    if (e == null) {
                        if (parseObjects.isEmpty()) { //set empty view
                            mEmptyView.setVisibility(View.VISIBLE);
                        } else { //set list adapter to returned relations
                            mEmptyView.setVisibility(View.GONE);
                            mChats = parseObjects;
                            mAdapter = new ChatListAdapter(mContext, mChats);
                            mListView.setAdapter(mAdapter);
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParseUser user1 = (ParseUser) mChats.get(position).get(Constants.USER1);
        String userId = user1.getObjectId();
        String relationId = mChats.get(position).getObjectId();

        ParseUser user;

        //find the other user in the relation
        if (userId.equals(mCurrentUser.getObjectId())) {
            user = (ParseUser) mChats.get(position).get(Constants.USER2);
        }
        else {
            user = (ParseUser) mChats.get(position).get(Constants.USER1);
        }

        //go to selected chat activity
        Intent intent = new Intent(mContext, MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, user.getObjectId());
        intent.putExtra(Constants.RECIPIENT_NAME, (String) user.get(Constants.NAME));
        intent.putExtra(Constants.OBJECT_ID, relationId);
        startActivity(intent);
        //overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

//    interface ChatSelection() {
//        public void
//    }
}
