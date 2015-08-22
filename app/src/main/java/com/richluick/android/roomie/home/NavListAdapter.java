package com.richluick.android.roomie.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.richluick.android.roomie.R;

import java.util.ArrayList;
import java.util.List;


public class NavListAdapter extends ArrayAdapter<NavItem> {

    private Context mContext;
    private ArrayList<NavItem> mItems;

    public NavListAdapter(Context context, List<NavItem> items) {
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
