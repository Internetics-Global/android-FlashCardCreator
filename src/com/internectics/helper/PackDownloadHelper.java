package com.internectics.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class PackDownloadHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mDownloadURL;
    private final ProgressDialog mDialog;
    private long mFileLen;
    private String mErrorMsg;


    public PackDownloadHelper(Context context, String downloadURL) {
        mContext = context;
        mDownloadURL = downloadURL;
        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Uploading ");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
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
            OutputStream output = new FileOutputStream("/sdcard/file_name.zip");

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress(total);
                output.write(data, 0, count);
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
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            Toast.makeText(mContext, "Pack successfully downloaded", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
        }
    }
}
