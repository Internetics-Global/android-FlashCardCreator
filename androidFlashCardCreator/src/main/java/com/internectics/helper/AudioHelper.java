package com.internectics.helper;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.widget.Toast;

import com.internectics.fragment.CardDetailFragment;

import junit.framework.Assert;

import java.io.IOException;

/**
 * Created by bournewang on 4/13/14.
 */
public class AudioHelper {

    private static MediaRecorder mRecorder;
    private static MediaPlayer   mp;

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

        stopAudio();

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

        stopAudio();

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
              mp.release();
              mp = null;
            }
        });

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.release();
                mp = null;
                return false;
            }
        });
    }

    public static void stopAudio() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

    /*
     * Clean all Audio Play related resources
     */
    public static void cleanupAudioPlayResource() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }


}
