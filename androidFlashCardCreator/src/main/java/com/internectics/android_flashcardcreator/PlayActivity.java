package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.helper.AudioHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.model.CardListModel;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
import com.internectics.util.UIHelper;
import com.internectics.util.VGViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;
import timber.log.Timber;

public class PlayActivity extends FragmentActivity implements SensorEventListener,VGViewPager.OnViewPagerClickListener{

    private Pack mCurrentPack;
    private int mPosition = 0;
    private List<Fragment> mFragments;

    private SensorManager mSensorManager;
    private boolean       mIsSensorAvailable;

    private float mOrigalRoll = 0;
    private boolean mIsResetRoll = false;
    private boolean mEnableA = true;
    private boolean mEnableB = true;

    private AutoScrollViewPager mPager;

    private GestureDetector mGestureDetector;

    private boolean mIsScrollStop = true;

    private ImageButton mCyclePlayImageButton;
    private ImageButton mAutoScrollImageButton;
    private SeekBar     mAutoPlaySpeedSeekBar;
    private ImageButton mPlayRecordImageButton;
    private ImageButton mMuteImageButton;

    private boolean     mIsAutoScroll;
    private boolean     mIsCyclePlay;
    private boolean     mIsMute;

    private boolean     mIsAutoShowQuestionOnly = true;
    private Timer       mAutoSwitchQATimer;


    private boolean   mRunOnceFlag; //only allow to run once

    //Text to speech related
    private int          mTextToSpeechContentArrayIndex;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        int packID = getIntent().getIntExtra("packID", -1);
        mCurrentPack = CardListModel.getPack(packID);

        setContentView(R.layout.play);
        getActionBar().hide();


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

        mAutoPlaySpeedSeekBar = (SeekBar) findViewById(R.id.auto_play_speed_seekbar);
        mAutoPlaySpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                //when auto scroll, mAutoPlaySpeedSeekBar is disabled, which is different with iOS version

