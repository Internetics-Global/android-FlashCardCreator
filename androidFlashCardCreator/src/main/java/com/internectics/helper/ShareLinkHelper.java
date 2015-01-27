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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.facebook.FacebookEvents;
import com.nostra13.socialsharing.facebook.FacebookFacade;

import timber.log.Timber;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Activity mActivity;
    private Pack mCurentPack;

    private String mFilePathInDropbox;

    private String mUnshortedFCCShareLink;//如果share link是在本类中生成，而不是作为一个参数引入，则会用到。否则忽略

    private String mShareLink;
    private String mRedirectedShareLink;//经过tinyurl.com处理过的短域名，无论是何种形式（isDirectShare）的分享，都会涉及到

    private boolean mIsDirectShare; //true: 没有经过上传，设密码等，直接share


    /*
      isDirectShare. yes: 直接分享，不经过上传，设密码等，这时shareLink参数必不可少；no:需要经过上传等操作才能share pack
      参数file和shareLink是互斥的，即file=nil,则sharelink！=nil，反之也是

      实际中，这个类有两个作用：
      a. share的完整过程，这时new ShareLinkHelper(...)，后执行execute
      b. 部分，比如仅仅执行execShareAction
     */
    public ShareLinkHelper(Activity activity, String file, String shareLink, Pack currentPack, Boolean isDirectShare) {
        mActivity = activity;
        mFilePathInDropbox = file;
        mCurentPack = currentPack;
        mIsDirectShare= isDirectShare;
        mShareLink = shareLink;
    }


    /*
    URL shorten
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if(mIsDirectShare) {
                mRedirectedShareLink= getRidirectedURL(mShareLink);
            } else {
                DropboxAPI.DropboxLink link = DropboxHelper.getDropboxAPI().share(mFilePathInDropbox);
                String shortedShareLink = link.url;
                String shareLink = getUnshortedURL(shortedShareLink);
                if (shareLink == null) {
                    return false;
                }
                mUnshortedFCCShareLink = shareLink.replace("https","fcc").replace("http","fcc");
                Timber.d(Global.debugTag, "the fcc share linkage is: " + mUnshortedFCCShareLink);
                mRedirectedShareLink = getRidirectedURL(mUnshortedFCCShareLink);
                if (mRedirectedShareLink.indexOf("http://") != 0) {
                    Toast.makeText(mActivity, "Redirect sevice is not available now, please try again", Toast.LENGTH_LONG).show();
                } else {
                    Timber.d(Global.debugTag, "the shareLink is: " + mRedirectedShareLink);
                    PackRecordHelper.savePackUploadRecord(mActivity, mCurentPack, mRedirectedShareLink,null);

                }
            }



        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
    Set max downloade count
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (mIsDirectShare) {
            execShareAction2(mRedirectedShareLink);
        } else {
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
    }

    /*
    URL unshorten
     */
    public static String getUnshortedURL(String shortedURL) {
        String location = null;
        try {
            final URL url = new URL(shortedURL);
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false); //this is very important
            location = urlConnection.getHeaderField("location");
            Log.d(Global.debugTag,"unshortened url is: " + location);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;

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


    /*
    用于自己创建的pack,自己share。有两种情况下会被调用
    1. 自动被调用：.execute执行后
    2. 手动被调用: 当已经保存了share link（比如上次share过），这时直接调用这个
     */
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


    /*
    直接分享：用于分享别人的，这时不允许设置密码上传等，
     */
    public void execShareAction2(final String finalShareLink) {

        new AlertDialog.Builder(mActivity)
                .setTitle("Share")
                .setItems(new String[] {"Facebook","Twitter","Email","Copy to clipboard"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        shareActionOnItemSelected(which,finalShareLink);
                    }
                })
                .show();
    }


    private void shareActionOnItemSelected (int position,String shareLink) {
        String finalPostString = "I've just created a pack of Flash Cards with the Flash Card Creator! ( " + shareLink +" ) Check it out!";
        switch (position) {
            case 0: {
                shareToFacebook(shareLink);
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
                Timber.d(Global.debugTag, "Email share");
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hi All");
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, finalPostString);
                mActivity.startActivity(Intent.createChooser(emailIntent, "Share via Email"));
                break;
            }
            case 3: {
                Timber.d(Global.debugTag, "Copy");
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


    private void shareToFacebook(final String shareLink) {

        FacebookEvents.addPostListener(postListener);

        final FacebookFacade facebook = new FacebookFacade(mActivity, "430339350417672");
        if (facebook.isAuthorized()) {
            facebook.publishMessage("I've just created a pack of Flash Cards with the Flash Card Creator! ( " + shareLink +" ) Check it out!");
        } else {
            // Start authentication dialog and publish message after successful authentication
            facebook.authorize(new AuthListener() {
                @Override
                public void onAuthSucceed() {
                    facebook.publishMessage("I've just created a pack of Flash Cards with the Flash Card Creator! ( " + shareLink +" ) Check it out!");
                }

                @Override
                public void onAuthFail(String error) { // Do noting
                    showToastOnUIThread("Authorization was failed");
                    FacebookEvents.removePostListener(postListener);
                }
            });
        }

    }


    private PostListener postListener = new PostListener() {
        @Override
        public void onPostPublishingFailed() {
            showToastOnUIThread("Post publishing was failed");
            FacebookEvents.removePostListener(postListener);
        }

        @Override
        public void onPostPublished() {
            showToastOnUIThread("Posted to Facebook successfully");
            FacebookEvents.removePostListener(postListener);
        }
    };


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


    private void showToastOnUIThread(final String text) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
            }
        });
    }


}
