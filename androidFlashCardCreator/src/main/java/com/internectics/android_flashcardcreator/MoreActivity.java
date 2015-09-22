package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.internectics.UI.togglebutton.ToggleButton;
import com.internectics.util.AppConfig;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MoreActivity extends Activity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.settings);

        final ToggleButton randomPlayToggleButton = (ToggleButton) findViewById(R.id.random_play_toggle_button);
        final ToggleButton muteSoundRecordingToggleButton = (ToggleButton) findViewById(R.id.mute_sound_recording_toggle_button);
        final ToggleButton textToSpeechToggleButton =(ToggleButton) findViewById(R.id.text_to_speech_toggle_button);
        final ToggleButton showQuestionOnlyToggleButton = (ToggleButton) findViewById(R.id.auto_show_question_only_toggle_button);
        final ToggleButton maleFemaleToggleButton = (ToggleButton) findViewById(R.id.male_female_voice_toggle_button);

        final DiscreteSeekBar countDownDiscreteSeekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        final TextView countDownTextView = (TextView) findViewById(R.id.count_down_textview);


        countDownTextView.setText(String.format("Count Down (%d)", AppConfig.sharedInstance().getCountDown()));
        countDownDiscreteSeekBar.setProgress(AppConfig.sharedInstance().getCountDown());
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



        if (AppConfig.sharedInstance().isRandomPlay()) {
            randomPlayToggleButton.setToggleOn();
        } else {
            randomPlayToggleButton.setToggleOff();
        }
        randomPlayToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setRandomPlay(on);
            }
        });

        if (AppConfig.sharedInstance().isMuteSoundRecording()) {
            muteSoundRecordingToggleButton.setToggleOn();
        } else {
            muteSoundRecordingToggleButton.setToggleOff();
        }
        muteSoundRecordingToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setMuteSoundRecording(on);
            }
        });



        if (AppConfig.sharedInstance().isTextToSpeech()) {
            textToSpeechToggleButton.setToggleOn();
        } else {
            textToSpeechToggleButton.setToggleOff();
        }
        textToSpeechToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setTextToSpeech(on);
            }
        });

        if (AppConfig.sharedInstance().isShowQuestionOnly()) {
            showQuestionOnlyToggleButton.setToggleOn();
        } else {
            showQuestionOnlyToggleButton.setToggleOff();
        }
        showQuestionOnlyToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setShowQuestionOnly(on);
            }
        });

        if (AppConfig.sharedInstance().isMaleVoice()) {
            maleFemaleToggleButton.setToggleOn();
        } else {
            maleFemaleToggleButton.setToggleOff();
        }
        maleFemaleToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setMaleVoice(on);

                new SweetAlertDialog(MoreActivity.this)
                        .setTitleText("Alert")
                        .setContentText("Not supported")
                        .show();
            }
        });

       findViewById(R.id.rl_about).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(MoreActivity.this, AboutActivity.class));
           }
       });

    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}