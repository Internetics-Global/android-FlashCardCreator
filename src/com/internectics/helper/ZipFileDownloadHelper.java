package com.internectics.helper;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

public class ZipFileDownloadHelper {
	
	
	/**
	 * you need to registerReceiver with IntentFilter of DownloadManager.ACTION_DOWNLOAD_COMPLETE 
	 * @param input
	 * @return boolean
	 */
	public static void downloadNow(Context mContext, String url) {
		DownloadManager downloadManager =(DownloadManager)(mContext.getSystemService(Context.DOWNLOAD_SERVICE));
		DownloadManager.Request downlodRequest=new DownloadManager.Request (Uri.parse("http://192.168.0.66:8080/qqinput.apk"));
		downlodRequest.setTitle("Download a pack");
		downlodRequest.setDescription("from somebody");
		downlodRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);  
		downlodRequest.setVisibleInDownloadsUi(true);  
		downlodRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		downlodRequest.setDestinationInExternalFilesDir(mContext, null, "qqinput.apk");   
		downloadManager.enqueue(downlodRequest);
	}
	
	
    	
	
}
