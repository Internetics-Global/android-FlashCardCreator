package com.flipflash.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flipflash.UI.FCCEditText;
import com.flipflash.UI.RoundedBottomRightImageView;
import com.flipflash.UI.ScaleHelper;
import com.flipflash.android_ffc.CropActivity;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.android_ffc.VideoViewActivity;
import com.flipflash.android_ffc.WebViewActivity;
import com.flipflash.data.Answer;
import com.flipflash.data.CSS;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.data.Question;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.helper.SymbolHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.FontHelper;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;
import com.flipflash.util.TipHelper;
import com.flipflash.util.UIHelper;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

public class CardDetailFragment extends Fragment implements FCCEditText.OnTouchListener {
    private static final String TAG = CardDetailFragment.class.getName();

    public Card mCurrentCard;
    public Pack mCurrentPack;

    public View mContentView;

    public ImageView mImage2;
    public ImageView mImage;

    private FCCEditText mSidebarTitle;
    private FrameLayout mSidebarBackground;
    public TextView mCardSN;
    private FCCEditText mTitle;
    private LinearLayout mTitleBackground;
    private FCCEditText mCreator;
    private FCCEditText mJobTitle;
    private FCCEditText mSubheading;
    private FCCEditText mMain;
    private FCCEditText mSub;

    public ImageView mLogoImage;

    private ImageView mChangeTemplateImage;
    private ImageView mChangeBackgroundImage;
    private ImageView mPlayRecordImage;

    private ImageView mLogoURLImage;

    private RadioButton mQuestionRadioButton;
    private RadioButton mAnswerRadioButton;
    private RadioGroup mRadioGroup;

    private InputMethodManager mIMM;

    public EditText mCurrentFocusedCardContentText;  // only applicable to subheading, main and sub text

    public boolean mIsCreatingCard = false;
    private boolean mIsPlayingCard = false;
    private boolean mIsSnapShotNotCurrent = false;//as to snapshot,we have different strategy on current showing card and other cards
    public boolean mIsQuestionShowing = true;
    private boolean mIsTakeSnapshotAllNeeded = false; //when fields that belong to current pack(like title) changes, it will be set true

    private static int mSemaphore = 0; //used to indicate all snapshots are done

    //切换过程中
    private boolean mIsSwitchingQuestionAnswerView = false;

    private boolean mIsImage2Active = false; //我们有两个image(image和image2),这个变量用于区分

    //用于
    // 1. onStop时，是否需要进行写入到数据库；
    // 2. resize完毕后，是否需要暂存prepareToSavingTextFontSizeInfo
    private boolean mIsSaveNeededAfterResize = false;

    //用于auto resize 逻辑
    private ViewTreeObserver mVtoSubheading;
    private ViewTreeObserver mVtoMain;
    private ViewTreeObserver mVtoSub;
    private ViewTreeObserver.OnGlobalLayoutListener mVtoSubheadingListener;
    private ViewTreeObserver.OnGlobalLayoutListener mVtoMainListener;
    private ViewTreeObserver.OnGlobalLayoutListener mVtoSubListener;

    //需要的理由：由于需要多次addTextChangedListener，而Android系统又不提供统一的remove的功能，
    private TextWatcher mSubheadingTextWatcher;
    private TextWatcher mMainTextWatcher;
    private TextWatcher mSubTextWatcher;
    private TextWatcher mTitleTextWatcher;
    private TextWatcher mCreatorTextWatcher;
    private TextWatcher mJobTitleTextWatcher;
    private TextWatcher mSidebarTitleTextWatcher;

    private LinearLayout mContentBodyLinearLayout;

    private final int REQUEST_CODE_FROM_LOGO  = 314;
    private final int REQUEST_CODE_FROM_IMAGE  = 315;
    private final int REQUEST_CODE_FROM_BACKGROUND  = 316;
    private final int REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED  = 317;


