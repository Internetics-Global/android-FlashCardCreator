

package com.internectics.helper.Dropbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.internectics.android_flashcardcreator.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * Upload pack
 */
public class DropboxUploadHelper extends AsyncTask<Void, Long, Boolean> {

    private final Handler     mHandler;

    private DropboxAPI<AndroidAuthSession> mApi;
    private File mFile;
    private String mFilePathInDropbox;

    private long mFileLen;
    private Context mContext;
    private final ProgressDialog mDialog;

    private String mErrorMsg;

    public DropboxUploadHelper(Context context, String dropboxPath,
                               File file,@NonNull Handler handler) {

        if (file.getName().endsWith(".zip") == false) {
            throw  new IllegalArgumentException("file should be end with .zip");
        }

        mContext = context;

        mApi = DropboxAuthHelper.sharedHelper(context).getDropboxAPI();


        mFileLen = file.length();
        mFile = file;
        mFilePathInDropbox = dropboxPath + file.getName();
        mHandler = handler;


        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage(mContext.getString(R.string.Indicator_Upload));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            FileInputStream fis = new FileInputStream(mFile);
            UploadRequest request = mApi.putFileOverwriteRequest(mFilePathInDropbox, fis, mFile.length(),
                    new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            // Update the progress bar every half-second or so
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });

            if (request != null) {
                request.upload();
                return true;
            }


        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_SUCCEED, 0, 0, mFile).sendToTarget();

        } else {
            Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();

            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_FAILED, 0, 0, mFile).sendToTarget();
        }
    }
}
