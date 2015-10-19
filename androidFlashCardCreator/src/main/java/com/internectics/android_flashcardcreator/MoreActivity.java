package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.internectics.UI.togglebutton.ToggleButton;
import com.internectics.helper.Dropbox.DropboxAuthHelper;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import timber.log.Timber;

public class MoreActivity extends Activity {

    private static final int LOGIN_REQUEST = 0;

    ToggleButton             mStorageProviderToggleButton;
    TextView                 mSocialAccountTextView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.settings);

        setTitle(getString(R.string.Title_Settings));

        final ToggleButton randomPlayToggleButton = (ToggleButton) findViewById(R.id.random_play_toggle_button);
        final ToggleButton muteSoundRecordingToggleButton = (ToggleButton) findViewById(R.id.mute_sound_recording_toggle_button);
        final ToggleButton textToSpeechToggleButton =(ToggleButton) findViewById(R.id.text_to_speech_toggle_button);
        final ToggleButton showQuestionOnlyToggleButton = (ToggleButton) findViewById(R.id.auto_show_question_only_toggle_button);
        final ToggleButton maleFemaleToggleButton = (ToggleButton) findViewById(R.id.male_female_voice_toggle_button);

        mSocialAccountTextView = (TextView) findViewById(R.id.random_play_social_account_textview);

        mStorageProviderToggleButton = (ToggleButton) findViewById(R.id.storage_provider_toggle_button);

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
                        .setTitleText(getString(R.string.DIALOG_AlERT))
                        .setContentText(getString(R.string.DIALOG_NOT_SUPPORTED))
                        .show();
            }
        });


        if (DropboxAuthHelper.sharedHelper(MoreActivity.this).isLinked()) {
            mStorageProviderToggleButton.setToggleOn();
        } else {
            mStorageProviderToggleButton.setToggleOff();
        }

        mStorageProviderToggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {

                if (on) {
                    DropboxAuthHelper.sharedHelper(MoreActivity.this).startAuthentication();
                } else {
                    DropboxAuthHelper.sharedHelper(MoreActivity.this).logOut();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            MoreActivity.this);
                    alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                    alertDialogBuilder
                            .setMessage(R.string.DIALOG_USE_AMAZON_AS_STORAGE).show();
                }

            }
        });

        findViewById(R.id.rl_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, AboutActivity.class));
            }
        });


        findViewById(R.id.rl_social_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseUser currentUser = ParseUser.getCurrentUser();

                if (currentUser != null) {
                    // User clicked to log out.

                    final SweetAlertDialog pDialog = new SweetAlertDialog(MoreActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Logging out...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {

                            mSocialAccountTextView.setText(R.string.Table_Item_Log_In_Social_Network);

                            pDialog.dismiss();

                            new SweetAlertDialog(MoreActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText(getString(R.string.DIALOG_AlERT))
                                .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_LOG_OUT_SUCCESS))
                                    .show();

                        }
                    });

                } else {
                    parseUserAuth();
                }
            }
        });


    }

    private void parseUserAuth() {

        ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                MoreActivity.this);
        Intent parseLoginIntent = loginBuilder.setParseLoginEnabled(true)
                .setParseLoginEmailAsUsername(false)
                .setParseSignupButtonText("Create account")
                .setParseSignupMinPasswordLength(4)
                .setAppLogo(R.drawable.sign_in_logo)
                .build();
        startActivityForResult(parseLoginIntent, LOGIN_REQUEST);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser() != null) {
            mSocialAccountTextView.setText(R.string.Table_Item_Log_Out_Social_Network);
        } else {
            mSocialAccountTextView.setText(R.string.Table_Item_Log_In_Social_Network);
        }

        if (DropboxAuthHelper.sharedHelper(MoreActivity.this).isLinked()) {
            mStorageProviderToggleButton.setToggleOn();
        } else {
            if (DropboxAuthHelper.sharedHelper(MoreActivity.this).isAuthenticationSuccessful()) {
                try {
                    // Mandatory call to complete the auth
                    DropboxAuthHelper.sharedHelper(MoreActivity.this).finishAuthentication();

                    // Store it locally in our app for later use
                    DropboxAuthHelper.sharedHelper(MoreActivity.this).storeAuth();

                } catch (IllegalStateException e) {
                    Log.w("ccaa", "Error authenticating", e);
                }

                mStorageProviderToggleButton.setToggleOn();

            } else {
                mStorageProviderToggleButton.setToggleOff();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Parse暂时不支持区分sign up或sign in
        //https://github.com/ParsePlatform/ParseUI-Android/issues/79

        if (requestCode == LOGIN_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                final ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.getUsername().length() > 20) { //表明这是一个系统生成的user name，而不是二次用户生成

                        final EditText passwordEditText = new EditText(MoreActivity.this);
                        passwordEditText.setSingleLine(true);
                        passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        new AlertDialog.Builder(MoreActivity.this)
                                .setTitle(R.string.DIALOG_CREATE_ACCOUNT_ALERT_MESSAGE)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setView(passwordEditText)
                                .setPositiveButton(R.string.DIALOG_DONE, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String username = passwordEditText.getText().toString().trim().toLowerCase(); //bucket name必须小写

                                        if (username.length() == 0) {

                                            new SweetAlertDialog(MoreActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                                                                .setTitleText("Oops...")
                                                                                                .setContentText(getString(R.string.DIALOG_ACCOUNT_USERNAME_EMPTY_ERROR))
                                                                                                .show();

                                            return;
                                        }

                                        currentUser.setUsername(username);
                                        currentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    new SweetAlertDialog(MoreActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                            .setTitleText(getString(R.string.DIALOG_AlERT))
                                                            .setContentText(getString(R.string.DIALOG_ACCOUNT_USERNAME_LINKED_SUCCESSFULLY))
                                                            .show();
                                                } else {
                                                    new SweetAlertDialog(MoreActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                            .setTitleText(getString(R.string.DIALOG_ERROR))
                                                            .setContentText(getString(R.string.DIALOG_ACCOUNT_USERNAME_HAS_BEEN_REGISTERED))
                                                            .show();

                                                }

                                            }
                                        });


                                    }
                                })
                                .setNegativeButton(R.string.DIALOG_CANCEL, null)
                                .show();

                    } else {

                        new SweetAlertDialog(MoreActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText(getString(R.string.DIALOG_AlERT))
                                .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_SIGNUP_OR_SIGNIN_SUCCESS))
                                .show();
                    }
                } else {
                    new SweetAlertDialog(MoreActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.DIALOG_ERROR))
                            .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_LOG_IN_FAILURE))
                            .show();
                    Timber.tag(Global.debugTag).w("sign up or sign in failure.currentUser should exist");
                }


            } else if (resultCode == Activity.RESULT_CANCELED) {

            } else {

                new SweetAlertDialog(MoreActivity.this,SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.DIALOG_ERROR))
                        .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_LOG_IN_FAILURE))
                        .show();
                Timber.tag(Global.debugTag).w("sign up or sign in failure with resultCode = " + resultCode);
            }
        }
    }
}