    public final static  String TAG_SUBHEADING          = "1001";
    public final static  String TAG_MAIN                = "1002";
    public final static  String TAG_SUB                 = "1003";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 18) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        } else {

        }

        mIMM = (InputMethodManager) (getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

        if (mIsPlayingCard) {
            //need to hide question/answer segment radio group
            mContentView = inflater.inflate(R.layout.card, container, false);
        } else {
            mContentView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        }

        getAllViews();

        if (!mIsPlayingCard) {
            configureSegmentView();
            configureChangeTemplateView();
            configureBackgroundChangeImageView();

        }
        configureLogoURLView();  // open email or web during play ode
        setImageVideoClickListener(); // play video during play mode
        configureSoundRecordPlayImageView(); //play sound during play mode
        configureLogoImageView();


        if (mIsSnapShotNotCurrent == true) {
            mContentView.setVisibility(View.INVISIBLE);

            ViewDidAppearTask dTask = new ViewDidAppearTask();
            dTask.execute(100);
        }


        return mContentView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LOGD(TAG, "onViewCreated: cardSN=" + mCurrentCard.cardSN);

        updateCommonContent();
        switchToQuestionViewWithOption(false);

        if (mIsPlayingCard) {
            disableCardEditable();
        } else {
            if (isEditableMode()) {
                enableCardEditable();
            } else {
                disableCardEditable();
            }
        }


        //we have to disable because of performance issue
        if (mIsCreatingCard) {
            mSidebarTitle.setEnabled(false);
            mTitle.setEnabled(false);
        }

        if (mIsPlayingCard || mIsCreatingCard) {
            setCardBackgroundMaskBlack();
        } else {
            setCardBackgroundMaskGray();
        }

        if (AppConfig.sharedInstance().isAllowToShowTooltip()) {
            showTooltips();
        }


    }

    /**
     * 在new CardDetailFragment()后，立马调用
     *
     * @param currentPack
     * @param currentCard:如果为null，则表示正在创建new card
     * @param source
     */
    public void setupParameters(Pack currentPack, Card currentCard, int source) {

        if (source == 1) {
            mIsCreatingCard = true;
        } else if (source == 2) {
            mIsPlayingCard = true;
            mIsCreatingCard = false;
        } else if (source == 3) {
            mIsSnapShotNotCurrent = true;
        } else {
            mIsPlayingCard = false;
            mIsCreatingCard = false;
            mIsSnapShotNotCurrent = false;
        }

        if (currentCard == null) {
            mCurrentPack = currentPack;

            mCurrentCard = new Card();
            mCurrentCard.packID = mCurrentPack.packID;
            mCurrentCard.cardSN = mCurrentPack.cards.size() + 1;
            mCurrentCard.cardID = Global.generateNoRepeatInt();

        } else {
            mCurrentCard = currentCard;
            mCurrentPack = currentPack;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LOGD(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        LOGD(TAG, "onResume:");
        mIsTakeSnapshotAllNeeded = false;  //necessary

        //need to be put onResume, see http://stackoverflow.com/questions/13721063/aftertextchanged-being-called-without-the-text-being-actually-changed

        setEditTextListener();

        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardVisibilityListener);

    }

    @Override
    public void onPause() {
        super.onPause();

        LOGD(TAG, "onPause:");

        removeEditTextListener();

        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if (Build.VERSION.SDK_INT < 16) {
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardVisibilityListener);
        } else {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardVisibilityListener);
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        LOGD(TAG, "onStop: " + String.format("cardSN = %d", mCurrentCard.cardSN));

        //当当前card移除时，比如进入下一卡片，如果进行过resize操作，则保存一下
        if (((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) && (mIsSaveNeededAfterResize)) {
            mIsSaveNeededAfterResize = false;
            //prepareToSavingTextFontSizeInfo,由于resize后，会主动执行一下，所以这里没有必要了
            mCurrentCard.save(AppContext.getAppContext());
            LOGD(TAG, "onStop: Saving to database after triggerResizeTextToFitFrame");
        }


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

        restoreDefaultCursorPosition();

        ((MainActivity) getActivity()).removeCSSToolbar();

    }

    private void keyboardDidShowNotification() {

        LOGD(TAG, "keyboardDidShowNotification");

    }


    @Override
    public void onDestroy() {
        LOGD(TAG, "onDestroy: " + String.format("cardSN = %d", mCurrentCard.cardSN));
        super.onDestroy();

        mImage.setImageURI(null);
        mImage2.setImageURI(null);
        mLogoImage.setImageURI(null);

//        mSubheadingTextWatcher = null;
//        mMainTextWatcher = null;
//        mSubTextWatcher = null;
//        mCreatorTextWatcher = null;
//        mTitleTextWatcher = null;
//        mJobTitleTextWatcher = null;
//        mSidebarTitleTextWatcher = null;


    }


    private void playVideo() {
        String targetStr = "";
        if (mIsQuestionShowing) {
            if (mIsImage2Active) {
                if (mCurrentCard.question.movieUriFormatStr2.length() > 0) {
                    targetStr = mCurrentCard.question.movieUriFormatStr2;
                }
            } else {
                if (mCurrentCard.question.movieUriFormatStr.length() > 0) {
                    targetStr = mCurrentCard.question.movieUriFormatStr;
                }
            }
        } else {
            if (mIsImage2Active) {
                if (mCurrentCard.answer.movieUriFormatStr2.length() > 0) {
                    targetStr = mCurrentCard.answer.movieUriFormatStr2;

                }
            } else {
                if (mCurrentCard.answer.movieUriFormatStr.length() > 0) {
                    targetStr = mCurrentCard.answer.movieUriFormatStr;

                }
            }
        }

        if (targetStr.length() > 0) {

            if (Build.FINGERPRINT.startsWith("generic")) {
                Toast.makeText(AppContext.getAppContext(), "Don't support to play on simulator", Toast.LENGTH_LONG).show();
                return;
            }

            if (targetStr.contains("http://") || targetStr.contains("https://")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(targetStr));
                startActivity(i);

            } else {
                String videoPath = FileOperationHelper.deleteUriSchemeHeader(targetStr);
                Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                intent.putExtra("videoPath", videoPath);
                startActivity(intent);
            }

        } else {
            Toast.makeText(AppContext.getAppContext(), "Not available video file", Toast.LENGTH_LONG).show();
        }


    }


    private void showYoutubeLinkageInputDialog() {
        final EditText textInput = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.DIALOG_INSERT_YOUTUBE_URL))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textInput)
                .setPositiveButton(getString(R.string.DIALOG_DONE), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String youtubeURLStr = textInput.getText().toString();
                        if (StringUtils.isYoutubeLinkage(youtubeURLStr)) {

                            if (mIsImage2Active) {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr2 = youtubeURLStr;
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr2 = youtubeURLStr;
                                }
                            } else {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr = youtubeURLStr;
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr = youtubeURLStr;
                                }
                            }

                            thumbnailImageFromURL(Uri.parse(youtubeURLStr));


                            if (!mIsCreatingCard) {
                                takeSnapshotCurrentCard();
                                //mCurrentCard.save(AppContext.getAppContext());
                            }
                        } else {
                            Toast.makeText(AppContext.getAppContext(), getString(R.string.DIALOG_INVALID_YOUTUBE_URL), Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton(getString(R.string.DIALOG_CANCEL), null)
                .show();

    }

    private void selectImageOrVideoFromLibrary() {
        if (mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
            try {

                final String galleryPackage1 = "com.android.gallery3d";
                final String galleryPackage2 = "com.google.android.gallery3d";

                boolean found = false;
                Intent intent = new Intent(android.content.Intent.ACTION_GET_CONTENT);
                intent.setType("video/*,image/*");
                List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(intent,0);
                if (!resInfo.isEmpty()) {
                    for (ResolveInfo info:resInfo) {
                        if (info.activityInfo.packageName.toLowerCase().contains(galleryPackage1) ||
                                info.activityInfo.packageName.toLowerCase().contains(galleryPackage2)) {
                            intent.setPackage(info.activityInfo.packageName);
                            found = true;
                            break;
                        }
                    }

                }

                if (!found) {
                    startActivityForResult(intent, REQUEST_CODE_FROM_IMAGE);
                } else {
                    startActivityForResult(Intent.createChooser(intent,"Select"),REQUEST_CODE_FROM_IMAGE);
                }



            } catch (Exception e) {
                Toast.makeText(AppContext.getAppContext(),
                        getString(R.string.DIALOG_NO_PHOTO_GALLERIES_FOUND),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            if (mIsQuestionShowing) {
                if (mIsImage2Active) {
                    if (mCurrentCard.question.movieUriFormatStr2.length() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.DIALOG_PLAY_ONLY_SUPPORTED_IN_PLAY);
                        builder.setTitle(getString(R.string.DIALOG_AlERT));
                        builder.create().show();
                    }
                } else {
                    if (mCurrentCard.question.movieUriFormatStr.length() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.DIALOG_PLAY_ONLY_SUPPORTED_IN_PLAY);
                        builder.setTitle(getString(R.string.DIALOG_AlERT));
                        builder.create().show();
                    }
                }
            } else {
                if (mIsImage2Active) {
                    if (mCurrentCard.answer.movieUriFormatStr2.length() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.DIALOG_PLAY_ONLY_SUPPORTED_IN_PLAY);
                        builder.setTitle(getString(R.string.DIALOG_AlERT));
                        builder.create().show();

                    }
                } else {
                    if (mCurrentCard.answer.movieUriFormatStr.length() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.DIALOG_PLAY_ONLY_SUPPORTED_IN_PLAY);
                        builder.setTitle(getString(R.string.DIALOG_AlERT));
                        builder.create().show();

                    }
                }
            }
        }
    }




    /**
     * The only purpose of this method is to keep text font size info after triggerResizeTextToFitFrame and prepare for coming save at onStop
     * mSubheading/mMain/mSub are shared both answer and question view
     */
    private void prepareToSavingTextFontSizeInfo() {

        //since we have made the text size bigger in play mode, we need to restore it to original value when saving.
        double scaleVal;
        if (mIsPlayingCard) {
            scaleVal = Global.scaleInPlayMode;
        } else {
            scaleVal = 1.0;
        }


        if (mIsQuestionShowing) {
            mCurrentCard.question.css.subheadingSize = UIHelper.pixelsToSp((float) (mSubheading.getTextSize() / scaleVal));
            mCurrentCard.question.css.mainSize = UIHelper.pixelsToSp((float) (mMain.getTextSize() / scaleVal));
            mCurrentCard.question.css.subSize = UIHelper.pixelsToSp((float) (mSub.getTextSize() / scaleVal));

        } else {
            mCurrentCard.answer.css.subheadingSize = UIHelper.pixelsToSp((float) (mSubheading.getTextSize() / scaleVal));
            mCurrentCard.answer.css.mainSize = UIHelper.pixelsToSp((float) (mMain.getTextSize() / scaleVal));
            mCurrentCard.answer.css.subSize = UIHelper.pixelsToSp((float) (mSub.getTextSize() / scaleVal));
        }
    }


    private void handleCrop(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED &&
                resultCode == Activity.RESULT_OK) {

            Uri selectedURI = data.getParcelableExtra("cropped_image_uri");
            Bitmap scaledBitmap = UIHelper.bitmapFromUri(getActivity(), selectedURI, 1024);

            if (scaledBitmap == null) {
                LOGE(TAG, "handleCrop, scaledBitmap = null");
                return;
            }

            File toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);
            setCardBackgroundImageWithBitmap(scaledBitmap);
            if (mIsQuestionShowing) {
                mCurrentCard.question.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
            } else {
                mCurrentCard.answer.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
            }

            if (mIsCreatingCard == false) {
                mCurrentCard.save(AppContext.getAppContext());
                if (mIsQuestionShowing) {
                    takeSnapshotCurrentCard();
                    Intent intent = new Intent();
                    intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                    intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
                    getActivity().sendBroadcast(intent);
                }
            }

            PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), mCurrentPack);
        }

    }

    /*
    通过uri，获取video的thumbnail
     */
    private void thumbnailImageFromURL(Uri selectedURI) {

        Bitmap resultBitmap = UIHelper.getVideoThumbnail(AppContext.getAppContext(), selectedURI);

        if (mIsImage2Active) {
            mImage2.setImageBitmap(resultBitmap);
        } else {
            mImage.setImageBitmap(resultBitmap);
        }


        File toSaveImageFile = UIHelper.saveImageToCaches(resultBitmap);

        if (mIsImage2Active) {
            if (mIsQuestionShowing) {
                mCurrentCard.question.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveImageFile);
            } else {
                mCurrentCard.answer.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveImageFile);
            }
        } else {
            if (mIsQuestionShowing) {
                mCurrentCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveImageFile);
            } else {
                mCurrentCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveImageFile);
            }
        }
    }


    /**
     * Confugure segment view in onCreateView
     */
    private void configureSegmentView() {
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mQuestionRadioButton.getId()) {
                    mQuestionRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    mQuestionRadioButton.setTextColor(Color.BLACK);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mAnswerRadioButton.setTextColor(Color.WHITE);
                    switchToQuestionViewWithOption(false);
                } else {
                    mQuestionRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mQuestionRadioButton.setTextColor(Color.WHITE);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    mAnswerRadioButton.setTextColor(Color.BLACK);
                    switchToAnswerView(false);
                }
            }
        });
    }


    private void showImageVideoSourceDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.Label_Select)
                .setMessage(R.string.DIALOG_IMAGE_VIDEO_SELECTION)
                .setPositiveButton(R.string.DIALOG_INSERT_YOUTUBE_URL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showYoutubeLinkageInputDialog();
                    }
                })
                .setNegativeButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectImageOrVideoFromLibrary();
                    }
                })
                .show();
    }


    /*
    配置logo image view listener
     */
    private void configureLogoImageView() {

        mLogoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mIsPlayingCard == false) {
                    if (isEditableMode()) {
                        try {
                            startActivityForResult(
                                    new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                    REQUEST_CODE_FROM_LOGO);
                        }catch (Exception e) {
                            Toast.makeText(AppContext.getAppContext(),
                                    getString(R.string.DIALOG_NO_PHOTO_GALLERIES_FOUND),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }



                    } else {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra("url", mCurrentPack.logoURL);
                        startActivity(intent);
                    }
                } else {
                    if (mCurrentPack.logoURL.contains("@") && (mCurrentPack.logoURL.contains("http") == false)) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, "mCurrentPack.logoURL");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    } else if (mCurrentPack.logoURL.contains("http") == true) {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra("url", mCurrentPack.logoURL);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.DIALOG_AlERT))
                                .setMessage(getString(R.string.DIALOG_INCORRECT_URL_OR_EMAIL))
                                .setPositiveButton(getString(R.string.DIALOG_OK), null)
                                .show();
                    }
                }

            }
        });
    }

    /*
        配置sound record&play imageview的click listner
         */
    private void configureSoundRecordPlayImageView() {

        if (mPlayRecordImage == null) {
            return;
        }

        mPlayRecordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isEditableMode()) {
                    showCreateSoundView();
                } else {
                    new SweetAlertDialog(AppContext.getAppContext())
                            .setTitleText(getString(R.string.DIALOG_AlERT))
                            .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();
                }
            }
        });
    }

    /**
     * 配置background change imageview的click listner
     */
    private void configureBackgroundChangeImageView() {

        LOGD(TAG, "configureBackgroundChangeImageView");

        if (mChangeBackgroundImage == null) {
            return;
        }

        mChangeBackgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditableMode()) {

                    String string;
                    if (mIsQuestionShowing) {
                        string = mCurrentCard.question.backgroundImageUriFormatStr;
                    } else {
                        string = mCurrentCard.answer.backgroundImageUriFormatStr;
                    }

                    if (string.length() > 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.Optional_Edit_Or_Remove)
                                .setPositiveButton(R.string.Optional_Remove_Background_Image, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        LOGD(TAG, "configureBackgroundChangeImageView onClick: remove background");

                                        if (mIsQuestionShowing) {
                                            mCurrentCard.question.backgroundImageUriFormatStr = "";
                                        } else {
                                            mCurrentCard.answer.backgroundImageUriFormatStr = "";
                                        }
                                        setCardBackgroundImageDefault();
                                    }
                                })
                                .setNegativeButton(R.string.Optional_Change_Background_Image, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            startActivityForResult(
                                                    new Intent(
                                                            Intent.ACTION_PICK,
                                                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                                    REQUEST_CODE_FROM_BACKGROUND);
                                        } catch (Exception e) {
                                            Toast.makeText(AppContext.getAppContext(),
                                                    getString(R.string.DIALOG_NO_PHOTO_GALLERIES_FOUND),
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                })
                                .show();
                    } else {
                        try {
                            startActivityForResult(
                                    new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                    REQUEST_CODE_FROM_BACKGROUND);
                        } catch (Exception e) {
                            Toast.makeText(AppContext.getAppContext(),
                                    getString(R.string.DIALOG_NO_PHOTO_GALLERIES_FOUND),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                } else {
                    new SweetAlertDialog(AppContext.getAppContext())
                            .setTitleText(getString(R.string.DIALOG_AlERT))
                            .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();
                }
            }
        });
    }


    /**
     * 配置image view的click listner
     * two choice
     * 1. input a youtube linkage
     * 2. select image/video from library
     */
    private void setImageVideoClickListener() {

        //step1: configure image

        if ((!mIsPlayingCard) && (isEditableMode())) {  // popup a choice dialog: youtube linkage or library
            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsImage2Active = false;
                    showImageVideoSourceDialog();

                }
            });

        } else {
            if (mIsPlayingCard) {
                mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsImage2Active = false;
                        playVideo();

                    }
                });

            } else {
                //不在play mode下，但是同时又不是自己创建的卡
                mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsImage2Active = false;
                        Toast.makeText(AppContext.getAppContext(), "Video play is only available in play mode", Toast.LENGTH_LONG).show();

                    }
                });

            }
        }

        //step2: configure image2

        if ((!mIsPlayingCard) && (isEditableMode())) {  // popup a choice dialog: youtube linkage or library
            mImage2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsImage2Active = true;
                    showImageVideoSourceDialog();

                }
            });

        } else {
            if (mIsPlayingCard) {
                mImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsImage2Active = true;
                        playVideo();

                    }
                });

            } else {
                //不在play mode下，但是同时又不是自己创建的卡
                mImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsImage2Active = true;
                        new SweetAlertDialog(AppContext.getAppContext())
                                .setTitleText(getString(R.string.DIALOG_AlERT))
                                .setContentText(getString(R.string.DIALOG_VIDEO_PLAY_ONLY_AVAILABLE_IN_PLAY))
                                .show();

                    }
                });

            }
        }

    }

    /**
     * 配置template change imageview的click listner
     */
    private void configureChangeTemplateView() {
        ActionItem questionActionItem0 = new ActionItem(0, null, getResources().getDrawable(R.drawable.question_templatescreenshot0));
        ActionItem questionActionItem1 = new ActionItem(1, null, getResources().getDrawable(R.drawable.question_templatescreenshot1));
        ActionItem questionActionItem2 = new ActionItem(2, null, getResources().getDrawable(R.drawable.question_templatescreenshot2));
        ActionItem questionActionItem3 = new ActionItem(3, null, getResources().getDrawable(R.drawable.question_templatescreenshot3));
        ActionItem questionActionItem4 = new ActionItem(4, null, getResources().getDrawable(R.drawable.question_templatescreenshot4));
        ActionItem questionActionItem5 = new ActionItem(5, null, getResources().getDrawable(R.drawable.question_templatescreenshot5));
        ActionItem questionActionItem6 = new ActionItem(6, null, getResources().getDrawable(R.drawable.question_templatescreenshot6));
        ActionItem questionActionItem7 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot7));
        ActionItem questionActionItem8 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot8));
        ActionItem questionActionItem9 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot9));
        ActionItem questionActionItem10 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot10));
        ActionItem questionActionItem11 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot11));
        ActionItem questionActionItem12 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot12));
        ActionItem questionActionItem13 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot13));
        ActionItem questionActionItem14 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot14));
        ActionItem questionActionItem15 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot15));
        ActionItem questionActionItem16 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot16));

        ActionItem answerActionItem0 = new ActionItem(0, null, getResources().getDrawable(R.drawable.answer_templatescreenshot0));
        ActionItem answerActionItem1 = new ActionItem(1, null, getResources().getDrawable(R.drawable.answer_templatescreenshot1));
        ActionItem answerActionItem2 = new ActionItem(2, null, getResources().getDrawable(R.drawable.answer_templatescreenshot2));
        ActionItem answerActionItem3 = new ActionItem(3, null, getResources().getDrawable(R.drawable.answer_templatescreenshot3));
        ActionItem answerActionItem4 = new ActionItem(4, null, getResources().getDrawable(R.drawable.answer_templatescreenshot4));
        ActionItem answerActionItem5 = new ActionItem(5, null, getResources().getDrawable(R.drawable.answer_templatescreenshot5));
        ActionItem answerActionItem6 = new ActionItem(6, null, getResources().getDrawable(R.drawable.answer_templatescreenshot6));
        ActionItem answerActionItem7 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot7));
        ActionItem answerActionItem8 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot8));
        ActionItem answerActionItem9 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot9));
        ActionItem answerActionItem10 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot10));
        ActionItem answerActionItem11 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot11));
        ActionItem answerActionItem12 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot12));
        ActionItem answerActionItem13 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot13));
        ActionItem answerActionItem14 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot14));
        ActionItem answerActionItem15 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot15));
        ActionItem answerActionItem16 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot16));

        final QuickAction questionQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
        final QuickAction answerQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);

