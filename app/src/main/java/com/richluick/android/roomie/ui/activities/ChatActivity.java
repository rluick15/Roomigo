package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.richluick.android.roomie.ui.adapters.ChatListAdapter;
import com.richluick.android.roomie.utils.ConnectionDetector;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChatActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ParseUser mCurrentUser;
    private ChatListAdapter mAdapter;
    private List<ParseObject> mChats;

    @InjectView(R.id.chatList) ListView mListView;
    @InjectView(R.id.emptyView) TextView mEmptyView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        executeQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check the connection
        if(!ConnectionDetector.getInstance(this).isConnected()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
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
        if(!ConnectionDetector.getInstance(this).isConnected()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
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
                            mAdapter = new ChatListAdapter(ChatActivity.this, mChats);
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
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, user.getObjectId());
        intent.putExtra(Constants.RECIPIENT_NAME, (String) user.get(Constants.NAME));
        intent.putExtra(Constants.OBJECT_ID, relationId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

//todo:solve this issue.
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == android.R.id.home) {
//            finish();
//            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            executeQuery();
        }

            return super.onOptionsItemSelected(item);
    }
}
