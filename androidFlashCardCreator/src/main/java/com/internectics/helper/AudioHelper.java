package com.internectics.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.internectics.fragment.CardDetailFragment;
import com.internectics.util.AppContext;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by bournewang on 4/13/14.
 */
public class AudioHelper {

    private static MediaRecorder mRecorder;
    private static MediaPlayer   mp;

    public static boolean isRecordFinished;

    /*
    outputPathString must be a .3gp format, otherwise exception will be thrown
     */
    public static void setupAudioRecord(String outputPathString) {

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

        stopAndCleanAudio();

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void stopRecord() {

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
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public static void playAudio(CardDetailFragment cardDetailFragment,boolean isMute) {

        String audioFileStr;
        if (cardDetailFragment.mIsQuestionShowing) {
            audioFileStr = cardDetailFragment.mCurrentCard.question.audioUriFormatStr;
        } else {
            audioFileStr = cardDetailFragment.mCurrentCard.answer.audioUriFormatStr;
        }

        if (audioFileStr.length() >0) {
             playAudio(FileOperationHelper.deleteUriSchemeHeader(audioFileStr),isMute);
        } else {
            //Toast.makeText(PlayActivity.this,"Not available audio file", Toast.LENGTH_LONG).show();
        }
    }


    /*
      Just play a sound, no UI
      Support two formats: AAC (iOS), 3GP(android)
     */
    public static void playAudio(String pathString, boolean isMute){

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
        if (mp != null) {

            boolean isPlaying = false;
            try {
                isPlaying = mp.isPlaying();
            } catch (IllegalStateException e) {
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
        if (mp != null) {
            mp.setVolume(1,1); //this is very important
            if(mp.isPlaying())
                mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }


    //由于TTS没有单独的音量控制，所以需要通过AudioManager全局控制，这种体验其实是不好的，但是也是唯一的方法.
    // DON'T use AudioManager to set volume! It will cause many side effects such as disabling silent mode, which will make your users mad!
    public static void muteTTS() {
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);

        int streamType = getAudioHardwareOutputType();
        audioManager.setStreamMute(streamType, true);
    }

    public static void  unmuteTTS() {
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);

        int streamType = getAudioHardwareOutputType();
        audioManager.setStreamMute(streamType, false);
    }

    public static int getAudioHardwareOutputType() {
        AudioManager audioManager=(AudioManager) AppContext.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isSpeakerphoneOn()) {
            return AudioManager.STREAM_MUSIC;
        } else if (audioManager.isWiredHeadsetOn()) {
            return AudioManager.STREAM_VOICE_CALL;
        } else {
            return AudioManager.STREAM_MUSIC;
        }
    }


}
