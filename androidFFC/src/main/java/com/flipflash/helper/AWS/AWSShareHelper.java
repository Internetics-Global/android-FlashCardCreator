package com.flipflash.helper.AWS;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.flipflash.android_ffc.R;
import com.flipflash.data.Pack;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;

import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.facebook.FacebookEvents;
import com.nostra13.socialsharing.facebook.FacebookFacade;

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

import static com.flipflash.util.LogUtils.LOGD;


public class AWSShareHelper extends AsyncTask<Void, Long, Boolean> {

    private static final String TAG = AWSShareHelper.class.getName();

    private Activity   mActivity;
    private Pack       mCurrentPack;

    /**
     * true: 没有经过上传，设密码等，直接share (upload逻辑在S3UploadHelper）
     */
    private boolean    mIsDirectShare;


    public AWSShareHelper(Activity activity, Pack currentPack, Boolean isDirectShare) {

        if (currentPack == null || StringUtils.isEmpty(currentPack.fileNameOnAWS)) {
            throw  new IllegalArgumentException("currentPack.fileNameOnAWS should be set before");
        }

        mActivity         = activity;
        mCurrentPack      = currentPack;
        mIsDirectShare    = isDirectShare;
    }


    @Override
    protected Boolean doInBackground(Void... params) {


        String fullPath_S3 = AWSUtils.fullPath_On_S3(mCurrentPack);
        if (fullPath_S3 == null) {
            //upload的动作一定在share前面，一旦upload后，会写入full path到meta info，所以这里的fullPath_S3一定有值
            throw  new IllegalArgumentException("check code, make sure to upload firstly before calling AWSShareHelper");
        }

        if(mIsDirectShare == false) {

            if (StringUtils.isEmpty(mCurrentPack.shareLink)) {
                mCurrentPack.shareLink = generateRedirectedURL(fullPath_S3);

                if (mCurrentPack.shareLink.indexOf("http://") != 0) {
                    Toast.makeText(AppContext.getAppContext(), R.string.DIALOG_REDIRECT_SERVICE_UNAVAILABLE, Toast.LENGTH_LONG).show();
                } else {
                    mCurrentPack.save(mActivity);

                    //直到我们短链接生成并保存，我们才最终认为upload完成
                    //同时为了保证savePackUploadRecord的发生，我们认为无论是isEmpty(mCurrentPack.shareLink)，都需要保存
                    PackRecordHelper.savePackUploadRecord(mCurrentPack);

                }
            } else {
            }

            //直到我们短链接生成并保存，我们才最终认为upload完成
            //同时为了保证savePackUploadRecord的发生，我们认为无论是isEmpty(mCurrentPack.shareLink)，都需要保存
            PackRecordHelper.savePackUploadRecord(mCurrentPack);


        } else {

        }

        return false;
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

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
                    .setPositiveButton(R.string.DIALOG_OK, new DialogInterface.OnClickListener() {
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


    String generateRedirectedURL(String path){

        String responseString= "";
        String fccPath = path.replace("https:","fcc:");
        String wholeURL = Global.URL_REDIRECT_API + fccPath;

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



    public void share() {

        new AlertDialog.Builder(mActivity)
                .setTitle("Share")
                .setItems(new String[] {"Facebook","Twitter","Email",mActivity.getString(R.string.Title_Copy_To_Clipboard)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String shareLink = mCurrentPack.shareLink;
                        shareActionOnItemSelected(which,shareLink);
                    }
                })
                .show();
    }



    private void shareActionOnItemSelected (int position,String shareLink) {
        String finalPostString = "I've just created a pack of Flash Cards with Flip Flash Cards app! ( " + shareLink +" ) Check it out! Get the Flip Flash Cards app http://www.apple.com";
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
                    Toast.makeText(AppContext.getAppContext(), R.string.DIALOG_NO_TWITTER_CLIENT_INSTALLED, Toast.LENGTH_LONG).show();
                }

                break;
            }
            case 2: {
                LOGD(TAG, "shareActionOnItemSelected: Email share");
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hi All");
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, finalPostString);
                mActivity.startActivity(Intent.createChooser(emailIntent, "Share via Email"));
                break;
            }
            case 3: {
                LOGD(TAG, "shareActionOnItemSelected: copy");
                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Service.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText( "share linkage",shareLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(AppContext.getAppContext(), R.string.Title_Copy_To_Clipboard, Toast.LENGTH_LONG).show();
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

        FacebookEvents.addPostListener(facebookPostListener);

        final FacebookFacade facebook = new FacebookFacade(mActivity, "430339350417672");
        if (facebook.isAuthorized()) {
            facebook.publishMessage("I've just created a pack of Flash Cards with Flip Flash Cards app! ( " + shareLink +" ) Check it out! Get the Flip Flash Cards app http://www.apple.com");
        } else {
            // Start authentication dialog and publish message after successful authentication
            facebook.authorize(new AuthListener() {
                @Override
                public void onAuthSucceed() {
                    facebook.publishMessage("I've just created a pack of Flash Cards with Flip Flash Cards app! ( " + shareLink +" ) Check it out! Get the Flip Flash Cards app http://www.apple.com");
                }

                @Override
                public void onAuthFail(String error) { // Do noting
                    showToastOnUIThread("Authorization was failed");
                    FacebookEvents.removePostListener(facebookPostListener);
                }
            });
        }

    }


    private PostListener facebookPostListener = new PostListener() {
        @Override
        public void onPostPublishingFailed() {
            showToastOnUIThread("Post publishing was failed");
            FacebookEvents.removePostListener(facebookPostListener);
        }

        @Override
        public void onPostPublished() {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
                    alertDialog.setTitle(mActivity.getString(R.string.DIALOG_AlERT));
                    alertDialog.setMessage(mActivity.getString(R.string.DIALOG_POST_FACEBOOK_SUCCESSFULLY));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mActivity.getString(R.string.DIALOG_OK),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });


            FacebookEvents.removePostListener(facebookPostListener);
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
