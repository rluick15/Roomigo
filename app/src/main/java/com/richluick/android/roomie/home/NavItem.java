package com.richluick.android.roomie.home;


import android.graphics.drawable.Drawable;

public class NavItem {

    private Drawable mNavIcon;
    private String mNavItemName;

    public NavItem(Drawable navIcon, String navItemName) {
        this.mNavIcon = navIcon;
        this.mNavItemName = navItemName;
    }

    public Drawable getNavIcon() {
        return mNavIcon;
    }

    public String getNavItemName() {
        return mNavItemName;
    }
}