//        questionQuickAction.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_darkgray_no_corner));
//        answerQuickAction.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_darkgray_no_corner));


        questionQuickAction.addActionItem(questionActionItem0);
        questionQuickAction.addActionItem(questionActionItem1);
        questionQuickAction.addActionItem(questionActionItem2);
        questionQuickAction.addActionItem(questionActionItem3);
        questionQuickAction.addActionItem(questionActionItem4);
        questionQuickAction.addActionItem(questionActionItem5);
        questionQuickAction.addActionItem(questionActionItem6);
        questionQuickAction.addActionItem(questionActionItem7);
        questionQuickAction.addActionItem(questionActionItem8);
        questionQuickAction.addActionItem(questionActionItem9);
        questionQuickAction.addActionItem(questionActionItem10);
        questionQuickAction.addActionItem(questionActionItem11);
        questionQuickAction.addActionItem(questionActionItem12);
        questionQuickAction.addActionItem(questionActionItem13);
        questionQuickAction.addActionItem(questionActionItem14);
        questionQuickAction.addActionItem(questionActionItem15);
        questionQuickAction.addActionItem(questionActionItem16);
        questionQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateActionItemClicked(pos);
            }
        });

        answerQuickAction.addActionItem(answerActionItem0);
        answerQuickAction.addActionItem(answerActionItem1);
        answerQuickAction.addActionItem(answerActionItem2);
        answerQuickAction.addActionItem(answerActionItem3);
        answerQuickAction.addActionItem(answerActionItem4);
        answerQuickAction.addActionItem(answerActionItem5);
        answerQuickAction.addActionItem(answerActionItem6);
        answerQuickAction.addActionItem(answerActionItem7);
        answerQuickAction.addActionItem(answerActionItem8);
        answerQuickAction.addActionItem(answerActionItem9);
        answerQuickAction.addActionItem(answerActionItem10);
        answerQuickAction.addActionItem(answerActionItem11);
        answerQuickAction.addActionItem(answerActionItem12);
        answerQuickAction.addActionItem(answerActionItem13);
        answerQuickAction.addActionItem(answerActionItem14);
        answerQuickAction.addActionItem(answerActionItem15);
        answerQuickAction.addActionItem(answerActionItem16);
        answerQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateActionItemClicked(pos);
            }
        });


        if (mChangeTemplateImage != null) {
            mChangeTemplateImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEditableMode()) {
                        if (mIsQuestionShowing) {
                            questionQuickAction.show(mChangeTemplateImage);
                        } else {
                            answerQuickAction.show(mChangeTemplateImage);
                        }
                    } else {
                        new SweetAlertDialog(AppContext.getAppContext())
                                .setTitleText(getString(R.string.DIALOG_AlERT))
                                .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                                .show();
                    }

                }
            });
        }


    }

    /**
     * 配置logo url view的click listner
     */
    private void configureLogoURLView() {
        mLogoURLImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputEditText = new EditText(getActivity());
                inputEditText.setSingleLine(true);
                inputEditText.setText(mCurrentPack.logoURL);
                inputEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                inputEditText.setSelection(inputEditText.getText().length());
                inputEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.DIALOG_AlERT)
                        .setMessage(R.string.DIALOG_ENTER_VALID_URL)
                        .setView(inputEditText)
                        .setPositiveButton(R.string.Keyboard_Done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurrentPack.logoURL = inputEditText.getText().toString();
                                if (mIMM.isActive()) {
                                    mIMM.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                                }

                                if (mIsCreatingCard == false) {
                                    mCurrentPack.save(AppContext.getAppContext());
                                }

                            }
                        })
                        .setNegativeButton(R.string.Keyboard_Cancel, null)
                        .show();
            }
        });

    }

    /**
     * 打开录制声音的view
     */
    public void showCreateSoundView() {

        CreateSoundFragment dialogFragment = new CreateSoundFragment();
        dialogFragment.mIsCreatingCard = mIsCreatingCard;
        dialogFragment.mCurrentCard = mCurrentCard;
        dialogFragment.mCurrentPack = mCurrentPack;
        dialogFragment.mIsQuestionShowing = mIsQuestionShowing;
        dialogFragment.show(getActivity().getFragmentManager(), "create_sound_fragment");
    }

    private void changeTemplateActionItemClicked(int index) {
        if (mIsQuestionShowing) {
            mCurrentCard.question.templateID = index;
            switchToQuestionViewWithOption(true);

        } else {
            mCurrentCard.answer.templateID = index;
            switchToAnswerView(true);
        }

        if ((mIsPlayingCard == false) && (mIsCreatingCard == false)) {

            new Thread() {
                public void run() {
                    try {

                        Thread.sleep(10);

                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                saveEditedCard();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }


    }

    public void switchQuestionAnswerView() {

        if ((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) {
            prepareToSavingTextFontSizeInfo(); // do this before reset mIsQuestionShowing
        }

        if (mIsQuestionShowing) {
            switchToAnswerView(false);
            mIsQuestionShowing = false;
        } else {
            switchToQuestionViewWithOption(false);
            mIsQuestionShowing = true;
        }

        if (mIsPlayingCard) {
            disableCardEditable();
        }


    }

    /**
     * *Set public since play modes need it
     *
     * @param ignoreResetTitleContent will trigger takeSnapAll function is it is set
     */
    public void switchToQuestionViewWithOption(boolean ignoreResetTitleContent) {
        mIsSwitchingQuestionAnswerView = true;
        mIsQuestionShowing = true;
        if (!ignoreResetTitleContent) {
            mTitle.setText(mCurrentPack.questionTitle);
        }

        updateQuestionViewTemplate();//updateQuestionContent，因为涉及到view的重定向
        updateQuestionContent();
        updateQuestionCSS();

        //hide placeholder image if play mode
        if (mIsPlayingCard) {
            if (mCurrentCard.question.imageUriFormatStr.contains("placeholder")) {
                mImage.setVisibility(View.INVISIBLE);
            }

            if (mCurrentCard.question.imageUriFormatStr2.contains("placeholder")) {
                mImage2.setVisibility(View.INVISIBLE);
            }
        }

        mIsSwitchingQuestionAnswerView = false;

    }


    /**
     * @param ignoreResetTitleContent will trigger takeSnapAll function is it is set
     */
    private void switchToAnswerView(boolean ignoreResetTitleContent) {

        mIsSwitchingQuestionAnswerView = true;
        mIsQuestionShowing = false;
        if (!ignoreResetTitleContent) {
            mTitle.setText(mCurrentPack.answerTitle);
        }

        updateAnswerViewTemplate(); //必须放在updateAnswerContent，因为涉及到view的重定向
        updateAnswerContent();
        updateAnswerCSS();

        //hide placeholder image if play mode
        if (mIsPlayingCard) {
            if (mCurrentCard.answer.imageUriFormatStr.contains("placeholder")) {
                mImage.setVisibility(View.INVISIBLE);
            }

            if (mCurrentCard.answer.imageUriFormatStr2.contains("placeholder")) {
                mImage2.setVisibility(View.INVISIBLE);
            }
        }

        mIsSwitchingQuestionAnswerView = false;
    }


    /**
     * 用于inflate后
     */
    private void getAllViews() {
        mSidebarTitle = (FCCEditText) mContentView.findViewById(R.id.sidebar_title);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSidebarTitle.getLayoutParams();
        double cardHeightDPUnit = UIHelper.getCardHeightDPUnit(getActivity());
        double cardSNHeightPXUnit = getResources().getDimensionPixelSize(R.dimen.cardsn_size) +  getResources().getDimensionPixelSize(R.dimen.cardsn_top_margin);
        params.width = UIHelper.getPixels((int)cardHeightDPUnit - 10) - (int)cardSNHeightPXUnit * 2; //10 is the margin between sn and mSidebarTitle
        mSidebarTitle.setLayoutParams(params);

        mSidebarBackground = (FrameLayout) mContentView.findViewById(R.id.sidebar_background_linearlayout);
        mCardSN = (TextView) mContentView.findViewById(R.id.card_sn);


        mTitle = (FCCEditText) mContentView.findViewById(R.id.title);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (FCCEditText) mContentView.findViewById(R.id.creator);
        mJobTitle = (FCCEditText) mContentView.findViewById(R.id.job_title);

        LinearLayout creatorLayout = (LinearLayout) mContentView.findViewById(R.id.creator_layout);

        mContentBodyLinearLayout = (LinearLayout) mContentView.findViewById(R.id.card_content_body);
        if (mIsCreatingCard || mIsPlayingCard == false) {
            mContentBodyLinearLayout.setVisibility(View.VISIBLE); //默认是隐藏的
        }

        createSubheading();
        createMain();
        createSub();
        createImage();
        createImage2();

        updateContentViewsPointers(0);


        if (mIsPlayingCard) {
            //在play mode中，我们只有play sound button，且独立
        } else {
            mChangeTemplateImage = (ImageView) mContentView.findViewById(R.id.change_template_button);
            mChangeBackgroundImage = (ImageView) mContentView.findViewById(R.id.change_background_button);
            mPlayRecordImage = (ImageView) mContentView.findViewById(R.id.play_record_button);
        }


        mLogoImage = (ImageView) mContentView.findViewById(R.id.logo_image);
        mLogoURLImage = (ImageView) mContentView.findViewById(R.id.logo_url_btn);


        if (!mIsPlayingCard) {
            mQuestionRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_question);
            mAnswerRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_answer);
            mRadioGroup = (RadioGroup) mContentView.findViewById(R.id.radio_segment);

        }


        if (!mIsPlayingCard) {
            mSubheading.setOnTouchListener(this);
            mMain.setOnTouchListener(this);
            mSub.setOnTouchListener(this);
            mCreator.setOnTouchListener(this);
            mJobTitle.setOnTouchListener(this);
            mSidebarTitle.setOnTouchListener(this);
            mTitle.setOnTouchListener(this);
        } else {
            mCreator.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mJobTitle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            creatorLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mCurrentPack.logoURL.contains("@") && (mCurrentPack.logoURL.contains("http") == false)) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, "mCurrentPack.logoURL");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentPack.logoURL)));
                    }
                    return true;
                }
            });
        }

    }

    /**
     * 由于mSubheading，mMain，mSub在不同的template下，指向的view不一样，所以每次更改template时，则都需要调用
     * @param templateID
     */
    private void updateContentViewsPointers(int templateID) {

        //OnTouchListener
        if (!mIsPlayingCard) {
            mSubheading.setOnTouchListener(this);
            mMain.setOnTouchListener(this);
            mSub.setOnTouchListener(this);

        }

        //EditorActionListener
        setEditTextListener();

        //Image的重新OnClickListener
        setImageVideoClickListener();

    }


    private void createSubheading() {

        mSubheading = new FCCEditText(getActivity());
        mSubheading.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSubheading.setGravity(Gravity.CENTER);
        mSubheading.setCursorVisible(true);
        mSubheading.setTag(TAG_SUBHEADING);
        mSubheading.setTypeface(Typeface.DEFAULT_BOLD);
        mSubheading.setTextColor(Color.BLACK);
        mSubheading.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mSubheading.setEms(10);
        mSubheading.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSubheading.setSingleLine(false);
        mSubheading.setPadding(0, 0, 0, 0);

        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mSubheading, R.drawable.cursor);
        } catch (Exception ignored) {
        }
    }

    private void createMain() {

        mMain = new FCCEditText(getActivity());
        mMain.setBackgroundResource(R.drawable.shape_edittext_editable);
        mMain.setCursorVisible(true);
        mMain.setGravity(Gravity.CENTER);
        mMain.setCursorVisible(true);
        mMain.setTag(TAG_MAIN);
        mMain.setTypeface(Typeface.DEFAULT_BOLD);
        mMain.setTextColor(Color.BLACK);
        mMain.setSingleLine(false);
        mMain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMain.setEms(10);
        mMain.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mMain.setPadding(0, 0, 0, 0);

        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mMain, R.drawable.cursor);
        } catch (Exception ignored) {
        }

    }

    private void createSub() {

        mSub = new FCCEditText(getActivity());
        mSub.setCursorVisible(true);
        mSub.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSub.setGravity(Gravity.CENTER);
        mSub.setCursorVisible(true);
        mSub.setTag(TAG_SUB);
        mSub.setTypeface(Typeface.DEFAULT_BOLD);
        mSub.setTextColor(Color.BLACK);
        mSub.setSingleLine(false);
        mSub.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mSub.setEms(10);
        mSub.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSub.setPadding(0, 0, 0, 0);

        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mSub, R.drawable.cursor);
        } catch (Exception ignored) {
        }

    }

    private void createImage() {
        LOGD(TAG, "createImage");
        String imagePathStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();

        mImage = new ImageView(getActivity());
        mImage.setImageURI(Uri.parse(imagePathStr));

        mImage.setPadding(5,5,5,5);

        if (isEditableMode() && (mIsPlayingCard == false)) {
            mImage.setBackgroundResource(R.drawable.shape_imageview_editable);
        }

    }

    private void createImage2() {
        LOGD(TAG, "createImage2");
        String imagePathStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();

        mImage2 = new ImageView(getActivity());
        mImage2.setImageURI(Uri.parse(imagePathStr));

        mImage2.setPadding(5, 5, 5, 5);

        if (isEditableMode() && (mIsPlayingCard == false)) {
            mImage2.setBackgroundResource(R.drawable.shape_imageview_editable);
        }
    }


    /**
     * 在以下情况下被自动触发：
     * 1. TextEdit的内容改变（TextWatcher）
     * 2. 在布局时：ViewTreeObserver.OnGlobalLayoutListener
     *
     * maxLines不再使用
     */
    private boolean flag_Subheading_OneoffIncrease;
    private boolean flag_Main_OneoffIncrease;
    private boolean flag_Sub_OneoffIncrease;
    private void triggerResizeTextToFitFrame(final EditText v, int targetLines) {

        synchronized (v) {

            String tag = (String) v.getTag();

            if (v.getText().length() == 0) {
                return;
            }


            //特殊逻辑，历史原因,sample pack中的这部分内容的line number不正确，需要二次修正
            if (v.getText().toString().contains("Knee how ma")) {
                targetLines = 5;
            }


            //noOfLines有可能返回0： getLineCount() will give you the correct number of lines only after a layout pass. That means the TextView must have been drawn at least once.
            int noOfLines = v.getLineCount(); //this is very important, when setTextSize execute, getLineCount could possibly be zero
            int textHeight = noOfLines * v.getLineHeight();
            int viewHeight = v.getHeight();
            int lineHeight = v.getLineHeight();

            if ((tag.equals(TAG_SUBHEADING) && flag_Subheading_OneoffIncrease) ||
                    (tag.equals(TAG_MAIN) && flag_Main_OneoffIncrease)||
                    (tag.equals(TAG_SUB) && flag_Sub_OneoffIncrease)) {

            } else {
                //In case it's too small
                //只允许一次，尽可能大，这样可以通过后续的缩小进行
                if (noOfLines < targetLines && noOfLines > 0) {

                    if (tag.equals(TAG_SUBHEADING)) {
                        flag_Subheading_OneoffIncrease = true;
                    } else if (tag.equals(TAG_MAIN)) {
                        flag_Main_OneoffIncrease = true;
                    } else if (tag.equals(TAG_SUB)) {
                        flag_Sub_OneoffIncrease = true;
                    }

                    float newTextSize = v.getTextSize() + (v.getTextSize())/3;
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

                    return;

                }
            }



            if (((textHeight > viewHeight) && (viewHeight > 1) && (noOfLines > 0)) || (noOfLines > targetLines && targetLines > 0)) {

                int cursorPosition = v.getSelectionStart();

                if ((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) {
                    // resize action
                    float textSize = v.getTextSize();
                    float newTextSize = 0;

                    if (textSize > 200) {
                        newTextSize = textSize - textSize/10;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

                    } else if ((textSize > 100) && (textSize <= 200)) {
                        newTextSize = textSize - textSize/40;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    } else if ((textSize > 50) && (textSize <= 100)) {
                        newTextSize = textSize - textSize/50;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    } else if ((textSize > 30) && (textSize <= 50)) {
                        newTextSize = textSize - 1;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    } else if (textSize <= 30) {
                        newTextSize = textSize - 1;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    } else {
                    }


                    //in case the font size still too big
                    noOfLines = v.getLineCount();
                    if ((targetLines > 0) && (targetLines < noOfLines)) {
                        newTextSize = v.getTextSize() - 2;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    } else {
                    }


                    //mIsSaveNeededAfterResize = true;


                } else {

                    if (textHeight < viewHeight + lineHeight) {
                        //we only do this during editable mode
                        String text = v.getText().toString();
                        int index = text.length() - 1;
                        if (index > 0) {
                            v.setText(text.substring(0, index));
                            if (cursorPosition == index + 1) {
                                v.setSelection(index);
                            } else {
                                v.setSelection(cursorPosition);
                            }
                        }
                    }
                }


            } else {


                //仅在如下情况起作用：
                //1. read only
                //2. mIsSaveNeededAfterResize
                //3. 不再question/answer切换中
                if (((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) && (mIsSaveNeededAfterResize) && (mIsSwitchingQuestionAnswerView == false)) {
                    //mIsSaveNeededAfterResize = false;，不能置false，因为我们在onstop时需要写入数据库,虽然这样会导致被调用多次

                    prepareToSavingTextFontSizeInfo();
                }


            }
        }

    }

    private void removeEditTextListener() {

        //1.
        // setOnEditorActionListener，我们不需要手工remove

        //2.
        if (Build.VERSION.SDK_INT < 16) {
            mVtoSubheading.removeGlobalOnLayoutListener(mVtoSubheadingListener);
            mVtoMain.removeGlobalOnLayoutListener(mVtoMainListener);
            mVtoSub.removeGlobalOnLayoutListener(mVtoSubListener);
        } else {
            mVtoSubheading.removeOnGlobalLayoutListener(mVtoSubheadingListener);
            mVtoMain.removeOnGlobalLayoutListener(mVtoMainListener);
            mVtoSub.removeOnGlobalLayoutListener(mVtoSubListener);
        }

        //3.
        mSubheading.removeTextChangedListener(mSubheadingTextWatcher);
        mMain.removeTextChangedListener(mMainTextWatcher);
        mSub.removeTextChangedListener(mSubTextWatcher);
        mTitle.removeTextChangedListener(mTitleTextWatcher);
        mCreator.removeTextChangedListener(mCreatorTextWatcher);
        mJobTitle.removeTextChangedListener(mJobTitleTextWatcher);
        mSidebarTitle.removeTextChangedListener(mSidebarTitleTextWatcher);

    }


    private void setEditTextListener() {

        LOGD(TAG, "setEditTextListener: cardSN=" + mCurrentCard.cardSN);

        if (mIsPlayingCard == false) {
            mSidebarTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEditedCard();
                    }
                    return false;
                }
            });
            mCreator.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEditedCard();
                    }
                    return false;
                }
            });
            mJobTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEditedCard();
                    }
                    return false;
                }
            });

            mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEditedCard();
                    }
                    return false;
                }
            });


            if (mTitleTextWatcher != null) {
                mTitle.removeTextChangedListener(mTitleTextWatcher);
            }
            mTitleTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mIsQuestionShowing) {
                        mCurrentPack.questionTitle = mTitle.getText().toString();
                        if ((!mIsPlayingCard)) {
                            mIsTakeSnapshotAllNeeded = true;
                        }
                    } else {
                        mCurrentPack.answerTitle = mTitle.getText().toString();
                    }
                    LOGD(TAG, "afterTextChanged: mTitle has changed");
                }
            };
            mTitle.addTextChangedListener(mTitleTextWatcher);

            if (mCreatorTextWatcher != null) {
                mCreator.removeTextChangedListener(mCreatorTextWatcher);
            }
            mCreatorTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mCurrentPack.creatorNickName = mCreator.getText().toString();
                    if ((!mIsPlayingCard)) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    LOGD(TAG, "afterTextChanged: mCreator has changed");

                }
            };
            mCreator.addTextChangedListener(mCreatorTextWatcher);

            if (mJobTitleTextWatcher != null) {
                mJobTitle.removeTextChangedListener(mJobTitleTextWatcher);
            }
            mJobTitleTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mCurrentPack.jobTitle = mJobTitle.getText().toString();
                    if ((!mIsPlayingCard)) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    LOGD(TAG, "afterTextChanged: mJobTitle has changed");

                }
            };
            mJobTitle.addTextChangedListener(mJobTitleTextWatcher);

            if (mSidebarTitleTextWatcher != null) {
                mSidebarTitle.removeTextChangedListener(mSidebarTitleTextWatcher);
            }
            mSidebarTitleTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mCurrentPack.sidebarTitle = mSidebarTitle.getText().toString();
                    if ((!mIsPlayingCard)) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    LOGD(TAG, "afterTextChanged: mSidebarTitle has changed");

                }
            };
            mSidebarTitle.addTextChangedListener(mSidebarTitleTextWatcher);
        }


        if (mSubheadingTextWatcher != null) {
            mSubheading.removeTextChangedListener(mSubheadingTextWatcher);
        }
        mSubheadingTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int maxLines = 0;
                if (mIsQuestionShowing) {
                    mCurrentCard.question.subheading = mSubheading.getText().toString();
                    maxLines = mCurrentCard.question.lineNoSubheading;
                } else {
                    mCurrentCard.answer.subheading = mSubheading.getText().toString();
                    maxLines = mCurrentCard.answer.lineNoSubheading;
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mSubheading, maxLines);
                }

            }
        };
        mSubheading.addTextChangedListener(mSubheadingTextWatcher);

        if (mMainTextWatcher != null) {
            mMain.removeTextChangedListener(mMainTextWatcher);
        }
        mMainTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int maxLines;
                if (mIsQuestionShowing) {
                    mCurrentCard.question.main = mMain.getText().toString();
                    maxLines = mCurrentCard.question.lineNoMain;
                } else {
                    mCurrentCard.answer.main = mMain.getText().toString();
                    maxLines = mCurrentCard.answer.lineNoMain;
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mMain, maxLines);
                }

            }
        };
        mMain.addTextChangedListener(mMainTextWatcher);

        if (mSubTextWatcher != null) {
            mSub.removeTextChangedListener(mSubTextWatcher);
        }
        mSubTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int maxLines;
                if (mIsQuestionShowing) {
                    mCurrentCard.question.sub = mSub.getText().toString();
                    maxLines = mCurrentCard.question.lineNoSub;
                } else {
                    mCurrentCard.answer.sub = mSub.getText().toString();
                    maxLines = mCurrentCard.answer.lineNoSub;
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mSub, maxLines);
                }

            }
        };
        mSub.addTextChangedListener(mSubTextWatcher);

        if ((mSubheading != null) && (mVtoSubheadingListener != null)) {
            if (Build.VERSION.SDK_INT < 16) {
                mSubheading.getViewTreeObserver().removeGlobalOnLayoutListener(mVtoSubheadingListener);
            } else {
                mSubheading.getViewTreeObserver().removeOnGlobalLayoutListener(mVtoSubheadingListener);
            }

        }
        mVtoSubheadingListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxLines;
                if (mIsQuestionShowing) {
                    ;
                    maxLines = mCurrentCard.question.lineNoSubheading;
                } else {
                    maxLines = mCurrentCard.answer.lineNoSubheading;
                }
                triggerResizeTextToFitFrame(mSubheading, maxLines);
            }
        };
        mVtoSubheading = mSubheading.getViewTreeObserver();
        mVtoSubheading.addOnGlobalLayoutListener(mVtoSubheadingListener);


        if ((mMain != null) && (mVtoMainListener != null)) {
            if (Build.VERSION.SDK_INT < 16) {
                mMain.getViewTreeObserver().removeGlobalOnLayoutListener(mVtoMainListener);
            } else {
                mMain.getViewTreeObserver().removeOnGlobalLayoutListener(mVtoMainListener);
            }

        }
        mVtoMainListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxLines;
                if (mIsQuestionShowing) {
                    ;
                    maxLines = mCurrentCard.question.lineNoMain;
                } else {
                    maxLines = mCurrentCard.answer.lineNoMain;
                }
                triggerResizeTextToFitFrame(mMain, maxLines);
            }
        };
        mVtoMain = mMain.getViewTreeObserver();
        mVtoMain.addOnGlobalLayoutListener(mVtoMainListener);

        if ((mSub != null) && (mVtoSubListener != null)) {
            if (Build.VERSION.SDK_INT < 16) {
                mSub.getViewTreeObserver().removeGlobalOnLayoutListener(mVtoSubListener);
            } else {
                mSub.getViewTreeObserver().removeOnGlobalLayoutListener(mVtoSubListener);
            }

        }
        mVtoSubListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxLines;
                if (mIsQuestionShowing) {
                    ;
                    maxLines = mCurrentCard.question.lineNoSub;
                } else {
                    maxLines = mCurrentCard.answer.lineNoSub;
                }
                triggerResizeTextToFitFrame(mSub, maxLines);
            }
        };
        mVtoSub = mSub.getViewTreeObserver();
        mVtoSub.addOnGlobalLayoutListener(mVtoSubListener);

    }


    /**
     * question和answer上有些内容是一样的，我们不需要做两次，所以把通用的内容的更新放在这个方法中
     */
    private void updateCommonContent() {
        if (mCurrentPack.sidebarTitle.contains("null")) {
            mSidebarTitle.setText("");
        } else {
            mSidebarTitle.setText(mCurrentPack.sidebarTitle);
        }
        mCardSN.setText(String.format("%d", mCurrentCard.cardSN));

        if (StringUtils.isEmpty(mCurrentPack.logoImageUriFormatStr) == false) {
            mLogoImage.setImageURI(Uri.parse(mCurrentPack.logoImageUriFormatStr));
        }

        mCreator.setText(mCurrentPack.creatorNickName);
        mJobTitle.setText(mCurrentPack.jobTitle);

        int sidebarBGResourceID = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[1];
        mSidebarBackground.setBackgroundResource(sidebarBGResourceID);
        int titleBGResourceID = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[2];
        mTitleBackground.setBackgroundResource(titleBGResourceID);

        if (!mIsPlayingCard) {
            if (mIsQuestionShowing) {
                mTitle.setText(mCurrentPack.questionTitle);
            } else {
                mTitle.setText(mCurrentPack.answerTitle);
            }
        }


        //Invisible if logo is a placeholder
        if ((mIsPlayingCard) && (mCurrentPack.logoImageUriFormatStr.contains("placeholder") == true)) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }


    }


    private void updateQuestionContent() {

        mSubheading.setText(mCurrentCard.question.subheading);

        mMain.setText(mCurrentCard.question.main);
        mSub.setText(mCurrentCard.question.sub);

        if (StringUtils.isEmpty(mCurrentCard.question.imageUriFormatStr) == false) {
            mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));
        }

        if (StringUtils.isEmpty(mCurrentCard.question.imageUriFormatStr2) == false) {
            mImage2.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr2));
        }

        if (StringUtils.isEmpty(mCurrentCard.question.backgroundImageUriFormatStr) == false) {
            setCardBackgroundImageWithUri(mCurrentCard.question.backgroundImageUriFormatStr);
        } else {
            setCardBackgroundImageDefault();
        }



    }

    private void updateAnswerContent() {
        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);

        if (StringUtils.isEmpty(mCurrentCard.answer.imageUriFormatStr) == false) {
            mImage.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));
        }

        if (StringUtils.isEmpty(mCurrentCard.answer.imageUriFormatStr2) == false) {
            mImage2.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr2));
        }

        if (StringUtils.isEmpty(mCurrentCard.answer.backgroundImageUriFormatStr) == false) {
            setCardBackgroundImageWithUri(mCurrentCard.answer.backgroundImageUriFormatStr);
        } else {
            setCardBackgroundImageDefault();
        }

    }


    private void enableCardEditable() {
        mLogoURLImage.setVisibility(View.VISIBLE);

        mTitle.setEnabled(true);
        mSidebarTitle.setEnabled(true);
        mSubheading.setEnabled(true);
        mMain.setEnabled(true);
        mSub.setEnabled(true);

        mCreator.setEnabled(true);
        mJobTitle.setEnabled(true);
        mImage.setEnabled(true);
        mImage2.setEnabled(true);

        mCreator.setBackgroundResource(R.drawable.shape_edittext_editable);
        mJobTitle.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSubheading.setBackgroundResource(R.drawable.shape_edittext_editable);
        mMain.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSub.setBackgroundResource(R.drawable.shape_edittext_editable);


    }

    private void disableCardEditable() {

        mLogoURLImage.setVisibility(View.INVISIBLE);

        mTitle.setEnabled(false);
        mSidebarTitle.setEnabled(false);
        mSubheading.setEnabled(false);
        mMain.setEnabled(false);
        mSub.setEnabled(false);

        mCreator.setEnabled(false);
        mJobTitle.setEnabled(false);

        if (mIsQuestionShowing) {
            if (mCurrentCard.question.movieUriFormatStr.length() > 0) {
                //allow to play movie
                mImage.setEnabled(true);

            } else {
                mImage.setEnabled(false);

            }

            if (mCurrentCard.question.movieUriFormatStr2.length() > 0) {
                //allow to play movie
                mImage2.setEnabled(true);

            } else {
                mImage2.setEnabled(false);

            }

        } else {
            if (mCurrentCard.answer.movieUriFormatStr.length() > 0) {
                //allow to play movie
                mImage.setEnabled(true);

            } else {
                mImage.setEnabled(false);

            }

            if (mCurrentCard.answer.movieUriFormatStr2.length() > 0) {
                //allow to play movie
                mImage2.setEnabled(true);

            } else {
                mImage2.setEnabled(false);

            }
        }

        mCreator.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mJobTitle.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSubheading.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mMain.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSub.setBackgroundResource(R.drawable.shape_edittext_no_editable);

    }

    /**
     * put save here when creating a new card
     * put save in onKeyboardClose when editting a current card
     */
    public void saveNewCreatedCard() {

        if (mIsTakeSnapshotAllNeeded) {
            ((MainActivity) getActivity()).setMaskButtonForContentUpdating();
            takeSnapshotAll();
            mCurrentPack.save(AppContext.getAppContext());

        } else {
            takeSnapshotCurrentCard();
        }

        mCurrentPack.addCard(AppContext.getAppContext(), mCurrentCard);
        LOGD(TAG, "saveNewCreatedCard: finish execution of saveNewCreatedCard");

        mIsTakeSnapshotAllNeeded = false;


    }

    /**
     * Snap all the cards under current pack
     * take care of notification updating master list view
     */
    public void takeSnapshotAll() {

        mSemaphore = 0;

        //step1: take snapshot on current card
        takeSnapshotCurrentCard();

        //step2: take snapshot on others card under current pack
        ((MainActivity) getActivity()).prepareSnapShotAllExceptCurrentCard(mCurrentPack, mCurrentCard);
    }


    /**
     * do save when editing current card
     * do NOT save when creating a new card
     * do NOT refresh card list view
     */
    private void takeSnapshotCurrentCard() {

        boolean toggle = false;

        if (isEditableMode()) {
            disableCardEditable();
        }

        if (mIsQuestionShowing == false) {
            switchToQuestionViewWithOption(false);
            toggle = true;
        }

        //hide logo image if its placeholder
        if (mCurrentPack.logoImageUriFormatStr.contains("placeholder") == true) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }

        View cardView = mContentView.findViewById(R.id.card);
        Bitmap bitmap = UIHelper.loadBitmapFromView(cardView);
        File savedFile = UIHelper.saveImageToCaches(bitmap);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        mCurrentCard.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(savedFile);

        //restore and show logo image
        mLogoImage.setVisibility(View.VISIBLE);

        if (isEditableMode()) {
            enableCardEditable();
        }

        if (mIsCreatingCard == false) {
            mCurrentCard.save(AppContext.getAppContext());
        }

        if (toggle == true) {
            switchToAnswerView(false);
        }

        //Notify master list view to update
        mSemaphore++;
        if (mSemaphore == mCurrentPack.cards.size()) {
            Intent intent = new Intent();
            intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
            getActivity().sendBroadcast(intent);
            mSemaphore = 0;

            ((MainActivity) getActivity()).finishSnapShotAllExceptCurrent();
        }

    }

    /**
     * 选某个主题颜色后的回调
     * @param cardColorTemplateIndex
     */
    public void cardColorTemplateSelectedPostAction(int cardColorTemplateIndex) {
        switch (cardColorTemplateIndex) {
            case 0:
                mSidebarBackground.setBackgroundResource(R.drawable.shape_card_blue_left_corner);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_blue);
                break;
            case 1:
                mSidebarBackground.setBackgroundResource(R.drawable.shape_card_coffee_left_corner);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_coffee);
                break;
            case 2:
                mSidebarBackground.setBackgroundResource(R.drawable.shape_card_gray_left_corner);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_gray);
                break;
            case 3:
                mSidebarBackground.setBackgroundResource(R.drawable.shape_card_purple_left_corner);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_purple);
                break;
            case 4:
                mSidebarBackground.setBackgroundResource(R.drawable.shape_card_red_left_corner);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_red);
                break;
            default:
                LOGE(TAG, "cardColorTemplateSelectedPostAction: Out of range");
        }

        String templateBackground = StringUtils.convertTemplateBackgroundIndexToString(cardColorTemplateIndex);
        mCurrentCard.templateBackground = templateBackground;


        if (!mIsCreatingCard) {


            for (Card card : mCurrentPack.cards) {
                card.templateBackground = templateBackground;
            }
            takeSnapshotAll();
        }
    }

    /**
     * determine whether the card is editable or not
     *
     * @return
     */
    private boolean isEditableMode() {
        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Milli seconds is returned
    */
    public int durationForQuestionRecordedSound () {

        int duration = 0;

        if (StringUtils.isEmpty(mCurrentCard.question.audioUriFormatStr)) {
            return duration;
        }

        try {
            Uri uri = Uri.parse(mCurrentCard.question.audioUriFormatStr);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(AppContext.getAppContext(),uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Integer.parseInt(durationStr);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return duration;
    }

    /*
     * Milli seconds is returned
     */
    public int durationForAnswerRecordedSound () {

        int duration = 0;

        if (StringUtils.isEmpty(mCurrentCard.answer.audioUriFormatStr)) {
            return duration;
        }

        try {
            Uri uri = Uri.parse(mCurrentCard.answer.audioUriFormatStr);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(AppContext.getAppContext(),uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Integer.parseInt(durationStr);
        } catch (Exception e) {
            e.printStackTrace();
        }



        return duration;
    }


    private void updateQuestionViewTemplate() {

        int templateID = mCurrentCard.question.templateID;


        switch (templateID) {
            case 0: {
                configTemplate_0();
                break;
            }
            case 1: {
                configTemplate_1();
                break;
            }
            case 2: {
                configTemplate_2();
                break;
            }
            case 3: {
                configTemplate_3();
                break;
            }
            case 4: {
                configTemplate_4();
                break;
            }
            case 5: {
                configTemplate_5();
                break;
            }

            case 6: {
                configTemplate_6();
                break;
            }

            case 7: {
                configTemplate_7();
                break;
            }

            case 8: {
                configTemplate_8();
                break;
            }

            case 9: {
                configTemplate_9();
                break;
            }

            case 10: {
                configTemplate_10();
                break;
            }

            case 11: {
                configTemplate_11();
                break;
            }

            case 12: {
                configTemplate_12();
                break;
            }

            case 13: {
                configTemplate_13();
                break;
            }

            case 14: {
                configTemplate_14();
                break;
            }

            case 15: {
                configTemplate_15();
                break;
            }

            case 16: {
                configTemplate_16();
                break;
            }

            default:
                LOGE(TAG, "updateQuestionViewTemplate: mCurrentCard.question.templateID is out of scope");
        }

        updateContentViewsPointers(templateID);

    }

    private void updateAnswerViewTemplate() {

        int templateID = mCurrentCard.answer.templateID;

        //we don't need to set font size here since it will be done in CSS construct

        switch (templateID) {
            case 0: {
                configTemplate_6();
                break;
            }
            case 1: {
                configTemplate_8();
                break;
            }
            case 2: {
                configTemplate_10();
                break;
            }
            case 3: {
                configTemplate_4();
                break;
            }
            case 4: {
                configTemplate_7();
                break;
            }
            case 5: {
                configTemplate_5();
                break;
            }
            case 6: {
                configTemplate_3();
                break;
            }
            case 7: {
                configTemplate_2();
                break;
            }
            case 8: {
                configTemplate_1();
                break;
            }
            case 9: {
                configTemplate_11();
                break;
            }

            case 10: {
                configTemplate_0();
                break;
            }

            case 11: {
                configTemplate_9();
                break;
            }

            case 12: {
                configTemplate_12();
                break;
            }

            case 13: {
                configTemplate_13();
                break;
            }

            case 14: {
                configTemplate_14();
                break;
            }

            case 15: {
                configTemplate_15();
                break;
            }

            case 16: {
                configTemplate_16();
                break;
            }

            default:
                LOGE(TAG, "updateAnswerViewTemplate: mCurrentCard.answer.templateID is out of scope");
        }

        updateContentViewsPointers(templateID);
    }


    private void configTemplate_0 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);

        //subheading
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 50;
        params.height = 0;
        mSubheading.setLayoutParams(params);
        mContentBodyLinearLayout.addView(mSubheading);


        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 350;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mMain);
    }

    private void configTemplate_1 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //subheading
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 50;
        params.height = 0;
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.question_template_1_margin_right);
        mSubheading.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mSubheading);


        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 180;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mMain);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 160;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mSub);
    }

    private void configTemplate_2 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //main
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 280;
        params.height = 0;
        mMain.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mMain);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 100;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mSub);
    }

    private void configTemplate_3 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);

        //main
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 200;
        params.height = 0;
        mMain.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mMain);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 190;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mSub);
    }

    private void configTemplate_4 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);

        //main
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mMain);
    }



    private void configTemplate_5 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.INVISIBLE);
        mSub.setVisibility(View.INVISIBLE);

        //mImage
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mImage);
    }

    private void configTemplate_6 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);

        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = LinearLayout.LayoutParams.FILL_PARENT;
        params.weight = 70;
        params.height = 0;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);


        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 360;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        left.addView(mMain);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_7 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);


        //5. main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMain.setLayoutParams(params);

        left.addView(mMain);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_8 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);

        //上布局
        LinearLayout top = new LinearLayout(getActivity());
        top.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 60f;
        params.rightMargin = UIHelper.getPixels(4);
        top.setLayoutParams(params);

        mContentBodyLinearLayout.addView(top);


        //下布局
        LinearLayout bottom = new LinearLayout(getActivity());
        bottom.setOrientation(LinearLayout.HORIZONTAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 345f;
        params.topMargin = UIHelper.getPixels(4);
        bottom.setLayoutParams(params);

        mContentBodyLinearLayout.addView(bottom);

        //子下左布局
        LinearLayout bottomLeft = new LinearLayout(getActivity());
        bottomLeft.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        bottomLeft.setLayoutParams(params);


        bottom.addView(bottomLeft);

        //子下右布局
        LinearLayout bottomRight = new LinearLayout(getActivity());
        bottomLeft.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 350f;
        bottomRight.setLayoutParams(params);

        bottom.addView(bottomRight);


        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSubheading.setLayoutParams(params);

        top.addView(mSubheading);

        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 295f;
        mMain.setLayoutParams(params);

        bottomLeft.addView(mMain);

        // sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 50f;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        bottomLeft.addView(mSub);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        bottomRight.addView(mImage);
    }

    private void configTemplate_9 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.VISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.INVISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);


        //mSubheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 70f;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);

        //image2
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 360f;
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin, margin, margin, margin);
        mImage2.setLayoutParams(params);

        left.addView(mImage2);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_10 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);


        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 215f;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);

        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 0;
        params.weight = 215f;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        left.addView(mMain);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_11 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);


        //mMain
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMain.setLayoutParams(params);
        left.addView(mMain);


        //mSub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSub.setLayoutParams(params);
        right.addView(mSub);
    }

    private void configTemplate_12 () {
        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.INVISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 145f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 545f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);


        //mMain
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMain.setLayoutParams(params);
        left.addView(mMain);


        //mSub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSub.setLayoutParams(params);
        right.addView(mSub);
    }

    private void configTemplate_13 () {
        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);

        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 355;
        params.height = 0;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);


        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 60;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        left.addView(mMain);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_14 () {
        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);

        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = LinearLayout.LayoutParams.FILL_PARENT;
        params.weight = 100;
        params.height = 0;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);


        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 180;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        left.addView(mMain);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 100;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        left.addView(mSub);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        right.addView(mImage);
    }

    private void configTemplate_15 () {
        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.VISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.INVISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);

        //image2
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 310;
        params.height = 0;
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin, margin, margin, margin);
        mImage2.setLayoutParams(params);

        left.addView(mImage2);

        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 60;
        params.height = 0;
        mSubheading.setLayoutParams(params);

        left.addView(mSubheading);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 310;
        params.height = 0;
        params.setMargins(margin, margin, margin, margin);
        mImage.setLayoutParams(params);

        right.addView(mImage);

        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 60;
        params.height = 0;
        mMain.setLayoutParams(params);

        right.addView(mMain);


    }

    private void configTemplate_16 () {
        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.VISIBLE);


        //左布局
        LinearLayout left = new LinearLayout(getActivity());
        left.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 360f;
        params.rightMargin = UIHelper.getPixels(4);
        left.setLayoutParams(params);

        mContentBodyLinearLayout.addView(left);

        //右布局
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout right = new LinearLayout(getActivity());
        right.setOrientation(LinearLayout.VERTICAL);
        params.width = 0;
        params.weight = 350f;
        right.setLayoutParams(params);

        mContentBodyLinearLayout.addView(right);

        //subheading
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 100;
        params.height = 0;
        mSubheading.setLayoutParams(params);

        right.addView(mSubheading);

        //main
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 180;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mMain.setLayoutParams(params);

        right.addView(mMain);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 100;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(4);
        mSub.setLayoutParams(params);

        right.addView(mSub);


        //image
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = UIHelper.getPixels(4);
        params.setMargins(margin,margin,margin,margin);
        mImage.setLayoutParams(params);

        left.addView(mImage);


    }

    private void removeAllSubViewsInContentBody() {

        if(mMain.getParent()!=null)
            ((ViewGroup)mMain.getParent()).removeView(mMain);

        if(mSubheading.getParent()!=null)
            ((ViewGroup)mSubheading.getParent()).removeView(mSubheading);


        if(mSub.getParent()!=null)
            ((ViewGroup)mSub.getParent()).removeView(mSub);

        if(mImage.getParent()!=null)
            ((ViewGroup)mImage.getParent()).removeView(mImage);

        if(mImage2.getParent()!=null)
            ((ViewGroup)mImage2.getParent()).removeView(mImage2);


        int n = mContentBodyLinearLayout.getChildCount();
        for (int i = 0; i < n; i++) {
            View item = mContentBodyLinearLayout.getChildAt(i);
            if (item.getClass().isInstance(ViewGroup.class)) {
                int m = ((ViewGroup) item).getChildCount();
                if (m > 0) {
                    ((ViewGroup)item).removeAllViews();
                }
            }

        }
        if (n > 0) {
            mContentBodyLinearLayout.removeAllViews();
        }




    }



    /**
     * 所谓CSS就是：颜色，字体，对其，大小
     */
    private void updateQuestionCSS() {

        mTitle.setTextColor(Color.parseColor("#0910FF"));

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subheadingAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.question.css.subheadingAlignVertical));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.mainAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.question.css.mainAlignVertical));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.question.css.subAlignVertical));

        //step2: size
        float scaleVal;
        if (mIsPlayingCard) {
            scaleVal = Global.scaleInPlayMode;
        } else {
            scaleVal = (float) 1.0;
        }

        mSubheading.setTextSize((mCurrentCard.question.css.subheadingSize * scaleVal));
        mMain.setTextSize((mCurrentCard.question.css.mainSize * scaleVal));
        mSub.setTextSize((mCurrentCard.question.css.subSize * scaleVal));

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subColor));


        mSubheading.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.subheadingFont), Typeface.BOLD);
        mMain.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.mainFont), Typeface.BOLD);
        mSub.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.subFont), Typeface.BOLD);
    }

    /**
     * 所谓CSS就是：颜色，字体，对其，大小
     */
    private void updateAnswerCSS() {

        float scaleVal;
        if (mIsPlayingCard) {
            scaleVal = Global.scaleInPlayMode;
        } else {
            scaleVal = (float) 1.0;
        }

        mTitle.setTextColor(Color.RED);

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subheadingAlignVertical));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.mainAlignVertical));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subAlignVertical));

        //step2: size
        mSubheading.setTextSize((mCurrentCard.answer.css.subheadingSize * scaleVal));
        mMain.setTextSize((mCurrentCard.answer.css.mainSize * scaleVal));
        mSub.setTextSize((mCurrentCard.answer.css.subSize * scaleVal));

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subColor));


        mSubheading.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.answer.css.subheadingFont), Typeface.BOLD);
        mMain.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.answer.css.mainFont), Typeface.BOLD);
        mSub.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.answer.css.subFont), Typeface.BOLD);
    }



    public void updateCSS(int menuID, int subMenuID) {
        CSS currentCSS;

        if ((mCurrentFocusedCardContentText == null) || (mCurrentFocusedCardContentText.getTag() == null)) {
            LOGE(TAG, "updateCSS: mCurrentFocusedCardContentText or mCurrentFocusedCardContentText.getTag()  is null during execution on updateCSS");
            return;
        }


        //Step2: determine operation target
        String editTextTag = (String) mCurrentFocusedCardContentText.getTag();
        if (mIsQuestionShowing) {
            currentCSS = mCurrentCard.question.css;
        } else {
            currentCSS = mCurrentCard.answer.css;
        }


        //Step3: fill values
        String[] sizeArray = ScaleHelper.getRealSizeStringArray(getActivity()); //我们不能从R.array.css_size获取，因为它仅仅是名义值，而不是真实的值


        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        String[] fontArray = getResources().getStringArray(R.array.css_font);
        switch (menuID) {
            case 0:   //stand for align

                //由于CSS存储时，只有两个值Vertical或者空，所以对于Vertical Alignment，我们需要特殊化一下（兼容ios）
                boolean isVerticalAlign = false;
                if (subMenuID == 3) {
                    isVerticalAlign = true;
                    alignArray[subMenuID + 1] = "Vertical";  //这里非常特殊，在iOS中，我们没有vertical center和vertical top的概念，只有vertical。所以如果是vertical，在android中认为是vertical center;否则为空
                } else if (subMenuID == 4) {
                    isVerticalAlign = true;
                    alignArray[subMenuID + 1] = ""; //vertical top
                }

                int horizontalGravity;
                int verticallGravity;

                if (editTextTag.equals(TAG_SUBHEADING)) {
                    if (isVerticalAlign) {
                        currentCSS.subheadingAlignVertical = alignArray[subMenuID + 1];
                    } else {
                        currentCSS.subheadingAlign = alignArray[subMenuID + 1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.subheadingAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.subheadingAlignVertical);

                } else if (editTextTag.equals(TAG_MAIN)) {
                    if (isVerticalAlign) {
                        currentCSS.mainAlignVertical = alignArray[subMenuID + 1];
                    } else {
                        currentCSS.mainAlign = alignArray[subMenuID + 1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.mainAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.mainAlignVertical);

                } else if (editTextTag.equals(TAG_SUB)) {
                    if (isVerticalAlign) {
                        currentCSS.subAlignVertical = alignArray[subMenuID + 1];
                    } else {
                        currentCSS.subAlign = alignArray[subMenuID + 1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.subAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.subAlignVertical);
                } else {
                    horizontalGravity = StringUtils.convertGravityStringToInt("");  //为了兼容iOS（iOS中只有vertical,没有vertical center或top概念）
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt("");
                }

                mCurrentFocusedCardContentText.setGravity(horizontalGravity | verticallGravity);

                break;

            case 1:   //stand for size

                float size = Float.parseFloat(sizeArray[subMenuID]);  //是个纯text real size的数组，不带Size描述

                if (editTextTag.equals(TAG_SUBHEADING)) {
                    currentCSS.subheadingSize = size;
                } else if (editTextTag.equals(TAG_MAIN)) {
                    currentCSS.mainSize = size;
                } else if (editTextTag.equals(TAG_SUB)) {
                    currentCSS.subSize = size;
                }

                double scaleVal;
                if (mIsPlayingCard) {
                    scaleVal = Global.scaleInPlayMode;
                } else {
                    scaleVal = 1.0;
                }


                mCurrentFocusedCardContentText.setTextSize((int) (size * scaleVal));

                break;
            case 2:   //stand for color

                if (editTextTag.equals(TAG_SUBHEADING)) {
                    currentCSS.subheadingColor = colorArray[subMenuID + 1];
                } else if (editTextTag.equals(TAG_MAIN)) {
                    currentCSS.mainColor = colorArray[subMenuID + 1];
                } else if (editTextTag.equals(TAG_SUB)) {
                    currentCSS.subColor = colorArray[subMenuID + 1];
                }

                switch (subMenuID) {
                    case 0:
                        mCurrentFocusedCardContentText.setTextColor(Color.RED);
                        break;
                    case 1:
                        mCurrentFocusedCardContentText.setTextColor(Color.BLUE);
                        break;
                    case 2:
                        mCurrentFocusedCardContentText.setTextColor(Color.BLACK);
                        break;
                    case 3:
                        mCurrentFocusedCardContentText.setTextColor(Color.YELLOW);
                        break;
                    case 4:
                        mCurrentFocusedCardContentText.setTextColor(Color.GREEN);
                        break;
                    case 5:
                        mCurrentFocusedCardContentText.setTextColor(Color.WHITE);
                        break;
                    default:
                        LOGE(TAG, "updateCSS: Out of range of subMenuID");
                }
                break;
            case 3:   //font


                if (editTextTag.equals(TAG_SUBHEADING)) {
                    currentCSS.subheadingFont = fontArray[subMenuID + 1];
                } else if (editTextTag.equals(TAG_MAIN)) {
                    currentCSS.mainFont = fontArray[subMenuID + 1];
                } else if (editTextTag.equals(TAG_SUB)) {
                    currentCSS.subFont = fontArray[subMenuID + 1];
                }

                mCurrentFocusedCardContentText.setTypeface(FontHelper.fontFromArrayIndex(AppContext.getAppContext(), subMenuID));

                break;
            default:
                LOGE(TAG, "updateCSS: Out of range of menuID");
        }


        if (!mIsCreatingCard) {
            if (mIsQuestionShowing) {
                mCurrentCard.question.css.save(AppContext.getAppContext());
            } else {
                mCurrentCard.answer.css.save(AppContext.getAppContext());
            }
        }

        ((MainActivity)getActivity()).updateSpinnersHighlightedItem(currentCSS, editTextTag);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        LOGD(TAG, "onTouch: "+ "event.getAction=" + event.getAction());


        if ((v.getTag() != null) && (event.getAction() == MotionEvent.ACTION_DOWN)) {

            ((MainActivity) getActivity()).mIsKeyboardVisible = true;
            ((MainActivity) getActivity()).setAsKeyboardStatus();

            String tag = (String) v.getTag();

            if ((tag.equals(TAG_SUBHEADING)) || (tag.equals(TAG_MAIN)) || (tag.equals(TAG_SUB))) {

                //check card.xml for tag
                ((MainActivity) getActivity()).mIsEdittingCard = true;

                ((MainActivity) getActivity()).prepareCSSToolbar();

                CSS currentCSS;
                if (mIsQuestionShowing) {
                    currentCSS = mCurrentCard.question.css;
                } else {
                    currentCSS = mCurrentCard.answer.css;
                }
                ((MainActivity) getActivity()).showCSSToolbar(currentCSS,tag);

                mCurrentFocusedCardContentText = (EditText) v;
            } else {
                ((MainActivity) getActivity()).removeCSSToolbar();
            }
        }

        return false; //don't set to false;
    }

    /**
     * simply close keyboard and do nothing
     */
    public void dismissKeyboard() {
        if (mCurrentFocusedCardContentText != null) {
            mIMM.hideSoftInputFromWindow(mCurrentFocusedCardContentText.getWindowToken(), 0);
        } else {
            LOGD(TAG, "dismissKeyboard: mCurrentFocusedCardContentText is null");
        }

    }

    /*
     * 键盘从出现到消失，Cursor is causing text to go up the screen
     */
    private void restoreDefaultCursorPosition() {

        mSubheading.setSelection(0);
        mMain.setSelection(0);
        mSub.setSelection(0);
    }



    /**
     * Check whether the view has appeared
     * Since there's no iOS like ViewDidAppear method, we have to do here
     */
    class ViewDidAppearTask extends AsyncTask<Integer, Integer, String> {

        final View cardView = mContentView.findViewById(R.id.card);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {

            while (cardView.getHeight() == 0 || cardView.getWidth() == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            takeSnapshotCurrentCard();
            mIsSnapShotNotCurrent = false;

        }

    }



    public void saveEditedCard() {

        //step2: prepare update info in mast list view
        if (mIsTakeSnapshotAllNeeded && (mIsCreatingCard == false)) {
            ((MainActivity) getActivity()).setMaskButtonForContentUpdating();
        }

        //step3:
        //mCurrentPack and mCurrentCard save have been done in addTextChangedListener

        //step4: take screenshot if necessary
        if (mIsQuestionShowing) {
            if (mIsTakeSnapshotAllNeeded) {
                takeSnapshotAll();
            } else {
                if (mIsQuestionShowing) {
                    takeSnapshotCurrentCard();
                }
            }
        }

        //step5:save logic if not creating a new card
        if (mIsCreatingCard) {
            //we will do that when we click the save button
        } else {
            //We do here
            removeAllLineBreaksBeforeCardSave();

            mCurrentPack.save(AppContext.getAppContext());
            mCurrentCard.save(AppContext.getAppContext());

            mIsTakeSnapshotAllNeeded = false; //after snapshot, we need to set to default value
        }

        //Update actionbar
        getActivity().getActionBar().show();
        ((MainActivity) getActivity()).mIsEdittingCard = false;
        getActivity().invalidateOptionsMenu();

        //Update master view (cover image)
        if ((mIsTakeSnapshotAllNeeded == false) && (mIsCreatingCard == false)) {
            Intent intent = new Intent();
            intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
            getActivity().sendBroadcast(intent);
        }

        ((MainActivity) getActivity()).removeCSSToolbar();

        PackRecordHelper.savePackUpdateRecord(AppContext.getAppContext(), mCurrentPack);
    }

    public void onGridViewItemClicked(String text) {

        int start = mCurrentFocusedCardContentText.getSelectionStart();
        String beforeString = mCurrentFocusedCardContentText.getText().toString().substring(0, start);
        String afterString = mCurrentFocusedCardContentText.getText().toString().substring(start);

        int offset = 0;
        String insertText = text;
        if (text.toLowerCase().equals(SymbolHelper.K_Space_Bar_Lowcase)) {
            insertText = " ";
            offset = 1;
            mCurrentFocusedCardContentText.setText(beforeString + insertText + afterString);
        } else if (text.toLowerCase().equals(SymbolHelper.K_Line_Break_Lowcase)) {
            insertText = "\n";
            offset = 1;
            mCurrentFocusedCardContentText.setText(beforeString + insertText + afterString);
        } else if (text.toLowerCase().equals(SymbolHelper.K_Delete_Lowcase)) {

            if (beforeString.length() > 0) {
                offset = -1;
            }

            if (beforeString.length() - 2 >= 0) {
                beforeString = beforeString.substring(0, beforeString.length() - 1);
            } else {
                beforeString = "";
            }

            mCurrentFocusedCardContentText.setText(beforeString + afterString);

        } else {
            offset = text.length();
            mCurrentFocusedCardContentText.setText(beforeString + insertText + afterString);
        }

        if (mCurrentFocusedCardContentText.getText().toString().length() == 0) {
            mCurrentFocusedCardContentText.setSelection(0);
        } else if (mCurrentFocusedCardContentText.getText().toString().length() == 1) {
            mCurrentFocusedCardContentText.setSelection(1);
        } else {
            mCurrentFocusedCardContentText.setSelection(start + offset);
        }


        LOGD(TAG, "onGridViewItemClicked: " + "the result is:" + mCurrentFocusedCardContentText.getText().toString());

    }

    private void removeAllLineBreaksBeforeCardSave() {

        String str = null;

        str = mCurrentCard.question.main.replaceAll("\\n+$", "");
        mCurrentCard.question.main = str;

        str = mCurrentCard.question.sub.replaceAll("\\n+$", "");
        mCurrentCard.question.sub = str;

        str = mCurrentCard.question.subheading.replaceAll("\\n+$", "");
        mCurrentCard.question.subheading = str;


        str = mCurrentCard.answer.main.replaceAll("\\n+$", "");
        mCurrentCard.answer.main = str;

        str = mCurrentCard.answer.sub.replaceAll("\\n+$", "");
        mCurrentCard.answer.sub = str;

        str = mCurrentCard.answer.subheading.replaceAll("\\n+$", "");
        mCurrentCard.answer.subheading = str;
    }


    /*
      Be sure to have exact size of bitamp with card, othervise, the rounded size could vary;
     */
    private void setCardBackgroundImageWithBitmap(Bitmap bitmap) {

        if (bitmap == null) {
            LOGD(TAG, "setCardBackgroundImageWithBitmap: null bitmap for setCardBackgroundImageWithBitmap");
            return;
        }

        //set background image

// 我们comment掉，因为有一种更好的解决方法（注释掉的方法执行效率较低）
//        int width = UIHelper.getCardBackgroundWidth(getActivity(),mIsPlayingCard);
//        int height = UIHelper.getCardBackgroundHeight(getActivity(),mIsPlayingCard);
//
//        Bitmap resizedBitmap = UIHelper.resizedBitmapWithScaleToFit(bitmap,width,height);
//
//        width = resizedBitmap.getWidth();
//        height = resizedBitmap.getHeight();
//
//        int pixel = getResources().getDimensionPixelSize(R.dimen.card_round_corner);
//        Bitmap bottomRightCornerBitmap = UIHelper.getRoundedBottomRightCornerBitmap(resizedBitmap,pixel);

        RoundedBottomRightImageView backgroundImageView = (RoundedBottomRightImageView) mContentView.findViewById(R.id.card_background_image);
        backgroundImageView.setImageBitmap(bitmap);

    }


    private void setCardBackgroundImageWithUri(String uriString) {
        File f = new File(FileOperationHelper.deleteUriSchemeHeader(uriString));
        Drawable drawable = Drawable.createFromPath(f.getAbsolutePath());

        if (drawable == null) {
            LOGD(TAG, "setCardBackgroundImageWithUri: null drawable for setCardBackgroundImageWithUri");
        } else {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            setCardBackgroundImageWithBitmap(bitmap);
        }
    }


    private void setCardBackgroundImageDefault() {
        RoundedBottomRightImageView backgroundImageView = (RoundedBottomRightImageView) mContentView.findViewById(R.id.card_background_image);
        backgroundImageView.setImageBitmap(null);

    }

    //used in edit card mode
    private void setCardBackgroundMaskGray() {
        View card = mContentView.findViewById(R.id.card_mask);
        card.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_gray));
    }

    //used in play mode and create card mode
    private void setCardBackgroundMaskBlack() {
        View card = mContentView.findViewById(R.id.card_mask);
        card.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_black));
    }


    public ArrayList textToSpeechContentArray() {
        ArrayList<String> arrayList = new ArrayList<String>();

        if (mIsQuestionShowing) {

            Question question = mCurrentCard.question;
            if (question.subheading.length() > 0) {

                arrayList.add(replaceBasicSymbol(question.subheading));
            }
            if (question.main.length() > 0) {
                arrayList.add(replaceBasicSymbol(question.main));
            }
            if (question.sub.length() > 0) {
                arrayList.add(replaceBasicSymbol(question.sub));
            }

        } else {
            Answer answer = mCurrentCard.answer;
            if (answer.subheading.length() > 0) {
                arrayList.add(replaceBasicSymbol(answer.subheading));
            }
            if (answer.main.length() > 0) {
                arrayList.add(replaceBasicSymbol(answer.main));
            }
            if (answer.sub.length() > 0) {
                arrayList.add(replaceBasicSymbol(answer.sub));
            }
        }

        if (arrayList.size() == 0) {
            arrayList.add("    "); //in auto delay mode, we need this. Otherwise, scroll could not go on since there's no content
        }

        return arrayList;
    }


    private String replaceBasicSymbol(String str) {
        String resultStr;

        String plusStr = " Plus ";
        String timesStr = " Times ";
        String dividedByStr = " divided by ";
        String minusStr = " minus ";

        String equalsStr = " equals ";
        String cubicMetresStr = "  Cubic Metres ";
        String squareMetresStr = " Square Metres ";
        String squareFeetStr = " Square feet ";

        String cubicFeetStr = "  Cubic Feet ";
        String squareInchesStr = " Square Inches ";
        String cubicInchesStr = " Cubic Inches ";
        String cubicCentimetresStr = " Cubic Centi metres ";

        String squareCentimetresStr = " Square Centi metres ";
        String cubicMillimetresStr = " Cubic Milli metres ";
        String squareMillimetresStr = " Square milli metres ";
        String degreesCelsiusStr = " Degrees Celsius ";

        String degreesFahrenheitStr = "  Degrees Fahrenheit ";
        String degreesRankinStr = " Degrees Rankin ";
        String degresssKelvinStr = " Degrees Kelvin ";
        String carbonDioxideStr = " Carbon Dioxide ";

        String nitrogenStr = " Nitrogen ";
        String oxygenStr = " Oxygen ";
        String pieStr = " Pie ";
        String squareRdiusStr = " square radius ";

        String OzoneStr = "  Ozone ";
        String perStr = " per ";
        String millibarStr = " milli bar equals";
        String percentStr = " percent ";

        String radiusStr = "  Radius equals ";
        String diameterStr = " Diameter equals ";
        String greaterThenStr = " Greater then ";
        String lessThenStr = " Less then ";

        String squareRootStr = " square root ";

        resultStr = str.replace("+", plusStr);
        resultStr = resultStr.replace("⨯", timesStr);
        resultStr = resultStr.replace("÷", dividedByStr);
        resultStr = resultStr.replace("-", minusStr);

        resultStr = resultStr.replace("cm²", squareCentimetresStr);
        resultStr = resultStr.replace("cm³", cubicCentimetresStr);
        resultStr = resultStr.replace("mm²", squareMillimetresStr);
        resultStr = resultStr.replace("mm³", cubicMillimetresStr);


        resultStr = resultStr.replace("m³", cubicMetresStr);
        resultStr = resultStr.replace("m²", squareMetresStr);
        resultStr = resultStr.replace("ft²", squareFeetStr);

        resultStr = resultStr.replace("ft³", cubicFeetStr);
        resultStr = resultStr.replace("in²", squareInchesStr);
        resultStr = resultStr.replace("in³", cubicInchesStr);

        resultStr = resultStr.replace("°C", degreesCelsiusStr);

        resultStr = resultStr.replace("°F", degreesFahrenheitStr);
        resultStr = resultStr.replace("°R", degreesRankinStr);
        resultStr = resultStr.replace("°K", degresssKelvinStr);
        resultStr = resultStr.replace("CO₂", carbonDioxideStr);

        resultStr = resultStr.replace("N₂", nitrogenStr);
        resultStr = resultStr.replace("O₂", oxygenStr);
        resultStr = resultStr.replace("π", pieStr);
        resultStr = resultStr.replace("r²", squareRdiusStr);

        resultStr = resultStr.replace("O₃", OzoneStr);
        resultStr = resultStr.replace("∕", perStr);
        resultStr = resultStr.replace("/", perStr);//not unicode
        resultStr = resultStr.replace("mb=", millibarStr);//mb = millibar
        resultStr = resultStr.replace("mb =", millibarStr);//mb = millibar
        resultStr = resultStr.replace("%", percentStr); //not unicode

        resultStr = resultStr.replace("r=", radiusStr);
        resultStr = resultStr.replace("r =", radiusStr);
        resultStr = resultStr.replace("d=", diameterStr);
        resultStr = resultStr.replace("d =", diameterStr);
        resultStr = resultStr.replace(">", greaterThenStr);
        resultStr = resultStr.replace("<", lessThenStr);

        resultStr = resultStr.replace("√", squareRootStr);

        resultStr = resultStr.replace(" = ", equalsStr);// , not unicode  //need to put the last


        return resultStr;

    }


    public boolean isCurrentFocusedCardContentTextUsingDefaultFont() {

        if (mCurrentFocusedCardContentText == null) {
            return true;
        }

        String fontStr = "";
        String editTextTag = (String) mCurrentFocusedCardContentText.getTag();
        if (editTextTag.equals(TAG_SUBHEADING)) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.subheadingFont;
            } else {
                fontStr = mCurrentCard.answer.css.subheadingFont;
            }
        } else if (editTextTag.equals(TAG_MAIN)) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.mainFont;
            } else {
                fontStr = mCurrentCard.answer.css.mainFont;
            }
        } else if (editTextTag.equals(TAG_SUB)) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.subFont;
            } else {
                fontStr = mCurrentCard.answer.css.subFont;
            }
        }

        if ((fontStr == null) || (fontStr.length() == 0) || (fontStr.toLowerCase().contains("default"))) {
            return true;
        } else {
            return false;
        }

    }

    public void showTooltips() {

        if (mIsPlayingCard) {
            return;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                TipHelper.showTipForLogo(getActivity(), mLogoImage);

                TipHelper.showTipForChangeTemplate(getActivity(), mChangeTemplateImage);
                TipHelper.showTipForRecordSound(getActivity(), mPlayRecordImage);
                TipHelper.showTipForChangeBackground(getActivity(), mChangeBackgroundImage);

                if (mImage.getVisibility() == View.VISIBLE) {
                    TipHelper.showTipForImage(getActivity(), mImage);
                }

                TipHelper.showTipForSegmentQuestion(getActivity(), mQuestionRadioButton);
                TipHelper.showTipForSegmentAnswer(getActivity(), mAnswerRadioButton);

                TipHelper.showTipForLinkButton(getActivity(), mLogoURLImage);

            }

        }, 300); // 300ms delay
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LOGD(TAG, "onActivityResult");

        final int requestCodeFinal = requestCode;
        final int resultCodeFinal = resultCode;
        final Intent dataFinal = data;

        //whatever RESULT_OK or RESULT_CANCELED, we need to do this first
        ((MainActivity) getActivity()).mIsAllowedToShowPackList = false;

        int delay = 0;
        if (mContentView.getHeight() > mContentView.getWidth()) {
            delay = 1000;
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (resultCodeFinal == Activity.RESULT_OK) {

                    Uri selectedURI = dataFinal.getData();

                    if (requestCodeFinal == REQUEST_CODE_FROM_BACKGROUND) {
                        LOGD(TAG, "onActivityResult: ready to crop");
                        Intent intent = new Intent(getActivity(), CropActivity.class);
                        intent.putExtra("uri",selectedURI);
                        startActivityForResult(intent, REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED);

                    } else if (requestCodeFinal == REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED) {

                        handleCrop(REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED,resultCodeFinal,dataFinal);

                    } else {
                        String decodeUriStr = "";
                        try {
                            decodeUriStr = URLDecoder.decode(selectedURI.toString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if (decodeUriStr.contains("/video")) { //video
                            //step1: get image
                            thumbnailImageFromURL(selectedURI);
                            //step2: get video
                            File toSaveVideoFile = UIHelper.saveVideoToCaches(AppContext.getAppContext(), selectedURI);

                            if (mIsImage2Active) {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveVideoFile);
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveVideoFile);
                                }
                            } else {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveVideoFile);
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveVideoFile);
                                }
                            }

                            if (mIsCreatingCard == false) {
                                mCurrentCard.save(AppContext.getAppContext());
                                if (mIsQuestionShowing) {
                                    takeSnapshotCurrentCard();
                                    Intent intent = new Intent();
                                    intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                                    intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
                                    getActivity().sendBroadcast(intent);
                                }
                            }

                        } else if (StringUtils.isEmpty(decodeUriStr) == false) {   //images
                            if (requestCodeFinal == REQUEST_CODE_FROM_LOGO) {
                                Bitmap scaledBitmap = UIHelper.bitmapFromUri(getActivity(), selectedURI, 100);

                                if (scaledBitmap == null) {
                                    LOGE(TAG, "handleCrop, scaledBitmap = null");
                                    return;
                                }

                                File toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);

                                mLogoImage.setImageBitmap(scaledBitmap);
                                mCurrentPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                                if (mIsCreatingCard == false) {
                                    mCurrentPack.save(AppContext.getAppContext());
                                    ((MainActivity) getActivity()).setMaskButtonForContentUpdating();
                                    takeSnapshotAll();
                                }

                            } else if (requestCodeFinal == REQUEST_CODE_FROM_IMAGE) {

                                Bitmap scaledBitmap = UIHelper.bitmapFromUri(getActivity(), selectedURI, 400);

                                if (scaledBitmap == null) {
                                    LOGE(TAG, "handleCrop, scaledBitmap = null");
                                    return;
                                }

                                File toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);

                                //TODO:如果有视频，则要删除掉

                                if (mIsImage2Active) {
                                    mImage2.setImageBitmap(scaledBitmap);
                                    if (mIsQuestionShowing) {
                                        mCurrentCard.question.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                                        mCurrentCard.question.movieUriFormatStr2 = "";
                                    } else {
                                        mCurrentCard.answer.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                                        mCurrentCard.answer.movieUriFormatStr2 = "";
                                    }
                                } else {
                                    mImage.setImageBitmap(scaledBitmap);
                                    if (mIsQuestionShowing) {
                                        mCurrentCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                                        mCurrentCard.question.movieUriFormatStr = "";
                                    } else {
                                        mCurrentCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                                        mCurrentCard.answer.movieUriFormatStr = "";
                                    }
                                }

                                if (mIsCreatingCard == false) {
                                    mCurrentCard.save(AppContext.getAppContext());
                                    if (mIsQuestionShowing) {
                                        takeSnapshotCurrentCard();
                                        Intent intent = new Intent();
                                        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                                        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
                                        getActivity().sendBroadcast(intent);
                                    }
                                }
                            }

                        } else {
                            new SweetAlertDialog(AppContext.getAppContext(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.DIALOG_AlERT))
                                    .setContentText(getString(R.string.DIALOG_UNSUPPORTED_IMAGE_SOURCE))
                                    .show();
                        }
                    }

                }
            }
        }, delay);




    }

}



