package com.flipflash.helper.GoogleDrive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.flipflash.util.Global;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

import static com.flipflash.util.LogUtils.LOGD;


public class GoogleDriveAuthHelper {

    private static final String TAG = GoogleDriveAuthHelper.class.getSimpleName();

    private static GoogleDriveAuthHelper       mGoogleDriveAuthHelper;

    private        Activity                    mActivity;

    private        GoogleAccountCredential     mCredential;

    private        Drive                       mDrive;

    private static final String PREF_ACCOUNT_NAME = "Google_Account_Name";

    // This is extra flag to check log in status. Background: in practice, we found it could be by mistake indicated log-in
    private static final String PREF_ACCOUNT_LOG_IN_EXTRA_FLAG = "Google_Account_Log_Out_Flag";


    public static GoogleDriveAuthHelper sharedHelper(Activity activity) {

        if (mGoogleDriveAuthHelper == null) {
            mGoogleDriveAuthHelper = new GoogleDriveAuthHelper(activity);

        }

        return mGoogleDriveAuthHelper;
    }


    private GoogleDriveAuthHelper(Activity activity) {

        mActivity = activity;

        if (mCredential == null) {
            setup();
        }


    }

    private void setup() {
        mCredential =
                GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(DriveScopes.DRIVE));
        loadCredential();
        if (isLinked()) {
            mDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), mCredential).build();
        }
    }

    public Drive getDriveService() {
        return mDrive;
    }


    /*
     * Unlink Google Drive account.
     * It's only disconnected with FFC app, and does not affect the status in system settings -> Account
     */
    public void logOut() {

        LOGD(TAG, "logOut");

            mCredential = null;

            removeCredential();

    }

    public boolean isLinked() {

        if (mCredential == null) {
            return false;
        }

        if (getLogInExtraFlag() == false) {
            return false;
        }


        if (mCredential.getSelectedAccountName() == null) {
            return false;
        } else {
            return true;
        }
    }


    /*
     * mean authorization process is finished
     */
    public void finishAuthentication(String accountName) {

        LOGD(TAG, "finishAuthentication with accountName = " + accountName);

        if (mCredential == null) {
            throw new IllegalStateException("mCredential should not be null");
        }

        mCredential.setSelectedAccountName(accountName);
        saveCredential(accountName);

        mDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), mCredential).build();
    }


    public  void startAuthenticationFromActivity(Activity activity) {

        if (mCredential == null) {
            setup();
        }

        LOGD(TAG, "startAuthenticationFromActivity");

        activity.startActivityForResult(mCredential.newChooseAccountIntent(), Global.REQUEST_CODE_GOOGLE_ACCOUNT_PICKER);
    }


    private void saveCredential(String accountName) {
        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.putBoolean(PREF_ACCOUNT_LOG_IN_EXTRA_FLAG,true);
        editor.commit();
    }

    private void loadCredential() {

        if (mCredential == null) {
            throw new IllegalStateException("mCredential should not be null");
        }

        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        String accountName = settings.getString(PREF_ACCOUNT_NAME, null);

        mCredential.setSelectedAccountName(accountName);

        //这里需要注意一个case: accountName（保存于SharedPreference)不为null，但是这时用户已经提前在系统设置中退出了accountName这个帐号，这时，即便执行了
        //mCredential.setSelectedAccountName(accountName), mCredential.getSelectedAccountName()仍然得到的将是null。
        //也就是说，mCredential.getSelectedAccountName返回的是真实的已经auth的信息


    }


    private boolean getLogInExtraFlag() {
        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        boolean result = settings.getBoolean(PREF_ACCOUNT_LOG_IN_EXTRA_FLAG, false);
        return result;
    }

    private void removeCredential() {

        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(PREF_ACCOUNT_NAME);
        editor.remove(PREF_ACCOUNT_LOG_IN_EXTRA_FLAG);
        editor.commit();
    }



    public static void cleanup() {
        mGoogleDriveAuthHelper = null;
    }

}
