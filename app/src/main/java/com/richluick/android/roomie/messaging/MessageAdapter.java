package com.richluick.android.roomie.messaging;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.constants.Constants;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Pair<WritableMessage, Integer>> messages;
    private Context mContext;
    private String mName;

    public MessageAdapter(Activity activity) {
        this.mContext = activity;
        messages = new ArrayList<>();
    }

    public void addMessage(WritableMessage message, int direction, String name) {
        mName = name;
        messages.add(new Pair<>(message, direction));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);
        final ViewHolder holder;

        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.message_left;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_right;
            }
            convertView = LayoutInflater.from(mContext).inflate(res, null);

            holder = new ViewHolder();
            holder.txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
            holder.nameField = (TextView) convertView.findViewById(R.id.txtSender);
            holder.dateField = (TextView) convertView.findViewById(R.id.txtDate);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setEnabled(false);
        convertView.setOnClickListener(null);

        WritableMessage message = messages.get(i).first;

        holder.dateField.setText(message.getHeaders().get(Constants.DATE));
        holder.txtMessage.setText(message.getTextBody());
        if(holder.nameField != null) {
            holder.nameField.setText(mName);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView txtMessage;
        TextView nameField;
        TextView dateField;
    }
}