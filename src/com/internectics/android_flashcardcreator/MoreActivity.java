package com.internectics.android_flashcardcreator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowManager;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.internectics.helper.DropboxHelper;
import com.internectics.util.AppConfig;

public class MoreActivity extends PreferenceActivity {

    private boolean mIsGoingAuthorization = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addPreferencesFromResource(R.xml.more);

        final CheckBoxPreference dropboxPreference = (CheckBoxPreference) findPreference("dropbox_preference");
        final CheckBoxPreference playPreference = (CheckBoxPreference) findPreference("play_preference");
        PreferenceScreen registerPreference = (PreferenceScreen) findPreference("register_preference");
        PreferenceScreen submitPreference = (PreferenceScreen) findPreference("submit_preference");
        PreferenceScreen helpPreference = (PreferenceScreen) findPreference("help_preference");
        PreferenceScreen aboutPreference = (PreferenceScreen) findPreference("about_preference");

        if (DropboxHelper.isLinked()) {
            dropboxPreference.setChecked(true);
        } else {
            dropboxPreference.setChecked(false);
        }

        dropboxPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!dropboxPreference.isChecked()) {
                    DropboxHelper.logOut(MoreActivity.this);
                } else {
                    DropboxHelper.getDropboxAPI().getSession().startAuthentication(MoreActivity.this);
                    mIsGoingAuthorization = true;
                }
                return false;
            }
        });

        playPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!playPreference.isChecked()) {
                    AppConfig.sharedInstance().setRandomPlay(false);
                } else {
                    AppConfig.sharedInstance().setRandomPlay(true);
                }
                return false;
            }
        });

        registerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });

        submitPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });

        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(MoreActivity.this, AboutActivity.class));
                return false;
            }
        });

        helpPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MoreActivity.this, WebViewActivity.class);
                intent.putExtra("url", "http://www.flipflashcards.com.au");
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsGoingAuthorization) {
            DropboxAPI<AndroidAuthSession> mDBApi = DropboxHelper.getDropboxAPI();

            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    mDBApi.getSession().finishAuthentication();
                    // Store it locally in our app for later use
                    TokenPair tokens = mDBApi.getSession().getAccessTokenPair();
                    DropboxHelper.storeKeys(this, tokens.key, tokens.secret);
                } catch (IllegalStateException e) {
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }

            mIsGoingAuthorization = false;
        }

    }
}