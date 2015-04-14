package com.richluick.android.roomie.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

    private Context context;
    private static ConnectionDetector instance;

    public ConnectionDetector(Context context){
        this.context = context;
    }

    public static ConnectionDetector getInstance(Context ctx) {
        if(instance == null) {
           instance = new ConnectionDetector(ctx);
        }
        return instance;
    }

    /**
     * Checking for all possible internet providers
     * **/
    public boolean isConnected(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}