package com.internectics.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.facebook.widget.FacebookDialog;
import com.internectics.android_flashcardcreator.MainActivity;
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
import java.util.List;

import com.facebook.*;
import com.facebook.model.*;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Activity mActivity;
    private String mFilePathInDropbox;
    private Pack mCurentPack;
    private String mUnshortedFCCShareLink;

    private boolean canPresentShareDialog;


    /*
      @param shareLink must enter a valid value when directly sharing; enter anything when creating share link first
     */
    public ShareLinkHelper(Activity activity, String file, Pack currentPack) {
        mActivity = activity;
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
                Toast.makeText(mActivity, "Redirect sevice is not available now, please try again", 1).show();
            } else {
                Log.d(Global.debugTag, "the shareLink is: " + redirectedShareLink);
                PackRecordHelper.savePackUploadRecord(mActivity, mCurentPack, redirectedShareLink,null);

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
        final  EditText editText = new EditText(mActivity);
        editText.setGravity(Gravity.CENTER);
        editText.setText("9999");
        editText.setSingleLine();
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(mActivity)
                .setTitle("Set max number of downloads")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm =(InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);

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

                        InputMethodManager imm =(InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);

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

    public static String getUnshortedURL(String shortedURL) {
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


    public void execShareAction() {

        new AlertDialog.Builder(mActivity)
                .setTitle("Share")
                .setItems(new String[] {"Facebook","Twitter","Email","Copy to clipboard"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String shareLink = PackRecordHelper.getCurrentPackShareLink(mCurentPack);
                        shareActionOnItemSelected(which,shareLink);
                    }
                })
                .show();
    }


    private void shareActionOnItemSelected (int position,String shareLink) {
        String finalPostString = "I've just created a pack of Flash Cards with the Flash Card Creator! ( " + shareLink +" ) Check it out!";
        switch (position) {
            case 0: {

                boolean isFacebookAppInstalled = true;
                try {
                    ApplicationInfo info = mActivity.getPackageManager().
                            getApplicationInfo("com.facebook.katana", 0 );
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    isFacebookAppInstalled = false;
                }

                if (isFacebookAppInstalled) {
                    shareToFacebook(shareLink);
                }  else {
                    Toast.makeText(mActivity, "Share is allowed only when Facebook app is installed.", Toast.LENGTH_LONG).show();
                }

                break;
            }

            case 1: {

                boolean isTwitterAppInstalled = true;
                try {
                    ApplicationInfo info = mActivity.getPackageManager().
                            getApplicationInfo("com.twitter.android", 0 );
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    isTwitterAppInstalled = false;
                }

                if (isTwitterAppInstalled) {
                    Intent intent = findTwitterClient();
                    if (intent != null) {
                        intent.putExtra(Intent.EXTRA_TEXT, finalPostString);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Share my pack");
                        mActivity.startActivity(Intent.createChooser(intent, "Share current pack to"));
                    }
                }  else {
                    Toast.makeText(mActivity, "Share is allowed only when Twitter app is installed.", Toast.LENGTH_LONG).show();
                }

                break;
            }
            case 2: {
                Log.d(Global.debugTag, "Email share");
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hi All");
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, finalPostString);
                mActivity.startActivity(Intent.createChooser(emailIntent, "Share via Email"));
                break;
            }
            case 3: {
                Log.d(Global.debugTag, "Copy");
                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Service.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText( "share linkage",shareLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mActivity, "Has copied to clipboard", Toast.LENGTH_LONG).show();
                break;
            }
            case 4: {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, finalPostString);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share my pack");
                mActivity.startActivity(Intent.createChooser(intent, "Share current pack to"));
            }
            default:
                break;
        }
    }


    private void shareToFacebook(String shareLink) {

        canPresentShareDialog = FacebookDialog.canPresentShareDialog(mActivity,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);

        Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction(shareLink);
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest((MainActivity) mActivity, "publish_actions"));
                return;
            }
        }

        if (canPresentShareDialog) {
            handlePendingAction(shareLink);
        }

    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }


    private void handlePendingAction(String shareLink) {
        if (canPresentShareDialog) {
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(mActivity)
                    .setName("Hi All:")
                    .setPicture("https://dl.dropbox.com/s/qprzhxl2gpzicoe/icon114x114iPhoneHiRes.png")
                    .setDescription("I've just created a pack of Flash Cards with the Flash Card Creator! Check it out!")
                    .setLink(shareLink)
                    .build();
            shareDialog.present();
        } else if (hasPublishPermission()) {
            final String message = "Test";
            Request request = Request
                    .newStatusUpdateRequest(Session.getActiveSession(), message, null, null, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            showPublishResult(message, response.getGraphObject(), response.getError());
                        }
                    });
            request.executeAsync();
        } else {
        }
    }


    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        if (error == null) {
            title = "Successfully done";
        } else {
            title = "Error during share, try gain";
        }

        new AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setPositiveButton("OK", null)
                .show();
    }


    public Intent findTwitterClient() {
        final String[] twitterApps = {
                // package // name - nb installs (thousands)
                "com.twitter.android", // official - 10 000
                "com.twidroid", // twidroid - 5 000
                "com.handmark.tweetcaster", // Tweecaster - 5 000
                "com.thedeck.android" }; // TweetDeck - 5 000 };
        Intent tweetIntent = new Intent();
        tweetIntent.setType("text/plain");
        final PackageManager packageManager = mActivity.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (int i = 0; i < twitterApps.length; i++) {
            for (ResolveInfo resolveInfo : list) {
                String p = resolveInfo.activityInfo.packageName;
                if (p != null && p.startsWith(twitterApps[i])) {
                    tweetIntent.setPackage(p);
                    return tweetIntent;
                }
            }
        }
        return null;

    }


}
