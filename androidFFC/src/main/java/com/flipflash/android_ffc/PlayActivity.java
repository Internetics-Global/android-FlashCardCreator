package com.flipflash.android_ffc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.helper.AudioHelper;
import com.flipflash.model.CardListModel;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;
import com.flipflash.util.UIHelper;
import com.flipflash.util.VGViewPager;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


public class PlayActivity extends FragmentActivity implements SensorEventListener,VGViewPager.OnViewPagerClickListener, ViewPager.OnPageChangeListener{

    private static final String TAG = PlayActivity.class.getName();

    private Pack              mCurrentPack;
    private int               mPosition = 0;
    private List<Fragment>    mFragments;

    private SensorManager     mSensorManager;
    private boolean           mIsSensorAvailable;

    private float             mOriginalRoll = 0;
    private boolean           mIsResetRoll = false;
    private boolean           mEnableA = true;
    private boolean           mEnableB = true;

    private VGViewPager       mPager;

    private boolean             mIsScrollStop = true;

    private ImageButton         mCyclePlayImageButton;
    private ImageButton         mAutoScrollImageButton;

    private DiscreteSeekBar     mDwellTimeSeekBar;
    private DiscreteSeekBar     mPauseForAnswerSeekBar;

    private ImageButton         mPlayRecordImageButton;
    private ImageButton         mMuteImageButton;
    private TextView            mCounterDownTextView;

    private boolean     mIsAutoScroll;
    private boolean     mIsCyclePlay;
    private boolean     mIsMuteSoundRecording = false;  //实际上不是真正的mute,在auto play模式中，实际上还是在播放，只是mute了

    private boolean     mRunOnceFlag; //only allow to run once

    //Text to speech related
    private int            mTextToSpeechContentArrayIndex;
    private TextToSpeech   mTTS;

    private Handler        mTTSDelayHandler                             = new Handler();
    private Handler        mAutoHideControlPanelHandler                 = new Handler();
    private Handler        mPauseForAnswerHandler                       = new Handler();
    private Handler        mFirstTimeDelayHandler                       = new Handler();
    private Handler        mFirstPageDelay_FixedMode_Handler            = new Handler();
    private Handler        mFirstPageDelay_AutoDelayMode_Handler        = new Handler();
    private Handler        mText2Speech_AfterSoundRecording_Handler     = new Handler();

    /**
     *  与iPhone不同的是，我们不需要这个
     */
//    private Handler        mDwellOnAnswerExpireHandler_ForFixedDelay    = new Handler();
//    private Handler        mDwellOnQuestionExpireHandler_ForFixedDelay  = new Handler();


    private Handler        mA_ForText2SpeechFinishedHandler              = new Handler();
    private Handler        mB_ForText2SpeechFinishedHandler              = new Handler();
    private Handler        mC_ForText2SpeechFinishedHandler              = new Handler();

    private Timer          mAutoSwitchQATimer;
    private Timer          mAutoScrollForFixedDelayTimer;    //only for fixed delay
    private Timer          mCountDownTimer;

    private boolean        mIsShuttingDown;

    private boolean        mIsFixedDelayAutoScroll;  //用来区分是否是fixed delay 还是 smart  auto scroll

    private int            mOneOffPlayType; //0, manually; 1, auto play; 2, auto play loop

    /*
     * 当isAutoShowQuestionOnly = true时，intervalBetweenCardSeconds ＝ mPauseForAnswerSeekBar
     * 当isAutoShowQuestionOnly ＝ false时,intervalBetweenCardSeconds = K_IntervalBetweenCardSeconds_ForQAOnly
     */
    private final int      K_IntervalBetweenCardMilliSeconds_ForQAOnly      = 4000; //4 seconds

    private final int      K_Big_Enough_For_Endless_Repeated_Timer     =600000;


