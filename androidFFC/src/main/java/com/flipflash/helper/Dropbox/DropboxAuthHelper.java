package com.flipflash.helper.Dropbox;

import android.app.Activity;
import android.content.SharedPreferences;


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.flipflash.util.AppContext;
import com.flipflash.util.StringUtils;

import java.util.concurrent.Callable;

import bolts.Task;

import static android.content.Context.MODE_PRIVATE;

/**
 * All Dropbox AndroidAuthSession and DropboxAPI related
 */
public class DropboxAuthHelper {

    private static final String TAG = DropboxAuthHelper.class.getSimpleName();

    private static DbxClientV2 sDbxClient;

    final private String PREP_FILE_NAME = "ffc_dropbox_auth";
    final private String PREP_TOKEN_KEY = "access-token";

    private boolean      isAuthenticationInProgress = false;


    private static DropboxAuthHelper mDropboxAuthHelper;

    /*
     * context没用
     */
    public static DropboxAuthHelper sharedHelper() {

        if (mDropboxAuthHelper == null) {
            mDropboxAuthHelper = new DropboxAuthHelper();
        }
        return mDropboxAuthHelper;
    }


    private DropboxAuthHelper() {

        loadAuth();

    }


    /*
     * Unlink Dropbox account
     */
    public void logOut() {

        clearAuth();

        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {

                if (sDbxClient != null) {
                    try {
                        sDbxClient.auth().tokenRevoke();
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        });
    }

    public boolean isLinked() {

        return isHasToken();
    }


    public void startAuthenticationFromActivity(Activity activity){

        Auth.startOAuth2Authentication(activity, Dropbox_Constant.APP_KEY);
        isAuthenticationInProgress = true;
    }

    public void finishAuthentication() {

        isAuthenticationInProgress = false;

        storeAuth();

        String token = getToken();
        initAndLoadData(token);
    }


    private String getToken() {
        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(PREP_FILE_NAME, MODE_PRIVATE);
        String accessToken = prefs.getString(PREP_TOKEN_KEY, null);
        return accessToken;
    }

    private boolean isHasToken() {
        String token = getToken();
        if (StringUtils.isEmpty(token)) {
            return false;
        } else {
            return true;
        }
    }

    private void clearAuth() {
        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(PREP_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(PREP_TOKEN_KEY);
        edit.commit();
    }


    public void storeAuth() {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(PREP_FILE_NAME, MODE_PRIVATE);
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            prefs.edit().putString(PREP_TOKEN_KEY, accessToken).commit();
        }
    }



    private void loadAuth() {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(PREP_FILE_NAME, MODE_PRIVATE);
        String accessToken = prefs.getString(PREP_TOKEN_KEY, null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(PREP_TOKEN_KEY, accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }

    }

    private void initAndLoadData(String accessToken) {

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("ffc_dropbox_v2")
                .withHttpRequestor(OkHttp3Requestor.INSTANCE)
                .build();

        sDbxClient = new DbxClientV2(requestConfig, accessToken);

        loadData();
    }

    private void loadData() {

    }

    public static void cleanup() {
        mDropboxAuthHelper = null;
    }


    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }

    public boolean isAuthenticationInProgress() {
        return isAuthenticationInProgress;
    }
}
