package com.richluick.android.roomie.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.objects.NavItem;
import com.richluick.android.roomie.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class NavAdapter extends ArrayAdapter<NavItem> {

    private Context mContext;
    private ArrayList<NavItem> mItems;

    public NavAdapter(Context context, List<NavItem> items) {
        super(context, R.layout.nav_row_adapter, items);

        this.mContext = context;
        this.mItems = (ArrayList<NavItem>) items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.nav_row_adapter, null);

            holder = new ViewHolder();
            holder.navIconField = (ImageView) convertView.findViewById(R.id.rowIcon);
            holder.navNameField = (TextView) convertView.findViewById(R.id.rowText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.navNameField.setText(mItems.get(position).getNavItemName());
        holder.navIconField.setImageDrawable(mItems.get(position).getNavIcon());

        return convertView;
    }

    private static class ViewHolder {
        ImageView navIconField;
        TextView navNameField;
    }
}