    private AudioIntentReceiver mAudioIntentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.play);
        getActionBar().hide();

        mAudioIntentReceiver = new AudioIntentReceiver();

        int packID = getIntent().getIntExtra("packID", -1);
        mOneOffPlayType = getIntent().getIntExtra("oneOffPlayType", -1);
        mCurrentPack = CardListModel.getPack(packID);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setupTextToSpeech();

        setupViews();


        switch (mOneOffPlayType) {
            case 0:
                mIsAutoScroll = false;
                EnableUserInteraction();
                break;

            case 1:
                mIsAutoScroll = true;
                DisableUserInteraction();
                mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
                break;
            case 2:
                mIsAutoScroll = true;
                DisableUserInteraction();
                mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
                break;
            default:
                break;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        LOGD(TAG, "onResume");

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mAudioIntentReceiver, filter);

        if (AppConfig.sharedInstance().isTextToSpeech()) {
            Boolean isSimulator = Build.FINGERPRINT.startsWith("generic");
            if (isSimulator) {
                Toast.makeText(getApplicationContext(), "TextToSpeech and Recording may not be supported on some Android simulators", Toast.LENGTH_LONG).show();
            }
        }

        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME); //considering different hardware, we need to set the fastest value
            mIsSensorAvailable = true;
        } else {
            mIsSensorAvailable = false;
            LOGE(TAG, "onResume: No Sensor.TYPE_ORIENTATION exists");
        }

        mIsResetRoll = true;

        //check first card to determine whether to hide play sound button
        CardDetailFragment firstDetailFragment = ((CardDetailFragment) (mFragments.get(0)));
        String soundFile = firstDetailFragment.mCurrentCard.question.audioUriFormatStr;
        if (soundFile.length() == 0) {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
        } else {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
        }


        if (mFirstTimeDelayHandler != null) {
            mFirstTimeDelayHandler.removeCallbacksAndMessages(null);
            mFirstTimeDelayHandler = null;
        }
        mFirstTimeDelayHandler = new Handler();
        mFirstTimeDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeAfterFirstTimeDelayExpire();
            }
        }, 1000);

        if (mAutoHideControlPanelHandler != null) {
            mAutoHideControlPanelHandler.removeCallbacksAndMessages(null);
            mAutoHideControlPanelHandler = null;
        }
        mAutoHideControlPanelHandler = new Handler();
        mAutoHideControlPanelHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                hideControlPanel();
            }

        }, 4000);


    }

    @Override
    protected void onPause() {
        super.onPause();

        LOGD(TAG, "onPause");

        unregisterReceiver(mAudioIntentReceiver);
    }

    private void setupViews() {

        LOGD(TAG, "setupViews");

        mFragments = getFragments();
        FCCPageAdapter pageAdapter = new FCCPageAdapter(getSupportFragmentManager(), mFragments);
        mPager = (VGViewPager) findViewById(R.id.viewpager);
        mPager.setStopScrollWhenTouch(false);
        mPager.setOnViewPagerClickListener(this);

        mCyclePlayImageButton = (ImageButton) findViewById(R.id.cycle_play_image_button);
        mCyclePlayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cyclePlayImageButtonClicked();
            }
        });

        mAutoScrollImageButton = (ImageButton) findViewById(R.id.auto_play_image_button);
        mAutoScrollImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoScrollImageButtonClicked();
            }
        });

        mPlayRecordImageButton = (ImageButton) findViewById(R.id.play_sound_image_button);
        mPlayRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecordedSoundImageButtonClicked();
            }
        });

        mMuteImageButton = (ImageButton) findViewById(R.id.play_mute_image_button);
        mMuteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                muteImageButtonClicked();
            }
        });


        if (AppConfig.sharedInstance().isMuteSoundRecording()) {
            mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_off));
            mIsMuteSoundRecording = true;
        } else {
            mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_on));
            mIsMuteSoundRecording = false;
        }

        mDwellTimeSeekBar = (DiscreteSeekBar) findViewById(R.id.auto_play_dwell_time_seek_bar).findViewById(R.id.seekbar);

        if (mCurrentPack.autoPlaySpeed == 0 ||
                mCurrentPack.autoPlaySpeed > Global.k_MAX_Auto_Play_Speed ||
                mCurrentPack.autoPlaySpeed < Global.k_MIN_Auto_Play_Speed) {
            mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
        } else {
            mDwellTimeSeekBar.setProgress(mCurrentPack.autoPlaySpeed);
        }


        mPauseForAnswerSeekBar = (DiscreteSeekBar) findViewById(R.id.pause_for_answer_seek_bar).findViewById(R.id.seekbar);

        mCounterDownTextView = (TextView) findViewById(R.id.count_down_textview);

        //used to get rid of interrupt during scroll
        View playMask = findViewById(R.id.play_mask);
        playMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mIsScrollStop == false);
            }
        });


        //特殊情况，当 width/height < Global.ratioOfCardInPlayMode
        double cardHeight = UIHelper.getScreenHeight(this) - UIHelper.getPixels(10 +10); //10 is top and bottom margin;
        double cardWidth = cardHeight * Global.ratioOfCardInPlayMode;

        double screenWidth = UIHelper.getScreenWidth(this);
        if (cardHeight *Global.ratioOfCardInPlayMode > screenWidth) {
            //这时的卡片宽度和高度将重新需要计算
            double horizontalMargin = UIHelper.getPixels(10);
            cardWidth =  screenWidth - 2*horizontalMargin;
            double verticalMarin =  (cardHeight - cardWidth/Global.ratioOfCardInPlayMode)/2;
            cardHeight = UIHelper.getCardHeight(this) - 2 *verticalMarin;

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPager.getLayoutParams();
            marginLayoutParams.topMargin = (int)verticalMarin;
            marginLayoutParams.bottomMargin = (int)verticalMarin;
            marginLayoutParams.leftMargin = (int)horizontalMargin;
            marginLayoutParams.rightMargin = (int)horizontalMargin;
            mPager.setLayoutParams(marginLayoutParams);

            LOGD(TAG, "setupViews: We are now in a very special case, width/height < 1.45");

        }

        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(pageAdapter);
        mPager.setOnPageChangeListener(this);

        View baseView = findViewById(R.id.play_baseview);
        baseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchControlPanelVisibility();

                if (mAutoHideControlPanelHandler != null) {
                    mAutoHideControlPanelHandler.removeCallbacksAndMessages(null);
                    mAutoHideControlPanelHandler = null;
                }
                mAutoHideControlPanelHandler = new Handler();
                mAutoHideControlPanelHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideControlPanel();
                    }

                }, 3000);
            }
        });

        if (isSmartDelay()) {
            mCounterDownTextView.setVisibility(View.GONE);
        }



        mDwellTimeSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

                if (fromUser) {
                    dwellTimeSeekBarProgressManuallyChanged();
                }

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        if (mCurrentPack.autoPlaySpeed < Global.k_MIN_Auto_Play_Speed
                || mCurrentPack.autoPlaySpeed > Global.k_MAX_Auto_Play_Speed) {
            mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
        } else {
            mDwellTimeSeekBar.setProgress(mCurrentPack.autoPlaySpeed);
        }

        mPauseForAnswerSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    pauseForAnswerSeekBarProgressManuallyChanged();
                }
            }


            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

    }

    private void dwellTimeSeekBarProgressManuallyChanged() {

        LOGD(TAG, "dwellTimeSeekBarProgressManuallyChanged");

        stopAudio();
        stopTextToSpeech();

        mCounterDownTextView.setVisibility(View.GONE);

        stopAllTimers();
        stopAllHandlers();

        showControlPanel();
        resetAutoHideControlPanelHandler();


        int duration = getDwellTimeMilliSeconds();
        mCurrentPack.autoPlaySpeed = duration;
        if (duration == Global.k_MIN_Auto_Play_Speed * 1000) {
            mIsFixedDelayAutoScroll = false;
        } else {
            mIsFixedDelayAutoScroll = true;
            mOneOffPlayType = -1;  //因为是one off的，所以一旦有新动作，需要重置
        }


        if (isSmartDelay()) {
            CardDetailFragment currentDetailFragment = getCurrentCardDetailFragment();
            playbackOnCard(currentDetailFragment);
        }  else {
            if (mIsAutoScroll) {
                beginFixedDelayAutoScroll();
            }
        }


    }

    private void pauseForAnswerSeekBarProgressManuallyChanged() {

        LOGD(TAG, "pauseForAnswerSeekBarProgressManuallyChanged");

        showControlPanel();
        resetAutoHideControlPanelHandler();
    }


    @Override
    protected void onStop() {
        super.onStop();
        LOGD(TAG, "onStop");

        
        if (mIsSensorAvailable) {
            mSensorManager.unregisterListener(this);
        }

        stopAudio();
        stopTextToSpeech();
    }

    protected void stopAllTimers() {

        LOGD(TAG, "stopAllTimers");

        if (mAutoScrollForFixedDelayTimer != null) {
            mAutoScrollForFixedDelayTimer.cancel();
        }

        if (mAutoSwitchQATimer != null) {
            mAutoSwitchQATimer.cancel();
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

    }



    protected void  stopAllHandlers() {

        LOGD(TAG, "stopAllHandlers");

        if (mTTSDelayHandler !=null) {
            mTTSDelayHandler.removeCallbacksAndMessages(null);
            mTTSDelayHandler = null;
        }

        if (mPauseForAnswerHandler !=null) {
            mPauseForAnswerHandler.removeCallbacksAndMessages(null);
            mPauseForAnswerHandler = null;
        }

        if (mFirstTimeDelayHandler !=null) {
            mFirstTimeDelayHandler.removeCallbacksAndMessages(null);
            mFirstTimeDelayHandler = null;
        }

        if (mAutoHideControlPanelHandler !=null) {
            mAutoHideControlPanelHandler.removeCallbacksAndMessages(null);
            mAutoHideControlPanelHandler = null;
        }

        if (mFirstPageDelay_FixedMode_Handler !=null) {
            mFirstPageDelay_FixedMode_Handler.removeCallbacksAndMessages(null);
        }

        if (mFirstPageDelay_AutoDelayMode_Handler !=null) {
            mFirstPageDelay_AutoDelayMode_Handler.removeCallbacksAndMessages(null);
            mFirstPageDelay_AutoDelayMode_Handler = null;
        }


        if (mText2Speech_AfterSoundRecording_Handler !=null) {
            mText2Speech_AfterSoundRecording_Handler.removeCallbacksAndMessages(null);
            mText2Speech_AfterSoundRecording_Handler = null;
        }


        if (mA_ForText2SpeechFinishedHandler !=null) {
            mA_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
            mA_ForText2SpeechFinishedHandler = null;
        }

        if (mB_ForText2SpeechFinishedHandler !=null) {
            mB_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
            mB_ForText2SpeechFinishedHandler = null;
        }

        if (mC_ForText2SpeechFinishedHandler !=null) {
            mC_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
            mC_ForText2SpeechFinishedHandler = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LOGD(TAG, "onDestroy");

        mTTS.setOnUtteranceProgressListener(null);
        utteranceProgressListener = null;

        stopAllTimers();

        stopAllHandlers();

        shutdownTextToSpeech();

        AudioHelper.cleanupAudioPlayResource();
        AudioHelper.cleanupRecorderResource();

        mFragments.clear();
        mFragments = null;
    }



    private void muteImageButtonClicked() {

        LOGD(TAG, "muteImageButtonClicked");

        showControlPanel();
        resetAutoHideControlPanelHandler();

        if (mIsMuteSoundRecording) {
            mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_on));
            mIsMuteSoundRecording = false;
        } else {
            mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_off));
            mIsMuteSoundRecording = true;
            stopAudio();
            stopTextToSpeech();
        }

        AppConfig.sharedInstance().setMuteSoundRecording(mIsMuteSoundRecording);


    }

    private void playRecordedSoundImageButtonClicked() {

        LOGD(TAG, "playRecordedSoundImageButtonClicked");

        showControlPanel();
        resetAutoHideControlPanelHandler();

        if (mIsMuteSoundRecording) {
            return;
        }

        boolean isEmpty;
        CardDetailFragment cardDetailFragment = getCurrentCardDetailFragment();
        if (cardDetailFragment != null) {
            if (cardDetailFragment.mIsQuestionShowing) {
                isEmpty = StringUtils.isEmpty(cardDetailFragment.mCurrentCard.question.audioUriFormatStr);
            } else {
                isEmpty = StringUtils.isEmpty(cardDetailFragment.mCurrentCard.answer.audioUriFormatStr);
            }
        } else {
            isEmpty = true;
        }

        if (isEmpty) {

            new SweetAlertDialog(getApplicationContext())
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.DIALOG_NO_AUDIO_ON_QUESTION_CARD))
                    .show();

        } else {
            AudioHelper.playAudio(cardDetailFragment, mIsMuteSoundRecording);
        }
    }

    private void autoScrollImageButtonClicked() {

        LOGD(TAG, "autoScrollImageButtonClicked");

        stopAudio();
        stopTextToSpeech();

        showControlPanel();
        resetAutoHideControlPanelHandler();

        stopAllTimers();
        stopAllHandlers();

        if (mIsAutoScroll == false) {
            mIsAutoScroll = true;

            if (getDwellTimeMilliSeconds() == Global.k_MIN_Auto_Play_Speed * 1000) {
                mIsFixedDelayAutoScroll = false;
            } else {
                mIsFixedDelayAutoScroll = true;
            }

            executeAutoPlay();

        } else {

            mIsFixedDelayAutoScroll = false;

            mOneOffPlayType = -1; //因为是one off的，所以一旦有新动作，需要重置

            screenOff();

            EnableUserInteraction();
            mIsAutoScroll = false;
            mAutoScrollImageButton.setImageDrawable(getResources().getDrawable(R.drawable.autoplay_off));
            mPager.stopAutoScroll();

            mCounterDownTextView.setVisibility(View.GONE);

        }

    }


    /**
     *  是Auto play中的其中一种（另外一种是fixed delay，就是用NSTimer进行固定时间间隔的切换卡片
     *  这是一种智能的方式，只有文本读完了，才切换到下一个卡片
     */
    private boolean isSmartDelay() {
        //我们采用了一种非常特殊的方法，就是slider的值到了最小值时，isSmartDelay ＝ YES

        LOGD(TAG, "isSmartDelay");


        if (mOneOffPlayType == 1 || mOneOffPlayType == 2) {
            return true;
        } else {
            //我们不check else的状态
        }


        if (mDwellTimeSeekBar.getProgress() == Global.k_MIN_Auto_Play_Speed) {
            return true;
        } else {
            return false;
        }
    }




    private void cyclePlayImageButtonClicked() {

        LOGD(TAG, "cyclePlayImageButtonClicked");

        showControlPanel();
        resetAutoHideControlPanelHandler();

        if (mIsCyclePlay) {
            mIsCyclePlay = false;
            mCyclePlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.repeat_unselected));
        } else {
            mIsCyclePlay = true;
            mCyclePlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.repeat_selected));
        }

        mPager.setCycle(mIsCyclePlay);


    }


    private void executeAfterFirstTimeDelayExpire() {

        LOGD(TAG, "executeAfterFirstTimeDelayExpire");

        if (mFirstTimeDelayHandler !=null) {
            mFirstTimeDelayHandler.removeCallbacksAndMessages(null);
            mFirstTimeDelayHandler = null;
        }

        showControlPanel();
        //_scrollView.userInteractionEnabled = YES;  //:TODO:XXX

        switch (mOneOffPlayType) {
            case 0: {
                mIsAutoScroll = false;
                CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();
                playbackOnCard(currentCardDetailFragment);

                break;
            }

            case 1:
                mIsAutoScroll = true;
                executeAutoPlay();
                break;

            case 2:
                mIsAutoScroll = true;
                executeAutoPlay();
                cyclePlayImageButtonClicked();

                break;

            default:
                break;
        }

    }

    private void executeAutoPlay() {

        LOGD(TAG, "executeAutoPlay");

        int countDownVal = AppConfig.sharedInstance().getCountDown();
        mCounterDownTextView.setText(String.valueOf(countDownVal));
        if (countDownVal > 0) {
            mCountDownTimer = new Timer();
            TimerTask countDownTimer = new CountDownTimerTask();
            mCountDownTimer.scheduleAtFixedRate(countDownTimer, 1000, 1000);
            mCounterDownTextView.setVisibility(View.VISIBLE);
        } else {
            mCounterDownTextView.setVisibility(View.GONE);
        }


        screenOn();
        DisableUserInteraction();
        mAutoScrollImageButton.setImageDrawable(getResources().getDrawable(R.drawable.autoplay_on));

        if (isSmartDelay()== false) {  //

            if (mFirstPageDelay_FixedMode_Handler != null) {
                mFirstPageDelay_FixedMode_Handler.removeCallbacksAndMessages(null);
                mFirstPageDelay_FixedMode_Handler = null;
            }
            mFirstPageDelay_FixedMode_Handler = new Handler();
            mFirstPageDelay_FixedMode_Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginFixedDelayAutoScroll();
                }
            }, countDownVal * 1000);

        } else {

            if (mFirstPageDelay_AutoDelayMode_Handler != null) {
                mFirstPageDelay_AutoDelayMode_Handler.removeCallbacksAndMessages(null);
                mFirstPageDelay_AutoDelayMode_Handler = null;
            }
            mFirstPageDelay_AutoDelayMode_Handler = new Handler();
            mFirstPageDelay_AutoDelayMode_Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();
                    playbackOnCard(currentCardDetailFragment);
                }
            }, countDownVal * 1000);
        }


    }

    private void beginFixedDelayAutoScroll() {

        LOGD(TAG, "beginFixedDelayAutoScroll");

        //1. prerequisite
        if (mIsFixedDelayAutoScroll == false) {
            throw new IllegalArgumentException("ccaa, mIsFixedDelayAutoScroll should not be false");
        }

        if (isSmartDelay() || mIsAutoScroll == false) {
            throw new IllegalArgumentException("ccaa, isSmartDelay should not be true or mIsAutoScroll should not be false");
        }

        //2. 我们不再需要这段逻辑，因为：如果是fixed delay auto play,则我们通过mAutoScrollForFixedDelayTimer；如果是smart auto delay,我们通过setOnUtteranceProgressListener回调
//        final int dwellTimeMilliSeconds = getDwellTimeMilliSeconds();
//        final int pauseForAnswerMilliSeconds = getPauseForAnswerMilliSeconds();
//
//        mPager.setInterval(dwellTimeMilliSeconds);
//        mPager.setPauseForAnswerMilliSeconds(pauseForAnswerMilliSeconds);
//        mPager.startAutoScroll();

        //3. auto scroll timer
        int dwellMilliSecondsTotally = getDwellMilliSecondsTotally();

        if (mAutoScrollForFixedDelayTimer != null) {
            mAutoScrollForFixedDelayTimer.cancel();
        }
        mAutoScrollForFixedDelayTimer = new Timer();
        TimerTask autoScrollForFixedDelayTimerTask = new AutoScrollForFixedDelayTimerTask();
        mAutoScrollForFixedDelayTimer.scheduleAtFixedRate(autoScrollForFixedDelayTimerTask, dwellMilliSecondsTotally, K_Big_Enough_For_Endless_Repeated_Timer * 1000);


        //4. play now
        CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();
        playbackOnCard(currentCardDetailFragment);


        //5.  question /answer switch timer
        int dwellMilliSecondsOnQuestionOnly = getDwellMilliSecondsOnQuestionOnly() + getPauseForAnswerMilliSeconds();

        if (mAutoSwitchQATimer != null) {
            mAutoSwitchQATimer.cancel();
        }
        if (AppConfig.sharedInstance().isShowQuestionOnly() == false) {
            mAutoSwitchQATimer = new Timer();
            TimerTask switchQATimer = new SwitchQATimerTask();
            mAutoSwitchQATimer.scheduleAtFixedRate(switchQATimer, dwellMilliSecondsOnQuestionOnly, K_Big_Enough_For_Endless_Repeated_Timer * 1000);
        }
    }


    private void EnableUserInteraction() {
        mPager.disableAllTouchEvent(false);
    }

    private void DisableUserInteraction() {
        mPager.disableAllTouchEvent(true);
    }

    private void screenOn() {
        LOGD(TAG, "screenOn");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void screenOff() {
        LOGD(TAG, "screenOff");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void OnViewPagerClickListener() {

        LOGD(TAG, "OnViewPagerClickListener");

        if (mIsAutoScroll == false) {

            //showControlPanel();

            switchQuestionAnswerViewManually(true);  //not allow to switch during auto play mode

            //resetAutoHideControlPanelHandler();
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LOGD(TAG, "onPageScrolled");
        if ((mPosition != position) && (positionOffsetPixels == 0)) {
            LOGD(TAG, "onPageScrolled: "+ "onPageScrolled, page index=" + position + " .mPosition=" + mPosition);


            //主要目的是及时释放内存，以防内存不断增加导致crash
            if (position-2 >=0) {
                CardDetailFragment cardDetailFragment = new CardDetailFragment();
                cardDetailFragment.setupParameters(mCurrentPack, mCurrentPack.cards.get(position-2), 2);
                mFragments.set(position-2,cardDetailFragment);
            }

            if (position +2 <= mCurrentPack.cards.size() -1) {
                CardDetailFragment cardDetailFragment = new CardDetailFragment();
                cardDetailFragment.setupParameters(mCurrentPack, mCurrentPack.cards.get(position +2), 2);
                mFragments.set(position +2,cardDetailFragment);
            }

           //不再需要执行如下，因为setOffscreenPageLimit已经建立了3个缓存（当前，前，后）
            if (mPosition >= 0 && mPosition <= mCurrentPack.cards.size() -1) {
                ((CardDetailFragment) (mFragments.get(mPosition))).switchToQuestionViewWithOption(false);
            }

            //update icon
            String soundFile = ((CardDetailFragment) (mFragments.get(position))).mCurrentCard.question.audioUriFormatStr;
            if (soundFile.length() == 0) {
                mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
            } else {
                mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
            }


            mPosition = position;

            mIsScrollStop = true;

            setActiveFragmentTag(position);


            playbackOnCard((CardDetailFragment) (mFragments.get(position)));

            if (isSmartDelay() == false) {

                if (mAutoSwitchQATimer != null) {
                    mAutoSwitchQATimer.cancel();
                }

                if (mIsAutoScroll && AppConfig.sharedInstance().isShowQuestionOnly() == false) {
                    int dwellMilliSecondsOnQuestionOnly = getDwellMilliSecondsOnQuestionOnly() + getPauseForAnswerMilliSeconds();
                    mAutoSwitchQATimer = new Timer();
                    TimerTask updateBall = new SwitchQATimerTask();
                    mAutoSwitchQATimer.scheduleAtFixedRate(updateBall, dwellMilliSecondsOnQuestionOnly, K_Big_Enough_For_Endless_Repeated_Timer*1000);
                }
            }




        } else {

            if (mTTSDelayHandler != null) {
                mTTSDelayHandler.removeCallbacksAndMessages(null);
                mTTSDelayHandler = null;
            }

            if (mPauseForAnswerHandler != null) {
                mPauseForAnswerHandler.removeCallbacksAndMessages(null);
                mPauseForAnswerHandler = null;
            }

            //只会运行一次
            if (mRunOnceFlag == false) {
                setActiveFragmentTag(0);
                mRunOnceFlag = true;
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        LOGD(TAG, "onPageSelected");

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        LOGD(TAG, "onPageScrollStateChanged");

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //LOGD(TAG, "onSensorChanged");

        if (mIsAutoScroll) {
            return;
            //throw new IllegalArgumentException("ccaa, mIsAutoScroll should not be true");  //not allow to switch during auto play mode
        }

        CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();
        if ((currentCardDetailFragment == null) || (currentCardDetailFragment.mCardSN == null))  {
            //this could happen when cardDetailFragment is not full inflated
            return;
        }


        if (mIsResetRoll) {
            mOriginalRoll = event.values[2];
            mIsResetRoll = false;
        }


        //range of values is 90 degrees to -90 degrees.
        float roll = event.values[2];
        //Timber.tag(Global.debugTag).i( "roll angle =" + roll);

        int orientation = getOrientation();
        if (orientation == 0) {
            if ((roll - mOriginalRoll > 15.0) && (mEnableA)) {
                switchQuestionAnswerViewManually(true);
                mEnableA = false;
            }
            if (roll - mOriginalRoll < 0) {
                mEnableA = true;
            }

        } else if ((orientation == 1) && (mEnableB)) {
            if (roll - mOriginalRoll < -15.0) {
                switchQuestionAnswerViewManually(true);
                mEnableB = false;
            }

            if (roll - mOriginalRoll > 0) {
                mEnableB = true;
            }
        }



    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        LOGD(TAG, "onAccuracyChanged");
        //do nothing
    }


    /**
     *  由于是个延时调用，我们必须重新check
     */
    private void switchQAFromTimerForFixedDelay() {
        LOGD(TAG, "switchQAFromTimerForFixedDelay");

        if (mIsAutoScroll == false) {
            throw new IllegalArgumentException("ccaa,  mIsAutoScroll should not be false");
        }

        if (AppConfig.sharedInstance().isShowQuestionOnly() == true) {
            throw new IllegalArgumentException("ccaa, isShowQuestionOnly should not be true");
        }

        if (mIsAutoScroll&& isSmartDelay()) {
            throw new IllegalArgumentException("ccaa, mIsAutoScroll and isSmartDelay should not be both true");
        }

        switchQuestionAnswerViewManually(false);

    }


    /*
     * 只有两种情况isManually ＝ true;
     * 1. OnViewPagerClickListener
     * 2. onSensorChanged （而sensor只是在manually play中才enable）
     */
    private boolean mIsSwitchQuestionAnswerViewManually_Processing = false;  //由于switch是个非常耗性能的动作，同时执行会有副作用
    private void switchQuestionAnswerViewManually(boolean isManually) {

        LOGD(TAG, "switchQuestionAnswerViewManually");

        if (mIsSwitchQuestionAnswerViewManually_Processing) {
            LOGD(TAG, "switchQuestionAnswerViewManually is aborted since it's processing ");
            return;
        } else {
            mIsSwitchQuestionAnswerViewManually_Processing = true;
        }

        if (isManually) { //在fixed delay或smart delay的auto scroll中，都是不允许手动切换question/answer view的
            stopAllHandlers();
            stopAllTimers();
        }

        stopAudio();
        stopTextToSpeech();

        CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();


        currentCardDetailFragment.switchQuestionAnswerView();

        //hide or show play recorded voice
        String soundFile;
        if (currentCardDetailFragment.mIsQuestionShowing) {
            soundFile = currentCardDetailFragment.mCurrentCard.question.audioUriFormatStr;
        } else {
            soundFile = currentCardDetailFragment.mCurrentCard.answer.audioUriFormatStr;
        }
        if (soundFile.length() == 0) {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
        } else {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
        }

        setActiveFragmentTag(mPosition);

        playbackOnCard(currentCardDetailFragment);

        mIsSwitchQuestionAnswerViewManually_Processing = false;
    }





    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        LOGD(TAG, "onKeyDown");
        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {
            if (mIsSensorAvailable) {
                mSensorManager.unregisterListener(this);
            }

            mCurrentPack.save(PlayActivity.this);

            mIsShuttingDown = true;
        }
        return super.onKeyDown(keyCode, event);  // need to use super to exit current activity
    }


    private void setupTextToSpeech() {

        LOGD(TAG, "setupTextToSpeech");

        if (mTTS == null) {


            mTTS = new TextToSpeech(AppContext.getAppContext(),new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        LOGD(TAG, "onInit: TTS Initialization Success");

                        Locale matchedLocale = getText2SpeechLocale();

                        mTTS.setLanguage(matchedLocale);

                        mTTS.setOnUtteranceProgressListener(utteranceProgressListener);



                    } else {
                        LOGE(TAG, "onInit: TTS Initialization Failed!");
                    }
                }
            });
            mTTS.setSpeechRate((float) 0.7);




        }

    }

    private Locale getText2SpeechLocale() {

        LOGD(TAG, "getText2SpeechLocale");

        try {
            Locale[] locales = Locale.getAvailableLocales();

            if (locales == null) {
                return Locale.ENGLISH;
            }

            List<Locale> localeList = new ArrayList<Locale>();
            for (Locale locale : locales) {
                int res = mTTS.isLanguageAvailable(locale);
                if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    localeList.add(locale);
                }
            }

            String languageStr = Locale.getDefault().getLanguage();
            String countryStr = Locale.getDefault().getCountry();

            if (languageStr == null || countryStr == null) {
                return Locale.ENGLISH;
            }

            if (languageStr.equals("en")) {
                return Locale.ENGLISH;
            }

            for (Locale item : localeList) {
                if (item.getCountry().equals(countryStr) && item.getLanguage().equals(languageStr)) {
                    return item;
                }
            }

        } catch (MissingResourceException ex) {
            System.out.println("Error " + ex.getMessage());

        } catch (Exception ex) {
            System.out.println("Error " + ex.getMessage());
        }

        return Locale.ENGLISH;

    }

    private void text2SpeechFinished() {

        LOGD(TAG, "text2SpeechFinished");

        CardDetailFragment cardDetailFragment = getCurrentCardDetailFragment();

        if (isSmartDelay() && mIsAutoScroll) {

            //TODO:XXX
//            if (_isAutoScroll && (isQuestionShowing == FALSE)) {
//                [self enableDwellTimeSlider];
//            }

            if (AppConfig.sharedInstance().isShowQuestionOnly()) {

                int pauseForAnswerMilliSeconds = getPauseForAnswerMilliSeconds();

                if (mA_ForText2SpeechFinishedHandler !=null) {
                    mA_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
                    mA_ForText2SpeechFinishedHandler = null;
                }
                mA_ForText2SpeechFinishedHandler = new Handler();
                mA_ForText2SpeechFinishedHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsShuttingDown == false && mIsAutoScroll && isSmartDelay()) {
                            scroll2NextPage();
                        }
                    }
                }, pauseForAnswerMilliSeconds);

            } else {
                if (cardDetailFragment.mIsQuestionShowing) {

                    int pauseForAnswerMilliSeconds = getPauseForAnswerMilliSeconds();

                    if (mB_ForText2SpeechFinishedHandler !=null) {
                        mB_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
                        mB_ForText2SpeechFinishedHandler = null;
                    }
                    mB_ForText2SpeechFinishedHandler = new Handler();
                    mB_ForText2SpeechFinishedHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsShuttingDown == false && mIsAutoScroll && isSmartDelay()) {
                                switchQuestionAnswerViewManually(false);
                            }
                        }
                    }, pauseForAnswerMilliSeconds);


                } else {

                    if (mC_ForText2SpeechFinishedHandler !=null) {
                        mC_ForText2SpeechFinishedHandler.removeCallbacksAndMessages(null);
                        mC_ForText2SpeechFinishedHandler = null;
                    }
                    mC_ForText2SpeechFinishedHandler = new Handler();
                    mC_ForText2SpeechFinishedHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsShuttingDown == false && mIsAutoScroll && isSmartDelay()) {
                                scroll2NextPage();
                            }
                        }
                    }, K_IntervalBetweenCardMilliSeconds_ForQAOnly);

                }
            }
        }

    }


    private void scroll2NextPage() {

        LOGD(TAG, "scroll2NextPage");

        stopAudio();
        stopTextToSpeech();

        if (mIsCyclePlay) {
            mPager.scroll2NextPage();
        } else {
            if (mPosition < mCurrentPack.cards.size()-1) {
                mPager.scroll2NextPage();
            }
        }

    }


