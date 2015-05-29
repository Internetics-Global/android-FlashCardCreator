package com.internectics.android_flashcardcreator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.WindowManager;
import com.internectics.util.AppConfig;

import timber.log.Timber;

public class MoreActivity extends PreferenceActivity {

    private boolean mIsGoingAuthorization = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addPreferencesFromResource(R.xml.more);

        final CheckBoxPreference playPreference = (CheckBoxPreference) findPreference("play_preference");

        final CheckBoxPreference textToSpeechPreference = (CheckBoxPreference) findPreference("text_to_speech_preference");

        PreferenceScreen helpPreference = (PreferenceScreen) findPreference("help_preference");
        PreferenceScreen aboutPreference = (PreferenceScreen) findPreference("about_preference");


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


        textToSpeechPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!textToSpeechPreference.isChecked()) {
                    AppConfig.sharedInstance().setTextToSpeech(false);
                } else {
                    AppConfig.sharedInstance().setTextToSpeech(true);
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}