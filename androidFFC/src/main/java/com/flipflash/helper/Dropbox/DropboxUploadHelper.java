

package com.flipflash.helper.Dropbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.dropbox.core.DbxException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadSessionAppendV2Uploader;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.flipflash.android_ffc.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.flipflash.util.LogUtils.LOGD;

/*
 * Upload pack
 */
public class DropboxUploadHelper extends AsyncTask<String, Void, FileMetadata> {

    private static final String TAG = DropboxUploadHelper.class.getSimpleName();

    private final Handler        mHandler;

    private       Activity       mActivity;
    private final ProgressDialog mDialog;

    private File   mFile;
    private String mFilePathInDropbox;

    private Exception mException;

    private boolean   mAbort = false;

    public DropboxUploadHelper(Activity activity, String dropboxPath,
                               File file, @NonNull Handler handler) {
        if (file.getName().endsWith(".zip") == false) {
            throw  new IllegalArgumentException("file should be end with .zip");
        }

        mActivity = activity;
        mFile = file;
        mHandler = handler;
        mFilePathInDropbox = dropboxPath + file.getName();

        mDialog = new ProgressDialog(activity);
        mDialog.setMax(100);
        mDialog.setMessage(mActivity.getString(R.string.Indicator_Upload));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mActivity.getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mAbort = true;
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected FileMetadata doInBackground(String... params) {

        final long size = mFile.length();

        if (size < CHUNKED_UPLOAD_CHUNK_SIZE *2) {

            //no abort and progress show if using this way.
            //however, we have to keep it here since there's a bug in Dropbox API if file size is too small: https://github.com/dropbox/dropbox-sdk-java/issues/88

            try {

                InputStream inputStream = new FileInputStream(mFile);

                DbxClientV2    dbxClient = DropboxAuthHelper.getClient();

                return dbxClient.files().uploadBuilder(mFilePathInDropbox)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mException = e;
            } catch (DbxException e) {
                e.printStackTrace();
                mException = e;
            } catch (IOException e) {
                e.printStackTrace();
                mException = e;
            }

        } else {

            DbxClientV2    dbxClient = DropboxAuthHelper.getClient();
            return chunkedUploadFile(dbxClient,mFile,mFilePathInDropbox);

        }

        return null;
    }

    // Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
    // result in fewer network requests, which will be faster. But if an error occurs, the entire
    // chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
    private static final long                CHUNKED_UPLOAD_CHUNK_SIZE = 256*1024; //512
    private static final int                 CHUNKED_UPLOAD_MAX_ATTEMPTS = 1;

    private FileMetadata chunkedUploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {

        mAbort = false;

        final long size = localFile.length();
        long uploaded = 0L;
        DbxException thrown = null;

        // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
        //
        //    (1)  Start: initiate the upload and get an upload session ID
        //    (2) Append: upload chunks of the file to append to our session
        //    (3) Finish: commit the upload and close the session
        //
        // We track how many bytes we uploaded to determine which phase we should be in.
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
            if (i > 0) {
                System.out.printf("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS);
            }

            try {

                InputStream in = new FileInputStream(localFile);

                // if this is a retry, make sure seek to the correct offset
                in.skip(uploaded);

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart()
                            .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE)
                            .getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                }

                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {

                    if (mAbort) {
                        return null;
                    }

                    dbxClient.files().uploadSessionAppendV2(cursor).uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    cursor = new UploadSessionCursor(sessionId, uploaded);

                    System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
                    final int percent = (int) (uploaded/(float)size * 100);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.setProgress(percent);
                        }
                    });

                }

                // (3) Finish
                long remaining = size - uploaded;

                CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath)
                        .withMode(WriteMode.OVERWRITE)
                        .build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
                        .uploadAndFinish(in, remaining);

                System.out.println(metadata.toStringMultiline());
                return metadata;

            } catch (RetryException ex) {
                thrown = ex;
                mException = ex;
                // RetryExceptions are never automatically retried by the client for uploads. Must
                // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                sleepQuietly(ex.getBackoffMillis());
                continue;
            } catch (NetworkIOException ex) {
                thrown = ex;
                mException = ex;
                // network issue with Dropbox (maybe a timeout?) try again
                continue;
            } catch (UploadSessionLookupErrorException ex) {
                mException = ex;
                if (ex.errorValue.isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                } else {
                    // Some other error occurred, give up.
                    System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                    return null;
                }
            } catch (UploadSessionFinishErrorException ex) {
                mException = ex;
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                            .getLookupFailedValue()
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                } else {
                    // some other error occurred, give up.
                    System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                    return null;
                }
            } catch (DbxException ex) {
                mException = ex;
                System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                return null;
            } catch (IOException ex) {
                mException = ex;
                System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
                return null;
            }
        }

        // if we made it here, then we must have run out of attempts
        System.err.println("Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());

        return null;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // just exit
            System.err.println("Error uploading to Dropbox: interrupted during backoff.");
            System.exit(1);
        }
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        mDialog.dismiss();

        if (mException != null) {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_FAILED, 0, 0, mException).sendToTarget();
        } else if (result == null) {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_FAILED, 0, 0, mFilePathInDropbox).sendToTarget();
        } else {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_SUCCEED, 0, 0, mFilePathInDropbox).sendToTarget();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        // this part of logic is in: // (2) Append

        LOGD(TAG, "onProgressUpdate");
    }
}
