package com.internectics.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

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

                    return false;
                }
            }

            output.flush();
            output.close();
            input.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
        mDialog.dismiss();
        if (result) {
            Toast.makeText(mContext, "Pack successfully downloaded", Toast.LENGTH_SHORT).show();
            File outputDirectory = FileOperationHelper.downloadedPackDirectory();
            try {
                //Step1: unzip
                ZipFileHelper.unzipPackFile(mSavedFilePath, outputDirectory.toString());

                //Step2: parse unzipped pack
                PackParserHelper.parse();

                //Step3: write flag if it's from example pack download
                if (mIsFromExamplePackDownload) {
                    AppConfig.sharedInstance().setExamplePackDownloadedFlag();
                }

                //Step4: notify master view to update
                Intent intent = new Intent();
                intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_PACK_DOWNLOADED);
                mContext.sendBroadcast(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
        }
    }
}
