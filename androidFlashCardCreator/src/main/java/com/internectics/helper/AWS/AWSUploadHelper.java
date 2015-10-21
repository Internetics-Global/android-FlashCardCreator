package com.internectics.helper.AWS;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.internectics.android_flashcardcreator.R;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.internectics.util.LogUtils.LOGE;

/**
 * Created by BourneWang on 28/05/15.
 */
public class AWSUploadHelper {

    private static final String TAG = AWSUploadHelper.class.getName();

    private TransferManager   mTransferManager;
    private final Handler     mHandler;
    private UploadThread      mUploadThread;

    private Context           mContext;

    public AWSUploadHelper(Context context, @NonNull Handler handler) {
        mTransferManager = new TransferManager(AppContext.getCredentialsProvider());
        mHandler     = handler;
        mContext = context;
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

            if (ParseUser.getCurrentUser() == null) {
                throw  new AssertionError("ParseUser.getCurrentUser() should be set before being here");
            }

            AmazonS3 s3client = AppContext.getS3Client();

            String expectedBucketName = ParseUser.getCurrentUser().getUsername().toLowerCase(); //bucket name必须是low case的，这是aws要求的

            //AWS对于bucket是有命名要求的：http://docs.rightscale.com/faq/clouds/aws/What_are_valid_S3_bucket_names.html
            expectedBucketName = String.format("%s-%s",expectedBucketName,Global.BucketPostfixAfterUserName);

            boolean succeeded = true;
            try {
                boolean existing = false;

                if (true) {
                    //一般来说返回true，表明在AWS全局范围内存在（但并不表明在当前账号下存在)；
                    // 但是在本应用中，由于bucket的命名的特殊性（见上面），可以说如果在全局范围内存在，也必定在当前账号下存在
                   existing = s3client.doesBucketExist(expectedBucketName);
                } else {
                    List<Bucket> list = s3client.listBuckets();
                    for (Bucket item:list) {
                        if (item.getName().equals(expectedBucketName)) {
                            existing = true;
                            break;
                        }
                    }
                }

                if (existing == false) {

                    CreateBucketRequest createBucketRequest = new CreateBucketRequest(expectedBucketName);
                    s3client.createBucket(createBucketRequest);

                }
            } catch (AmazonServiceException ase) {
                succeeded = false;
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                succeeded = false;
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }

            if (succeeded == false) {
                new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(mContext.getString(R.string.DIALOG_ERROR))
                    .setContentText(mContext.getString(R.string.DIALOG_FAILURE_TO_CREATE_BUCKET))
                    .show();
                return;
            }


            if (mFile != null) {
                try {
                    PutObjectRequest putObjectRequest = new PutObjectRequest(expectedBucketName.toLowerCase(),mFile.getName(),mFile);
                    putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
                    mUpload = mTransferManager.upload(putObjectRequest);

                    mUpload.addProgressListener(mListener);
                } catch (Exception e) {
                    LOGE(TAG, "", e);
                }
            }
        }
    }
}
