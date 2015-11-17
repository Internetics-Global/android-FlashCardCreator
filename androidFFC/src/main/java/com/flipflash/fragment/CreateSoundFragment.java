package com.flipflash.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.helper.AudioHelper;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.util.AppContext;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by bournewang on 4/16/14.
 */
public class CreateSoundFragment extends DialogFragment {
    private static final String TAG = CreateSoundFragment.class.getName();

    public View mContentView;

    private TextView mDescriptionTextView;
    private Button   mRecordButton;
    private Button   mPlayButton;
    private Button   mSaveButton;
    private Button   mCloseButton;
    private Button   mDeleteButton;

    public Card      mCurrentCard;
    public Pack      mCurrentPack;
    public Boolean   mIsQuestionShowing;

    public boolean   mIsCreatingCard = false;


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

        mCloseButton = (Button) mContentView
                .findViewById(R.id.close_button);

        mDeleteButton = (Button) mContentView
                .findViewById(R.id.delete_button);

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

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeButtonClicked();
            }
        });


        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());
                alertDialogBuilder.setTitle(getString(R.string.DIALOG_AlERT));
                alertDialogBuilder
                        .setMessage(getString(R.string.DIALOG_DELETE_PACK));
                alertDialogBuilder.setPositiveButton(getString(R.string.DIALOG_OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteButtonClicked();
                    }
                });
                alertDialogBuilder.setNegativeButton(getString(R.string.DIALOG_CANCEL),null);
                alertDialogBuilder.show();
            }
        });

        mSaveButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);


        return mContentView;
    }

    private void recordButtonClicked() {

        AudioHelper.startRecord();
        AudioHelper.isRecordFinished = true;

        MainActivity mainActivity = (MainActivity)(getActivity());
        mainActivity.dismissCreateSoundFragment(true);

        dismiss();
    }


    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.add_pack_window_height);
        mContentView.setLayoutParams(params);

        if (AudioHelper.isRecordFinished) {

            AudioHelper.stopRecord();

            mSaveButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
            mDescriptionTextView.setText(getString(R.string.Record_Introduction_Text2));

            mRecordButton.setText(getString(R.string.Title_Record_Start));
        } else {

            AudioHelper.setupAudioRecord(temporaryRecordedSoundPath().toString());

            mSaveButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.INVISIBLE);
            mDescriptionTextView.setText(getString(R.string.Record_Introduction_Text));

        }


        String audioUriFormatStr;
        if (mIsQuestionShowing) {
            audioUriFormatStr = mCurrentCard.question.audioUriFormatStr;
        } else {
            audioUriFormatStr = mCurrentCard.answer.audioUriFormatStr;
        }
        if (new File(FileOperationHelper.deleteUriSchemeHeader(audioUriFormatStr)).exists()) {
            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            mDeleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RefWatcher refWatcher = AppContext.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    private void deleteButtonClicked() {
        String audioUriFormatStr;
        if (mIsQuestionShowing) {
            audioUriFormatStr = mCurrentCard.question.audioUriFormatStr;
        } else {
            audioUriFormatStr = mCurrentCard.answer.audioUriFormatStr;
        }
        boolean result = new File((FileOperationHelper.deleteUriSchemeHeader(audioUriFormatStr))).delete();

        if (result) {
            mDeleteButton.setVisibility(View.INVISIBLE);
        } else {
            new SweetAlertDialog(getActivity())
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText("Fail to delete or file does not exit")
                    .show();
        }
    }

    private void closeButtonClicked() {

        AudioHelper.isRecordFinished = false;
        MainActivity mainActivity = (MainActivity)(getActivity());
        mainActivity.dismissCreateSoundFragment(false);

        AudioHelper.cleanupRecorderResource();

        dismiss();
    }


    private void playButtonClicked() {

        AudioHelper.playAudio(temporaryRecordedSoundPath().toString(), false);

    }

    private void saveButtonClicked() {

        AudioHelper.stopAndCleanAudio();

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
        FileOperationHelper.moveFile(sourceFile.toString(), saveToPath);

        if (mIsCreatingCard) {
          //我们在这里不做处理，而是在create card上处理
        } else {
            mCurrentCard.save(AppContext.getAppContext());
        }


        PackRecordHelper.savePackUpdateRecord(mCurrentPack);

        AudioHelper.isRecordFinished = false;

        AudioHelper.cleanupRecorderResource();

        dismiss();

    }



    public static File temporaryRecordedSoundPath() {
        File tempFile = new File(FileOperationHelper.cacheDirectory(), "temp.3gp");
        return tempFile;
    }

}