                //TODO: _currentPack.autoPlaySpeed = slider.value
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        if (AppConfig.sharedInstance().isMute()) {
          mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_off));
        } else {
            mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_on));
        }
        mMuteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConfig.sharedInstance().isMute()) {
                    mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_off));
                    AppConfig.sharedInstance().setMute(false);
                } else {
                    mMuteImageButton.setImageDrawable(getResources().getDrawable(R.drawable.sound_on));
                    AppConfig.sharedInstance().setMute(true);
                }
            }
        });



        mFragments = getFragments();
        FCCPageAdapter pageAdapter = new FCCPageAdapter(getSupportFragmentManager(), mFragments);
        mPager = (AutoScrollViewPager) findViewById(R.id.viewpager);
        //mPager.setOnViewPagerClickListener(this); TODO:XXX






        //used to get rid of interrupt during scroll
        View playMask = findViewById(R.id.play_mask);
        playMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mIsScrollStop == false);
            }
        });

        //Keep same size with non-playmode
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPager.getLayoutParams();
        double screenWidth = UIHelper.getScreenWidth(this);
        double screenHeight = UIHelper.getScreenHeight(this);
        double ratio = screenHeight/screenWidth;
        double marginHorizontal;
        double marginVertical;
        double widthOfCard;
        double heightOfCard;

        double ratioCard = UIHelper.getCardRatio(this);

        if (ratio >= ratioCard) {
            //mean we should use width as reference
            marginHorizontal = 10;
            widthOfCard = screenWidth - 2*marginHorizontal;
            heightOfCard = (int)(widthOfCard *ratioCard);
            marginVertical = (screenHeight - heightOfCard)/2;
        } else {
            marginVertical = 10;
            heightOfCard = screenHeight - 2*marginVertical;
            widthOfCard = (int) (heightOfCard / ratioCard);
            marginHorizontal = (screenWidth - widthOfCard)/2;
        }

        Global.scaleInPlayMode = (float)(widthOfCard/Global.widthOfCardInEditMode);
        if ((Global.scaleInPlayMode >2) || (Global.scaleInPlayMode <0.5)) {
            Timber.tag(Global.debugTag).e("the value of scaleInPlayMode is out of normal value");
            Global.scaleInPlayMode = (float)1.2; //default value
        }

        marginLayoutParams.leftMargin = (int)marginHorizontal;
        marginLayoutParams.rightMargin =  (int)marginHorizontal;
        marginLayoutParams.topMargin =  (int)marginVertical;
        marginLayoutParams.bottomMargin =  (int)marginVertical;
        mPager.setLayoutParams(marginLayoutParams);

        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(pageAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if ((mPosition != i) && (i2 == 0)) {
                    Timber.tag(Global.debugTag).i( "onPageScrolled, page index=" + i + " .mPosition=" + mPosition);

                    ((CardDetailFragment) (mFragments.get(i))).switchToQuestionView(false);

                    //hide or show play recorded voice
                    String soundFile = ((CardDetailFragment) (mFragments.get(i))).mCurrentCard.question.audioUriFormatStr;
                    if (soundFile.length() == 0) {
                        mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
                    } else {
                        mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
                    }

                    //Restore previous card to question view
                    ((CardDetailFragment) (mFragments.get(mPosition))).switchToQuestionView(false);

                    mPosition = i;

                    mIsScrollStop = true;

                    setActiveFragmentTag(i);

                    exeuteTextToSpeechOrPlayAudio((CardDetailFragment) (mFragments.get(i)));

                    if (mIsAutoScroll && mIsAutoShowQuestionOnly == false) {
                        if (mAutoSwitchQATimer != null) {
                            mAutoSwitchQATimer.cancel();
                            mAutoSwitchQATimer = null;
                        }
                        mAutoSwitchQATimer = new Timer();
                        TimerTask updateBall = new SwitchQATimer();
                        mAutoSwitchQATimer.scheduleAtFixedRate(updateBall, getAutoPlaySpeedMilliSeconds() / 2 - 500, 3600*1000);
                    }



                } else {
                    //只会运行一次
                    if (mRunOnceFlag == false) {
                      setActiveFragmentTag(0);
                      mRunOnceFlag = true;
                    }
                }
            }


            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }

        });


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //check first card to determine whether to hide play sound button
        CardDetailFragment firstDetailFragment = ((CardDetailFragment) (mFragments.get(0)));
        String soundFile = firstDetailFragment.mCurrentCard.question.audioUriFormatStr;
        if (soundFile.length() == 0) {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
        } else {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
        }

        setupTextToSpeech((CardDetailFragment) (mFragments.get(mPosition)));

    }

    private int getAutoPlaySpeedMilliSeconds () {
        int interval = mAutoPlaySpeedSeekBar.getProgress();
        if (interval == 0) {
            interval = Global.k_Default_Auto_Play_Speed *1000;
        }
        return interval;
    }

    private void playRecordedSoundImageButtonClicked() {

        boolean isEmpty = true;
        CardDetailFragment cardDetailFragment = (CardDetailFragment) (mFragments.get(mPosition));
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

            new SweetAlertDialog(this)
                .setTitleText("Alert")
                .setContentText("There is no audio on the question card")
                .show();

        } else {
            playAudio();
        }


    }

    private void autoScrollImageButtonClicked() {

        if (mIsAutoScroll == false) {
            new AlertDialog.Builder(PlayActivity.this)
                    .setMessage("Please select")
                    .setPositiveButton("Show question only", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mIsAutoShowQuestionOnly = true;
                            autoScrollPopoveriewItemSelected();
                        }
                    })
                    .setNegativeButton("Both question and answer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mIsAutoShowQuestionOnly = false;
                            autoScrollPopoveriewItemSelected();
                        }
                    })
                    .show();
        } else {
            mIsAutoScroll = false;
            mAutoPlaySpeedSeekBar.setEnabled(true);
            mAutoScrollImageButton.setImageDrawable(getResources().getDrawable(R.drawable.autoplay_off));
            mPager.stopAutoScroll();

            if (mAutoSwitchQATimer != null) {
                mAutoSwitchQATimer.cancel();
                mAutoSwitchQATimer = null;
            }
        }
    }

    private void autoScrollPopoveriewItemSelected() {
        mIsAutoScroll = true;
        mAutoPlaySpeedSeekBar.setEnabled(false);
        mAutoScrollImageButton.setImageDrawable(getResources().getDrawable(R.drawable.autoplay_on));

        int interval = getAutoPlaySpeedMilliSeconds();
        mPager.setInterval(interval);
        mPager.startAutoScroll();

        mAutoSwitchQATimer = new Timer();
        TimerTask updateBall = new SwitchQATimer();
        mAutoSwitchQATimer.scheduleAtFixedRate(updateBall, getAutoPlaySpeedMilliSeconds() / 2 - 500, 3600*1000);


    }

    private void cyclePlayImageButtonClicked() {
        if (mIsCyclePlay) {
            mIsCyclePlay = false;
            mCyclePlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.repeat_unselected));
        } else {
            mIsCyclePlay = true;
            mCyclePlayImageButton.setImageDrawable(getResources().getDrawable(R.drawable.repeat_selected));
        }

        mPager.setCycle(mIsCyclePlay);

    }

    private void exeuteTextToSpeechOrPlayAudio(CardDetailFragment cardDetailFragment) {

        if (AppConfig.sharedInstance().isMute()) {
            Timber.tag(Global.debugTag).i("Can not playAudio because of mute");
            return;
        }

        if (AppConfig.sharedInstance().isTextToSpeech()) {
            textToSpeechAllContentNow(cardDetailFragment);//先text-to-speech，然后再播放audio
        } else {
            playAudio();
        }
    }

    private void playAudio() {


        CardDetailFragment cardDetailFragment = (CardDetailFragment) (mFragments.get(mPosition));

        String targetStr;
        if (cardDetailFragment.mIsQuestionShowing) {
            targetStr = cardDetailFragment.mCurrentCard.question.audioUriFormatStr;
        } else {
            targetStr = cardDetailFragment.mCurrentCard.answer.audioUriFormatStr;
        }

        if (targetStr.length() >0) {
            Boolean isSimulator = Build.FINGERPRINT.startsWith("generic");
            if (isSimulator) {
                Toast.makeText(PlayActivity.this,"Audio possily could not be supported on simulator",Toast.LENGTH_LONG).show();
            } else {
                AudioHelper.playAudio(FileOperationHelper.deleteUriSchemeHeader(targetStr));
            }

        } else {
            //Toast.makeText(PlayActivity.this,"Not available audio file", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME); //considering different hardware, we need to set the fastest value
            mIsSensorAvailable = true;
        } else {
            mIsSensorAvailable = false;
            Timber.tag(Global.debugTag).w("No Sensor.TYPE_ORIENTATION exists");
        }


        mIsResetRoll = true;


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsSensorAvailable) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTS != null) {
            if (mTTS.isSpeaking()) {
                mTTS.stop();
            }
            mTTS.shutdown();
        }

        mFragments.clear();
        mFragments = null;
    }

    @Override
    public void OnViewPagerClickListener() {
        switchQuestionAnswerView();
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


    private List<Fragment> getFragments() {

        ArrayList<Card> cardsArray = mCurrentPack.cards;
        int size = cardsArray.size();

        List<Fragment> fList = new ArrayList<Fragment>();

        if (mCurrentPack == null) {
            Timber.tag(Global.debugTag).w( "mCurrentPack could not be null in PlayActictiy");
            return fList;
        }

        for (int i = 0; i < size; i++) {
            CardDetailFragment cardDetailFragment = new CardDetailFragment();
            cardDetailFragment.configureParameters(mCurrentPack, cardsArray.get(i), 2);
            fList.add(i, cardDetailFragment);
            Timber.tag(Global.debugTag).d( String.format("new CardDetailFragment %d", i));

        }

        if (AppConfig.sharedInstance().isRandomPlay()) {
            Collections.shuffle(fList);
        }

        return fList;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        CardDetailFragment cardDetailFragment = ((CardDetailFragment) (mFragments.get(mPosition)));
        if ((cardDetailFragment == null) || (cardDetailFragment.mCardSN == null))  {
            //this could happen when cardDetailFragment is not full inflated
            //Timber.tag(Global.debugTag).w( "cardDetailFragment is not fully intialized during play mode");
            return;
        }


        if (mIsResetRoll) {
            mOrigalRoll = event.values[2];
            mIsResetRoll = false;
        }


        //range of values is 90 degrees to -90 degrees.
        float roll = event.values[2];
        //Timber.tag(Global.debugTag).i( "roll angle =" + roll);

        int orientation = getOrientation();
        if (orientation == 0) {
            if ((roll - mOrigalRoll > 15.0) && (mEnableA)) {
                cardDetailFragment.switchQuestionAnswerView();
                exeuteTextToSpeechOrPlayAudio(cardDetailFragment);
                mEnableA = false;
            }
            if (roll - mOrigalRoll < 0) {
                mEnableA = true;
            }

        } else if ((orientation == 1) && (mEnableB)) {
            if (roll - mOrigalRoll < -15.0) {
                cardDetailFragment.switchQuestionAnswerView();
                exeuteTextToSpeechOrPlayAudio(cardDetailFragment);
                mEnableB = false;
            }

            if (roll - mOrigalRoll > 0) {
                mEnableB = true;
            }
        }



    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }


    /**
     * -1, other orientation; 0, landscape; 1. reverse landscape
     */
    private int getOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if ((rotation == Surface.ROTATION_0)
                    || (rotation == Surface.ROTATION_90)) {
                //Timber.tag(Global.debugTag).d( "current rotation is landscape");
                return 0; //landscape (for nexus 7, camera is left side of screen)
            }

            if ((rotation == Surface.ROTATION_180)
                    || (rotation == Surface.ROTATION_270)) {
                //Timber.tag(Global.debugTag).d( "current rotation is reverselandscape");
                return 1; //reverse landscape   (for nexus 7, camera is right side of screen)
            }

        }

        return -1; //other rotation

    }


    private void switchQuestionAnswerView() {

        if (mAutoSwitchQATimer != null) {
            mAutoSwitchQATimer.cancel();
            mAutoSwitchQATimer = null;
        }

        CardDetailFragment targetDetailFragment = ((CardDetailFragment) (mFragments.get(mPosition)));

        targetDetailFragment.switchQuestionAnswerView();

        //hide or show play recorded voice
        String soundFile;
        if (targetDetailFragment.mIsQuestionShowing) {
            soundFile = targetDetailFragment.mCurrentCard.question.audioUriFormatStr;
        } else {
            soundFile = targetDetailFragment.mCurrentCard.answer.audioUriFormatStr;
        }
        if (soundFile.length() == 0) {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_dimmed));
        } else {
            mPlayRecordImageButton.setImageDrawable(getResources().getDrawable(R.drawable.play25_normal));
        }

        setActiveFragmentTag(mPosition);

        exeuteTextToSpeechOrPlayAudio(targetDetailFragment);
    }





    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {
            if (mIsSensorAvailable) {
                mSensorManager.unregisterListener(this);
            }
        }
        return super.onKeyDown(keyCode, event);  // need to use super to exit current activity
    }


    private void setupTextToSpeech(final CardDetailFragment cardDetailFragment) {

        if (mTTS == null) {
            mTTS = new TextToSpeech(this,new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Timber.tag(Global.debugTag).i("TTS", "Initilization Success");


                        mTTS.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {

                                mTextToSpeechContentArrayIndex ++;

                                CardDetailFragment cardDetailFragment = ((CardDetailFragment) (mFragments.get(mPosition)));

                                ArrayList<String> textToSpeechArray = cardDetailFragment.textToSpeechContentArray();
                                if (textToSpeechArray.size() > mTextToSpeechContentArrayIndex) {

                                    try {
                                        Thread.sleep(500);
                                        HashMap<String, String> params = new HashMap<String, String>();
                                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");//必不可少
                                        mTTS.speak(textToSpeechArray.get(mTextToSpeechContentArrayIndex),TextToSpeech.QUEUE_FLUSH,params);
                                        Timber.tag(Global.debugTag).d("speak" + textToSpeechArray.get(mTextToSpeechContentArrayIndex));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    playAudio();
                                }

                            }
                        });

                        exeuteTextToSpeechOrPlayAudio(cardDetailFragment); //once setup is finished, automatically playback


                    } else {
                        Timber.tag(Global.debugTag).e("TTS", "Initilization Failed!");
                    }
                }
            });
            mTTS.setSpeechRate((float)0.7);




        }

    }


    private void textToSpeechAllContentNow(CardDetailFragment cardDetailFragment) {

        if (AppConfig.sharedInstance().isMute()) {
            return;
        }

        ArrayList<String> textToSpeechArray = cardDetailFragment.textToSpeechContentArray();
        if (textToSpeechArray.size() >0) {

            if (mTTS.isSpeaking()) {
                mTTS.stop();
            }

            mTextToSpeechContentArrayIndex = 0;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");//必不可少
            mTTS.speak(textToSpeechArray.get(0),TextToSpeech.QUEUE_FLUSH,params);
            Timber.tag(Global.debugTag).d("speak" + textToSpeechArray.get(mTextToSpeechContentArrayIndex));

        }  else {
            playAudio();
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


    class SwitchQATimer extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchQuestionAnswerView();
                }
            });
        }
    }



}
