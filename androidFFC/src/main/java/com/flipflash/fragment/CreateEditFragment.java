package com.flipflash.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Base64;

import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.UIHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

public class CreateEditFragment extends DialogFragment implements TextView.OnEditorActionListener {
    private static final String TAG = CreateEditFragment.class.getSimpleName();

    private View mContentView;
    private Pack mCurrentPack;

    private boolean mIsEditPack = false ;

    private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;

    private EditText           mPackNameEditText;
    private EditText           mSidebarTitleEditText;
    private EditText           mCreatorEditText;
    private EditText           mJobTitleEditText;
    private DiscreteSeekBar    mAutoPlaySpeedSeekbar;
    private ImageView          mCoverImageView;

    private EditText           mAdminPasswordEditText;
    private EditText           mConfirmAdminPasswordEditText;

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
        getDialog().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        TextView titleTextView = (TextView) mContentView
                .findViewById(R.id.dialog_title);

        if (mIsEditPack) {
            titleTextView.setText(R.string.Title_Edit_Pack);
        } else {
            titleTextView.setText(R.string.Title_Add_A_New_Pack);
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

                didClickedImageSelectionButton();

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
        mConfirmAdminPasswordEditText = (EditText) mContentView
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
            mPackNameEditText.setText(mCurrentPack.packName);
            mSidebarTitleEditText.setText(mCurrentPack.sidebarTitle);
            mCreatorEditText.setText(mCurrentPack.creatorNickName);
            mJobTitleEditText.setText(mCurrentPack.jobTitle);
            if (mCurrentPack.autoPlaySpeed == 0) {
                mAutoPlaySpeedSeekbar.setProgress(Global.kDefault_Auto_Play_Speed);
            } else {
                mAutoPlaySpeedSeekbar.setProgress(mCurrentPack.autoPlaySpeed);
            }

            String imagePath = mCurrentPack.coverImageUriFormatStr;
            mCoverImageView.setImageURI(Uri.parse(imagePath));

            String decodedString = new String(Base64.decode(mCurrentPack.restorePassword,0));
            mAdminPasswordEditText.setText(decodedString);
            mConfirmAdminPasswordEditText.setText(decodedString);


        } else {
            mCurrentPack = new Pack();

            mAutoPlaySpeedSeekbar.setProgress(Global.kDefault_Auto_Play_Speed);
        }


        mAutoPlaySpeedSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value;
            }

            @Override
            public String transformToString(int value) {
                return "Auto";
            }

            @Override
            public boolean useStringTransform() {

                if (mAutoPlaySpeedSeekbar.getProgress() == 4) {
                    return true;
                } else {
                    return false;
                }

            }
        });



        return mContentView;
    }


    private void didClickedImageSelectionButton() {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.DIALOG_PACK_LIST_IMAGE_SELECTION)
                .setMessage(R.string.Title_Image_Copyright)
                .setNegativeButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaOptions options = MediaOptions.createDefault();
                        if (options != null) {
                            MediaPickerActivity.open(CreateEditFragment.this,CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY,options);
                        }
                    }
                })
                .setPositiveButton(R.string.DIALOG_REMOVE_IMAGE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mCurrentPack.coverImageUriFormatStr = FileOperationHelper.getPackCoverDefaultImagePath();
                        String imagePath = mCurrentPack.coverImageUriFormatStr;
                        mCoverImageView.setImageURI(Uri.parse(imagePath));

                    }
                })
                .show();

    }


    public void setPack(Pack pack) {
        this.mCurrentPack = pack;
    }

    public void setIsEditPack(boolean mIsEditPack) {
        this.mIsEditPack = mIsEditPack;
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.add_pack_window_height);
        mContentView.setLayoutParams(params);

        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardVisibilityListener);

    }

    @Override
    public void onStop() {
        super.onStop();

        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardVisibilityListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void save() {


        if (mAdminPasswordEditText.getText().toString().equals(mConfirmAdminPasswordEditText.getText().toString()) == false) {
            new SweetAlertDialog(getActivity())
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.DIALOG_WRONG_PASSWORD))
            .show();
            return;
        }


        if (mAutoPlaySpeedSeekbar.getProgress() > Global.k_MAX_Auto_Play_Speed
                || mAutoPlaySpeedSeekbar.getProgress() < Global.k_MIN_Auto_Play_Speed) {
            new SweetAlertDialog(getActivity())
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(String.format("The value of auto play speed should be between %d and %d seconds",
                            Global.k_MIN_Auto_Play_Speed,Global.k_MAX_Auto_Play_Speed))
                    .show();
            return;
        }

        if ((mIsEditPack == false) && (checkExistingPackName(mPackNameEditText.getText().toString()))) {
            new SweetAlertDialog(getActivity())
                .setTitleText(getResources().getString(R.string.DIALOG_AlERT))
                .setContentText(getString(R.string.DIALOG_EXISTING_PACK_NAME))
                    .show();
            return;
        }


