package com.flipflash.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by BourneWang on 7/10/2015.
 */
public class UnzipAndParsePackHelper extends AsyncTask<Void, Long, Boolean> {

    private static final String TAG = UnzipAndParsePackHelper.class.getName();

    public final static int        UNZIP_SUCCEED  = 0;
    public final static int        UNZIP_FAILED   = 1;

    private Context mContext;
    private  String mZipFileName;
    private  String mPassword;

    private final ProgressDialog mDialog;

    private final Handler mHandler;

    public UnzipAndParsePackHelper(Context context, String zipFileName, String password, @NonNull Handler handler) {
        this.mContext = context;
        this.mZipFileName = zipFileName;
        this.mPassword = password;
        this.mHandler = handler;

        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage("Unzipping and parsing....");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        File outputDirectory = FileOperationHelper.downloadedPackDirectory();
        try {

            //Step1, unzip pack
            ZipFile zipFile = new ZipFile(mZipFileName);

            if (zipFile.isEncrypted()) {
                if ((mPassword == null) || (mPassword.equals(""))) {
                } else {
                    zipFile.setPassword(mPassword);
                }
            }
            zipFile.extractAll(outputDirectory.toString());

            ArrayList<String> zippedCardFileArray = FileOperationHelper.listAllZipCardFilesUnderDirectory(outputDirectory.toString());

            //Step2, unzip cards in the pack
            for (int i = 0; i < zippedCardFileArray.size(); i++) {
                File unzippedDirectory = new File(outputDirectory + File.separator + String.format("card%d", i));
                if (!unzippedDirectory.exists())
                    unzippedDirectory.mkdir();

                zipFile = new ZipFile(zippedCardFileArray.get(i));
                zipFile.extractAll(unzippedDirectory.toString());
            }

            //parse unzipped pack
            PackParserHelper.parse(mContext);

        } catch (ZipException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        mDialog.dismiss();
        if (result) {
            mHandler.obtainMessage(UnzipAndParsePackHelper.UNZIP_SUCCEED, 0, 0, null).sendToTarget();

        } else {
            mHandler.obtainMessage(UnzipAndParsePackHelper.UNZIP_FAILED, 0, 0, null).sendToTarget();
        }
    }
}
