package com.internectics.android_flashcardcreator;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.*;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.helper.AudioHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.model.CardListModel;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;
import com.internectics.util.UIHelper;
import com.internectics.util.VGViewPager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class PlayActivity extends FragmentActivity implements SensorEventListener,GestureDetector.OnDoubleTapListener,GestureDetector.OnGestureListener {

    private Pack mCurrentPack;
    private int mPosition = 0;
    private List<Fragment> mFragments;

    private SensorManager mSensorManager;
    private boolean       mIsSensorAvailable;

    private float mOrigalRoll = 0;
    private boolean mIsResetRoll = false;
    private boolean mEnableA = true;
    private boolean mEnableB = true;

    private VGViewPager mPager;

    private GestureDetector mGestureDetector;

    private boolean mIsScrollStop = true;

    private ImageView mPlayRecordImage;

    private ImageView mMuteImage;

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

        mPlayRecordImage = (ImageView) findViewById(R.id.play_record_button);
        mPlayRecordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              playAudio();
            }
        });

        mMuteImage = (ImageView) findViewById(R.id.mute_button);
        if (AppConfig.sharedInstance().isMute()) {
          mMuteImage.setImageDrawable(getResources().getDrawable(R.drawable.mute_button));
        } else {
            mMuteImage.setImageDrawable(getResources().getDrawable(R.drawable.un_mute_button));
        }
        mMuteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConfig.sharedInstance().isMute()) {
                    mMuteImage.setImageDrawable(getResources().getDrawable(R.drawable.un_mute_button));
                    AppConfig.sharedInstance().setMute(false);
                } else {
                    mMuteImage.setImageDrawable(getResources().getDrawable(R.drawable.mute_button));
                    AppConfig.sharedInstance().setMute(true);
                }
            }
        });



        mFragments = getFragments();
        FCCPageAdapter pageAdapter = new FCCPageAdapter(getSupportFragmentManager(), mFragments);
        mPager = (VGViewPager) findViewById(R.id.viewpager);




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
            Log.e(Global.debugTag,"the value of scaleInPlayMode is out of normal value");
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
                    Log.i(Global.debugTag, "onPageScrolled, page index=" + i + " .mPosition=" + mPosition);

                    ((CardDetailFragment) (mFragments.get(i))).switchToQuestionView(false);

                    //hide or show play recorded voice
                    String soundFile = ((CardDetailFragment) (mFragments.get(i))).mCurrentCard.question.audioUriFormatStr;
                    if (soundFile.length() == 0) {
                        mPlayRecordImage.setVisibility(View.INVISIBLE);
                    } else {
                        mPlayRecordImage.setVisibility(View.VISIBLE);
                    }

                    //Restore previous card to question view
                    ((CardDetailFragment) (mFragments.get(mPosition))).switchToQuestionView(false);

                    mPosition = i;

                    mIsScrollStop = true;
                    Log.d(Global.debugTag, "Stopped");

                    exeuteTextToSpeechOrPlayAudio((CardDetailFragment) (mFragments.get(i)));



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

        mGestureDetector = new GestureDetector(this);

        //check first card to determine whether to hide play sound button
        CardDetailFragment firstDetailFragment = ((CardDetailFragment) (mFragments.get(0)));
        String soundFile = firstDetailFragment.mCurrentCard.question.audioUriFormatStr;
        if (soundFile.length() == 0) {
            mPlayRecordImage.setVisibility(View.INVISIBLE);
        } else {
            mPlayRecordImage.setVisibility(View.VISIBLE);
        }

        setupTextToSpeech((CardDetailFragment) (mFragments.get(mPosition)));

    }

    private void exeuteTextToSpeechOrPlayAudio(CardDetailFragment cardDetailFragment) {

        if (AppConfig.sharedInstance().isMute()) {
            Log.i(Global.debugTag,"Can not playAudio because of mute");
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
            Log.w(Global.debugTag, "No Sensor.TYPE_ORIENTATION exists");
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
            Log.w(Global.debugTag, "mCurrentPack could not be null in PlayActictiy");
            return fList;
        }

        for (int i = 0; i < size; i++) {
            CardDetailFragment cardDetailFragment = new CardDetailFragment();
            cardDetailFragment.configureParameters(mCurrentPack, cardsArray.get(i), 2);
            fList.add(i, cardDetailFragment);
            Log.d(Global.debugTag, String.format("new CardDetailFragment %d", i));

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
            //Log.w(Global.debugTag, "cardDetailFragment is not fully intialized during play mode");
            return;
        }


        if (mIsResetRoll) {
            mOrigalRoll = event.values[2];
            mIsResetRoll = false;
        }


        //range of values is 90 degrees to -90 degrees.
        float roll = event.values[2];
        //Log.i(Global.debugTag, "roll angle =" + roll);

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
                //Log.d(Global.debugTag, "current rotation is landscape");
                return 0; //landscape (for nexus 7, camera is left side of screen)
            }

            if ((rotation == Surface.ROTATION_180)
                    || (rotation == Surface.ROTATION_270)) {
                //Log.d(Global.debugTag, "current rotation is reverselandscape");
                return 1; //reverse landscape   (for nexus 7, camera is right side of screen)
            }

        }

        return -1; //other rotation

    }


    private void switchQuestionAnswerView() {

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
            mPlayRecordImage.setVisibility(View.INVISIBLE);
        } else {
            mPlayRecordImage.setVisibility(View.VISIBLE);
        }

        exeuteTextToSpeechOrPlayAudio(targetDetailFragment);
    }



    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(Global.debugTag3, "onSingleTapConfirmed");
        switchQuestionAnswerView();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(Global.debugTag3, "onDoubleTap");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(Global.debugTag3, "onDoubleTapEvent");
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(Global.debugTag3, "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(Global.debugTag3, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(Global.debugTag3, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(Global.debugTag3, "onScroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(Global.debugTag3, "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        Log.d(Global.debugTag3, "OnFlying");

        if ((e1 == null) || (e2 == null)) {
            Log.d(Global.debugTag3, "e1 or e2 is null");
          return true;
        }

        final float xDistance = Math.abs(e1.getX() - e2.getX());

        final float yDistance = Math.abs(e1.getY() - e2.getY());

        if (Math.abs(xDistance) < 100) {
            if (e1.getRawY() < e2.getRawY() - 30) {
                Log.d(Global.debugTag, "Down swipe");
                switchQuestionAnswerView();
            } else if (e1.getRawY() > e2.getRawY() + 10) {
                Log.d(Global.debugTag, "Up swipe");
                switchQuestionAnswerView();
            }

        }

        if (Math.abs(yDistance) < 100) {
            if (e1.getRawX() > e2.getRawX() + 10) {
                Log.d(Global.debugTag, "swipe Left, mPosition is: " + mPosition);

                if (mPosition < mFragments.size() - 1) {
                    mIsScrollStop = false;
                    mPager.setCurrentItem(mPosition + 1, true);
                }

            } else if (e1.getRawX() < e2.getRawX() - 10) {
                Log.d(Global.debugTag, "Swipe Right" + mPosition);

                if (mPosition >= 1) {
                    mIsScrollStop = false;
                    mPager.setCurrentItem(mPosition - 1, true);
                }
            }
        }

        return true;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(Global.debugTag3, "onTouchEvent");
        return mGestureDetector.onTouchEvent(event);
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
                        Log.i("TTS", "Initilization Success");


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
                                        Log.d(Global.debugTag,"speak" + textToSpeechArray.get(mTextToSpeechContentArrayIndex));
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
                        Log.e("TTS", "Initilization Failed!");
                    }
                }
            });




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
            Log.d(Global.debugTag,"speak" + textToSpeechArray.get(mTextToSpeechContentArrayIndex));

        }  else {
            playAudio();
        }



    }



}
