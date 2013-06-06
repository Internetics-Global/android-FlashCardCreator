package com.internectics.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
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

    private InputMethodManager mIMM;

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
        titleTextView.setText("Add Pack");
        final Button closeButton = (Button) mContentView
                .findViewById(R.id.dialog_head_close_btn);
        Button saveButton = (Button) mContentView
                .findViewById(R.id.dialog_head_save_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIMM.isActive()) {
                    mIMM.hideSoftInputFromInputMethod(closeButton.getWindowToken(),0);
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

        mPackNameEditText.setOnEditorActionListener(this);
        mSidebarTitleEditText.setOnEditorActionListener(this);
        mCreatorEditText.setOnEditorActionListener(this);

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
        pack.platform = UIHelper.getCurrentPlatform();
        // we set pack.coverImageUriFormatStr in image select or by default
        pack.creatorID = OpenUDID_manager.getOpenUDID();
        pack.platform = UIHelper.getCurrentPlatform();
        pack.userID = Global.USER_ID;
        pack.packID = Global.generateNoRepeatInt();
        pack.save(AppContext.getAppContext());

        Card defaultCard = new Card();
        defaultCard.cardSN = 1;
        defaultCard.packID = pack.packID;
        defaultCard.save(AppContext.getAppContext());

        PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), pack);

        AppConfig.sharedInstance().set(Global.mostRecentPackCreatedID_Property, String.format("%d", pack.packID));
        AppConfig.sharedInstance().set(Global.mostRecentPackCreatedDate_Property, StringUtils.getCurrentTimeDate());

        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_NEW_PACK);
        getActivity().sendBroadcast(intent);

        dismiss();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageURI = data.getData();

                Bitmap resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
                if (resultBitmap == null) {
                    Log.d(Global.debugTag, "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    ImageView coverImageView = (ImageView) mContentView
                            .findViewById(R.id.fragment_add_pack_coverImage);
                    coverImageView.setImageBitmap(resultBitmap);

                    pack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Log.d(Global.debugTag, pack.coverImageUriFormatStr);
                }
            }
        }
    }


    private boolean checkExistingPackName(String packName) {
        ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;
        if (packs.size() ==0){
            return false;
        }
        for (Pack pack:packs) {
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
                mIMM.hideSoftInputFromInputMethod(v.getWindowToken(),0);
                break;
        }
        return false;
    }
}
