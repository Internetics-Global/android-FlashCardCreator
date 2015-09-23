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
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackRecordHelper;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import com.internectics.util.UIHelper;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;
import timber.log.Timber;

public class CreateEditFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private View mContentView;
    private Pack pack;

    private boolean mIsEditPack = false ;

    private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;

    private EditText           mPackNameEditText;
    private EditText           mSidebarTitleEditText;
    private EditText           mCreatorEditText;
    private EditText           mJobTitleEditText;
    private DiscreteSeekBar    mAutoPlaySpeedSeekbar;
    private ImageView          mCoverImageView;

    private EditText           mAdminPasswordEditText;
    private EditText           mConfirmAdminPassowrdEditText;

    private InputMethodManager mIMM;

    private ArrayList<Pack> mPacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {

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

        if (mIsEditPack) {
            titleTextView.setText(R.string.editpack_title);
        } else {
            titleTextView.setText(R.string.addpack_title);
        }

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

        mCoverImageView = (ImageView) mContentView
                .findViewById(R.id.fragment_add_pack_coverImage);
        mCoverImageView.setOnClickListener(new View.OnClickListener() {

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

        mAdminPasswordEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_admin_password);
        mConfirmAdminPassowrdEditText = (EditText) mContentView
                .findViewById(R.id.fragment_add_pack_confirm_admin_password);


        mAutoPlaySpeedSeekbar = (DiscreteSeekBar) mContentView.findViewById(R.id.seekbar);


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

        if (mIsEditPack) {
            mPackNameEditText.setText(pack.packName);
            mSidebarTitleEditText.setText(pack.sidebarTitle);
            mCreatorEditText.setText(pack.creatorNickName);
            mJobTitleEditText.setText(pack.jobTitle);
            if (pack.autoPlaySpeed == 0) {
                mAutoPlaySpeedSeekbar.setProgress(Global.k_Default_Auto_Play_Dwell_Time);
            } else {
                mAutoPlaySpeedSeekbar.setProgress(pack.autoPlaySpeed);
            }

            String imagePath = pack.coverImageUriFormatStr;
            mCoverImageView.setImageURI(Uri.parse(imagePath));

            String decodedString = new String(Base64.decode(pack.restorePassword,0));
            mAdminPasswordEditText.setText(decodedString);
            mConfirmAdminPassowrdEditText.setText(decodedString);


        } else {
            pack = new Pack();
        }

        return mContentView;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }

    public void setIsEditPack(boolean mIsEditPack) {
        this.mIsEditPack = mIsEditPack;
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


        if (mAdminPasswordEditText.getText().toString().equals(mConfirmAdminPassowrdEditText.getText().toString()) == false) {
            new SweetAlertDialog(getActivity())
                    .setTitleText("Alert")
                    .setContentText("Passwords do not match")
            .show();
            return;
        }


        if (mAutoPlaySpeedSeekbar.getProgress() > Global.k_MAX_Auto_Play_Speed
                || mAutoPlaySpeedSeekbar.getProgress() < Global.k_MIN_Auto_Play_Speed) {
            new SweetAlertDialog(getActivity())
                    .setTitleText("Alert")
                    .setContentText(String.format("The value of auto play speed should be between %d and %d seconds",
                            Global.k_MIN_Auto_Play_Speed,Global.k_MAX_Auto_Play_Speed))
                    .show();
            return;
        }

        if ((mIsEditPack == false) && (checkExistingPackName(mPackNameEditText.getText().toString()))) {
            new SweetAlertDialog(getActivity())
                .setTitleText("Alert")
                .setContentText("Existing pack name, please rename it")
                    .show();
            return;
        }

        pack.packName = mPackNameEditText.getText().toString();
        pack.sidebarTitle = mSidebarTitleEditText.getText().toString();
        pack.creatorNickName = mCreatorEditText.getText().toString();
        pack.jobTitle = mJobTitleEditText.getText().toString();
        pack.platform = UIHelper.getCurrentPlatform();
        pack.platform = UIHelper.getCurrentPlatform();
        pack.userID = Global.USER_ID;
        pack.packID = Global.generateNoRepeatInt();
        pack.lastVistDate = Global.currentTimeSeconds();

        byte[] encodedVal = Base64.encode(mAdminPasswordEditText.getText().toString().getBytes(),0);
        pack.restorePassword = new String(encodedVal).replace("\n", "").replace("\r", "");;

        final Card defaultCard = new Card();
        if (mIsEditPack) {
        } else {
            pack.creatorID = OpenUDID_manager.getOpenUDID();
            pack.createDate = Global.currentTimeSeconds();
            defaultCard.cardSN = 1;
            defaultCard.packID = pack.packID;
        }

        PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), pack);


        if (mAdminPasswordEditText.getText().toString().length() == 0) {
            new SweetAlertDialog(getActivity())
                    .setTitleText("Alert")
                    .setContentText("No admin password set. Setting an admin password allows you to edit this pack on on another device, or retrieve your editing rights on a pack that has been deleted off the device.")
                    .show();
        }

        dismiss();

        final Activity activity = getActivity();
        new Thread() {
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent();
                        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                        if (mIsEditPack) {
                            pack.save(AppContext.getAppContext());
                            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_EDIT_PACK);
                        } else {
                            User.defaultUser(AppContext.getAppContext()).addPack(pack);
                            pack.addCard(AppContext.getAppContext(),defaultCard);
                            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_NEW_PACK);
                        }
                        if (activity != null) {
                            activity.sendBroadcast(intent);
                            if (AppConfig.sharedInstance().isAllowToShowTooltip()) {
                                ((MainActivity)activity).showTooltips();
                            }

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
                    Timber.tag(Global.debugTag).w( "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    mCoverImageView.setImageBitmap(resultBitmap);

                    pack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Timber.tag(Global.debugTag).d( "pack.coverImageUriFormatStr = " + pack.coverImageUriFormatStr);
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
