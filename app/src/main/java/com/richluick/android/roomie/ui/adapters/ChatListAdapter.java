package com.richluick.android.roomie.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.ParseObject;
import com.richluick.android.roomie.R;

import java.util.List;

public class ChatListAdapter extends ArrayAdapter<ParseObject> {

    public ChatListAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.chat_item_adapter, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
