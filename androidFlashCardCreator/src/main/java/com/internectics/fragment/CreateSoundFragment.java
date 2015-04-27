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
import com.internectics.data.Pack;
import com.internectics.helper.AudioHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackRecordHelper;
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
    public Pack      mCurrentPack;
    public Boolean   mIsQuestionShowing;

    public boolean mIsCreatingCard = false;

    public enum Record_Status {
        Record_Status_Unkown, Record_Status_Recording, Record_Status_Stop, Record_Status_Normal
    }

    public Record_Status mRecordStatus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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
                if (mRecordStatus == Record_Status.Record_Status_Normal) {
                    mRecordStatus = Record_Status.Record_Status_Recording;
                    mRecordButton.setText("Stop");
                    recordButtonClicked();
                } else if (mRecordStatus == Record_Status.Record_Status_Recording) {
                    mRecordStatus = Record_Status.Record_Status_Stop;

                    mPlayButton.setEnabled(true);
                    mSaveButton.setVisibility(View.VISIBLE);
                    mPlayButton.setVisibility(View.VISIBLE);
                    mDescriptionTextView.setText("When you click ‘Record’ you have a maximum of ten seconds to record your message. \nClick 'Stop' when ready to stop recording. \nYou can then click 'Play' to hear it, or 'Save' to save it to the card.");

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    AudioHelper.stopRecord();
                    AudioHelper.cleanupRecorderResource();
                    mRecordStatus = Record_Status.Record_Status_Normal;
                    mRecordButton.setText("Record");
                }
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

        mRecordStatus = Record_Status.Record_Status_Normal;

        return mContentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioHelper.cleanupRecorderResource();
    }

    private void recordButtonClicked() {

        AudioHelper.setupAudioRecord(temporaryRecordedSoundPath().toString());

        mPlayButton.setEnabled(false);
        mPlayButton.setVisibility(View.INVISIBLE);
        mSaveButton.setVisibility(View.INVISIBLE);

        AudioHelper.startRecord();

        TimerAysncTask dTask = new TimerAysncTask();
        dTask.execute(100);


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
            File saveToFile = FileOperationHelper.generateUniqueAudio3GPFilePath();
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

        if (mIsCreatingCard) {
          //我们在这里不做处理，而是在create card上处理
        } else {
            mCurrentCard.save(AppContext.getAppContext());
        }


        PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), mCurrentPack);

    }


    public static File temporaryRecordedSoundPath() {
        File tempFile = new File(FileOperationHelper.cacheDirectory(), "temp.3gp");
        return tempFile;
    }


    class TimerAysncTask extends AsyncTask<Integer, Integer, String> {

        int i = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {

            while ((i < 600) && (mRecordStatus == Record_Status.Record_Status_Recording)) {
                try {
                    publishProgress(60- i/10); //剩余时间
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
            mDescriptionTextView.setText("When you click ‘Record’ you have a maximum of ten seconds to record your message. \nClick 'Stop' when ready to stop recording. \nYou can then click 'Play' to hear it, or 'Save' to save it to the card.");

            if (mRecordStatus == Record_Status.Record_Status_Recording) {
                AudioHelper.stopRecord();
                AudioHelper.cleanupRecorderResource();
                mRecordButton.setText("Record");
            }

            mRecordStatus = Record_Status.Record_Status_Normal;

        }

    }
}
