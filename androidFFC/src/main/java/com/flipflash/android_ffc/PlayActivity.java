package com.flipflash.android_ffc;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.flipflash.UI.FFCRatioFrameLayout;
import com.flipflash.UI.OnSwipeTouchListener;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.helper.AudioHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;
import com.flipflash.util.UIHelper;
import com.flipflash.UI.VGViewPager;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


public class PlayActivity extends FragmentActivity implements SensorEventListener, ViewPager.OnPageChangeListener{

    private static final String TAG = PlayActivity.class.getSimpleName();

    private Pack              mCurrentPack;
    private int               mPosition = 0;
    private List<Fragment>    mFragments;

    private SensorManager     mSensorManager;
    private boolean           mIsSensorAvailable;

    private VGViewPager       mPager;

    private ViewGroup         mBaseView;

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

    private Handler        mTTSDelayHandler                             = new Handler();  //we don't use this function any more, but still keep here
    private Handler        mAutoHideControlPanelHandler                 = new Handler();
    private Handler        mPauseForAnswerHandler                       = new Handler();
    private Handler        mFirstTimeDelayHandler                       = new Handler();
    private Handler        mFirstPageDelay_FixedMode_Handler            = new Handler();
    private Handler        mFirstPageDelay_AutoDelayMode_Handler        = new Handler();
    private Handler        mText2Speech_AfterSoundRecording_Handler     = new Handler();


    //我们在1秒后才开始Text2Speech.而不是切换Q/A或next card后立马进行
    private final int      K_Text2Speech_Delay_MilliSecond              = 1000;
    private Handler        mText2Speech_Delay_Handler                   = new Handler();

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

    /*
     * 两种case。主要是为了最开始（startActivity)的逻辑判断，之后如果因为原因2改变赋值为-1
     * 1. 在启动PlayActivity时直接传入：//0, manually; 1, auto play; 2, auto play loop
     * 2. 如果用户改变了mAutoScrollImageButton或mDwellTimeSeekBar，则永久性变成-1
     */
    private int            mOneOffPlayType;

    /*
     * 当isAutoShowQuestionOnly = true时，intervalBetweenCardSeconds ＝ mPauseForAnswerSeekBar
     * 当isAutoShowQuestionOnly ＝ false时,intervalBetweenCardSeconds = K_IntervalBetweenCardSeconds_ForQAOnly
     */
    private final int      K_IntervalBetweenCardMilliSeconds_ForQAOnly      = 4000; //4 seconds

    private final int      K_Big_Enough_For_Endless_Repeated_Timer     =600000;


    private AudioIntentReceiver mAudioIntentReceiver;

    private Locale              mMatchedLocale;

    /**
     *  ROTATION RELATED
     */
    boolean      _isDeviceRotating;
    boolean      _isRotationJustFinish;

    private HandlerThread mSensorThread;
    private Handler       mSensorHandler;

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
        mCurrentPack = User.getPack(AppContext.getAppContext(),packID);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setupTextToSpeech();

        setupViews();


        switch (mOneOffPlayType) {
            case 0:
                mIsAutoScroll = false;
                enableUserInteraction();
                break;

            case 1:
                mIsAutoScroll = true;
                disableUserInteraction();
                mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
                break;
            case 2:
                mIsAutoScroll = true;
                disableUserInteraction();
                mDwellTimeSeekBar.setProgress(Global.kDefault_Auto_Play_Speed);
                break;
            default:
                break;
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        //disable rotation
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch(rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_180:
                break;
            case Surface.ROTATION_270:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
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

        }, 5000);

