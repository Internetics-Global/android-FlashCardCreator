package com.internectics.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Reachibility {
	public static Boolean apiReachable(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		Log.d(Global.debugTag, "Network is reachibable or not:"+activeNetwork.isConnected());
		return activeNetwork.isConnected();
	}

}
