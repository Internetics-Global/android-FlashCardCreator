package com.flipflash.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.util.AppContext;

import junit.framework.Assert;

import java.io.IOException;
import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/**
 * Created by bournewang on 4/13/14.
 */
public class AudioHelper {

    private static final String TAG = AudioHelper.class.getName();

    private static MediaRecorder mRecorder;
    private static MediaPlayer   mp;

    public static boolean isRecordFinished;

    /*
    outputPathString must be a .3gp format, otherwise exception will be thrown
     */
    public static void setupAudioRecord(String outputPathString) {
        LOGD(TAG, "setupAudioRecord");

        Assert.assertTrue("outputPathString is null or is not file with .3gp extension",
                ((outputPathString != null) && (outputPathString.toLowerCase().contains(".3gp") == true)));


        if (mRecorder == null) {
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(outputPathString);

            mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    mRecorder.release();

                }
            });
            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {

                }
            });
        }

    }

    public static void startRecord() {
        LOGD(TAG, "startRecord");

        stopAndCleanAudio();

        try {
            mRecorder.prepare();
            mRecorder.start();
            LOGD(TAG, "startRecord: mRecorder.start() is executed");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void stopRecord() {
        LOGD(TAG, "stopRecord");

        try {
            if (mRecorder != null)  {
                mRecorder.stop();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }




    }

    /*
     * Clean all Recorded related resources
     */
    public static void cleanupRecorderResource() {
        LOGD(TAG, "cleanupRecorderResource");
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public static void playAudio(CardDetailFragment cardDetailFragment,boolean isMute) {
        LOGD(TAG, "playAudio");

        String audioFileStr;
        if (cardDetailFragment.mIsQuestionShowing) {
            audioFileStr = cardDetailFragment.mCurrentCard.question.audioUriFormatStr;
        } else {
            audioFileStr = cardDetailFragment.mCurrentCard.answer.audioUriFormatStr;
        }

        final  boolean   f_IsMute = isMute;
        final  String    f_AudioFileStr = audioFileStr;

        if (audioFileStr.length() >0) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        playAudio(FileOperationHelper.deleteUriSchemeHeader(f_AudioFileStr),f_IsMute);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        } else {
            //Toast.makeText(PlayActivity.this,"Not available audio file", Toast.LENGTH_LONG).show();
        }
    }


    /*
      Just play a sound, no UI
      Support two formats: AAC (iOS), 3GP(android)
     */
    public static void playAudio(String pathString, boolean isMute){
        LOGD(TAG, "playAudio");

        stopAndCleanAudio();

        //set up MediaPlayer
        if (mp == null) {
            mp = new MediaPlayer();
        }

        if (isMute) {
            mp.setVolume(0,0);
        } else {
            mp.setVolume(1,1);
        }

        try {
            mp.setDataSource(pathString);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.start();


        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                cleanupAudioPlayResource();
            }
        });

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                cleanupAudioPlayResource();
                return true;
            }
        });
    }

    public static void stopAndCleanAudio() {
        LOGD(TAG, "stopAndCleanAudio");
        if (mp != null) {

            boolean isPlaying = false;
            try {
                isPlaying = mp.isPlaying();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            if (isPlaying) {
                mp.stop();
            }
            cleanupAudioPlayResource();
        }
    }

    /*
     * Clean all Audio Play related resources
     */
    public static void cleanupAudioPlayResource() {
        LOGD(TAG, "cleanupAudioPlayResource");
        if (mp != null) {
            mp.setVolume(1,1); //this is very important
            if(mp.isPlaying())
                mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }


    /*
     * 实际上，这将控制整个stream的音频播放，而不仅仅是TTS，比如MediaPlayer的输出（如果MediaPlayer的stream也是同一类型
     * 由于TTS没有单独的音量控制，所以只能采用这种方式，这种体验其实是不好的，但是也是唯一的方法.
     */
    public static void muteTTS() {
        LOGD(TAG, "muteTTS");
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);

        int streamType = getAudioHardwareOutputType();
        audioManager.setStreamMute(streamType, true);
    }

    public static void  unmuteTTS() {
        LOGD(TAG, "unmuteTTS");
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);

        int streamType = getAudioHardwareOutputType();
        audioManager.setStreamMute(streamType, false);
    }

    public static int getAudioHardwareOutputType() {
        LOGD(TAG, "getAudioHardwareOutputType");
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isSpeakerphoneOn()) {
            return AudioManager.STREAM_MUSIC;
        } else if (audioManager.isWiredHeadsetOn()) {   //只是检查Checks whether a wired headset is connected or not
            return AudioManager.STREAM_VOICE_CALL;
        } else {
            return AudioManager.STREAM_MUSIC;
        }
    }


}