        mBaseView.getViewTreeObserver().addOnGlobalLayoutListener(mRotationChangeListener);


    }

    @Override
    protected void onPause() {
        super.onPause();

        LOGD(TAG, "onPause");

        unregisterReceiver(mAudioIntentReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBaseView.getViewTreeObserver().removeOnGlobalLayoutListener(mRotationChangeListener);
        } else {
            mBaseView.getViewTreeObserver().removeGlobalOnLayoutListener(mRotationChangeListener);
        }
    }

    private void setupViews() {

        LOGD(TAG, "setupViews");

        mBaseView = (ViewGroup) findViewById(R.id.play_baseview);

        if (mFragments == null) {
            mFragments = getFragments();
        }
        FCCPageAdapter pageAdapter = new FCCPageAdapter(getSupportFragmentManager(), mFragments);
        mPager = (VGViewPager) findViewById(R.id.viewpager);
        mPager.setStopScrollWhenTouch(false);
        mPager.setOnTouchListener(mSwipeTouchListener);

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


        if (AppConfig.sharedInstance().isSoundRecording() == false) {
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

        mDwellTimeSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value;
            }

            @Override
            public String transformToString(int value) {
                return "Auto";
            }

            @Override
            public boolean useStringTransform() {

                if (mDwellTimeSeekBar.getProgress() == 4) {
                    return true;
                } else {
                    return false;
                }

            }
        });


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

        mBaseView.setOnClickListener(new View.OnClickListener() {
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

                }, 4000);
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

        mOneOffPlayType = -1;  //因为是one off的，所以一旦有新动作，需要重置

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

        if (mText2Speech_Delay_Handler !=null) {
            mText2Speech_Delay_Handler.removeCallbacksAndMessages(null);
            mText2Speech_Delay_Handler = null;
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

        if (mSensorThread != null) {
            mSensorThread.quit();
        }

        if (mSensorHandler != null) {
            mSensorHandler.removeCallbacksAndMessages(null);
            mSensorHandler = null;
        }

        if (mTTS != null) {
            mTTS.setOnUtteranceProgressListener(null);
            utteranceProgressListener = null;
        }
        shutdownTextToSpeech();

        stopAllTimers();

        stopAllHandlers();



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

        AppConfig.sharedInstance().setSoundRecording(!mIsMuteSoundRecording);


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

            new SweetAlertDialog(PlayActivity.this)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.DIALOG_NO_AUDIO_ON_QUESTION_CARD))
                    .show();

        } else {
            AudioHelper.playAudio(cardDetailFragment, mIsMuteSoundRecording);
        }
    }

    private void autoScrollImageButtonClicked() {

        LOGD(TAG, "autoScrollImageButtonClicked");

        mOneOffPlayType = -1; //因为是one off的，所以一旦有新动作，需要重置

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

            screenOff();

            enableUserInteraction();
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


        //用作start activity后的首次判断：mOneOffPlayType一旦被mAutoScrollImageButton或mDwellTimeSeekBar改变，自动赋值-1
        if (mOneOffPlayType == 1 || mOneOffPlayType == 2) {
            return true;
        } else if (mOneOffPlayType == 0) {
            return false;
        } else {
            //这时mOneOffPlayType ＝ -1，则无法判断
        }

        //之后，用这个方法进行判断
        if (mIsAutoScroll &&mDwellTimeSeekBar.getProgress() == Global.k_MIN_Auto_Play_Speed) {
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
                mPager.mCardDetailFragmentWeakReference = new WeakReference<>(currentCardDetailFragment);

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


        // Configure orientation sensor
        // 我们把这个sensor的初始化逻辑放在这里，主要是为了防止误出发Q/A switch（比如加载card比较慢，然后用户又同时roll设备就会引起这个问题）
//        Sensor orientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        Sensor accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (accelSensor != null && magSensor != null) {

            mSensorThread = new HandlerThread("sensorThread");
            mSensorThread.start();
            mSensorHandler = new Handler(mSensorThread.getLooper());
            mSensorManager.registerListener(PlayActivity.this, accelSensor,
                    SensorManager.SENSOR_DELAY_FASTEST, mSensorHandler);
            mSensorManager.registerListener(PlayActivity.this, magSensor,
                    SensorManager.SENSOR_DELAY_FASTEST, mSensorHandler);

            mIsSensorAvailable = true;
        } else {
            mIsSensorAvailable = false;
            LOGE(TAG, "onResume: No Sensor.TYPE_ORIENTATION exists");
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
        disableUserInteraction();
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


    private void enableUserInteraction() {

        FFCRatioFrameLayout layout = (FFCRatioFrameLayout) findViewById(R.id.play_ratio_layout);
        layout.disableAllTouchEvent(false);
    }


    private void disableUserInteraction() {
        FFCRatioFrameLayout layout = (FFCRatioFrameLayout) findViewById(R.id.play_ratio_layout);
        layout.disableAllTouchEvent(true);
    }

    private void screenOn() {
        LOGD(TAG, "screenOn");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void screenOff() {
        LOGD(TAG, "screenOff");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*
     * 在切换到下一张卡片， onPageScrolled会被调用多次;首次运行，即使不滑动，也会被调用
    */

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //LOGD(TAG, "onPageScrolled");


        if ((mPosition != position) && (positionOffsetPixels == 0)) {

            if (mIsAutoScroll == false) {
                disableUserInteraction();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableUserInteraction();
                    }
                }, 650);
            }

            //LOGD(TAG, "onPageScrolled: "+ "onPageScrolled, page index=" + position + " .mPosition=" + mPosition);

            //主要目的是及时释放内存，以防内存不断增加导致crash
            if (position-2 >=0) {
                Card targetCard = getShuffleCardIndexWithPageNumber(position-2);
                CardDetailFragment cardDetailFragment = new CardDetailFragment();
                cardDetailFragment.setupParameters(mCurrentPack, targetCard, 2);

//                CardDetailFragment oldFragment = (CardDetailFragment) mFragments.get(position -2);
//                RefWatcher refWatcher = AppContext.getRefWatcher(PlayActivity.this);
//                refWatcher.watch(oldFragment);

                mFragments.set(position - 2,cardDetailFragment);
            }

            if (position +2 <= mCurrentPack.cards.size() -1) {
                Card targetCard = getShuffleCardIndexWithPageNumber(position + 2);
                CardDetailFragment cardDetailFragment = new CardDetailFragment();
                cardDetailFragment.setupParameters(mCurrentPack, targetCard, 2);
                mFragments.set(position +2,cardDetailFragment);

//                CardDetailFragment oldFragment = (CardDetailFragment) mFragments.get(position + 2);
//                RefWatcher refWatcher = AppContext.getRefWatcher(PlayActivity.this);
//                refWatcher.watch(oldFragment);
            }


            if (mPosition >= 0 && mPosition <= mCurrentPack.cards.size() -1) {
                ((CardDetailFragment) (mFragments.get(mPosition))).switchToQuestionViewWithOption(false);
            }


            mPager.mCardDetailFragmentWeakReference = new WeakReference<>((CardDetailFragment) mFragments.get(position));

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

            //一般滑动view pager，我们需要停止

            if (mTTSDelayHandler != null) {
                mTTSDelayHandler.removeCallbacksAndMessages(null);
                mTTSDelayHandler = null;
            }

            if (mPauseForAnswerHandler != null) {
                mPauseForAnswerHandler.removeCallbacksAndMessages(null);
                mPauseForAnswerHandler = null;
            }

            if (mText2Speech_AfterSoundRecording_Handler != null) {
                mText2Speech_AfterSoundRecording_Handler.removeCallbacksAndMessages(null);
                mText2Speech_AfterSoundRecording_Handler = null;
            }

            if (mTTS != null && mTTS.isSpeaking()) {
                mTTS.stop();
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

    private final float  UP_THRESHOLD_RADIUS = 0.3f;
    private final float  DOWN_THRESHOLD_RADIUS = -0.3f;

    private boolean      resetRoll    = true;
    private boolean      upSwitchFlag    = false;
    private boolean      downSwitchFlag    = false;
    private boolean      isQASwitching    = false;
    private int          downCount = 0;

    /**
     *  Timeout logic
     */
    private long        _startDateForTimeout = System.currentTimeMillis();
    private float       _lowestRollDegree = 0;
    private float       _highestRollDegree = 0;

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {

        LOGD("onSensorChanged", "onSensorChanged, event.values is: " + String.format("%f,%f,%f", event.values[0], event.values[1], event.values[2]));

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION: {
                //roll(event.values[2]); 其中event.values[2]是度单位，需要转换成radius，其余则不需要改变
                break;
            }
            case Sensor.TYPE_ACCELEROMETER: {
                mGravity = event.values;

                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                mGeomagnetic = event.values;

                break;
            }
            default:{

            }
        }

        if (mGravity != null && mGeomagnetic != null) {

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float rollVal = orientation[2]; // orientation contains: azimut, pitch and roll

                roll( - rollVal); //这里有个负数，注意
            }
        }

    }

    /*
     * IF a card has just been flipped THEN don't allow flip card function to flip for 1 to 1.5 seconds

     */
    private long last_Flip_Time = 0;

    /*
     * 弧度和度的关系：rollRadius = (float)(rollDegress *3.14/180);
     * 以下方法如果是通过TYPE_ORIENTATION，也可直接使用
     */
    private void roll(float rollRadius) {

        LOGD("roll","rollVal:" + rollRadius);

        if (System.currentTimeMillis() - last_Flip_Time < 1000) {
            return;
        }


        CardDetailFragment currentCardDetailFragment = getCurrentCardDetailFragment();
        if ((currentCardDetailFragment == null) || (currentCardDetailFragment.mCardSN == null))  {
            //this could happen when cardDetailFragment is not full inflated
            return;
        }

        if (mIsAutoScroll == false && mOneOffPlayType == 0 ) {

            //范围-90到90
            //Android: home在左边，屏幕在右边时: 上边靠近身体这边负数，远离身体正数 （home在右边，屏幕在左边时，相反）
            //iOS:    home在左边，屏幕在右边时: 上边靠近身体这边正数，远离身体负数。与Android刚好相反。
            //越是垂直，越是绝对数大，这在iOS和Android是一致的

            int orientation = getOrientation();
            //LOGD(TAG, "onSensorChanged: oriention is: " + orientation + ";rollRadius = " + rollRadius);

            if (_lowestRollDegree == 0) {
                _lowestRollDegree = rollRadius;
                _highestRollDegree = rollRadius;
            }
            if (rollRadius > _highestRollDegree) {
                _highestRollDegree = rollRadius;
            }
            if (rollRadius < _lowestRollDegree) {
                _lowestRollDegree = rollRadius;
            }

            long methodFinish = System.currentTimeMillis();;
            long executionTime = methodFinish - _startDateForTimeout;
            if (executionTime > 2 * 1000) {

                if (_highestRollDegree < _lowestRollDegree + 6) {
                    resetRoll = true;

                    //[iConsole log:@"Timeout for flip function, reset now"];
                }

                _lowestRollDegree = 0;
                _highestRollDegree = 0;
                _startDateForTimeout = System.currentTimeMillis();;;



            }

            if (_isDeviceRotating) {
                return;
            }

            if (isQASwitching) {
                return;
            }

            if (_isRotationJustFinish) {
                _isRotationJustFinish = false;
                resetRoll = true;
            }

            if (resetRoll == true) {

                resetRoll = false;

                downCount = 0;

                downSwitchFlag = true;
                upSwitchFlag = false;

            }

            if (orientation == 0) {
                //(home在右边，UIDeviceOrientationLandscapeleft)
                if (rollRadius > UP_THRESHOLD_RADIUS && upSwitchFlag) {
                    if (downCount == 1) {

                        downCount = 0;

                        upSwitchFlag = false;
                        downSwitchFlag = true;

                        isQASwitching = true;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                last_Flip_Time = System.currentTimeMillis();
                                switchQuestionAnswerViewManually(true);
                                isQASwitching = false;
                            }
                        });

                    }

                } else if (rollRadius < DOWN_THRESHOLD_RADIUS && downSwitchFlag) {
                    downCount = 1;

                    upSwitchFlag = true;
                    downSwitchFlag = false;

                } else {
                    //do nothing
                }

            } else if (orientation == 1) {
                if (rollRadius < -UP_THRESHOLD_RADIUS && upSwitchFlag) {
                    if (downCount == 1) {

                        downCount = 0;

                        upSwitchFlag = false;
                        downSwitchFlag = true;

                        isQASwitching = true;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                last_Flip_Time = System.currentTimeMillis();
                                switchQuestionAnswerViewManually(true);
                                isQASwitching = false;
                            }
                        });
                    }

                } else if (rollRadius > -DOWN_THRESHOLD_RADIUS && downSwitchFlag) {
                    downCount = 1;

                    upSwitchFlag = true;
                    downSwitchFlag = false;

                } else {
                    //do nothing
                }
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
            resetAutoHideControlPanelHandler(); //这个必要，因为stopAllHandlers中disable了，这时如果control panel is visible，就无法自动关闭了。
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

        mMatchedLocale = getText2SpeechLocale();

        if (mTTS == null) {

            mTTS = new TextToSpeech(AppContext.getAppContext(),new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        LOGD(TAG, "onInit: TTS Initialization Success");

                        mTTS.setLanguage(mMatchedLocale);

                        mTTS.setOnUtteranceProgressListener(utteranceProgressListener);



                    } else {
                        LOGE(TAG, "onInit: TTS Initialization Failed!");
                    }
                }
            });
            mTTS.setSpeechRate((float) 0.4);

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

        //需要确保remove delay的工作
        if (mText2Speech_Delay_Handler != null) {
            mText2Speech_Delay_Handler.removeCallbacksAndMessages(null);
        }


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

                        mText2Speech_Delay_Handler = new Handler();
                        mText2Speech_Delay_Handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                text2SpeechAllContentNow(cardDetailFragment, isMuteText2Speech);
                            }
                        },K_Text2Speech_Delay_MilliSecond);

                    } else {

                        if ((AppConfig.sharedInstance().isTextToSpeech() == false) && (mIsAutoScroll == false) &&
                                (isSmartDelay() == false)) {
                        } else {
                            mText2Speech_AfterSoundRecording_Handler = new Handler();
                            mText2Speech_AfterSoundRecording_Handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (mIsShuttingDown == false) {

                                        mText2Speech_Delay_Handler = new Handler();
                                        mText2Speech_Delay_Handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                text2SpeechAllContentNow(cardDetailFragment, isMuteText2Speech);
                                            }
                                        },K_Text2Speech_Delay_MilliSecond);

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

                    mText2Speech_Delay_Handler = new Handler();
                    mText2Speech_Delay_Handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            text2SpeechAllContentNow(cardDetailFragment, isMuteText2Speech);
                        }
                    },K_Text2Speech_Delay_MilliSecond);
                }
            }

        } else {


            AudioHelper.playAudio(cardDetailFragment, mIsMuteSoundRecording);



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
    private void text2SpeechAllContentNow(CardDetailFragment cardDetailFragment,boolean isMuteText2Speech) {
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
            LOGD(TAG, "textToSpeechAllContentNow: "+ "speak" + textToSpeechArray.get(0));

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

    /*
     * Shuffle后，不能再根据mCurrentPack.cards.get(pageNumber)来获取正确的mCurrentCard，而是需要如下的方法
     */
    private Card getShuffleCardIndexWithPageNumber(int pageNumber) {
        if (mFragments == null) {
            mFragments = getFragments();
        }

        int targetCardID = ((CardDetailFragment)mFragments.get(pageNumber)).mCurrentCard.cardID;

        Iterator<Card> iterator = mCurrentPack.cards.iterator();
        while (iterator.hasNext()) {
            Card item = iterator.next();
            if (item.cardID == targetCardID) {
                return item;
            }
        }

        throw  new IllegalStateException("Should not be here ");

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
                return 0; //landscape (home在右边，UIDeviceOrientationLandscapeleft)
            }

            if ((rotation == Surface.ROTATION_180)
                    || (rotation == Surface.ROTATION_270)) {
                return 1; //reverse landscape   (home在左边，UIDeviceOrientationLandscapeRight)
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

        mDwellTimeSeekBar.setAlwaysShowIndicator(false);
        mPauseForAnswerSeekBar.setAlwaysShowIndicator(false);

    }

    private void showControlPanel() {
        LOGD(TAG, "showControlPanel");
        View controlPanelView = findViewById(R.id.play_control_panel);
        controlPanelView.setVisibility(View.VISIBLE);

        mDwellTimeSeekBar.setAlwaysShowIndicator(true);
        mPauseForAnswerSeekBar.setAlwaysShowIndicator(true);
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

        }, 5000);
    }





    class CountDownTimerTask extends  TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int value = Integer.parseInt(mCounterDownTextView.getText().toString());
                    if (value == 1) {
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

            //我们在Q/A切换过程中不允许执行
            if (mIsSwitchQuestionAnswerViewManually_Processing) {
                return;
            }

            LOGD("UtteranceProgressListener", "onStart");

        }

        /*
         * 一定是在非UI线程中
         */
        @Override
        public void onDone(String utteranceId) {

            //我们在Q/A切换过程中不允许执行
            if (mIsSwitchQuestionAnswerViewManually_Processing) {
                return;
            }

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
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");//必不可少
                if (textToSpeechArray.size() > mTextToSpeechContentArrayIndex && (mTTS != null)) {
                    String text = textToSpeechArray.get(mTextToSpeechContentArrayIndex);
                    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                    LOGD(TAG, "TTS Speak text: " + text);
                }

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

    /*
     * 触发当：
     * 1. view created
     * 2. 切换到下一个卡片或QA switch
     * 3. 或者landscape到reverse landscape
     */
    private ViewTreeObserver.OnGlobalLayoutListener mRotationChangeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            LOGD(TAG, "onGlobalLayout");

            //但是我们无法确定是否在rotating,只能确定它的确经过了一个rotation动作

            resetRoll = true;
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


    private OnSwipeTouchListener mSwipeTouchListener = new OnSwipeTouchListener(AppContext.getAppContext()) {
        @Override
        public void onSwipeLeft() {
            super.onSwipeLeft();

            //实际中，我们没有用到，而是让view pager自身处理左右滑动的回调

        }

        @Override
        public void onSwipeRight() {
            super.onSwipeRight();

            //实际中，我们没有用到，而是让view pager自身处理左右滑动的回调
        }

        @Override
        public void onSwipeTop() {
            super.onSwipeTop();

            if (mIsAutoScroll == false) {
                switchQuestionAnswerViewManually(true);  //not allow to switch during auto play mode
            }
        }

        @Override
        public void onSwipeBottom() {
            super.onSwipeBottom();

            if (mIsAutoScroll == false) {
                switchQuestionAnswerViewManually(true);  //not allow to switch during auto play mode
            }
        }

        @Override
        public void onSimpleTapConfirmed() {
            super.onSimpleTapConfirmed();

            if (mIsAutoScroll == false) {
                switchQuestionAnswerViewManually(true);  //not allow to switch during auto play mode
            }
        }
    };
}