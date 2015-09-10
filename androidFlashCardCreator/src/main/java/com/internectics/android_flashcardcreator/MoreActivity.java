package com.internectics.android_flashcardcreator;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.WindowManager;
import android.widget.TextView;

import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MoreActivity extends PreferenceActivity {

    private boolean mIsGoingAuthorization = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addPreferencesFromResource(R.xml.more);
        setContentView(R.layout.more);


        final CheckBoxPreference randomPlayPreference = (CheckBoxPreference) findPreference("more_random_play_preference");
        final PreferenceScreen aboutPreference = (PreferenceScreen) findPreference("more_about_preference");
        final CheckBoxPreference maleFemalePreference = (CheckBoxPreference) findPreference("more_male_female_voice_preference");
        final CheckBoxPreference textToSpeechPreference = (CheckBoxPreference) findPreference("more_text_to_speech_preference");
        final CheckBoxPreference showQuestionOnlyPreference = (CheckBoxPreference) findPreference("more_show_question_only_preference");


        final DiscreteSeekBar countDownDiscreteSeekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        final TextView countDownTextView = (TextView) findViewById(R.id.count_down_textview);


        countDownTextView.setText(String.format("Count Down (%d)", AppConfig.sharedInstance().getCountDown()));
        countDownDiscreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                AppConfig.sharedInstance().setCountDown(value);
                countDownTextView.setText(String.format("Count Down (%d)", value));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        randomPlayPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!randomPlayPreference.isChecked()) {
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


        textToSpeechPreference.setChecked(AppConfig.sharedInstance().isTextToSpeech());
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


        showQuestionOnlyPreference.setChecked(AppConfig.sharedInstance().isShowQuestionOnly());
        showQuestionOnlyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!showQuestionOnlyPreference.isChecked()) {
                    AppConfig.sharedInstance().setShowQuestionOnly(false);
                } else {
                    AppConfig.sharedInstance().setShowQuestionOnly(true);
                }
                return false;
            }
        });

        maleFemalePreference.setChecked(AppConfig.sharedInstance().isMaleVoice());
        maleFemalePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!showQuestionOnlyPreference.isChecked()) {
                    AppConfig.sharedInstance().setMaleVoice(false);
                } else {
                    AppConfig.sharedInstance().setMaleVoice(true);
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