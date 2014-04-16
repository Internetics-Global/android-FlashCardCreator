package com.internectics.fragment;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.helper.AudioHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.AppContext;

import java.io.File;

/**
 * Created by bournewang on 4/16/14.
 */
public class CreateSoundFragment extends DialogFragment {

    public View mContentView;

    private TextView mDescriptionTextView;
    private Button   mRecordButton;
    private Button   mPlayButton;
    private Button   mSaveButton;

    public Card      mCurrentCard;
    public Boolean   mIsQuestionShowing;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        AudioHelper.setupAudioRecord(temporaryRecordedSoundPath().toString());

        mContentView = inflater.inflate(R.layout.create_sound, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mDescriptionTextView = (TextView) mContentView
                .findViewById(R.id.descripton_textview);
        mRecordButton = (Button) mContentView
                .findViewById(R.id.record_button);
        mPlayButton = (Button) mContentView
                .findViewById(R.id.play_button);
        mSaveButton = (Button) mContentView
                .findViewById(R.id.save_button);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButtonClicked();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonClicked();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });

        mSaveButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioHelper.releaseRecord();
    }

    private void recordButtonClicked() {

        mPlayButton.setEnabled(false);
        mPlayButton.setVisibility(View.INVISIBLE);
        mSaveButton.setVisibility(View.INVISIBLE);
        TimerAysncTask dTask = new TimerAysncTask();
        dTask.execute(100);

        AudioHelper.startRecord();



    }

    private void playButtonClicked() {

        AudioHelper.playAudio(temporaryRecordedSoundPath().toString());

    }

    private void saveButtonClicked() {
        dismiss();

        String saveToPath = "";
        if (mIsQuestionShowing) {
            saveToPath = mCurrentCard.question.audioUriFormatStr;
        } else {
            saveToPath = mCurrentCard.answer.audioUriFormatStr;
        }

        if (saveToPath.length() == 0) {
            File saveToFile = FileOperationHelper.generateUniqueAudioAACFilePath();
            saveToPath = saveToFile.toString();

            if (mIsQuestionShowing) {
                mCurrentCard.question.audioUriFormatStr = FileOperationHelper.convertToUriFormatFile(saveToFile);
            } else {
                mCurrentCard.answer.audioUriFormatStr = FileOperationHelper.convertToUriFormatFile(saveToFile);
            }

        } else {
            saveToPath = FileOperationHelper.deleteUriSchemeHeader(saveToPath);
        }

        File sourceFile = temporaryRecordedSoundPath();
        FileOperationHelper.moveFile(sourceFile.toString(),saveToPath);

        mCurrentCard.save(AppContext.getAppContext());

    }


    public static File temporaryRecordedSoundPath() {
        File tempFile = new File(FileOperationHelper.cacheDirectory(), "temp.aac");
        return tempFile;
    }


    class TimerAysncTask extends AsyncTask<Integer, Integer, String> {

        int k_maxTime = 100; //10 seconds
        int i = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {

            while (i < 100) {
                try {
                    publishProgress(10- i/10); //剩余时间
                    i++;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            mDescriptionTextView.setText("Time left:" + progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mPlayButton.setEnabled(true);
            mSaveButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
            mDescriptionTextView.setText("When you click Start you will have five seconds in which to record your alarm. You can then Play it back for review or Save it for use in the app.");

            AudioHelper.stopRecord();
        }

    }
}
