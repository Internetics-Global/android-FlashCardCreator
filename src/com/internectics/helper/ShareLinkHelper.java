package com.internectics.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.internectics.data.Pack;
import com.internectics.util.Global;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mFilePathInDropbox;
    private Pack mCurentPack;


    /*
      @param shareLink must enter a valid value when directly sharing; enter anything when creating share link first
     */
    public ShareLinkHelper(Context context, String file, Pack currentPack) {
        mContext = context;
        mFilePathInDropbox = file;
        mCurentPack = currentPack;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DropboxAPI.DropboxLink link = DropboxHelper.getDropboxAPI().share(mFilePathInDropbox);
            String shortedShareLink = link.url;
            String shareLink = getUnshortedURL(shortedShareLink);
            String fccShareLink = shareLink.replace("https","fcc").replace("http","fcc");
            String redirectedShareLink = getRidirectedURL(fccShareLink);
            Log.d(Global.debugTag, "the shareLink is: " + redirectedShareLink);
            PackRecordHelper.savePackUploadRecord(mContext, mCurentPack, shareLink);
            execShareAction();

        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }


    String getUnshortedURL(String shortedURL) {
        URLConnection conn = null;
        try {
            URL inputURL = new URL(shortedURL);
            conn = inputURL.openConnection();

        } catch (MalformedURLException e) {
            Log.d(Global.debugTag,"Please input a valid URL");
        } catch (IOException ioe) {
            Log.d(Global.debugTag,"Can not connect to the URL");
        }

        String str =  conn.getHeaderField("location");

        return str;

    }

    String getRidirectedURL(String url){

        String responseString= "";

        String wholeURL = Global.URL_REDIRECT_API + url;

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = httpclient.execute(new HttpGet(wholeURL));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (responseString.contains("http://") == false) {
            Log.e(Global.debugTag,"The generated redirected URL from tinyurl.com is not correct:" + responseString);
        } else {
            Log.e(Global.debugTag,"The generated redirected URL from tinyurl.com is:" + responseString);
        }

        return responseString;
    }


    public void execShareAction() {
        String shareLink = PackRecordHelper.getCurrentPackShareLink(mCurentPack);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareLink);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Something to say:");
        mContext.startActivity(Intent.createChooser(intent, "Share current pack to"));
    }

}
