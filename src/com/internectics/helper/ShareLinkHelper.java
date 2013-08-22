package com.internectics.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.internectics.data.Pack;
import com.internectics.helper.AmazonSDB.SimpleDBHelper;
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
import java.util.HashMap;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mFilePathInDropbox;
    private Pack mCurentPack;
    private String mUnshortedFCCShareLink;


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
            mUnshortedFCCShareLink = shareLink.replace("https","fcc").replace("http","fcc");
            Log.d(Global.debugTag, "the fcc share linkage is: " + mUnshortedFCCShareLink);
            String redirectedShareLink = getRidirectedURL(mUnshortedFCCShareLink);
            if (redirectedShareLink.indexOf("http://") != 0) {
                Toast.makeText(mContext, "Redirect sevice is not available now, please try again", 1).show();
            } else {
                Log.d(Global.debugTag, "the shareLink is: " + redirectedShareLink);
                PackRecordHelper.savePackUploadRecord(mContext, mCurentPack, redirectedShareLink,null);

            }


        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        //Dialog to show max allowable download time
        final  EditText editText = new EditText(mContext);
        editText.setGravity(Gravity.CENTER);
        editText.setText("9999");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(mContext)
                .setTitle("Set max number of downloads")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(mUnshortedFCCShareLink);
                        String simpleDBItemNamedata = uri.getLastPathSegment();
                        simpleDBItemNamedata = simpleDBItemNamedata.substring(0, simpleDBItemNamedata.indexOf(".zip"));
                        Global.currentAmazonSimpleDBItemName = simpleDBItemNamedata;
                        int maxNo = Integer.parseInt(editText.getText().toString());
                        insertIntoAmazonSimpleDB(simpleDBItemNamedata, maxNo);
                    }
                })
                .setNegativeButton("Unlimited", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(mUnshortedFCCShareLink);
                        String simpleDBItemNamedata = uri.getLastPathSegment();
                        simpleDBItemNamedata = simpleDBItemNamedata.substring(0, simpleDBItemNamedata.indexOf(".zip"));
                        Global.currentAmazonSimpleDBItemName = simpleDBItemNamedata;
                        int maxNo = 9999999;
                        insertIntoAmazonSimpleDB(simpleDBItemNamedata, maxNo);
                    }
                })
                .show();
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
            responseString = "";
        } else {
            Log.d(Global.debugTag,"The generated redirected URL from tinyurl.com is:" + responseString);
        }

        return responseString;
    }





    public void execShareAction() {
        String shareLink = PackRecordHelper.getCurrentPackShareLink(mCurentPack);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareLink);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share:");
        mContext.startActivity(Intent.createChooser(intent, "Share current pack to"));
    }

    public boolean insertIntoAmazonSimpleDB(final String itemName, int maxNo) {
        Log.d(Global.debugTag, "Now begin to execute insertIntoAmazonSimpleDB");
        boolean result = false;
        final HashMap<String, String> rowData = new HashMap<String, String>();
        rowData.put("currentNo","0");
        rowData.put("maxNo",String.format("%d",maxNo));

        new Thread()
        {
            @Override
            public void run() {
                SimpleDBHelper.updateAttributesForItem(Global.amazon_sdb_domain_name,itemName,rowData);
            }
        }.start();

        execShareAction();

        return result;

    }

}
