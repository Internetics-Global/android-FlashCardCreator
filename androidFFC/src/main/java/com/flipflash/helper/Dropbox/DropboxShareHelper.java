package com.flipflash.helper.Dropbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.flipflash.android_ffc.R;
import com.flipflash.data.Pack;
import com.flipflash.event.FacebookShareFinishEvent;
import com.flipflash.helper.AWS.SimpleDBHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;
import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.facebook.FacebookEvents;
import com.nostra13.socialsharing.facebook.FacebookFacade;
import com.orhanobut.hawk.Hawk;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static com.flipflash.util.LogUtils.LOGD;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class DropboxShareHelper extends AsyncTask<Void, Long, Boolean> {

    private static final String TAG = DropboxShareHelper.class.getSimpleName();

    private Activity        mActivity;
    private Pack            mCurrentPack;

    private ProgressDialog  mDialog;

    private String mFilePathInDropbox;

    /**
     * true: 没有经过上传，设密码等，直接share (upload逻辑在S3UploadHelper）
     */
    private boolean  mIsDirectShare;



    public DropboxShareHelper(Activity activity, Pack currentPack, String filePathInDropbox,Boolean isDirectShare) {

        if (currentPack == null) {
            throw  new IllegalArgumentException("currentPack should not be null");
        }

        mActivity = activity;
        mCurrentPack = currentPack;
        mIsDirectShare= isDirectShare;

        mFilePathInDropbox = filePathInDropbox;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mDialog = new ProgressDialog(mActivity);
        mDialog.setMax(100);
        mDialog.setMessage(mActivity.getString(R.string.Indicator_Share_Process_Processing));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        //与iOS不同的是，我们这里是不允许cancel的，原因在于：dropbox的share linkage生成，shorted linkage但都是不可中断的
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    /*
        URL shorten
         */
    @Override
    protected Boolean doInBackground(Void... params) {

        if(mIsDirectShare) {
        } else
        {

            //这段逻辑用于解决：当在dropbox和google drive相互切换时

            boolean toSavePackUploadRecord = true;
            boolean toGenerateShareLink = true;
            if (StringUtils.isEmpty(mCurrentPack.shareLink) == false) {
                String currentShareLink = StringUtils.getUnshortedURL(mCurrentPack.shareLink);

                if (currentShareLink != null && currentShareLink.toLowerCase().contains("dropbox.com")) {
                    toGenerateShareLink = false;

                }
            }

            if (toGenerateShareLink) {

                DbxClientV2 mDbxClient = DropboxAuthHelper.getClient();
                try {
                    ListSharedLinksResult resultList= mDbxClient.sharing().listSharedLinksBuilder().withPath(mFilePathInDropbox).start();

                    String shareLink;
                    if (resultList.getLinks() != null && resultList.getLinks().size() > 0) {

                        shareLink = resultList.getLinks().get(0).getUrl();

                    } else {
                        SharedLinkMetadata metaData = mDbxClient.sharing().createSharedLinkWithSettings(mFilePathInDropbox);

                        shareLink = metaData.getUrl();
                    }


                    String undirectedURL = shareLink.replace("https","fcc").replace("http","fcc");
                    LOGD(TAG, "doInBackground: " +  "the fcc share linkage is: " + undirectedURL);
                    mCurrentPack.shareLink = generateRedirectedURL(undirectedURL);

                    if (mCurrentPack.shareLink.indexOf("http://") != 0) {
                        toSavePackUploadRecord = false;
                        Toast.makeText(AppContext.getAppContext(), R.string.DIALOG_REDIRECT_SERVICE_UNAVAILABLE, Toast.LENGTH_LONG).show();
                    } else {

                        mCurrentPack.save(mActivity);

                    }

                } catch (DbxException e) {
                    e.printStackTrace();
                }

            }

            if (toSavePackUploadRecord) {
                //直到我们短链接生成并保存，我们才最终认为upload完成
                //同时为了保证savePackUploadRecord的发生，我们认为无论是isEmpty(mCurrentPack.shareLink)，都需要保存
                PackRecordHelper.savePackUploadRecord(mCurrentPack);
            }
        }

        return false;
    }

    /*
    Set max download count
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mIsDirectShare) {
            share();
        } else {
            //Dialog to show max allowable download time
            final  EditText editText = new EditText(mActivity);
            editText.setGravity(Gravity.CENTER);
            editText.setText("9999");
            editText.setSingleLine();
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.DIALOG_SET_MAX_NUMBER_OF_DOWNLOADS)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(editText)
                    .setNeutralButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            InputMethodManager imm =(InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editText.getWindowToken(),0);

                            int maxNo = Integer.parseInt(editText.getText().toString());
                            didDismissDownloadTimesDialog(maxNo);
                        }
                    })
                    .setNegativeButton(R.string.Keyboard_Unlimited, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            InputMethodManager imm =(InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editText.getWindowToken(),0);

                            didDismissDownloadTimesDialog(9999);
                        }
                    })
                    .setPositiveButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            InputMethodManager imm =(InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editText.getWindowToken(),0);

                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }



    private void didDismissDownloadTimesDialog(int maxNo) {

        final HashMap<String, String> rowData = new HashMap<String, String>();
        rowData.put("currentNo","0");
        rowData.put("maxNo",String.format("%d",maxNo));

        new Thread()
        {
            @Override
            public void run() {

                String simpleDBItemNameData = mCurrentPack.fileNameOnAWS;
                simpleDBItemNameData = simpleDBItemNameData.substring(0, simpleDBItemNameData.indexOf(".zip"));
                Global.currentAmazonSimpleDBItemName = simpleDBItemNameData;

                SimpleDBHelper.updateAttributesForItem(Global.amazon_sdb_domain_name,simpleDBItemNameData,rowData);
            }
        }.start();

        share();
    }


    String generateRedirectedURL(String url){


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
            LOGD(TAG, "generateRedirectedURL: " + "The generated redirected URL from tinyurl.com is not correct:" + responseString);
            responseString = "";
        } else {
            LOGD(TAG, "generateRedirectedURL: " + "The generated redirected URL from tinyurl.com is:" + responseString);
        }

        return responseString;
    }


    /*
    用于自己创建的pack,自己share。有两种情况下会被调用
    1. 自动被调用：.execute执行后
    2. 手动被调用: 当已经保存了share link（比如上次share过），这时直接调用这个
     */
    public void share() {

        if (StringUtils.isEmpty(mCurrentPack.shareLink)) {

            HashMap savedDownloadLinkageDict = Hawk.get("savedDownloadLinkage");
            final String ffcURL = (String) savedDownloadLinkageDict.get(String.format("%d",mCurrentPack.packID));

            if (ffcURL == null) {
                throw new IllegalArgumentException("ffcURL should be saved after download pack");
            }

            ExecutorService taskExecutor = Executors.newCachedThreadPool();
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mCurrentPack.shareLink = generateRedirectedURL(ffcURL); //由于网络任务不允许在主线程
                }
            });
            taskExecutor.shutdown();
            try {
                taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {

            }
        }

        if (mCurrentPack.shareLink != null) {
            showShareSocialListAlert();
        } else {
            Toast.makeText(AppContext.getAppContext(), R.string.DIALOG_REDIRECT_SERVICE_UNAVAILABLE, Toast.LENGTH_LONG).show();
        }


    }

    public void showShareSocialListAlert() {
        new AlertDialog.Builder(mActivity)
                .setTitle("Share")
                .setItems(new String[] {"Facebook","Twitter","Email","Copy","Exit"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which != 4) {
                            shareActionOnItemSelected(which,mCurrentPack.shareLink);
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }


    private void shareActionOnItemSelected (int position,String shareLink) {
        String finalPostString = StringUtils.getShareMessage(mActivity,shareLink);
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
                        Global.showActionListAgain = true;
                    }
                }  else {
                    Toast.makeText(AppContext.getAppContext(), R.string.DIALOG_NO_TWITTER_CLIENT_INSTALLED, Toast.LENGTH_LONG).show();
                }

                break;
            }
            case 2: {
                LOGD(TAG, "shareActionOnItemSelected: Email share");
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hi All");
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_TEXT, finalPostString);
                mActivity.startActivity(Intent.createChooser(emailIntent, "Share via Email"));
                Global.showActionListAgain = true;
                break;
            }
            case 3: {
                LOGD(TAG, "shareActionOnItemSelected: copy");
                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Service.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText( "share linkage",shareLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AppContext.getAppContext(), mActivity.getString(R.string.DIALOG_COPY_DONE), Toast.LENGTH_LONG).show();
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
            String message = StringUtils.getShareMessage(mActivity,shareLink);
            facebook.publishMessage(message);
        } else {
            // Start authentication dialog and publish message after successful authentication
            facebook.authorize(new AuthListener() {
                @Override
                public void onAuthSucceed() {
                    String message = StringUtils.getShareMessage(mActivity,shareLink);
                    facebook.publishMessage(message);
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
            EventBus.getDefault().post(new FacebookShareFinishEvent());
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
                Toast.makeText(AppContext.getAppContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }


}
