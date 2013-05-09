package com.internectics.helper;

import android.content.Context;
import android.content.SharedPreferences;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

/**
 * All Dropbox AndroidAuthSession and DropboxAPI related
 */
public class DropboxHelper {

    private static Context mContext;
    /**
     * Dropbox key and secret
     */
    final static private String APP_KEY = "rl7510fe1641dyl";
    final static private String APP_SECRET = "3twb9tcccje56kg";
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    /**
     * You don't need to change these, leave them alone.
     */
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    /*
     * Return  DrpboxAPI, which is the main handle in Dropbox operation
     */
    public static DropboxAPI<AndroidAuthSession> getDropboxAPI(Context context) {
        mContext = context;
        AndroidAuthSession session = buildSession();
        DropboxAPI<AndroidAuthSession> mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        return mDBApi;
    }

    /*
     * Unlink Dropbox account
     */
    public static void logOut(Context context) {
        // Remove credentials from the session
        DropboxHelper.getDropboxAPI(context).getSession().unlink();
        // Clear our stored keys
        clearKeys(context);
    }

    private static AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }

    private static String[] getKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    public static void storeKeys(Context context, String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    public static void clearKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
