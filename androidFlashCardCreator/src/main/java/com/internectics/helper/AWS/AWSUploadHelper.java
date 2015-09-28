package com.internectics.helper.AWS;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.internectics.util.AppContext;

import java.io.File;



/**
 * Created by BourneWang on 28/05/15.
 */
public class AWSUploadHelper {

    private TransferManager   mTransferManager;
    private final Handler     mHandler;
    private UploadThread      mUploadThread;

    public AWSUploadHelper(Context context, @NonNull Handler handler) {
        mTransferManager = new TransferManager(AppContext.getCredentialsProvider());
        mHandler     = handler;
    }

    public void upload(@NonNull File file) {
        if (mUploadThread != null) {
            mUploadThread.cancel();
            mUploadThread = null;
        }

        mUploadThread = new UploadThread(file);
        mUploadThread.start();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {

        if (mUploadThread != null) {
            mUploadThread.cancel();
            mUploadThread = null;
        }
    }


    private class UploadThread extends Thread {
        private ProgressListener mListener;
        private File             mFile;
        private Upload           mUpload;

        public UploadThread(final File file) {
            mFile = file;

            if (file.getName().endsWith(".zip") == false) {
              throw  new IllegalArgumentException("file should be end with .zip");
            }


            mListener = new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent progressEvent) {

                    if (progressEvent.getEventCode() != com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE) {
                        int percent = (int)(mUpload.getProgress().getPercentTransferred());
                        mHandler.obtainMessage(AWS_Constant.UPLOAD_PROGRESS, -1,percent, file).sendToTarget();
                    } else {
                        mHandler.obtainMessage(AWS_Constant.UPLOAD_PROGRESS, 0, 100, file).sendToTarget();
                    }
                }
            };
        }

        public void cancel() {
            if (mUpload != null) {
                mUpload.removeProgressListener(mListener);
                mUpload.abort();
            }

        }

        @Override
        public void run() {
            super.run();

            if (mFile != null) {
                try {
                    mUpload = mTransferManager.upload(
                            AWS_Constant.S3_BUCKET_NAME.toLowerCase(),
                            mFile.getName(),   //mFile.getName include extension
                            mFile);
                    mUpload.addProgressListener(mListener);
                } catch (Exception e) {
                    Log.e("ccaa", "", e);
                }
            }
        }
    }
}
