package com.internectics.helper.Dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

/**
 * All Dropbox AndroidAuthSession and DropboxAPI related
 */
public class DropboxAuthHelper {

    private static final String TAG = DropboxAuthHelper.class.getName();

    private static Context mContext;
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    private static DropboxAPI<AndroidAuthSession> mApi;

    /**
     * You don't need to change these, leave them alone.
     */
    final private String ACCOUNT_PREFS_NAME = "prefs";
    final private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private static final boolean USE_OAUTH1 = false;


    private static DropboxAuthHelper mDropboxAuthHelper;

    public static DropboxAuthHelper sharedHelper(Context context) {
        if (mDropboxAuthHelper == null) {
            mDropboxAuthHelper = new DropboxAuthHelper(context);
        }
        return mDropboxAuthHelper;
    }


    private DropboxAuthHelper(Context context) {
        mContext = context;

        if (mApi == null) {

            // We create a new AuthSession so that we can use the Dropbox API.
            AndroidAuthSession session = buildSession();
            mApi = new DropboxAPI<AndroidAuthSession>(session);
        }

    }

    public DropboxAPI getDropboxAPI() {
        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        return mApi;
    }


    /*
     * Unlink Dropbox account
     */
    public void logOut() {

        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
    }

    public boolean isLinked() {

        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        boolean b = mApi.getSession().isLinked();
        return b;
    }

    public boolean isAuthenticationSuccessful() {
        AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            return true;
        } else {
            return false;
        }
    }

    public void startAuthentication() {

        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        if (USE_OAUTH1) {
            mApi.getSession().startAuthentication(mContext);
        } else {
            mApi.getSession().startOAuth2Authentication(mContext);
        }
    }

    public void finishAuthentication() {
        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        mApi.getSession().finishAuthentication();
    }

    public void storeAuth() {

        if (mApi == null) {
            throw new IllegalStateException("should call getDropboxAPI first");
        }

        storeAuth(mApi.getSession());
    }


    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }



    private void clearKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }


    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(Dropbox_Constant.APP_KEY, Dropbox_Constant.APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }


    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

}
