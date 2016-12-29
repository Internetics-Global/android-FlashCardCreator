

package com.flipflash.helper.Dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
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

    private       Context        mContext;
    private final ProgressDialog mDialog;

    private File   mFile;
    private String mFilePathInDropbox;

    private Exception mException;

    public DropboxUploadHelper(Context context, String dropboxPath,
                               File file, @NonNull Handler handler) {
        if (file.getName().endsWith(".zip") == false) {
            throw  new IllegalArgumentException("file should be end with .zip");
        }

        mContext = context;
        mFile = file;
        mHandler = handler;
        mFilePathInDropbox = dropboxPath + file.getName();

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage(mContext.getString(R.string.Indicator_Upload));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //todo: there's no way to cancel a upload a request yet. https://github.com/dropbox/dropbox-sdk-java/issues/87
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected FileMetadata doInBackground(String... params) {

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

        return null;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        mDialog.dismiss();
        if (mException != null) {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_FAILED, 0, 0, mFilePathInDropbox).sendToTarget();
        } else if (result == null) {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_FAILED, 0, 0, mFilePathInDropbox).sendToTarget();
        } else {
            mHandler.obtainMessage(Dropbox_Constant.UPLOAD_SUCCEED, 0, 0, mFilePathInDropbox).sendToTarget();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        //Todo: Dropbox API does not support to show progress yet: https://github.com/dropbox/dropbox-sdk-java/issues/66

        LOGD(TAG, "onProgressUpdate");
    }
}