/*
 *  只要 isTextToSpeech 或 isSmartDelay一个为真，就为执行如下逻辑。否则我们不执行audio或TextToSpeech
 *1. 如果mIsMuteSoundRecording ＝ YES， 只执行TextToSpeech
 *2. 如果mIsMuteSoundRecording ＝ NO， 则先audio，然后执行只执行TextToSpeech
 */
    private void playbackOnCard(final CardDetailFragment cardDetailFragment) {

        LOGD(TAG, "playbackOnCard");

        AudioHelper.unmuteTTS(); //我们需要确保这时音频的音量是可用的。
        AudioHelper.stopAndCleanAudio();

        if (AppConfig.sharedInstance().isTextToSpeech() || isSmartDelay()) {

            final boolean isMuteText2Speech;  //Text2Speech is still on, but mute
            if (AppConfig.sharedInstance().isTextToSpeech()) {
                isMuteText2Speech = false;
            } else {
                isMuteText2Speech = true;
            }

            if (mIsShuttingDown == false) {

                if (mIsMuteSoundRecording == false) {

                    int durationForRecordedSound;
                    if (cardDetailFragment.mIsQuestionShowing) {
                        durationForRecordedSound = cardDetailFragment.durationForQuestionRecordedSound();
                    } else {
                        durationForRecordedSound = cardDetailFragment.durationForAnswerRecordedSound();
                    }

                    if (mText2Speech_AfterSoundRecording_Handler != null) {
                        mText2Speech_AfterSoundRecording_Handler.removeCallbacksAndMessages(null);
                        mText2Speech_AfterSoundRecording_Handler = null;
                    }

                    if (durationForRecordedSound == 0) {
                        textToSpeechAllContentNow(cardDetailFragment,isMuteText2Speech);
                    } else {

                        if ((AppConfig.sharedInstance().isTextToSpeech() == false) && (mIsAutoScroll == false) &&
                                (isSmartDelay() == false)) {
                        } else {
                            mText2Speech_AfterSoundRecording_Handler = new Handler();
                            mText2Speech_AfterSoundRecording_Handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (mIsShuttingDown == false) {
                                        textToSpeechAllContentNow(cardDetailFragment,isMuteText2Speech);
                                    }
                                }
                            },durationForRecordedSound + 1000);  //这里1000（1秒）是适当的，因为mPauseForAnswerSeekBar或K_IntervalBetweenCardSeconds_ForQAOnly都远大于这个数

                        }

                        AudioHelper.playAudio(cardDetailFragment,mIsMuteSoundRecording);
                    }

                } else {

                    if (mText2Speech_AfterSoundRecording_Handler !=null) {
                        mText2Speech_AfterSoundRecording_Handler.removeCallbacksAndMessages(null);
                        mText2Speech_AfterSoundRecording_Handler = null;
                    }

                    textToSpeechAllContentNow(cardDetailFragment,isMuteText2Speech);
                }
            }

        } else {
            AudioHelper.playAudio(cardDetailFragment,mIsMuteSoundRecording);

        }

