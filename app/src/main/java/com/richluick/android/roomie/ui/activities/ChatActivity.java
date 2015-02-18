package com.richluick.android.roomie.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.adapters.ChatListAdapter;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ParseUser mCurrentUser;
    private ListView mListView;
    private ChatListAdapter mAdapter;
    private List<ParseObject> mChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.title_chats));
        setContentView(R.layout.activity_chat);

        mCurrentUser = ParseUser.getCurrentUser();
        mListView = (ListView) findViewById(R.id.chatList);
        mListView.setOnItemClickListener(this);

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
                mChats = parseObjects;
                mAdapter = new ChatListAdapter(ChatActivity.this, mChats);
                mListView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParseUser user1 = (ParseUser) mChats.get(position).get(Constants.USER1);
        String userId = user1.getObjectId();

        ParseUser user;

        if (userId.equals(mCurrentUser.getObjectId())) {
            user = (ParseUser) mChats.get(position).get(Constants.USER2);
        }
        else {
            user = (ParseUser) mChats.get(position).get(Constants.USER1);
        }

        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(Constants.RECIPIENT_ID, user.getObjectId());
        intent.putExtra(Constants.RECIPIENT_NAME, (String) user.get(Constants.NAME));
        startActivity(intent);
    }
}
