package com.internectics.helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import com.internectics.helper.AmazonSDB.SimpleDBHelper;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;
import net.lingala.zip4j.core.ZipFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/*
 * Download Pack
 */
public class PackDownloadHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mDownloadURL;
    private final ProgressDialog mDialog;
    private long mFileLen;
    private String mErrorMsg;
    private String mSavedFilePath;

    private boolean mIsAllowPostExecute = true;

    public boolean mIsFromExamplePackDownload = false;


    public PackDownloadHelper(Context context, String downloadURL, String downloadedZipFile) {
        mContext = context;

        //Delete previous in case
        File file = FileOperationHelper.downloadedPackDirectory();
        FileOperationHelper.deleteAllFileUnderFolder(file);

        mDownloadURL = downloadURL;
        mSavedFilePath = downloadedZipFile;
        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        if (mIsFromExamplePackDownload) {
            mDialog.setMessage("Download sample pack now");
        } else {
            mDialog.setMessage("Downloading...");
        }
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mIsAllowPostExecute = false;
            }
        });
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Global.debugTag,"Download failed:" + e.getCause() );
            mErrorMsg = "Download failed";
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(mContext, "Download pack successfully.\n       Loading...", Toast.LENGTH_SHORT).show();
            try {

                ZipFile zipFile = new ZipFile(mSavedFilePath);
                if (zipFile.isEncrypted()) {

                    final EditText passwordEditText = new EditText(mContext);
                    passwordEditText.setSingleLine(true);
                    passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    new AlertDialog.Builder(mContext)
                            .setTitle("Input a password")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(passwordEditText)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ZipFileHelper.unzipPackFile(mContext, mSavedFilePath,passwordEditText.getText().toString());
                                    parsePackAndGoOn();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                } else { // no password or the password is empty
                    ZipFileHelper.unzipPackFile(mContext, mSavedFilePath, "");
                    parsePackAndGoOn();
                }



            } catch (Exception e) {
                Log.e(Global.debugTag,"Error:", e.getCause());
                e.printStackTrace();
            }

        } else {
            Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
        }

        mDialog.dismiss();
    }

    private void parsePackAndGoOn() {
        //Step2: parse unzipped pack
        PackParserHelper.parse();

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
        Log.d(Global.debugTag, "Now begin to execute updateDownloadLimitCount");
        final HashMap<String, String> rowData = new HashMap<String, String>();
        rowData.put("currentNo",String.format("%d",Global.currentAmazonSimpleDBItemDownloadCount + 1));

        SimpleDBHelper.updateAttributesForItem(Global.amazon_sdb_domain_name, Global.currentAmazonSimpleDBItemName, rowData);

    }

}