//        //two cases:
//        //1. normal text to speech
//        //2. text to speech but mute, which is used in auto delay mode
//        if (AppConfig.sharedInstance().isTextToSpeech() ||
//                (AppConfig.sharedInstance().isTextToSpeech()== false && isSmartDelay())) {
//            textToSpeechAllContentNow(cardDetailFragment);//先text-to-speech，然后再播放audio
//        } else {
//            playAudio();
//        }
    }


    private void stopAudio() {
        LOGD(TAG, "stopAudio");
        AudioHelper.stopAndCleanAudio();

    }

    private void shutdownTextToSpeech() {

        LOGD(TAG, "shutdownTextToSpeech");

        if (mTTS != null) {
            mTTS.shutdown();
        }

        mTTS = null;
    }

    private void stopTextToSpeech() {
        LOGD(TAG, "stopTextToSpeech");

        mTextToSpeechContentArrayIndex = 0; //这个非常重要


        if (mTTS!= null) {
            mTTS.stop();
        }

        AudioHelper.unmuteTTS(); //这个非常重要
    }


    /*
     * isMuteText2Speech, if false, mute but still text2speech
     */
    private void textToSpeechAllContentNow(CardDetailFragment cardDetailFragment,boolean isMuteText2Speech) {
        LOGD(TAG, "textToSpeechAllContentNow");

        if (mIsShuttingDown) {
            return;
        }


//        mute mode (but still play)
        if (mTTS != null) {
            mTTS.stop();
        }

        if (isMuteText2Speech) {
            AudioHelper.muteTTS();
        } else {
            AudioHelper.unmuteTTS();
        }

        ArrayList<String> textToSpeechArray = cardDetailFragment.textToSpeechContentArray();
        if (textToSpeechArray.size() >0) {

            mTextToSpeechContentArrayIndex = 0;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");//必不可少
            mTTS.speak(textToSpeechArray.get(0), TextToSpeech.QUEUE_FLUSH, params);
            LOGD(TAG, "textToSpeechAllContentNow: "+ "speak" + textToSpeechArray.get(mTextToSpeechContentArrayIndex));

        }  else {
            //playAudio(); //play audio after Text2Speech finished
        }



    }

    /*
      在VGViewPager，我们需要获取到当前显示的card的image,image2（而不是前一个card），所以需要设置标志，以方便查找
      需要在如下方法中调用
      1. 默认显示第一张卡片
      2. 卡片的切换（因为这时,mImage等的指向会发生变化）
      3. scroll到下一张卡片
     */
    private void setActiveFragmentTag(int indexShowing) {
        LOGD(TAG, "setActiveFragmentTag");
        for (int i = 0; i < mFragments.size(); i ++) {
            CardDetailFragment cardDetailFragment = (CardDetailFragment) mFragments.get(i);
            if (i == indexShowing) {
                if (cardDetailFragment.mImage != null) {
                    cardDetailFragment.mImage.setTag(Global.mImage_Showing);
                }

                if (cardDetailFragment.mImage2 != null) {
                    cardDetailFragment.mImage2.setTag(Global.mImage2_Showing);
                }

                if (cardDetailFragment.mLogoImage != null) {
                    cardDetailFragment.mLogoImage.setTag(Global.mLogoImage_Showing);
                }

            } else {
                if (cardDetailFragment.mImage != null) {
                    cardDetailFragment.mImage.setTag(Global.mImages_Not_Showing);
                }

                if (cardDetailFragment.mImage2 != null) {
                    cardDetailFragment.mImage2.setTag(Global.mImages_Not_Showing);
                }

                if (cardDetailFragment.mLogoImage != null) {
                    cardDetailFragment.mLogoImage.setTag(Global.mImages_Not_Showing);
                }
            }
        }
    }


    private CardDetailFragment getCurrentCardDetailFragment () {
       // LOGD(TAG, "getCurrentCardDetailFragment");
        CardDetailFragment cardDetailFragment = (CardDetailFragment) (mFragments.get(mPosition));
        return cardDetailFragment;
    }

    private List<Fragment> getFragments() {

        LOGD(TAG, "getFragments");

        ArrayList<Card> cardsArray = mCurrentPack.cards;
        int size = cardsArray.size();

        List<Fragment> fList = new ArrayList<Fragment>();

        if (mCurrentPack == null) {
            LOGE(TAG, "getFragments: mCurrentPack could not be null in PlayActivity");
            return fList;
        }

        for (int i = 0; i < size; i++) {
            CardDetailFragment cardDetailFragment = new CardDetailFragment();
            cardDetailFragment.setupParameters(mCurrentPack, cardsArray.get(i), 2);
            fList.add(i, cardDetailFragment);
            LOGD(TAG, "getFragments: " + String.format("new CardDetailFragment %d", i));

        }

        if (AppConfig.sharedInstance().isRandomPlay()) {
            Collections.shuffle(fList);
        }

        return fList;
    }

    /**
     * -1, other orientation; 0, landscape; 1. reverse landscape
     */
    private int getOrientation() {

        //LOGD(TAG, "getOrientation");

        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if ((rotation == Surface.ROTATION_0)
                    || (rotation == Surface.ROTATION_90)) {
                return 0; //landscape (for nexus 7, camera is left side of screen)
            }

            if ((rotation == Surface.ROTATION_180)
                    || (rotation == Surface.ROTATION_270)) {
                return 1; //reverse landscape   (for nexus 7, camera is right side of screen)
            }

        }

        return -1; //other rotation

    }


    /*
     * Including question/ answer dwell time and pause for answer time
     */
    private int getDwellMilliSecondsTotally() {

        LOGD(TAG, "getDwellMilliSecondsTotally");

        final int dwellTimeMilliSeconds = getDwellTimeMilliSeconds();
        final int pauseForAnswerMilliSeconds = getPauseForAnswerMilliSeconds();

        int dwellMilliSecondsTotally;
        if (AppConfig.sharedInstance().isShowQuestionOnly()) {
            dwellMilliSecondsTotally = dwellTimeMilliSeconds;
        } else {
            dwellMilliSecondsTotally = dwellTimeMilliSeconds *2 + pauseForAnswerMilliSeconds;
        }

        dwellMilliSecondsTotally = dwellMilliSecondsTotally + K_IntervalBetweenCardMilliSeconds_ForQAOnly;

        return dwellMilliSecondsTotally;
    }

    /*
     * Only question dwell time
     */
    private int getDwellMilliSecondsOnQuestionOnly() {

        LOGD(TAG, "getDwellMilliSecondsOnQuestionOnly");

        final int dwellTimeMilliSeconds = getDwellTimeMilliSeconds();

        return dwellTimeMilliSeconds;

    }


    /*
     * time on question or answer
     */
    private int getDwellTimeMilliSeconds() {
        LOGD(TAG, "getDwellTimeMilliSeconds");
        int interval = mDwellTimeSeekBar.getProgress();
        return interval*1000;
    }


    private int getPauseForAnswerMilliSeconds () {
        LOGD(TAG, "getPauseForAnswerMilliSeconds");
        int interval = mPauseForAnswerSeekBar.getProgress();
        return interval*1000;
    }


    private void hideControlPanel() {
        LOGD(TAG, "hideControlPanel");
        View controlPanelView = findViewById(R.id.play_control_panel);
        controlPanelView.setVisibility(View.INVISIBLE);
    }

    private void showControlPanel() {
        LOGD(TAG, "showControlPanel");
        View controlPanelView = findViewById(R.id.play_control_panel);
        controlPanelView.setVisibility(View.VISIBLE);
    }

    private boolean isControlPanelVisible() {
        LOGD(TAG, "isControlPanelVisible");
        View controlPanelView = findViewById(R.id.play_control_panel);
        if (controlPanelView.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }

    }

    private void switchControlPanelVisibility() {

        LOGD(TAG, "switchControlPanelVisibility");
        if (isControlPanelVisible()) {
            hideControlPanel();
        } else {
            showControlPanel();
        }
    }

    private void resetAutoHideControlPanelHandler() {

        LOGD(TAG, "resetAutoHideControlPanelHandler");

        if (mAutoHideControlPanelHandler != null) {
            mAutoHideControlPanelHandler.removeCallbacksAndMessages(null);
            mAutoHideControlPanelHandler = null;
        }
        mAutoHideControlPanelHandler = new Handler();
        mAutoHideControlPanelHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                hideControlPanel();
            }

        }, 4000);
    }





    class CountDownTimerTask extends  TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int value = Integer.parseInt(mCounterDownTextView.getText().toString());
                    if (value == 0) {
                        mCounterDownTextView.setVisibility(View.GONE);
                        mCountDownTimer.cancel();

                    } else {
                        mCounterDownTextView.setVisibility(View.VISIBLE);
                        mCounterDownTextView.setText(String.format("%d",value -1));
                    }

                }
            });
        }
    }

    class AutoScrollForFixedDelayTimerTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scroll2NextPage();
                }
            });
        }
    }


    /*
     * 仅仅用于fixed delay mode
     */
    class SwitchQATimerTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchQAFromTimerForFixedDelay();
                }
            });
        }
    }


    public class FCCPageAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mFragments;

        public FCCPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mFragments.get(position);
        }

        @Override
        public int getCount() {
            return this.mFragments.size();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        LOGD(TAG, "onBackPressed");

        stopAudio();
        stopTextToSpeech();
    }

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            LOGD("UtteranceProgressListener", "onStart");

        }

        @Override
        public void onDone(String utteranceId) {
            LOGD("UtteranceProgressListener", "onDone");
            //go to next
            mTextToSpeechContentArrayIndex ++;

            CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();

            final ArrayList<String> textToSpeechArray = currentCardDetailFragment.textToSpeechContentArray();
            if (textToSpeechArray.size() > mTextToSpeechContentArrayIndex) {
                if (mTTSDelayHandler != null) {
                    mTTSDelayHandler.removeCallbacksAndMessages(null);
                    mTTSDelayHandler = null;
                }
                mTTSDelayHandler = new Handler(getMainLooper());
                mTTSDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");//必不可少
                        if (textToSpeechArray.size() > mTextToSpeechContentArrayIndex) {
                            String text = textToSpeechArray.get(mTextToSpeechContentArrayIndex);
                            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                            LOGD(TAG, "TTS Speak text: " + text);
                        }
                    }
                }, 500);

            } else {

                if (textToSpeechArray.size() >0 && isSmartDelay()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text2SpeechFinished();
                        }
                    });

                } else {
                    //playAudio(); do nothing
                }

            }

        }

        @Override
        public void onError(String utteranceId) {
            LOGD("UtteranceProgressListener", "onError");

        }
    };

    private int original_audio_stream_state = -1;
    private class AudioIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {

            LOGD(TAG, "onReceive");

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);

                //由于Activity首次起来时也会调用onReceive，而我们只希望在后续改变进行通知，所以加了这个条件。
                if (original_audio_stream_state == -1) {
                    original_audio_stream_state = state;
                    return;
                }

                switch (state) {
                    case 0: {
                        LOGD(TAG, "onReceive: Headset is unplugged at PlayActivity");
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                PlayActivity.this);
                        alertDialogBuilder.setTitle(getString(R.string.Title_HANDSET_UNPLUGGED));
                        alertDialogBuilder
                                .setMessage(getString(R.string.Title_HANDSET_CHANGE_MESSAGE))
                                .setNegativeButton(getString(R.string.DIALOG_CLOSE),null)
                                .show();
                        break;
                    }
                    case 1: {
                        LOGD(TAG, "onReceive: Headset is plugged");
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                PlayActivity.this);
                        alertDialogBuilder.setTitle(getString(R.string.Title_HANDSET_PLUGGED));
                        alertDialogBuilder
                                .setMessage(getString(R.string.Title_HANDSET_CHANGE_MESSAGE))
                                .setNegativeButton(getString(R.string.DIALOG_CLOSE),null)
                                .show();
                        break;
                    }
                    default: {
                        LOGD(TAG, "onReceive: I have no idea what the headset state is");
                    }
                }
            }
        }
    }
}
