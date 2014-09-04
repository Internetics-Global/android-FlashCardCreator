package com.internectics.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackRecordHelper;
import com.internectics.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AddPackFragment extends DialogFragment implements TextView.OnEditorActionListener {

    public View mContentView;
    public Pack pack;
    private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;

    private EditText mPackNameEditText;
    private EditText mSidebarTitleEditText;
    private EditText mCreatorEditText;
    private EditText mJobTitleEditText;

    private InputMethodManager mIMM;

    private ArrayList<Pack> mPacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pack = new Pack();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.fragment_add_pack, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView titleTextView = (TextView) mContentView
                .findViewById(R.id.dialog_title);
        titleTextView.setText(R.string.addpack_title);
        final Button closeButton = (Button) mContentView
                .findViewById(R.id.dialog_head_close_btn);
        Button saveButton = (Button) mContentView
                .findViewById(R.id.dialog_head_save_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIMM.isActive()) {
                    mIMM.hideSoftInputFromInputMethod(closeButton.getWindowToken(), 0);
                }
                dismiss();

            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                save();

            }
        });

        ImageView coverImageView = (ImageView) mContentView
                .findViewById(R.id.fragment_add_pack_coverImage);
        coverImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY);

            }
        });

        mIMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);


        mPackNameEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_pack_name);
        mSidebarTitleEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_sidebar_title);
        mCreatorEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_creator);
        mJobTitleEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_job_title);

        mPackNameEditText.setOnEditorActionListener(this);
        mSidebarTitleEditText.setOnEditorActionListener(this);
        mCreatorEditText.setOnEditorActionListener(this);
        mJobTitleEditText.setOnEditorActionListener(this);

        //this get mPacks is a time-cost operation, we put in background
        new Thread()
        {
            @Override
            public void run() {
                mPacks = User.defaultUser(AppContext.getAppContext()).packs;
            }
        }.start();

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        mContentView.setLayoutParams(params);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                mIMM.showSoftInput(mPackNameEditText, 0);
            }

        }, 500);

    }

    private void save() {


        if (checkExistingPackName(mPackNameEditText.getText().toString())) {
            Toast.makeText(getActivity(), "Existing pack name, please rename it", Toast.LENGTH_SHORT).show();
            return;
        }

        pack.packName = mPackNameEditText.getText().toString();
        pack.sidebarTitle = mSidebarTitleEditText.getText().toString();
        pack.creatorNickName = mCreatorEditText.getText().toString();
        pack.jobTitle = mJobTitleEditText.getText().toString();
        pack.platform = UIHelper.getCurrentPlatform();
        // we set pack.coverImageUriFormatStr in image select or by default
        pack.creatorID = OpenUDID_manager.getOpenUDID();
        pack.platform = UIHelper.getCurrentPlatform();
        pack.userID = Global.USER_ID;
        pack.packID = Global.generateNoRepeatInt();
        pack.createDate = (int)System.currentTimeMillis();
        pack.lastVistDate = (int)System.currentTimeMillis();

        final Card defaultCard = new Card();
        defaultCard.cardSN = 1;
        defaultCard.packID = pack.packID;

        PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), pack);

        dismiss();

        final Activity activity = getActivity();
        new Thread() {
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        User.defaultUser(AppContext.getAppContext()).addPack(pack);
                        pack.addCard(AppContext.getAppContext(),defaultCard);

                        Intent intent = new Intent();
                        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_NEW_PACK);
                        if (activity != null) {
                            activity.sendBroadcast(intent);
                        }

                    }
                });
            };
        }.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY) {

            ((MainActivity)getActivity()).mIsAllowedToShowPackList = false;

            if (resultCode == Activity.RESULT_OK) {

                Bitmap resultBitmap = null;
                Uri selectedImageURI = data.getData();

                //step1: get image
                final String[] filePathColumn = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };
                Cursor cursor = getActivity().getContentResolver().query(selectedImageURI, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex;
                    // if it is a picasa image on newer devices with OS 3.0 and up
                    if ((selectedImageURI.toString().startsWith("content://com.google.android.gallery3d"))
                            ||(selectedImageURI.toString().startsWith("content://com.sec.android.gallery3d"))){
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                        if (columnIndex != -1) {
                            final Uri picasaUri = selectedImageURI;
                            resultBitmap = UIHelper.getResized400SizeBitmapFromPicasa(getActivity(), picasaUri);
                        }
                    } else { // it is a regular local image file
                        resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
                    }
                    cursor.close();
                }

                if (resultBitmap == null) {
                    Log.w(Global.debugTag, "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    ImageView coverImageView = (ImageView) mContentView
                            .findViewById(R.id.fragment_add_pack_coverImage);
                    coverImageView.setImageBitmap(resultBitmap);

                    pack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Log.d(Global.debugTag, "pack.coverImageUriFormatStr = " + pack.coverImageUriFormatStr);
                }
            }
        }
    }


    private boolean checkExistingPackName(String packName) {
        if (mPacks == null) {
            mPacks = User.defaultUser(AppContext.getAppContext()).packs;
        }
        if (mPacks.size() == 0) {
            return false;
        }
        for (Pack pack : mPacks) {
            if (pack.packName.equals(packName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_DONE:
                mIMM.hideSoftInputFromInputMethod(v.getWindowToken(), 0);
                break;
        }
        return false;
    }
}