//        if (StringUtils.isAlphanumeric(mPackNameEditText.getText().toString()) == false || StringUtils.isEmpty(mPackNameEditText.getText().toString())) {
//            new SweetAlertDialog(getActivity())
//                    .setTitleText(getResources().getString(R.string.DIALOG_AlERT))
//                    .setContentText(getString(R.string.DIALOG_ONLY_ALPHANUMBER_PERMITTED))
//                    .show();
//            return;
//        }

        mCurrentPack.packName = mPackNameEditText.getText().toString();
        mCurrentPack.sidebarTitle = mSidebarTitleEditText.getText().toString();
        mCurrentPack.creatorNickName = mCreatorEditText.getText().toString();
        mCurrentPack.jobTitle = mJobTitleEditText.getText().toString();
        mCurrentPack.platform = UIHelper.getCurrentPlatform();
        mCurrentPack.platform = UIHelper.getCurrentPlatform();
        mCurrentPack.userID = Global.USER_ID;
        mCurrentPack.lastVistDate = Global.currentTimeSeconds();
        mCurrentPack.autoPlaySpeed = mAutoPlaySpeedSeekbar.getProgress();

        byte[] encodedVal = Base64.encode(mAdminPasswordEditText.getText().toString().getBytes(),0);
        mCurrentPack.restorePassword = new String(encodedVal).replace("\n", "").replace("\r", "");;

        final Card defaultCard = new Card();
        if (mIsEditPack) {

            if (mCurrentPack.packID == -1) {
                throw new IllegalStateException("During editing, packID should no be -1");
            }

        } else {
            mCurrentPack.packID = Global.generateNoRepeatInt();
            mCurrentPack.creatorID = OpenUDID_manager.getOpenUDID();
            mCurrentPack.createDate = Global.currentTimeSeconds();
            defaultCard.cardSN = 1;
            defaultCard.packID = mCurrentPack.packID;
        }

        PackRecordHelper.savePackUpdateRecord(mCurrentPack);


        if (mAdminPasswordEditText.getText().toString().length() == 0) {
            Toast.makeText(AppContext.getAppContext(),
                    getString(R.string.DIALOG_NO_ADMIN_PASSWORD_WARNING),
                    Toast.LENGTH_SHORT)
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
                            mCurrentPack.save(AppContext.getAppContext());
                            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_EDIT_PACK);
                        } else {
                            User.defaultUser(AppContext.getAppContext()).addPack(mCurrentPack);
                            mCurrentPack.addCard(AppContext.getAppContext(),defaultCard);
                            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_NEW_PACK);
                        }
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

                List<MediaItem> mMediaSelectedList = MediaPickerActivity
                        .getMediaItemSelected(data);
                MediaItem item = mMediaSelectedList.get(0);//因为是单选，所以永远是第一个
                Uri selectedImageURI = item.getUriOrigin();

                //step1: get image
                ImageSize targetSize = new ImageSize(400, 400);
                ImageLoader imageLoader = ImageLoader.getInstance();
                resultBitmap = imageLoader.loadImageSync(selectedImageURI.toString(),targetSize);

                if (resultBitmap == null) {
                    LOGD(TAG, "onActivityResult: resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    mCoverImageView.setImageBitmap(resultBitmap);

                    mCurrentPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    LOGD(TAG, "onActivityResult: " + "pack.coverImageUriFormatStr = " + mCurrentPack.coverImageUriFormatStr);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

//        RefWatcher refWatcher = AppContext.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }



    boolean mKeyboardDidDismissFlag = true;
    boolean mKeyboardDidShowFlag = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardVisibilityListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);

            final int softKeyboardHeight = 100;
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
            int heightDiff = rootView.getBottom() - r.bottom;
            if (heightDiff > softKeyboardHeight * dm.density) {
                mKeyboardDidDismissFlag = false;
                if (mKeyboardDidShowFlag == false) {
                    mKeyboardDidShowFlag = true;
                    keyboardDidShowNotification();
                }

            } else if (heightDiff == 0) {
                mKeyboardDidShowFlag = false;
                if (mKeyboardDidDismissFlag == false) {
                    // 意味着，keyboard刚刚关闭
                    mKeyboardDidDismissFlag = true;

                    keyboardDidHideNotification();

                }
            }

        }
    };

    private void keyboardDidHideNotification() {

        LOGD(TAG, "keyboardDidHideNotification: ");

        mAutoPlaySpeedSeekbar.setAlwaysShowIndicator(true);

    }

    private void keyboardDidShowNotification() {

        LOGD(TAG, "keyboardDidShowNotification");

        mAutoPlaySpeedSeekbar.setAlwaysShowIndicator(false);

    }
}
