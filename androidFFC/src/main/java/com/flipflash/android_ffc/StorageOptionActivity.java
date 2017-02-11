package com.flipflash.android_ffc;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.flipflash.helper.Dropbox.DropboxAuthHelper;
import com.flipflash.helper.GoogleDrive.GoogleDriveAuthHelper;
import com.flipflash.util.Global;
import com.google.firebase.auth.FirebaseAuth;


import static com.flipflash.util.LogUtils.LOGD;

/**
 * Created by BourneWang on 5/12/2015.
 */
public class StorageOptionActivity extends Activity{

    Button mUseDropboxButton;
    Button mUseGoogleDriveButton;
    Button mUseAWSButton;

    private static final String TAG = PlayActivity.class.getSimpleName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate:");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.storage_option_setting);

        setTitle(getString(R.string.DIALOG_STORAGE_SELECTION));


        mUseDropboxButton = (Button) findViewById(R.id.storage_option_dropbox_button);
        mUseGoogleDriveButton = (Button) findViewById(R.id.storage_option_google_drive_button);
        mUseAWSButton = (Button) findViewById(R.id.storage_option_aws_button);

        findViewById(R.id.rl_storage_option_google_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUseGoogleDriveButton.getVisibility() != View.VISIBLE) {
                    DropboxAuthHelper.sharedHelper().logOut();
                    FirebaseAuth.getInstance().signOut();
                    GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).startAuthenticationFromActivity(StorageOptionActivity.this);
                } else {
                    GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).logOut();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            StorageOptionActivity.this);
                    alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                    alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                    alertDialogBuilder
                            .setMessage(R.string.DIALOG_GOOGLE_DRIVE_DISCONNECTED).show();
                }

                mUseGoogleDriveButton.setVisibility(View.INVISIBLE);

            }
        });


        findViewById(R.id.rl_storage_option_dropbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUseDropboxButton.getVisibility() != View.VISIBLE) {
                    GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).logOut();
                    FirebaseAuth.getInstance().signOut();
                    DropboxAuthHelper.sharedHelper().startAuthenticationFromActivity(StorageOptionActivity.this);
                } else {
                    DropboxAuthHelper.sharedHelper().logOut();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            StorageOptionActivity.this);
                    alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                    alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                    alertDialogBuilder
                            .setMessage(R.string.DIALOG_DROPBOX_DISCONNECTED).show();

                    mUseDropboxButton.setVisibility(View.INVISIBLE);
                }

            }
        });

        findViewById(R.id.rl_storage_option_aws).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUseAWSButton.getVisibility() != View.VISIBLE) {
                    DropboxAuthHelper.sharedHelper().logOut();
                    GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).logOut();
                    startActivity(new Intent(StorageOptionActivity.this, FirebaseSignInActivity.class));
                } else {
                    FirebaseAuth.getInstance().signOut();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            StorageOptionActivity.this);
                    alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                    alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                    alertDialogBuilder
                            .setMessage(R.string.DIALOG_AWS_DISCONNECTED).show();

                    mUseAWSButton.setVisibility(View.INVISIBLE);
                }

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (DropboxAuthHelper.sharedHelper().isAuthenticationInProgress()) {
            boolean success = DropboxAuthHelper.sharedHelper().finishAuthentication();

            if (success) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        StorageOptionActivity.this);
                alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                alertDialogBuilder
                        .setMessage(R.string.DIALOG_SUCCESS_TO_LOG_DROPBOX).show();
            }

        }

        if (DropboxAuthHelper.sharedHelper().isLinked()) {

            mUseDropboxButton.setVisibility(View.VISIBLE);

        } else {
            mUseDropboxButton.setVisibility(View.INVISIBLE);
        }

        if (GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).isLinked()) {

            mUseGoogleDriveButton.setVisibility(View.VISIBLE);

        } else {
            mUseGoogleDriveButton.setVisibility(View.INVISIBLE);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            mUseAWSButton.setVisibility(View.VISIBLE);

        } else {
            mUseAWSButton.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGD(TAG, "onActivityResult with request code = " + requestCode);

        if (requestCode == Global.REQUEST_CODE_GOOGLE_ACCOUNT_PICKER) {

            if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    GoogleDriveAuthHelper.sharedHelper(StorageOptionActivity.this).finishAuthentication(accountName);
                    mUseGoogleDriveButton.setVisibility(View.VISIBLE);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            StorageOptionActivity.this);
                    alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                    alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                    alertDialogBuilder
                            .setMessage(R.string.DIALOG_GOOGLE_DRIVE_LOGIN_SUCCESS).show();


                }
            }

        } else {

        }

    }

}