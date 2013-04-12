package com.example.newecosns.utnils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {

	/*
	 * 接続可能ならtrueを、ダメならfalseを返す
	 * context は thisじゃなくてthis.getApplicationContext()がいいらしい
	 */
	public static boolean isConnected(Context context){
	    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = cm.getActiveNetworkInfo();
	    if(info != null ){
	        return cm.getActiveNetworkInfo().isConnected();
	    }
	    return false;
	}

}
