

package com.flipflash.helper.GoogleDrive;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.flipflash.android_ffc.BuildConfig;
import com.flipflash.android_ffc.R;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import bolts.Task;

import static com.flipflash.util.LogUtils.LOGD;

/*
 * Upload pack
 */
public class GoogleDriveUploadHelper {

    private static final String TAG = GoogleDriveUploadHelper.class.getSimpleName();

    private final Handler        mHandler;

    private String               mFolderNameInGoogleDrive;
    private String               mFolderIDInGoogleDrive;

    private java.io.File         mUploadFile;
    private String               mUploadedFileID = "";

    private Activity             mContext;

    private final ProgressDialog mDialog;

    public GoogleDriveUploadHelper(Activity context, String googleDriveFolderName,
                                   java.io.File file, @NonNull Handler handler) {

        if (file.getName().endsWith(".zip") == false) {
            throw  new IllegalArgumentException("file should be end with .zip");
        }

        mContext = context;
        mFolderNameInGoogleDrive = googleDriveFolderName;

        mUploadFile = file;

        mHandler = handler;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage(mContext.getString(R.string.Indicator_Upload));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);

        //Google APIs does not support cancel operation, see here: https://github.com/google/google-http-java-client/issues/343
        //It's a strongly requested function to be able to cancel, we will implement it when API supports this.
        if (false) {
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

//                             mUploadRequest.abort();
                        } }).start();
                }
            });
        }

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }


    public void execute() {

        mUploadedFileID = "";

        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {

                try {
                    if (checkFolderOfFlipFlashCardsPacksExist()) {
                    } else {
                        createFolderOfFlipFlashCardsPacks();
                    }

                    String existingFileID = checkFileExist();
                    if (StringUtils.isEmpty(existingFileID)) {
                        upload();
                    } else {
                        update(existingFileID);
                    }

                } catch (UserRecoverableAuthIOException e) {
                    e.printStackTrace();
                    mContext.startActivityForResult(e.getIntent(), Global.REQUEST_CODE_GOOGLE_DRIVE_REQUEST_PERMISSION);
                } catch (IOException e) {
                    e.printStackTrace();

                    if (BuildConfig.DEBUG) {
                        handleError("Google Drive service error. Possible reason: You may have built the apk on another MAC, you have to re-create signing-certificate fingerprint");
                    } else {
                        handleError("Google Drive service error.  Please try again.");
                    }


                }

                return "";
            }
        });
    }

    private boolean checkFolderOfFlipFlashCardsPacksExist() throws IOException {

        LOGD(TAG, "checkFolderOfFlipFlashCardsPacksExist");

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        String Q = String.format("mimeType = 'application/vnd.google-apps.folder' and name = '%s' and trashed = false",mFolderNameInGoogleDrive);

        FileList result = driveService.files().list()
                .setQ(Q)
                .setSpaces("drive")
                .execute();
        for(File file: result.getFiles()) {
            System.out.printf("Found file: %s (%s)\n",
                    file.getName(), file.getId());
            mFolderIDInGoogleDrive = file.getId();
            return true;

        }

        return false;

    }

    /*
     * 与Dropbox不同，Google Drive允许同一文件夹下多个同一文件名存在（通过fileID）区分。我们的做法是：
     * 1. 获取第一个具有相同文件名的fileID，然后通过update的方式，而不是通过upload方式（https://developers.google.com/drive/v2/reference/files/update）
     */
    private String checkFileExist() throws IOException {

        LOGD(TAG, "checkFileExist");

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        String expectedFileName = mUploadFile.getName();

        String Q = String.format("mimeType = 'application/zip' and name = '%s' and trashed = false",expectedFileName);

        FileList result = driveService.files().list()
                .setQ(Q)
                .setSpaces("drive")
                .execute();
        for(File file: result.getFiles()) {
            System.out.printf("Found file: %s (%s)\n",
                    file.getName(), file.getId());
            return file.getId();

        }

        return "";


    }

    private void createFolderOfFlipFlashCardsPacks() throws IOException {

        LOGD(TAG, "createFolderOfFlipFlashCardsPacks");

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(mFolderNameInGoogleDrive);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        System.out.println("Folder ID: " + file.getId());
        mFolderIDInGoogleDrive = file.getId();

    }


    /*
     * 当文件存在时，我们通过update方式：http://www.labnol.org/internet/update-files-in-google-drive/28928/
     */
    private void update(String currentFileID) throws IOException {

        LOGD(TAG, "update");

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        File newFile = new File();
        newFile.setMimeType("application/zip");
        newFile.setName(mUploadFile.getName());

        FileContent newContent = new FileContent("application/zip",mUploadFile);

        try {
            Drive.Files.Update request = driveService.files().update(currentFileID,newFile,newContent);
            request.setFields("id");
            MediaHttpUploader uploader = request.getMediaHttpUploader();
            uploader.setProgressListener(mUploadProgressListener);
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
            File file = request.execute(); //block until upload finishes or fails
            if (file != null && file.getId() != null) {
                mUploadedFileID = file.getId();

                makeItPublic();
                String shareLink = getShareLink();

                handleSuccess(shareLink);
            } else {
                handleError("Google Drive service error.  Please try again.");
            }


        } catch (IOException e) {
            e.printStackTrace();

            handleError("Google Drive service error.  Please try again.");
        }

    }

    /*
     * 仅当文件不存在时
     */
    private void upload() {

        LOGD(TAG, "upload");

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(mUploadFile.getName());

        fileMetadata.setParents(Arrays.asList(mFolderIDInGoogleDrive));

        FileContent content = new FileContent("application/zip",mUploadFile);

        try {
            Drive.Files.Create request = driveService.files().create(fileMetadata,content);
            request.setFields("id");
            MediaHttpUploader uploader = request.getMediaHttpUploader();
            uploader.setProgressListener(mUploadProgressListener);
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
            File file = request.execute(); //block until upload finishes or fails
            if (file != null && file.getId() != null) {
                mUploadedFileID = file.getId();

                makeItPublic();
                String shareLink = getShareLink();

                handleSuccess(shareLink);
            } else {
                handleError("Google Drive service error.  Please try again.");
            }


        } catch (IOException e) {
            e.printStackTrace();

            handleError("Google Drive service error.  Please try again.");
        }

    }


    private void makeItPublic() throws IOException {

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        Permission userPermission = new Permission()
                .setType("anyone")
                .setRole("reader");
        driveService.permissions().create(mUploadedFileID, userPermission)
                .setFields("id")
                .execute();

    }

    private String getShareLink() throws IOException {

        Drive driveService = GoogleDriveAuthHelper.sharedHelper(mContext).getDriveService();

        File file = driveService.files().get(mUploadedFileID).setFields("id, webContentLink").execute();

        if (file != null) {
            return file.getWebContentLink();
        } else {
            return "";
        }

    }

    private MediaHttpUploaderProgressListener mUploadProgressListener = new MediaHttpUploaderProgressListener() {
        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    System.out.println("Initiation has started!");
                    break;
                case INITIATION_COMPLETE:
                    System.out.println("Initiation is complete!");
                    break;
                case MEDIA_IN_PROGRESS:

                    final int percent = (int) (100 * uploader.getProgress());
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mDialog.setProgress(percent);
                        }
                    });

                    break;
                case MEDIA_COMPLETE:
                    System.out.println("Upload is complete!");
            }
        }
    };


    public void cancel(boolean mayInterruptIfRunning) {

        //stub, no use

    }

    private void handleError(final String errorMessage) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mDialog.dismiss();

                Toast.makeText(AppContext.getAppContext(), errorMessage, Toast.LENGTH_SHORT).show();

                mHandler.obtainMessage(GoogleDrive_Constant.UPLOAD_FAILED, 0, 0, mUploadFile).sendToTarget();
            }
        });
    }

    private void handleSuccess(final String shareLink) {

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mDialog.dismiss();
                mHandler.obtainMessage(GoogleDrive_Constant.UPLOAD_SUCCEED,0, 0, shareLink).sendToTarget();

            }
        });
    }





}
