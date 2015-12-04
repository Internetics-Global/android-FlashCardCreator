package com.flipflash.helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.flipflash.android_ffc.R;
import com.flipflash.cryptor.CryptoHelper;
import com.flipflash.helper.AWS.SimpleDBHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;

import net.lingala.zip4j.core.ZipFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/*
 * Download Pack
 */
public class PackDownloadHelper extends AsyncTask<Void, Long, Boolean> {

    private static final String TAG = PackDownloadHelper.class.getName();

    private Context mContext;
    private String mDownloadURL;
    private ProgressDialog mDialog;
    private long mFileLen;
    private String mErrorMsg;
    private String mSavedFilePath;

    private String mDownloadedLinkage;

    private boolean mIsAllowPostExecute = true;

    public boolean mIsFromExamplePackDownload = false;


    public PackDownloadHelper(Context context, String downloadURL, String downloadedZipFile) {
        mContext = context;

        mDownloadedLinkage = downloadURL;

        //Delete previous in case
        File file = FileOperationHelper.downloadedPackDirectory();
        FileOperationHelper.deleteAllFileUnderFolder(file);

        mDownloadURL = downloadURL;
        mSavedFilePath = downloadedZipFile;

        mDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        mDialog.setMax(100);
        if (mIsFromExamplePackDownload) {
            mDialog.setMessage(mContext.getResources().getString(R.string.DIALOG_DOWNLOAD_EXAMPLE_PACK_FIRST));
        } else {
            mDialog.setMessage(mContext.getString(R.string.Title_Downloading));
        }
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton(mContext.getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mIsAllowPostExecute = false;
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            URL url = new URL(mDownloadURL);
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            mFileLen = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(mSavedFilePath);

            byte data[] = new byte[1024 * 16];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress(total);
                output.write(data, 0, count);

                if (!mIsAllowPostExecute) {
                    mErrorMsg = "Download cancelled";
                    output.flush();
                    output.close();
                    input.close();
                    return false;
                }
            }

            output.flush();
            output.close();
            input.close();

            publishProgress(total);

            boolean result =CryptoHelper.decryptFileWithSameOutput(mSavedFilePath);
            if (result == false) {
                mErrorMsg = mContext.getString(R.string.DIALOG_DECRYPT_FAILED);
            }


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGE(TAG, "doInBackground: " + "Download failed:" + e.getCause());
            mErrorMsg = mContext.getString(R.string.DIALOG_DOWNLOAD_FAILED);
        }

        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / mFileLen + 0.5);
        if (percent >= 100) {
            mDialog.setMessage(mContext.getString(R.string.DIALOG_NOW_DECRYPTING));
        } else {
            mDialog.setProgress(percent);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {

        mDialog.dismiss();

        if (result) {

            try {
                ZipFile zipFile = new ZipFile(mSavedFilePath);
                if (zipFile.isEncrypted()) {

                    final EditText passwordEditText = new EditText(mContext);
                    passwordEditText.setSingleLine(true);
                    passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.DIALOG_SET_PASSWORD)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(passwordEditText)
                            .setPositiveButton(R.string.DIALOG_DONE, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    UnzipAndParsePackHelper unzipParsePackHelper = new UnzipAndParsePackHelper(mContext,
                                            mSavedFilePath,passwordEditText.getText().toString(),mUnzipPackHandler);
                                    unzipParsePackHelper.execute();

                                }
                            })
                            .setNegativeButton(R.string.DIALOG_CANCEL, null)
                            .show();

                } else { // no password or the password is empty

                    UnzipAndParsePackHelper unzipParsePackHelper = new UnzipAndParsePackHelper(mContext,
                            mSavedFilePath,"", mUnzipPackHandler);
                    unzipParsePackHelper.execute();

                }



            } catch (Exception e) {
                LOGE(TAG, "onPostExecute: " + "Error:" + e.getCause());
                e.printStackTrace();
            }

        } else {
            Toast.makeText(AppContext.getAppContext(), mErrorMsg, Toast.LENGTH_SHORT).show();
        }

    }

    private void unzipPackTaskFinished() {

        if (mIsFromExamplePackDownload == false) {
            new Thread()
            {
                @Override
                public void run() {
                    updateDownloadLimitCount();
                }
            }.start();

        }

        //Step3: write flag if it's from example pack download
        if (mIsFromExamplePackDownload) {
            AppConfig.sharedInstance().setExamplePackDownloadedFlag();
        }

        //Step4: notify master view to update
        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_PACK_DOWNLOADED);
        mContext.sendBroadcast(intent);
    }

    private static void updateDownloadLimitCount () {
        LOGD(TAG, "updateDownloadLimitCount: Now begin to execute updateDownloadLimitCount");
        final HashMap<String, String> rowData = new HashMap<String, String>();
        rowData.put("currentNo",String.format("%d",Global.currentAmazonSimpleDBItemDownloadCount + 1));

        SimpleDBHelper.updateAttributesForItem(Global.amazon_sdb_domain_name, Global.currentAmazonSimpleDBItemName, rowData);

    }

    //TODO:  lint This Handler class should be static or leaks might occur (null)
    private final Handler mUnzipPackHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            unzipPackTaskFinished();
        }
    };

}
