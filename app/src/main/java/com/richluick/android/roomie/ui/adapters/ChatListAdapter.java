package com.richluick.android.roomie.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.ParseObject;
import com.richluick.android.roomie.R;

import java.util.List;

public class ChatListAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;

    public ChatListAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.chat_item_adapter, objects);

        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_item_adapter, null);
        }

        return convertView;
    }
}
