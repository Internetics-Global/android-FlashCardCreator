package com.flipflash.android_ffc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.flipflash.UI.togglebutton.ToggleButton;
import com.flipflash.helper.Dropbox.DropboxAuthHelper;
import com.flipflash.helper.GoogleDrive.GoogleDriveAuthHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.Global;
import com.flipflash.util.MutipleTargetHelper;
import com.orhanobut.hawk.Hawk;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.io.IOException;

import static com.flipflash.util.LogUtils.LOGD;

public class MoreActivity extends Activity {

    private static final String TAG = MoreActivity.class.getSimpleName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.settings);

        setTitle(getString(R.string.Title_Settings));

        final ToggleButton randomPlayToggleButton = (ToggleButton) findViewById(R.id.random_play_toggle_button);
        final ToggleButton soundRecordingToggleButton = (ToggleButton) findViewById(R.id.mute_sound_recording_toggle_button);
        final ToggleButton textToSpeechToggleButton =(ToggleButton) findViewById(R.id.text_to_speech_toggle_button);
        final ToggleButton functionPromptToggleButton =(ToggleButton) findViewById(R.id.functon_prompt_toggle_button);
        final ToggleButton showQuestionOnlyToggleButton = (ToggleButton) findViewById(R.id.auto_show_question_only_toggle_button);
//        final ToggleButton maleFemaleToggleButton = (ToggleButton) findViewById(R.id.male_female_voice_toggle_button);

        final DiscreteSeekBar countDownDiscreteSeekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        final TextView countDownTextView = (TextView) findViewById(R.id.count_down_textview);


        countDownTextView.setText(String.format("%s (%d)", getString(R.string.Table_Item_Count_Down), AppConfig.sharedInstance().getCountDown()));
        countDownDiscreteSeekBar.setProgress(AppConfig.sharedInstance().getCountDown());
        countDownDiscreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                AppConfig.sharedInstance().setCountDown(value);
                countDownTextView.setText(String.format("%s (%d)", getString(R.string.Table_Item_Count_Down), value));
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

        if (AppConfig.sharedInstance().isSoundRecording()) {
            soundRecordingToggleButton.setToggleOn();
        } else {
            soundRecordingToggleButton.setToggleOff();
        }
        soundRecordingToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setSoundRecording(on);
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

                if (on) {
                    Boolean isSimulator = Build.FINGERPRINT.startsWith("generic");
                    if (isSimulator) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                MoreActivity.this);
                        alertDialogBuilder.setTitle("Warn");
                        alertDialogBuilder
                                .setMessage("TextToSpeech and Recording may not be supported on some Android simulators")
                                .setNegativeButton(R.string.DIALOG_CANCEL,null)
                                .show();
                    }
                }
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


        if (AppConfig.sharedInstance().isFunctionPromptOff()) {
            functionPromptToggleButton.setToggleOff();
        } else {
            functionPromptToggleButton.toggleOn();
        }

        functionPromptToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                AppConfig.sharedInstance().setFunctionPromptOff(on == false);

            }
        });

//        if (AppConfig.sharedInstance().isMaleVoice()) {
//            maleFemaleToggleButton.setToggleOn();
//        } else {
//            maleFemaleToggleButton.setToggleOff();
//        }
//        maleFemaleToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
//            @Override
//            public void onToggle(boolean on) {
//                AppConfig.sharedInstance().setMaleVoice(on);
//
//                new SweetAlertDialog(MoreActivity.this)
//                        .setTitleText(getString(R.string.DIALOG_AlERT))
//                        .setContentText(getString(R.string.DIALOG_NOT_SUPPORTED))
//                        .show();
//            }
//        });


        findViewById(R.id.rl_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, AboutActivity.class));
            }
        });

        findViewById(R.id.rl_select_speech_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, SelectText2SpeechLanguageActivity.class));
            }
        });

        findViewById(R.id.rl_storage_option).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MutipleTargetHelper.isFullVersion() == false ) {
                } else {
                    startActivity(new Intent(MoreActivity.this, StorageOptionActivity.class));
                }

            }
        });

        findViewById(R.id.rl_play_option).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, PlayOptionActivity.class));
            }
        });


        TextView textView = (TextView) findViewById(R.id.storage_textview);
        if (MutipleTargetHelper.isFullVersion() == false) {
            textView.setTextColor(Color.DKGRAY);
        } else {
            textView.setTextColor(Color.WHITE);
        }

        if (MutipleTargetHelper.isFullVersion() == false && MutipleTargetHelper.isNoAdVersion() == true) {
            findViewById(R.id.rl_upgrade_full_version).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rl_upgrade_full_version).setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.rl_upgrade_full_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                MutipleTargetHelper.showPurchaseView();
            }
        });


        findViewById(R.id.rl_send_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLogcatMail();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        LOGD(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //https://github.com/ParsePlatform/ParseUI-Android/issues/79

        if (requestCode == Global.REQUEST_LOGIN) {
            //old parse logic here
        }
    }


    public void sendLogcatMail(){

        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                "logcat.txt");
        if (outputFile != null) {
            boolean succeeded = outputFile.delete();
            LOGD(TAG, "sendLogcatMail: delete file succeed = " + succeeded);
        }

        //clear logcat:    adb logcat -c

        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:clive@internetics.net.au"));
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputFile));
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email Subject");

        startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
    }

    @Override
    protected void onDestroy() {
        DropboxAuthHelper.cleanup(); //avoid memory leak
        GoogleDriveAuthHelper.cleanup(); //avoid memory leak
        super.onDestroy();
        LOGD(TAG, "onDestroy");

    }
}