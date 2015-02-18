package com.richluick.android.roomie.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;
    private ArrayList<ParseObject> mRelations;

    public ChatListAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.chat_item_adapter, objects);

        this.mContext = context;
        this.mRelations = (ArrayList<ParseObject>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        ParseObject relation = null;
        try {
            relation = mRelations.get(position).fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_item_adapter, null);

            holder = new ViewHolder();
            holder.profImage = (ImageView) convertView.findViewById(R.id.profImage);
            holder.nameField = (TextView) convertView.findViewById(R.id.nameField);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseUser user = null;
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (relation != null) {
            ParseUser user1 = (ParseUser) relation.get(Constants.USER1);
            String userId = user1.getObjectId();

            if (userId.equals(currentUser.getObjectId())) {
                user = (ParseUser) relation.get(Constants.USER2);
            }
            else {
                user = (ParseUser) relation.get(Constants.USER1);
            }
        }

        String name = "";
        ParseFile profImage = null;
        if (user != null) {
            name = (String) user.get(Constants.NAME);
            profImage = (ParseFile) user.get(Constants.PROFILE_IMAGE);
        }

        holder.nameField.setText(name);

        if (profImage != null) {
            profImage.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.profImage.setImageBitmap(image);
                    }
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView profImage;
        TextView nameField;
    }
}
