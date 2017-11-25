package com.flipflash.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.flipflash.UI.FCCEditText;
import com.flipflash.UI.MultimediaView.FFCMultimediaType;
import com.flipflash.UI.MultimediaView.MultimediaView;
import com.flipflash.UI.MultimediaView.OnFrescoImageViewLoadCompletionListener;
import com.flipflash.UI.RoundedBottomRightImageView;
import com.flipflash.UI.ScaleHelper;
import com.flipflash.android_ffc.CropActivity;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.PlayActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.android_ffc.WebViewActivity;
import com.flipflash.data.Answer;
import com.flipflash.data.CSS;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.data.Question;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.helper.SymbolHelper;

import com.flipflash.helper.Text2SpeechHelper;
import com.flipflash.model.LockObject;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.FontHelper;
import com.flipflash.util.Global;
import com.flipflash.util.MutipleTargetHelper;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;
import com.flipflash.util.TipHelper;
import com.flipflash.util.UIHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.squareup.leakcanary.RefWatcher;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import cn.pedant.SweetAlert.SweetAlertDialog;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;



public class CardDetailFragment extends Fragment implements FCCEditText.OnTouchListener {
    private static final String TAG = CardDetailFragment.class.getSimpleName();

    public Card mCurrentCard;
    public Pack mCurrentPack;

    public View mContentView;

    public MultimediaView mImage2;
    public MultimediaView mImage;

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

    private Button      mPreviewButton;
    private Button      mSaveButton;

    private ScrollView  mVerticalScrollView;

    private RoundedBottomRightImageView mBackgroundImageView;

    private InputMethodManager mIMM;

    public EditText mCurrentFocusedCardContentText;  // only applicable to subheading, main and sub text

    public boolean mIsCreatingCard = false;
    private boolean mIsPlayingCard = false;

    /*
     * true:表明正在进行snapshot
     * we have different strategy on current showing card and other cards
     */
    private boolean mIsSnapShotNotCurrent = false;
    public boolean mIsQuestionShowing = true;


    /*
     * 用于标识是否需要snapshot all。如果是当前fragment是发起方，切需要snapshot all，则当前fragment的这个值= true，其它fragment则为false
     */
    private boolean mIsTakeSnapshotAllNeeded = false;

    /*
     * -1,表示永远不需要disable snapshot all功能，否则用于同步
     * 需要设置成-1当：save已经存在的开片;save一个新创建的卡片，但是不需要snapshot all
     */
    private static int mSnapshotAllCardsSemaphore = 0; //used to indicate all snapshots are done

    private boolean mIsImage2Active = false; //我们有两个image(image和image2),这个变量用于区分

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

    private LinearLayout mFunctionalAreaLinearLayout;

    public final static  String TAG_SUBHEADING          = "1001";
    public final static  String TAG_MAIN                = "1002";
    public final static  String TAG_SUB                 = "1003";

    public final static  String TAG_IMAGE                = "2002";
    public final static  String TAG_IMAGE2                 = "2003";

    public final static  String TAG_TITLE                 = "4001";
    public final static  String TAG_CREATOR               = "4002";
    public final static  String TAG_JOB_TITLE             = "4003";
    public final static  String TAG_SIDE_BAR_TITLE        =  "4004";

    /*
     * triggerResizeTextToFitFrame中有诸多的限制条件，比如仅允许non-editable条件下。
     * 这个标志位主要是为了不必需要的执行，而仅允许执行updateQuestionCSS(updateAnswerCSS)中setTextSize的执行后才执行
     * 这个标志位一旦true，则不再false,因为我们的triggerResizeTextToFitFrame中仅仅在non-editabble中执行
     */
    private boolean      mAllowToTriggerResizeTextToFitFrame = false;
    private DisplayImageOptions mDisplayImageOptions;

    /*
     * triggerResizeTextToFitFrame永远只是在卡片不可编辑上进行
     */
    private Timer mResizeMonitorTimer;


    /*
     * Background
     * To show a gif could take second. Before screenshoting, we have to wait for this to finish
     */
    private LockObject mLockForScreenshotGif = new LockObject();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LOGD(TAG, "onCreateView");


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

        if (MutipleTargetHelper.isFullVersion() == false) {
            if (mRadioGroup != null) {
                mRadioGroup.setVisibility(View.INVISIBLE);
            }

            if (mFunctionalAreaLinearLayout != null) {
                mFunctionalAreaLinearLayout.setVisibility(View.INVISIBLE);
            }

            if (mSaveButton != null) {
                mSaveButton.setVisibility(View.INVISIBLE);
            }

            if (mPreviewButton != null) {
                mPreviewButton.setVisibility(View.INVISIBLE);
            }

        }

        return mContentView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // LOGD(TAG, "onViewCreated: cardSN=" + mCurrentCard.cardSN);

        if (mCurrentPack == null) {
            return;
        }

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


        if (mIsCreatingCard) {
            mTitle.setEnabled(false); //same as iOS


            if (mSaveButton != null) {
                mSaveButton.setVisibility(View.INVISIBLE);
            }

            if (mPreviewButton != null) {
                mPreviewButton.setVisibility(View.INVISIBLE);
            }
        }

        if (mIsPlayingCard || mIsCreatingCard) {
            setCardBackgroundMaskBlack();
        } else {
            setCardBackgroundMaskGray();
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
    public void onStop() {
        super.onStop();

        LOGD(TAG, "onStop: " + String.format("cardSN = %d", mCurrentCard.cardSN));

    }


    /*
     * 与onPause匹配使用
     */
    @Override
    public void onResume() {
        super.onResume();
        LOGD(TAG, "onResume:");

        //need to be put onResume, see http://stackoverflow.com/questions/13721063/aftertextchanged-being-called-without-the-text-being-actually-changed
        setTextsListener();

        mContentBodyLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(mContentBodyListener);

        final View rootView = getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(mKeyboardVisibilityListener);

    }


    @Override
    public void onPause() {
        super.onPause();

        stopEmbeddedVideoAndGif();

        LOGD(TAG, "onPause:");

        removeEditTextListener();

        if (Build.VERSION.SDK_INT < 16) {
            mContentBodyLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(mContentBodyListener);
        } else {
            mContentBodyLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(mContentBodyListener);
        }

        //会在switchToAnswerView和switchToQuestionViewWithOption重置，所以不需要在onResume进行设置
        if (mResizeMonitorTimer != null) {
            mResizeMonitorTimer.cancel();
        }

        final View rootView = getRootView();
        if (Build.VERSION.SDK_INT < 16) {
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(mKeyboardVisibilityListener);
        } else {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(mKeyboardVisibilityListener);
        }

    }


    private View getRootView() {
        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        return rootView;
    }

    private boolean isAllowShowTransparentFullScreenView() {
        if (mIsPlayingCard == false) {
            return false;
        }

        boolean result = AppConfig.sharedInstance().isFunctionPromptOff();

        return (result == false);
    }


    public void showFingerAnimationGifImageView() {

        SimpleDraweeView gifImageView = (SimpleDraweeView) mContentView.findViewById(R.id.transparent_finger_animation_gif);

        if (isAllowShowTransparentFullScreenView() == false) {
            return;
        }

        Uri uri;
        if (mIsQuestionShowing) {
            uri = Uri.parse("res:///" + R.drawable.question_gif);

        } else {
            uri = Uri.parse("res:///" + R.drawable.answer_gif);
        }

        gifImageView.setVisibility(View.VISIBLE);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(false)
                .setControllerListener(new ControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {

                    }

                    @Override
                    public void onFinalImageSet(String id, @javax.annotation.Nullable ImageInfo imageInfo, @javax.annotation.Nullable Animatable animatable) {

                        if (animatable != null) {
                            try {
                                Field field = AnimatedDrawable.class.getDeclaredField("mTotalLoops");
                                field.setAccessible(true);
                                field.set(animatable, 1);
                            } catch (Exception e) {
                                //e.printStackTrace();
                            }
                            animatable.start();
                        }

                    }

                    @Override
                    public void onIntermediateImageSet(String id, @javax.annotation.Nullable ImageInfo imageInfo) {

                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {

                    }

                    @Override
                    public void onRelease(String id) {

                    }
                })
                .build();
        gifImageView.setController(controller);


    }

    public void hideFingerAnimationGifImageView() {

        SimpleDraweeView gifImageView = (SimpleDraweeView) mContentView.findViewById(R.id.transparent_finger_animation_gif);

        if (gifImageView.getController() != null) {
            Animatable animatable = gifImageView.getController().getAnimatable();
            if (animatable != null) {
                animatable.stop();
            }
        }

        gifImageView.setController(null);

        if (gifImageView.getVisibility() != View.GONE) {
            gifImageView.setVisibility(View.GONE);
        }
    }


    /*
     * 这里所谓的content view就是subheading, main, sub的parent view。
     * 由于需要避免看到resizing的过程，我们的策略是先隐藏，然后resizing结束后显示
     */
    private void setContentViewVisibility() {

        if (mIsCreatingCard || isEditableMode()) {
            mContentBodyLinearLayout.setVisibility(View.VISIBLE); //默认是隐藏的
        } else {
            mContentBodyLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    /*
     * 监视resizing过程是否已经完成，如果已经完成，则显示内容
     * 与setContentViewVisibility配合使用。
     */
    private void resetResizingMonitorTimer() {

        if (isEditableMode() == false) {

            if (mSubheading.getText().toString().length() == 0) {
                flag_Subheading_ResizeFinished = true;
            } else {
                flag_Subheading_ResizeFinished = false;
            }
            if (mMain.getText().toString().length() == 0) {
                flag_Main_ResizeFinished = true;
            } else {
                flag_Main_ResizeFinished = false;
            }
            if (mSub.getText().toString().length() == 0) {
                flag_Sub_ResizeFinished = true;
            } else {
                flag_Sub_ResizeFinished = false;
            }

            if (mResizeMonitorTimer != null) {
                mResizeMonitorTimer.cancel();
                mResizeMonitorTimer = null;
            }
            mResizeMonitorTimer = new Timer();
            mResizeMonitorTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (flag_Subheading_ResizeFinished && flag_Main_ResizeFinished && flag_Sub_ResizeFinished) {
                        LOGD(TAG, "onResume: Set content visible after resizing is finished. cardSN: " + mCardSN.getText());

                        if (getActivity() != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContentBodyLinearLayout.setVisibility(View.VISIBLE);
                                }
                            });

                            mResizeMonitorTimer.cancel();
                        }
                    }

                    //LOGD(TAG, "run: " + flag_Subheading_ResizeFinished + " " + flag_Main_ResizeFinished + "  " + flag_Sub_ResizeFinished);
                }
            }, 0, 50);
        }
    }




    /*
     * 此部分的逻辑用于takeSnapshotAll(): 当一个新的fragment生成后，自动进行screenshot
     * 由于我们是动态布局（通过weight)，而ScrollView（见card.xml）中要求内容是确定的高度，而不是match_parent
     */
    private ViewTreeObserver.OnGlobalLayoutListener mContentBodyListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            LOGD(TAG, "mContentBodyListener onGlobalLayout with sn:" + mCardSN.getText());

            if (Build.VERSION.SDK_INT < 16) {
                mContentBodyLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                mContentBodyLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }

            //动态设置高度
            float marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10 + 10, getResources().getDisplayMetrics());
            //不能用getActivity.findViewById，因为有两个card_content_body_fr_with_background_image
            View contentBodyBackground =  mContentView.findViewById(R.id.card_content_body_fr_with_background_image);
            mContentBodyLinearLayout.getLayoutParams().height = contentBodyBackground.getHeight() - (int)marginPx;  //在ScrollView中，高度必须是一个固定值
            if (isEditableMode() == false  || mIsPlayingCard) {
                //不能scroll
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentBodyLinearLayout.getLayoutParams();
                params.bottomMargin = 0;
                mContentBodyLinearLayout.setLayoutParams(params);
                mVerticalScrollView.setEnabled(false);
            }
            mContentBodyLinearLayout.requestLayout();

            contentBodyBackground.setVisibility(View.VISIBLE);

            if (mIsSnapShotNotCurrent) {
                mIsSnapShotNotCurrent = false;

                // 460ms 在onGlobalLayout调用后，需要一段时间完成onDraw方法（比如setText), 460是个经验值
                Task.delay(460).continueWith(new Continuation<Void, String>() {
                    @Override
                    public String then(Task<Void> task) throws Exception {
                        takeSnapshotCurrentCard();
                        return null;
                    }
                },Task.UI_THREAD_EXECUTOR);
            }

        }
    };


    boolean mKeyboardDidDismissFlag = true;
    boolean mKeyboardDidShowFlag = false;
    private ViewTreeObserver.OnGlobalLayoutListener mKeyboardVisibilityListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            final View rootView = getRootView();

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

                LOGD(TAG, "keyboard height is: " + heightDiff/dm.density);

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

        //((MainActivity) getActivity()).removeCSSToolbar();

    }

    private void keyboardDidShowNotification() {

        LOGD(TAG, "keyboardDidShowNotification");

    }


    @Override
    public void onDestroy() {
        LOGD(TAG, "onDestroy: " + String.format("cardSN = %d", mCurrentCard.cardSN));

        mLogoImage.setImageURI(null);


//        mSubheadingTextWatcher = null;
//        mMainTextWatcher = null;
//        mSubTextWatcher = null;
//        mCreatorTextWatcher = null;
//        mTitleTextWatcher = null;
//        mJobTitleTextWatcher = null;
//        mSidebarTitleTextWatcher = null;


        super.onDestroy();

//        RefWatcher refWatcher = AppContext.getRefWatcher(getActivity());
//        refWatcher.watch(this);


    }


    private void playYoutubeVideo() {

        String gifStr = "";
        String videoStr = "";

        if (mIsQuestionShowing) {
            if (mIsImage2Active) {
                if (mCurrentCard.question.movieUriFormatStr2.length() > 0) {
                    videoStr = mCurrentCard.question.movieUriFormatStr2;
                }
                gifStr= mCurrentCard.question.imageUriFormatStr2;
            } else {
                if (mCurrentCard.question.movieUriFormatStr.length() > 0) {
                    videoStr = mCurrentCard.question.movieUriFormatStr;
                }
                gifStr= mCurrentCard.question.imageUriFormatStr;
            }
        } else {
            if (mIsImage2Active) {
                if (mCurrentCard.answer.movieUriFormatStr2.length() > 0) {
                    videoStr = mCurrentCard.answer.movieUriFormatStr2;

                }
                gifStr= mCurrentCard.answer.imageUriFormatStr2;
            } else {
                if (mCurrentCard.answer.movieUriFormatStr.length() > 0) {
                    videoStr = mCurrentCard.answer.movieUriFormatStr;

                }
                gifStr= mCurrentCard.answer.imageUriFormatStr;
            }
        }

        if (videoStr.length() > 0) {

            if (videoStr.contains("http://") || videoStr.contains("https://")) {

                YoutubeFragment dialogFragment = new YoutubeFragment();
                dialogFragment.setYoutubeLink(videoStr);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "youtube_fragment");

            }

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

                            thumbnailImageFromVideoURL(Uri.parse(youtubeURLStr));

                            if (mIsImage2Active) {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr2 = youtubeURLStr;
                                    mImage2.setStaticImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr2));
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr2 = youtubeURLStr;
                                    mImage2.setStaticImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr2));
                                }


                            } else {
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr = youtubeURLStr;
                                    mImage.setStaticImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr = youtubeURLStr;
                                    mImage.setStaticImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));
                                }
                            }




                            if (!mIsCreatingCard) {
                                mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card

                                Task.delay(460).continueWith(new Continuation<Void, String>() {
                                    @Override
                                    public String then(Task<Void> task) throws Exception {
                                        takeSnapshotCurrentCard();
                                        return null;
                                    }
                                },Task.UI_THREAD_EXECUTOR);

//                                mCurrentCard.save(AppContext.getAppContext());
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
            MediaOptions.Builder builder = new MediaOptions.Builder();
            MediaOptions options = builder.canSelectBothPhotoVideo()
                    .canSelectMultiPhoto(false).canSelectMultiVideo(false)
                    .build();
            if (options != null) {
                MediaPickerActivity.open(CardDetailFragment.this, Global.REQUEST_CODE_FROM_IMAGE, options);
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

        if (requestCode == Global.REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED &&
                resultCode == Activity.RESULT_OK) {

            Uri selectedURI = data.getParcelableExtra("cropped_image_uri");

            ImageSize targetSize = new ImageSize(1024, 1024); //但是最终是一个小于这个大小的图片（因为最终需要防止图片变形）
            ImageLoader imageLoader = ImageLoader.getInstance();
            Bitmap scaledBitmap = imageLoader.loadImageSync(selectedURI.toString(),targetSize);

            if (scaledBitmap == null) {
                LOGE(TAG, "handleCrop, scaledBitmap = null");
                return;
            }

            File toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);
            mBackgroundImageView.setImageBitmap(scaledBitmap);
            if (mIsQuestionShowing) {
                mCurrentCard.question.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
            } else {
                mCurrentCard.answer.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
            }

            if (mIsCreatingCard == false) {
                mCurrentCard.save(AppContext.getAppContext());
                if (mIsQuestionShowing) {
                    mSnapshotAllCardsSemaphore = -1; //we only need to screenshot current card
                    takeSnapshotCurrentCard();
                }
            }

            PackRecordHelper.savePackUpdateRecord(mCurrentPack);
        }

    }

    /*
    通过uri，获取video的thumbnail
     */
    private void thumbnailImageFromVideoURL(Uri selectedURI) {

        Bitmap resultBitmap = UIHelper.getVideoThumbnail(AppContext.getAppContext(), selectedURI);

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
                .setTitle(R.string.DIALOG_IMAGE_VIDEO_SELECTION)
                .setMessage(R.string.Title_Image_Copyright)
                .setNegativeButton(R.string.DIALOG_INSERT_YOUTUBE_URL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showYoutubeLinkageInputDialog();
                    }
                })
                .setNeutralButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectImageOrVideoFromLibrary();
                    }
                })
                .setPositiveButton(R.string.DIALOG_REMOVE_VIDEO_IMAGE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteImageAndVideo();

                        if (mIsImage2Active) {
                            String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();

                            mImage2.setStaticImageURI(Uri.parse(placeholderImagePath));

                            if (mIsQuestionShowing) {
                                mCurrentCard.question.imageUriFormatStr2 = "";
                            } else {
                                mCurrentCard.answer.imageUriFormatStr2 = "";
                            }
                        } else {
                            String placeholderImagePath = FileOperationHelper.getAnswerImagePlaceholderImagePath();
                            mImage.setStaticImageURI(Uri.parse(placeholderImagePath));

                            if (mIsQuestionShowing) {
                                mCurrentCard.question.imageUriFormatStr = "";
                            } else {
                                mCurrentCard.answer.imageUriFormatStr = "";
                            }
                        }

                        if (mIsCreatingCard == false) {
                            mCurrentCard.save(AppContext.getAppContext());
                            if (mIsQuestionShowing) {
                                mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card
                                takeSnapshotCurrentCard();
                            }
                        }
                    }
                })
                .show();
    }

    private void deleteImageAndVideo() {

        String targetImage;
        String targetVideo;
        if (mIsImage2Active) {
            if (mIsQuestionShowing) {
                targetVideo = mCurrentCard.question.movieUriFormatStr2;
                targetImage = mCurrentCard.question.imageUriFormatStr2;
            } else {
                targetVideo = mCurrentCard.answer.movieUriFormatStr2;
                targetImage = mCurrentCard.answer.imageUriFormatStr2;
            }
        } else {
            if (mIsQuestionShowing) {
                targetVideo = mCurrentCard.question.movieUriFormatStr;
                targetImage = mCurrentCard.question.imageUriFormatStr;
            } else {
                targetVideo = mCurrentCard.answer.movieUriFormatStr;
                targetImage = mCurrentCard.answer.imageUriFormatStr;
            }
        }

        boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(targetImage);
        if (success == false) {
            LOGE(TAG, "failure to delete: " + targetImage);
        }

        success = FileOperationHelper.deleteFileExceptPlaceHolder(targetVideo);
        if (success == false) {
            LOGE(TAG, "failure to delete: " + targetVideo);
        }
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

                        if (StringUtils.isEmptyOrPlaceHolder(mCurrentPack.logoImageUriFormatStr)) {

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.DIALOG_LOGO_IMAGE_SELECTION)
                                    .setMessage(R.string.Title_Image_Copyright)
                                    .setNegativeButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            MediaOptions options = MediaOptions.createDefault();
                                            if (options != null) {
                                                MediaPickerActivity.open(CardDetailFragment.this, Global.REQUEST_CODE_FROM_LOGO, options);
                                            }

                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.DIALOG_LOGO_IMAGE_SELECTION)
                                    .setMessage(R.string.Title_Image_Copyright)
                                    .setNegativeButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            MediaOptions options = MediaOptions.createDefault();
                                            if (options != null) {
                                                MediaPickerActivity.open(CardDetailFragment.this, Global.REQUEST_CODE_FROM_LOGO, options);
                                            }

                                        }
                                    })
                                    .setPositiveButton(R.string.DIALOG_REMOVE_LOGO_IMAGE, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            String placeholderImagePath = FileOperationHelper.getLogoPlaceholderImagePath();
                                            ImageLoader imageLoader = ImageLoader.getInstance();
                                            imageLoader.displayImage(placeholderImagePath, mLogoImage,mDisplayImageOptions);
                                            mCurrentPack.logoImageUriFormatStr = "";

                                            //step5:save logic if not creating a new card
                                            if (mIsCreatingCard) {
                                                //we will do that when we click the save button
                                                mIsTakeSnapshotAllNeeded = true;
                                            } else {

                                                boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(mCurrentPack.logoImageUriFormatStr);
                                                if (success == false) {
                                                    LOGE(TAG, "failure to delete: " + mCurrentPack.logoImageUriFormatStr);
                                                }

                                                mCurrentPack.save(AppContext.getAppContext());
                                                takeSnapshotAll();
                                            }

                                        }
                                    })
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
                    new SweetAlertDialog(getActivity())
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
                                .setTitle(R.string.DIALOG_BACKGROUND_IMAGE_SELECTION)
                                .setMessage(R.string.Title_Image_Copyright)
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

                                        if (mIsCreatingCard == false) {
                                            mCurrentCard.save(AppContext.getAppContext());
                                            if (mIsQuestionShowing) {
                                                mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card
                                                takeSnapshotCurrentCard();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.Optional_Change_Background_Image, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MediaOptions options = MediaOptions.createDefault();;
                                        if (options != null) {
                                            MediaPickerActivity.open(CardDetailFragment.this, Global.REQUEST_CODE_FROM_BACKGROUND, options);
                                        }
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.DIALOG_BACKGROUND_IMAGE_SELECTION)
                                .setMessage(R.string.Title_Image_Copyright)
                                .setPositiveButton(R.string.DIALOG_SELECT_FROM_LIBRARY, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MediaOptions options = MediaOptions.createDefault();;
                                        if (options != null) {
                                            MediaPickerActivity.open(CardDetailFragment.this, Global.REQUEST_CODE_FROM_BACKGROUND, options);
                                        }
                                    }
                                })
                                .show();
                    }

                } else {
                    new SweetAlertDialog(getActivity())
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
                        playYoutubeVideo();

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
                        playYoutubeVideo();

                    }
                });

            } else {
                //不在play mode下，但是同时又不是自己创建的卡
                mImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsImage2Active = true;
                        new SweetAlertDialog(getActivity())
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

        if (mChangeTemplateImage == null) {
            throw  new IllegalStateException("mChangeTemplateImage should not be null in configureChangeTemplateView");
        }

        if (isEditableMode() == false) {

            mChangeTemplateImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SweetAlertDialog(getActivity())
                            .setTitleText(getString(R.string.DIALOG_AlERT))
                            .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();

                }
            });

        } else {

            mChangeTemplateImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsQuestionShowing) {
                        showQuestionAction();
                    } else {
                        showAnswerAction();
                    }

                }
            });
        }


    }

    private void showAnswerAction() {

        QuickAction answerQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);

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
        ActionItem answerActionItem17 = new ActionItem(7, null, getResources().getDrawable(R.drawable.answer_templatescreenshot17));

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
        answerQuickAction.addActionItem(answerActionItem17);

        answerQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateActionItemClicked(pos);
            }
        });

        answerQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });

        answerQuickAction.show(mChangeTemplateImage);
    }

    private void showQuestionAction() {

        QuickAction questionQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);

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
        ActionItem questionActionItem17 = new ActionItem(7, null, getResources().getDrawable(R.drawable.question_templatescreenshot17));


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
        questionQuickAction.addActionItem(questionActionItem17);

        questionQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateActionItemClicked(pos);
            }
        });
        questionQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });


        questionQuickAction.show(mChangeTemplateImage);
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

            Task.delay(10).continueWith(new Continuation<Void, String>() {
                @Override
                public String then(Task<Void> task) throws Exception {
                    saveEditedCard();
                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);

        }


    }

    /*
     * Used only in play mode
     */
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

        LOGD(TAG, "switchToQuestionViewWithOption");

        if (mContentBodyLinearLayout == null) {
            return;
        }

        if (getActivity() == null  || isAdded() == false) {
            return;
        }

        setContentViewVisibility();

        mIsQuestionShowing = true;
        if (!ignoreResetTitleContent) {
            mTitle.setText(mCurrentPack.questionTitle);
        }

        //set title color
        int colorResourceID[] = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground));
        mTitle.setTextColor(colorResourceID[4]);

        updateQuestionViewTemplate();//updateQuestionContent，因为涉及到view的重定向
        updateQuestionContent();
        updateQuestionCSS();

        resetResizingMonitorTimer();//必须放到updateAnswerCSS后，因为在play mode中，会在updateAnswerCSS中额外的setTextSize

        updateSemiTransparentPolicy();

        //hide placeholder image if play mode
        if (mIsPlayingCard) {
            if (StringUtils.isEmptyOrPlaceHolder(mCurrentCard.question.imageUriFormatStr)) {
                mImage.setVisibility(View.INVISIBLE);
            }

            if (StringUtils.isEmptyOrPlaceHolder(mCurrentCard.question.imageUriFormatStr2)) {
                mImage2.setVisibility(View.INVISIBLE);
            }
        }


    }


    private void updateSemiTransparentPolicy() {

        float ALPHA_VALUE;

        if (mIsPlayingCard) {
            ALPHA_VALUE = 0;
        } else {
            ALPHA_VALUE = 0.5f;
        }

        if (mIsQuestionShowing) {

            if (mCurrentCard.question.css.subheadingSemiTransparent) {
                mSubheading.setAlpha(ALPHA_VALUE);
            } else {
                mSubheading.setAlpha(1);
            }

            if (mCurrentCard.question.css.mainSemiTransparent) {
                mMain.setAlpha(ALPHA_VALUE);
            } else {
                mMain.setAlpha(1);
            }

            if (mCurrentCard.question.css.subSemiTransparent) {
                mSub.setAlpha(ALPHA_VALUE);
            } else {
                mSub.setAlpha(1);
            }

        } else {

            if (mCurrentCard.answer.css.subheadingSemiTransparent) {
                mSubheading.setAlpha(ALPHA_VALUE);
            } else {
                mSubheading.setAlpha(1);
            }

            if (mCurrentCard.answer.css.mainSemiTransparent) {
                mMain.setAlpha(ALPHA_VALUE);
            } else {
                mMain.setAlpha(1);
            }

            if (mCurrentCard.answer.css.subSemiTransparent) {
                mSub.setAlpha(ALPHA_VALUE);
            } else {
                mSub.setAlpha(1);
            }
        }




    }


    /**
     * @param ignoreResetTitleContent will trigger takeSnapAll function is it is set
     */
    private void switchToAnswerView(boolean ignoreResetTitleContent) {

        LOGD(TAG, "switchToAnswerView:");

        setContentViewVisibility();

        mIsQuestionShowing = false;
        if (!ignoreResetTitleContent) {
            mTitle.setText(mCurrentPack.answerTitle);
        }

        //set title color
        int colorResourceID[] = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground));
        mTitle.setTextColor(colorResourceID[5]);

        updateAnswerViewTemplate(); //必须放在updateAnswerContent，因为涉及到view的重定向
        updateAnswerContent();
        updateAnswerCSS();

        resetResizingMonitorTimer(); //必须放到updateAnswerCSS后，因为在play mode中，会在updateAnswerCSS中额外的setTextSize

        updateSemiTransparentPolicy();

        //hide placeholder image if play mode
        if (mIsPlayingCard) {
            if (StringUtils.isEmptyOrPlaceHolder(mCurrentCard.answer.imageUriFormatStr)) {
                mImage.setVisibility(View.INVISIBLE);
            }

            if (StringUtils.isEmpty(mCurrentCard.answer.imageUriFormatStr2)) {
                mImage2.setVisibility(View.INVISIBLE);
            }
        }
    }

    private ViewTreeObserver.OnScrollChangedListener mOnVerticalScrollViewChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            LOGD(TAG,"mVerticalScrollView onScrollChanged");

            if (mVerticalScrollView.getScrollY() != 0) {

                stopEmbeddedVideoAndGif();
            }
        }
    };


    /**
     * 用于inflate后
     */
    private void getAllViews() {

        if (mDisplayImageOptions == null) {
            mDisplayImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.stub_image_for_universal_image_loader)
                    .build();
        }

        mSidebarTitle = (FCCEditText) mContentView.findViewById(R.id.sidebar_title);
        mSidebarTitle.setTag(TAG_SIDE_BAR_TITLE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSidebarTitle.getLayoutParams();
        double cardHeightDPUnit = UIHelper.getCardHeightDPUnit(getActivity());
        double cardSNHeightPXUnit = getResources().getDimensionPixelSize(R.dimen.cardsn_size) +  getResources().getDimensionPixelSize(R.dimen.cardsn_top_margin);
        params.width = UIHelper.getPixels((int)cardHeightDPUnit - 10) - (int)cardSNHeightPXUnit * 2; //10 is the margin between sn and mSidebarTitle
        mSidebarTitle.setLayoutParams(params);

        mSidebarBackground = (FrameLayout) mContentView.findViewById(R.id.sidebar_background_linearlayout);
        mCardSN = (TextView) mContentView.findViewById(R.id.card_sn);

        mPreviewButton = (Button) mContentView.findViewById(R.id.preview_button);
        if (mPreviewButton != null) {
            mPreviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    previewButtonClicked();
                }
            });
        }
        mSaveButton = (Button) mContentView.findViewById(R.id.save_button);
        if (mSaveButton != null) {
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveButtonClicked();
                }
            });
        }


        mVerticalScrollView = (ScrollView) mContentView.findViewById(R.id.card_vertical_scrollview);
        mVerticalScrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnVerticalScrollViewChangedListener);
        mVerticalScrollView.getViewTreeObserver().addOnScrollChangedListener(mOnVerticalScrollViewChangedListener);


        mTitle = (FCCEditText) mContentView.findViewById(R.id.title);
        mTitle.setTag(TAG_TITLE);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (FCCEditText) mContentView.findViewById(R.id.creator);
        mCreator.setTag(TAG_CREATOR);
        mJobTitle = (FCCEditText) mContentView.findViewById(R.id.job_title);
        mJobTitle.setTag(TAG_JOB_TITLE);

        LinearLayout creatorLayout = (LinearLayout) mContentView.findViewById(R.id.creator_layout);

        mContentBodyLinearLayout = (LinearLayout) mContentView.findViewById(R.id.card_content_body);
        if (isEditableMode() || mIsCreatingCard) {
            //在非edit模式下，我们是在resize结束后才显示的
            mContentBodyLinearLayout.setVisibility(View.VISIBLE);
        }

        mBackgroundImageView = (RoundedBottomRightImageView) mContentView.findViewById(R.id.card_background_image);

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

            mFunctionalAreaLinearLayout = (LinearLayout) mContentView.findViewById(R.id.function_area);

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

    public void stopEmbeddedVideoAndGif() {

        mImage.stopVideo();
        mImage.stopGif();

        mImage2.stopVideo();
        mImage2.stopGif();
    }

    private void saveButtonClicked() {

        if (isEditableMode() == false) {

            new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.SAVE_NOT_AVAILABLE_THAT_IS_NOT_YOU))
                    .show();

        } else {

            saveEditedCard();

        }




    }

    private void previewButtonClicked() {

        Card previewCard = copyCurrentUnsavedCardForPreview();

        Pack previewPack = new Pack();
        ArrayList cards = new ArrayList<>();
        cards.add(previewCard);
        previewPack.cards = cards;

        ((MainActivity) getActivity()).mIsAllowedToShowPackList = false;

        {
            //currently, we only need info of creator, but for future potential benefit, we try to copy everything except packID info, shareLink, and fileNameOnAWS
            previewPack.packName = mCurrentPack.packName;
            previewPack.sidebarTitle = mCurrentPack.sidebarTitle;
            previewPack.coverImageUriFormatStr = mCurrentPack.coverImageUriFormatStr;
            previewPack.userID = mCurrentPack.userID;
//            previewPack.languageName = mCurrentPack.languageName;
            previewPack.creatorID = mCurrentPack.creatorID; //check later, please
            previewPack.creatorNickName = mCurrentPack.creatorNickName;
            previewPack.creatorNickName = mCurrentPack.creatorNickName;
            previewPack.lastVistDate = mCurrentPack.lastVistDate;
            previewPack.createDate = mCurrentPack.createDate;
            previewPack.restorePassword = mCurrentPack.restorePassword;
//            screenshotPack.isAllowShare = mCurrentPack.isAllowShare;
            previewPack.autoPlaySpeed = mCurrentPack.autoPlaySpeed;
            previewPack.platform = mCurrentPack.platform;

            previewPack.shareLink = "";
            previewPack.fileNameOnAWS = "";
            previewPack.packID = Global.generateNoRepeatInt();
        }

        Global.previewPack = previewPack;



        //play
        Intent intentPlay = new Intent(getActivity(), PlayActivity.class);
        intentPlay.putExtra("packID", previewPack.packID);
        intentPlay.putExtra("previewOnly", true);
        intentPlay.putExtra("oneOffPlayType", 0);  //only manually is supported
        startActivity(intentPlay);


    }

    private Card copyCurrentUnsavedCardForPreview() {

        Card card = mCurrentCard.deepCopy();

        return card;


    }

    public void resetVerticalScrollViewBottomMargin() {
        LOGD(TAG, "resetVerticalScrollViewBottomMargin");
        if (mVerticalScrollView.getScrollY() != 0) {
            mVerticalScrollView.setScrollY(0);
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
        setTextsListener();

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

        mImage = new MultimediaView(getActivity());
        mImage.setTag(TAG_IMAGE);

        mImage.setPadding(5,5,5,5);


//        mImage.setBackgroundColor(Color.RED);

        if (isEditableMode() && (mIsPlayingCard == false)) {
            mImage.setBackgroundResource(R.drawable.shape_imageview_editable);
        }


    }

    private void createImage2() {
        LOGD(TAG, "createImage2");

        mImage2 = new MultimediaView(getActivity());
        mImage2.setTag(TAG_IMAGE2);

        mImage2.setPadding(5, 5, 5, 5);


        if (isEditableMode() && (mIsPlayingCard == false)) {
            mImage2.setBackgroundResource(R.drawable.shape_imageview_editable);
        }
    }


    /*
     * 最初想法这是一个one off的标志。当字体太小时，我们给予一个足够大的字体，这时可以认为这样只需要做一次。
     * 但是实际中发现，这样会导致字体太大（同时文字又在合理的frame内），所以最后的做法是，这不再是一个一次性的标志，而是用来标志：字体增大的动作是否已经完成。
     * 返回true: 当字体增加的动作已经彻底完成，或压根不需要做这个动作
     */
    private boolean flag_Subheading_OneoffIncrease;
    private boolean flag_Main_OneoffIncrease;
    private boolean flag_Sub_OneoffIncrease;

    /*
     *用来判断resize是否完成，如果因为text空，则设置成true。需要以下三个都是true，才算最终完成
     */
    private boolean flag_Subheading_ResizeFinished;
    private boolean flag_Main_ResizeFinished;
    private boolean flag_Sub_ResizeFinished;

    /**仅在non-editable条件下工作
     * 为了提高执行效率，仅仅当setTextSize自动引起的ViewTreeObserver.OnGlobalLayoutListener下触发（OnGlobalLayoutListener的触发可以通过setText或setTextSize)。
     */
    private void triggerResizeTextToFitFrame(final EditText v, int targetLines) {

        if (Global.checkLineNumberWhenResizeTextToFitFrame == false) {
            targetLines = -1;
        }

        //debug code:
//        if (true) {
//            flag_Subheading_ResizeFinished = true;
//            flag_Main_ResizeFinished = true;
//            flag_Sub_ResizeFinished = true;
//            return;
//        }

        synchronized (v) {   //TODO:  lint warning synchronization on local variable or method parameter

            boolean isResized = false; //每次执行了setTextSize都会置成false;

            String tag = (String) v.getTag();

            if (isEditableMode()) {
                //我们不允许在可编辑情况下进行自动resize，因为这是没有必要的
               // LOGD(TAG, "triggerResizeTextToFitFrame: aborted since we don't do this in edit mode");
                return;
            }

            if (mAllowToTriggerResizeTextToFitFrame == false) {
                return;
            }

            if (v.getText().length() == 0) {
                if (tag.equals(TAG_SUBHEADING)) {
                    flag_Subheading_ResizeFinished = true;
                    //LOGD(TAG, "triggerResizeTextToFitFrame: return because subheading text is empty");
                } else if (tag.equals(TAG_MAIN)) {
                    flag_Main_ResizeFinished = true;
                    //LOGD(TAG, "triggerResizeTextToFitFrame: return because main text is empty");
                } else if (tag.equals(TAG_SUB)) {
                    flag_Sub_ResizeFinished = true;
                    //LOGD(TAG, "triggerResizeTextToFitFrame: return because sub text is empty");
                }

                return;
            } else {
                //LOGD(TAG, "triggerResizeTextToFitFrame ***begin with content: " + v.getText().toString());
            }


            //特殊逻辑，历史原因,sample pack中的这部分内容的line number不正确，需要二次修正
            if (v.getText().toString().contains("General knowledge")) {
                Log.d("ccaa","coming the magic, textsize = " + v.getTextSize());
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
                if (noOfLines < targetLines && noOfLines > 0) {

                    boolean highAccuracy = false;
                    if (targetLines - noOfLines == 1) {
                        highAccuracy = true;
                    }

                    // resize action
                    float textSize = v.getTextSize();
                    float newTextSize = 0;
                    float delta;

                    if (highAccuracy) {
                        delta = 0.3f;
                    } else {
                        if (textSize > 200) {
                            delta = textSize/10;
                        } else if ((textSize > 100) && (textSize <= 200)) {
                            delta = textSize/40;
                        } else if ((textSize > 50) && (textSize <= 100)) {
                            delta = textSize/50;
                        } else if ((textSize > 30) && (textSize <= 50)) {
                            delta = 0.5f;
                        } else {
                            delta = 1;
                        }
                    }
                    newTextSize = textSize + delta;
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    isResized = true;

                    //LOGD(TAG, "triggerResizeTextToFitFrame: make  bigger. Check if running > 1 with text " + v.getText() + " noOfLines= " + noOfLines + " targetLines= " + targetLines + " textSize=" +textSize + " delta = " + delta);

                    return;

                } else {

                    if (tag.equals(TAG_SUBHEADING)) {
                        flag_Subheading_OneoffIncrease = true;
                    } else if (tag.equals(TAG_MAIN)) {
                        flag_Main_OneoffIncrease = true;
                    } else if (tag.equals(TAG_SUB)) {
                        flag_Sub_OneoffIncrease = true;
                    }
                }
            }



            if (((textHeight > viewHeight) && (viewHeight > 1) && (noOfLines > 0)) ||
                    (noOfLines > targetLines && targetLines > 0)) {

                boolean highAccuracy = false;
                if (noOfLines - targetLines <= 1 && (textHeight-viewHeight <10)) { //textHeight-viewHeight <10的限定原因是因为实际发现,如果没有这个,就会造成性能问题
                    highAccuracy = true;
                }

                // resize action
                float textSize = v.getTextSize();
                float newTextSize = 0;
                float delta;

                if (highAccuracy) {
                    delta = 0.45f;
                } else {
                    if (textSize > 200) {
                        delta = textSize/10;
                    } else if ((textSize > 100) && (textSize <= 200)) {
                        delta = textSize/40;
                    } else if ((textSize > 50) && (textSize <= 100)) {
                        delta = textSize/50;
                    } else if ((textSize > 30) && (textSize <= 50)) {
                        delta = 0.5f;
                    } else {
                        delta = 1;
                    }
                }
                newTextSize = textSize - delta;
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

                isResized = true;

                LOGD(TAG, "triggerResizeTextToFitFrame: make size smaller on " + v.getText() + " noOfLines= " + noOfLines + " targetLines= " + targetLines + " textSize=" +textSize + " delta = " + delta);


                //in case the font size still too big
                noOfLines = v.getLineCount();
                if ((targetLines > 0) && (targetLines < noOfLines)) {
                    newTextSize = v.getTextSize() - 2;
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                    isResized = true;

                    LOGD(TAG, "triggerResizeTextToFitFrame: make size smaller again on " + v.getText());

                }

            }

            if (isResized) {
                if (tag.equals(TAG_SUBHEADING)) {
                    flag_Subheading_ResizeFinished = false;
                } else if (tag.equals(TAG_MAIN)) {
                    flag_Main_ResizeFinished = false;
                } else if (tag.equals(TAG_SUB)) {
                    flag_Sub_ResizeFinished = false;
                }
            } else {
                if (tag.equals(TAG_SUBHEADING)) {
                    flag_Subheading_ResizeFinished = true;
                } else if (tag.equals(TAG_MAIN)) {
                    flag_Main_ResizeFinished = true;
                } else if (tag.equals(TAG_SUB)) {
                    flag_Sub_ResizeFinished = true;
                }
            }

            //LOGD(TAG, "triggerResizeTextToFitFrame ***end");
        }

    }

    private void removeEditTextListener() {

        //1.
        // setOnEditorActionListener，我们不需要手工remove

        mVerticalScrollView.getViewTreeObserver().removeOnScrollChangedListener(mOnVerticalScrollViewChangedListener);

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

        //4.


    }


    private void setTextsListener() {

       // LOGD(TAG, "setTextsListener: cardSN=" + mCurrentCard.cardSN);

        if (mIsPlayingCard == false) {
            mSidebarTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if ((mIsPlayingCard == false) && (mIsCreatingCard == false)) {
                            saveEditedCard();
                        } else {
                        }
                        mIsTakeSnapshotAllNeeded = true;
                    }
                    return false;
                }
            });
            mCreator.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if ((mIsPlayingCard == false) && (mIsCreatingCard == false)) {
                            saveEditedCard();
                        } else {
                        }
                        mIsTakeSnapshotAllNeeded = true;
                    }
                    return false;
                }
            });
            mJobTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if ((mIsPlayingCard == false) && (mIsCreatingCard == false)) {
                            saveEditedCard();
                        } else {
                        }
                        mIsTakeSnapshotAllNeeded = true;
                    }
                    return false;
                }
            });

            mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if ((mIsPlayingCard == false) && (mIsCreatingCard == false)) {
                            saveEditedCard();
                        } else {
                        }
                        mIsTakeSnapshotAllNeeded = true;
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
                        //之所以做这个逻辑是因为即便是setText也会call这个方法，这在初始化card中将成为灾难
                        if ((!mIsPlayingCard)
                                && (TAG_TITLE.equals(getCurrentFocusedViewTag()))) {
                            mIsTakeSnapshotAllNeeded = true;
                        }
                    } else {
                        mCurrentPack.answerTitle = mTitle.getText().toString();
                    }
                    //LOGD(TAG, "afterTextChanged: mTitle has changed");
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
                    //之所以做这个逻辑是因为即便是setText也会call这个方法，这在初始化card中将成为灾难
                    if ((!mIsPlayingCard)
                            &&(TAG_CREATOR.equals(getCurrentFocusedViewTag()))) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    //LOGD(TAG, "afterTextChanged: mCreator has changed");

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
                    //之所以做这个逻辑是因为即便是setText也会call这个方法，这在初始化card中将成为灾难
                    if ((!mIsPlayingCard)
                            && (TAG_JOB_TITLE.equals(getCurrentFocusedViewTag()))) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    //LOGD(TAG, "afterTextChanged: mJobTitle has changed");

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
                    //之所以做这个逻辑是因为即便是setText也会call这个方法，这在初始化card中将成为灾难
                    mCurrentPack.sidebarTitle = mSidebarTitle.getText().toString();
                    if ((!mIsPlayingCard)
                            && (TAG_SIDE_BAR_TITLE.equals(getCurrentFocusedViewTag()))) {
                        mIsTakeSnapshotAllNeeded = true;
                    }

                    //LOGD(TAG, "afterTextChanged: mSidebarTitle has changed");

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
                //LOGD(TAG, "afterTextChanged: on subheading: " + s.toString() + " with line count = " + maxLines);

                if (mIsQuestionShowing) {
                    mCurrentCard.question.subheading = mSubheading.getText().toString();

                } else {
                    mCurrentCard.answer.subheading = mSubheading.getText().toString();
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
                //LOGD(TAG, "afterTextChanged: on main: " + s.toString() + " with line count = " + maxLines);

                if (mIsQuestionShowing) {
                    mCurrentCard.question.main = mMain.getText().toString();

                } else {
                    mCurrentCard.answer.main = mMain.getText().toString();

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
                //LOGD(TAG, "afterTextChanged: on sub: " + s.toString() + " with line count = " + maxLines);

                if (mIsQuestionShowing) {
                    mCurrentCard.question.sub = mSub.getText().toString();

                } else {
                    mCurrentCard.answer.sub = mSub.getText().toString();
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
                    maxLines = mCurrentCard.question.lineNoSubheading;

                    if (isEditableMode()) {
                        mCurrentCard.question.lineNoSubheading = mSubheading.getLineCount();
                    }

                } else {
                    maxLines = mCurrentCard.answer.lineNoSubheading;

                    if (isEditableMode()) {
                        mCurrentCard.answer.lineNoSubheading = mSubheading.getLineCount();
                    }
                }

                //我们不允许在可编辑情况下进行自动resize，因为这是没有必要的
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

                    maxLines = mCurrentCard.question.lineNoMain;

                    if (isEditableMode()) {
                        mCurrentCard.question.lineNoMain = mMain.getLineCount();
                    }

                } else {
                    maxLines = mCurrentCard.answer.lineNoMain;

                    if (isEditableMode()) {
                        mCurrentCard.answer.lineNoMain = mMain.getLineCount();
                    }
                }

                //我们不允许在可编辑情况下进行自动resize，因为这是没有必要的
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
                    maxLines = mCurrentCard.question.lineNoSub;

                    if (isEditableMode()) {
                        mCurrentCard.question.lineNoSub = mSub.getLineCount();
                    }

                } else {
                    maxLines = mCurrentCard.answer.lineNoSub;

                    if (isEditableMode()) {
                        mCurrentCard.answer.lineNoSub = mSub.getLineCount();
                    }
                }

                //我们不允许在可编辑情况下进行自动resize，因为这是没有必要的
                triggerResizeTextToFitFrame(mSub, maxLines);
            }
        };
        mVtoSub = mSub.getViewTreeObserver();
        mVtoSub.addOnGlobalLayoutListener(mVtoSubListener);

    }

    /*
     *我们尽量避免输出null
     */
    private String getCurrentFocusedViewTag() {
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            return "";
        } else {
            if (view instanceof FCCEditText){
                return (String) view.getTag();
            } else {
                return "";
            }
        }
    }


    /**
     * question和answer上有些内容是一样的，我们不需要做两次，所以把通用的内容的更新放在这个方法中
     */
    private void updateCommonContent() {

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (("null").contains(mCurrentPack.sidebarTitle)) {
            mSidebarTitle.setText("");
        } else {
            mSidebarTitle.setText(mCurrentPack.sidebarTitle);
        }

        mCardSN.setText(String.format("%d", mCurrentCard.cardSN));

        if (StringUtils.isEmpty(mCurrentPack.logoImageUriFormatStr) == false) {
            imageLoader.displayImage(mCurrentPack.logoImageUriFormatStr, mLogoImage,mDisplayImageOptions);
        }

        mCreator.setText(mCurrentPack.creatorNickName);
        mJobTitle.setText(mCurrentPack.jobTitle);

        int colorResourceID[] = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground));
        mSidebarBackground.setBackgroundResource(colorResourceID[1]);
        mTitleBackground.setBackgroundResource(colorResourceID[2]);
        mCardSN.setBackgroundResource(colorResourceID[3]);

        if (mIsQuestionShowing) {
            mTitle.setTextColor(colorResourceID[4]);
        } else {
            mTitle.setTextColor(colorResourceID[5]);
        }

        if (!mIsPlayingCard) {
            if (mIsQuestionShowing) {
                mTitle.setText(mCurrentPack.questionTitle);
            } else {
                mTitle.setText(mCurrentPack.answerTitle);
            }
        }


        boolean isPlaceHolderLogoImage =  StringUtils.isEmptyOrPlaceHolder(mCurrentPack.logoImageUriFormatStr);
        if (isPlaceHolderLogoImage) {
            String placeholderImagePath = FileOperationHelper.getLogoPlaceholderImagePath();
            imageLoader.displayImage(placeholderImagePath,mLogoImage,mDisplayImageOptions);

        } else {
            imageLoader.displayImage(mCurrentPack.logoImageUriFormatStr, mLogoImage,mDisplayImageOptions);
        }

        if (mIsPlayingCard && isPlaceHolderLogoImage) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }


    }


    private void updateQuestionContent() {

        mSubheading.setText(mCurrentCard.question.subheading);

        mMain.setText(mCurrentCard.question.main);
        mSub.setText(mCurrentCard.question.sub);

        {
            final boolean isGif = isGif(mCurrentCard.question.imageUriFormatStr);
            final boolean isLocalVideo = isLocalVideo(mCurrentCard.question.movieUriFormatStr);

            if (mImage.getVisibility() == View.VISIBLE) {

                if (isLocalVideo) {

                    mImage.setMultimediaType(FFCMultimediaType.Video);
                    mImage.setVideoUriPath(mCurrentCard.question.movieUriFormatStr,mCurrentCard.question.imageUriFormatStr);

                } else {

                    mImage.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener(){

                                @Override
                                public void onGlobalLayout() {
                                    if (Build.VERSION.SDK_INT < 16) {
                                        mImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    } else {
                                        mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                    mImage.setAnimitableImage(Uri.parse(mCurrentCard.question.imageUriFormatStr),isGif,mOnFrescoImageViewLoadCompletionListener);
                                }

                            });
                }

            }
        }



        {
            final boolean isGif = isGif(mCurrentCard.question.imageUriFormatStr2);
            final boolean isLocalVideo = isLocalVideo(mCurrentCard.question.movieUriFormatStr2);

            if (mImage2.getVisibility() == View.VISIBLE) {

                if (isLocalVideo) {

                    mImage2.setMultimediaType(FFCMultimediaType.Video);
                    mImage2.setVideoUriPath(mCurrentCard.question.movieUriFormatStr2,mCurrentCard.question.imageUriFormatStr2);

                } else {

                    mImage2.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener(){

                                @Override
                                public void onGlobalLayout() {
                                    if (Build.VERSION.SDK_INT < 16) {
                                        mImage2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    } else {
                                        mImage2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                    mImage2.setAnimitableImage(Uri.parse(mCurrentCard.question.imageUriFormatStr2),isGif,mOnFrescoImageViewLoadCompletionListener);
                                }

                            });
                }

            }
        }

        if (StringUtils.isEmpty(mCurrentCard.question.backgroundImageUriFormatStr) == false && (mBackgroundImageView.getVisibility() == View.VISIBLE)) {
            setCardBackgroundImageWithUri(mCurrentCard.question.backgroundImageUriFormatStr);
        } else {
            setCardBackgroundImageDefault();
        }



    }

    private void updateAnswerContent() {
        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);

        {
            final boolean isGif = isGif(mCurrentCard.answer.imageUriFormatStr);
            final boolean isLocalVideo = isLocalVideo(mCurrentCard.answer.movieUriFormatStr);

            if (mImage.getVisibility() == View.VISIBLE) {

                if (isLocalVideo) {

                    mImage.setMultimediaType(FFCMultimediaType.Video);
                    mImage.setVideoUriPath(mCurrentCard.answer.movieUriFormatStr,mCurrentCard.answer.imageUriFormatStr);

                } else {

                    mImage.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener(){

                                @Override
                                public void onGlobalLayout() {
                                    if (Build.VERSION.SDK_INT < 16) {
                                        mImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    } else {
                                        mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                    mImage.setAnimitableImage(Uri.parse(mCurrentCard.answer.imageUriFormatStr),isGif,mOnFrescoImageViewLoadCompletionListener);
                                }

                            });
                }

            }
        }



        {
            final boolean isGif = isGif(mCurrentCard.answer.imageUriFormatStr2);
            final boolean isLocalVideo = isLocalVideo(mCurrentCard.answer.movieUriFormatStr2);

            if (mImage2.getVisibility() == View.VISIBLE) {

                if (isLocalVideo) {

                    mImage2.setMultimediaType(FFCMultimediaType.Video);
                    mImage2.setVideoUriPath(mCurrentCard.answer.movieUriFormatStr2,mCurrentCard.answer.imageUriFormatStr2);

                } else {

                    mImage2.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener(){

                                @Override
                                public void onGlobalLayout() {
                                    if (Build.VERSION.SDK_INT < 16) {
                                        mImage2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    } else {
                                        mImage2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }

                                    mImage2.setAnimitableImage(Uri.parse(mCurrentCard.answer.imageUriFormatStr2),isGif,mOnFrescoImageViewLoadCompletionListener);
                                }

                            });
                }

            }
        }

        if (StringUtils.isEmpty(mCurrentCard.answer.backgroundImageUriFormatStr) == false && (mBackgroundImageView.getVisibility() == View.VISIBLE)) {
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
            if (mCurrentCard.question.movieUriFormatStr.length() > 0 || isGif(mCurrentCard.question.imageUriFormatStr)) {
                //allow to play movie
                mImage.setEnabled(true);

            } else {
                mImage.setEnabled(false);

            }

            if (mCurrentCard.question.movieUriFormatStr2.length() > 0 || isGif(mCurrentCard.question.imageUriFormatStr2)) {
                //allow to play movie
                mImage2.setEnabled(true);

            } else {
                mImage2.setEnabled(false);

            }

        } else {
            if (mCurrentCard.answer.movieUriFormatStr.length() > 0 || isGif(mCurrentCard.answer.imageUriFormatStr)) {
                //allow to play movie
                mImage.setEnabled(true);

            } else {
                mImage.setEnabled(false);

            }

            if (mCurrentCard.answer.movieUriFormatStr2.length() > 0 || isGif(mCurrentCard.answer.imageUriFormatStr2)) {
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

        mImage.setBackgroundResource(R.drawable.shape_imageview_no_editable);
        mImage2.setBackgroundResource(R.drawable.shape_imageview_no_editable);

    }

    /**
     * put save here when creating a new card
     * put save in onKeyboardClose when editting a current card
     */
    public void saveNewCreatedCard() {

        LOGD(TAG, "saveNewCreatedCard: ");

        mCurrentPack.addCard(AppContext.getAppContext(), mCurrentCard); //新截图没有包含

        if (mIsTakeSnapshotAllNeeded) {
            mSnapshotAllCardsSemaphore = 0;
            takeSnapshotAll(); //在这里会自动save包含新截图的数据，在之前必须先保存新增的卡片，即执行mCurrentPack.addCard
            mCurrentPack.save(AppContext.getAppContext());

        } else {
            mSnapshotAllCardsSemaphore = -1;  //表明不需要snapshot all cards,最多只是当前的
            takeSnapshotCurrentCard();//在这里会自动save包含新截图的数据
        }

        mIsTakeSnapshotAllNeeded = false;


    }

    /**
     * 支持仅有一个card的情况
     * Snap all the cards under current pack
     * take care of notification updating master list view
     */
    public void takeSnapshotAll() {

        LOGD(TAG, "takeSnapshotAll");

        ((MainActivity) getActivity()).showSnapShotProgressDialog();

        //step1: take snapshot on current card
        takeSnapshotCurrentCard();

        //step2: take snapshot on others card under current pack
        ((MainActivity) getActivity()).prepareDataForSnapShotAllExceptCurrentCard(mCurrentPack, mCurrentCard);
    }


    /**
     * 凡是call这个方法的，都会自动导致更新card list view。这也是从MainActivity或CardDetailFragment回调更新card list view的唯一途径
     */
    Boolean subHeadingQuestionAlphaRevertNeeded = false;
    Boolean mainQuestionAlphaRevertNeeded = false;
    Boolean subQuestionAlphaRevertNeeded = false;
    private void takeSnapshotCurrentCard() {

        LOGD(TAG, "takeSnapshotCurrentCard with cardSN = " + mCurrentCard.cardSN);

        boolean toggle = false;


        resetVerticalScrollViewBottomMargin();

        if (isEditableMode()) {
            disableCardEditable();


            if (mSubheading.getAlpha() == 0.5) {
                subHeadingQuestionAlphaRevertNeeded = true;
                mSubheading.setAlpha(0);
            }

            if (mMain.getAlpha() == 0.5) {
                mainQuestionAlphaRevertNeeded = true;
                mMain.setAlpha(0);
            }

            if (mSub.getAlpha() == 0.5) {
                subQuestionAlphaRevertNeeded = true;
                mSub.setAlpha(0);
            }
        }

        if (mIsQuestionShowing == false) {
            switchToQuestionViewWithOption(false);
            toggle = true;
        }

        //hide logo image if its placeholder
        if (StringUtils.isEmptyOrPlaceHolder(mCurrentPack.logoImageUriFormatStr)) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }

        if (toggle == true) {
            Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            beginScreenshot(true);
                        }

                    }, 1);
        } else {
            beginScreenshot(false);
        }

    }


    /*
     * toggleBackNecessary is used to determine whether to go back to answer view finally
     */
    private void beginScreenshot(boolean toggleBackNecessary) {
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

            if (subHeadingQuestionAlphaRevertNeeded) {
                subHeadingQuestionAlphaRevertNeeded = false;
                mSubheading.setAlpha(0.5f);
            }

            if (mainQuestionAlphaRevertNeeded) {
                mainQuestionAlphaRevertNeeded = false;
                mMain.setAlpha(0.5f);
            }

            if (subQuestionAlphaRevertNeeded) {
                subQuestionAlphaRevertNeeded = false;
                mSub.setAlpha(0.5f);
            }
        }

        mCurrentCard.save(AppContext.getAppContext());

        if (toggleBackNecessary == true) {
            switchToAnswerView(false);
        }


        if (mCurrentPack.cards.size() > 1) {

            if (mSnapshotAllCardsSemaphore == -1) {
                //表示不需要snapshot所有卡片
                ((MainActivity) getActivity()).cleanupDataForSnapShotAllExceptCurrent(); //并通知更新card list view
            } else {
                //表示需要同步snapshot所有卡片后执行
                mSnapshotAllCardsSemaphore++;
                if (mSnapshotAllCardsSemaphore == mCurrentPack.cards.size()) {
                    mSnapshotAllCardsSemaphore = 0;
                    ((MainActivity) getActivity()).cleanupDataForSnapShotAllExceptCurrent(); //并通知更新card list view
                }
            }
        } else {
            //如果只有一个卡片，则直接结束，并并通知更新card list view
            ((MainActivity) getActivity()).cleanupDataForSnapShotAllExceptCurrent();
        }
    }

    /**
     * 选某个主题颜色后的回调
     * @param cardColorTemplateIndex
     */
    public void cardColorTemplateSelectedPostAction(int cardColorTemplateIndex) {

        String templateBackground = StringUtils.convertTemplateBackgroundIndexToString(cardColorTemplateIndex);
        mCurrentCard.templateBackground = templateBackground;

        int colorResourceID[] = (StringUtils.convertTemplateBackgroundStringToResourceID(templateBackground));
        mSidebarBackground.setBackgroundResource(colorResourceID[1]);
        mTitleBackground.setBackgroundResource(colorResourceID[2]);
        mCardSN.setBackgroundResource(colorResourceID[3]);

        if (mIsQuestionShowing) {
            mTitle.setTextColor(colorResourceID[4]);
        } else {
            mTitle.setTextColor(colorResourceID[5]);
        }


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
        if ((mCurrentPack != null) && (mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {
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

            case 17: {
                configTemplate_17();
                break;
            }

            default:
                LOGE(TAG, "updateQuestionViewTemplate: mCurrentCard.question.templateID is out of scope");
        }

        updateContentViewsPointers(templateID);

        mContentBodyLinearLayout.requestLayout();

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

            case 17: {
                configTemplate_17();
                break;
            }

            default:
                LOGE(TAG, "updateAnswerViewTemplate: mCurrentCard.answer.templateID is out of scope");
        }

        updateContentViewsPointers(templateID);


        mContentBodyLinearLayout.requestLayout();
    }


    private void configTemplate_0 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);

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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);

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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));

        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
        mMain.setVisibility(View.GONE);
        mSub.setVisibility(View.GONE);

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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
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
        params.weight = 355f;
        params.rightMargin = UIHelper.getPixels(4);
        bottomLeft.setLayoutParams(params);


        bottom.addView(bottomLeft);

        //子下右布局
        LinearLayout bottomRight = new LinearLayout(getActivity());
        bottomLeft.setOrientation(LinearLayout.VERTICAL);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = 0;
        params.weight = 330f;
        int topMargin = UIHelper.getPixels(4);
        int bottomMargin = UIHelper.getPixels(10);
        int leftMargin = UIHelper.getPixels(4);
        int rightMargin = UIHelper.getPixels(14);
        params.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
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
        mImage.setLayoutParams(params);

        bottomRight.addView(mImage);
    }

    private void configTemplate_9 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mImage.setVisibility(View.VISIBLE);

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.VISIBLE);
        mImage2.setStaticImageURI(Uri.parse(placeholderImagePath));


        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.GONE);
        mSub.setVisibility(View.GONE);


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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
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
        mImage.setVisibility(View.GONE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.VISIBLE);
        mImage2.setStaticImageURI(Uri.parse(placeholderImagePath));


        mSubheading.setVisibility(View.VISIBLE);
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


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

        String placeholderImagePath = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        mImage.setStaticImageURI(Uri.parse(placeholderImagePath));


        mImage2.setVisibility(View.GONE);
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


    private void configTemplate_17 () {

        removeAllSubViewsInContentBody();

        mContentBodyLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mImage.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.GONE);
        mSubheading.setVisibility(View.GONE);
        mMain.setVisibility(View.GONE);
        mSub.setVisibility(View.VISIBLE);


        //image
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 230;
        params.height = 0;

        int topMargin = UIHelper.getPixels(20);
        int bottomMargin = UIHelper.getPixels(20);
        int horizontalMargin = UIHelper.getPixels(155);
        params.setMargins(horizontalMargin,topMargin,horizontalMargin,bottomMargin);
        mImage.setLayoutParams(params);
        mContentBodyLinearLayout.addView(mImage);

        //sub
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 100;
        params.height = 0;
        params.topMargin = UIHelper.getPixels(0);
        mSub.setLayoutParams(params);

        mContentBodyLinearLayout.addView(mSub);
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
            if (ViewGroup.class.isInstance(item)) {
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

        //由于这是一个one off的标志，所以必须设置最后改变文字内容的地方，也就是这里
        flag_Subheading_OneoffIncrease = false;
        flag_Main_OneoffIncrease = false;
        flag_Sub_OneoffIncrease = false;

        mAllowToTriggerResizeTextToFitFrame = true;
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

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subheadingAlignVertical));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.mainAlignVertical));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subAlignVertical));

        //step2: size

        //由于这是一个one off的标志，所以必须设置最后改变文字内容的地方，也就是这里
        flag_Subheading_OneoffIncrease = false;
        flag_Main_OneoffIncrease = false;
        flag_Sub_OneoffIncrease = false;

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

                if (subMenuID == 6) {
                    //semi transparent logic

                    if (editTextTag.equals(TAG_SUBHEADING)) {

                        if (currentCSS.subheadingSemiTransparent) {
                            mCurrentFocusedCardContentText.setAlpha(1.0f);
                        } else {
                            mCurrentFocusedCardContentText.setAlpha(0.5f);
                        }

                        currentCSS.subheadingSemiTransparent = ! (currentCSS.subheadingSemiTransparent);

                    } else if (editTextTag.equals(TAG_MAIN)) {

                        if (currentCSS.mainSemiTransparent) {
                            mCurrentFocusedCardContentText.setAlpha(1.0f);
                        } else {
                            mCurrentFocusedCardContentText.setAlpha(0.5f);
                        }

                        currentCSS.mainSemiTransparent = ! (currentCSS.mainSemiTransparent);

                    } else if (editTextTag.equals(TAG_SUB)) {

                        if (currentCSS.subSemiTransparent) {
                            mCurrentFocusedCardContentText.setAlpha(1.0f);
                        } else {
                            mCurrentFocusedCardContentText.setAlpha(0.5f);
                        }

                        currentCSS.subSemiTransparent = ! (currentCSS.subSemiTransparent);
                    }

                } else {

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
            case 4:   //language
                ArrayList<String> list = Text2SpeechHelper.sharedHelper().availableLanguageLocalStringList();

                if (editTextTag.equals(TAG_SUBHEADING)) {
                    currentCSS.subheadingText2SpeechSound = list.get(subMenuID); //it's subMenuID + 1, rather than subMenuID
                } else if (editTextTag.equals(TAG_MAIN)) {
                    currentCSS.mainText2SpeechSound = list.get(subMenuID ); //it's subMenuID + 1, rather than subMenuID
                } else if (editTextTag.equals(TAG_SUB)) {
                    currentCSS.subText2SpeechSound = list.get(subMenuID); //it's subMenuID + 1, rather than subMenuID
                }

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


    /*
     * 同saveNewCreatedCard所区别。这是的卡片是已经存在的，而不是正在创建的
     */
    public void saveEditedCard() {

        LOGD(TAG, "saveEditedCard");

        if (mIsCreatingCard) {
            throw new IllegalStateException("saveEditedCard should never be called when mIsCreatingCard = true");
        }

        mSnapshotAllCardsSemaphore = -1; //表明不需要snapshot all cards,最多只是当前的

        //step2: prepare update info in mast list view
        if (mIsTakeSnapshotAllNeeded && (mIsCreatingCard == false)) {
            ((MainActivity) getActivity()).showSnapShotProgressDialog();
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


        ((MainActivity) getActivity()).removeCSSToolbar();

        PackRecordHelper.savePackUpdateRecord(mCurrentPack);
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



    private void setCardBackgroundImageWithUri(final String uriString) {
        LOGD(TAG, "setCardBackgroundImageWithUri: " + uriString);
        if (uriString == null) {
            mBackgroundImageView.setImageURI("");
        } else {

            mBackgroundImageView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener(){

                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT < 16) {
                                mBackgroundImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                mBackgroundImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }


                            if (mBackgroundImageView.getWidth() > 0 && mBackgroundImageView.getHeight() > 0) {
                                ResizeOptions resizeOptions = new ResizeOptions(mBackgroundImageView.getWidth(),mBackgroundImageView.getHeight());
                                ImageRequest request;
                                if (resizeOptions.width == 0 || resizeOptions.height == 0) {
                                    request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uriString))
                                            .build();
                                } else {
                                    request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uriString))
                                            .setResizeOptions(resizeOptions)
                                            .build();
                                }
                                DraweeController controller = Fresco.newDraweeControllerBuilder()
                                        .setOldController(mBackgroundImageView.getController())
                                        .setImageRequest(request)
                                        .build();

                                mBackgroundImageView.setController(controller);
                            }

                        }

                    });


        }

    }


    private void setCardBackgroundImageDefault() {
        mBackgroundImageView.setImageURI("");

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
        ArrayList<HashMap> arrayList = new ArrayList<HashMap>();

        if (mIsQuestionShowing) {

            Question question = mCurrentCard.question;
            if (question.subheading.length() > 0) {

                String str = replaceBasicSymbol(question.subheading);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("subheadingQuestion",itemStr);
                    arrayList.add(myMap);
                }
            }
            if (question.main.length() > 0) {
                String str = replaceBasicSymbol(question.main);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("mainQuestion",itemStr);
                    arrayList.add(myMap);
                }
            }
            if (question.sub.length() > 0) {
                String str = replaceBasicSymbol(question.sub);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("subQuestion",itemStr);
                    arrayList.add(myMap);
                }
            }

            if (arrayList.size() == 0) {
                HashMap myMap = new HashMap<String, String>();
                myMap.put("subheadingQuestion","    ");
                arrayList.add(myMap); //in auto delay mode, we need this. Otherwise, scroll could not go on since there's no content
            }

        } else {
            Answer answer = mCurrentCard.answer;
            if (answer.subheading.length() > 0) {
                String str = replaceBasicSymbol(answer.subheading);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("subheadingAnswer",itemStr);
                    arrayList.add(myMap);
                }
            }
            if (answer.main.length() > 0) {
                String str = replaceBasicSymbol(answer.main);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("mainAnswer",itemStr);
                    arrayList.add(myMap);
                }
            }
            if (answer.sub.length() > 0) {
                String str = replaceBasicSymbol(answer.sub);

                String[] array = str.split("(?<=\n)|(?=\n)");

                for (String itemStr :array) {
                    HashMap myMap = new HashMap<String, String>();
                    myMap.put("subAnswer",itemStr);
                    arrayList.add(myMap);
                }
            }

            if (arrayList.size() == 0) {
                HashMap myMap = new HashMap<String, String>();
                myMap.put("subheadingAnswer","    ");
                arrayList.add(myMap); //in auto delay mode, we need this. Otherwise, scroll could not go on since there's no content
            }
        }


        return arrayList;
    }

    /*
     * 故事的背景：iOS在text2speech中行与行之间的朗读是间隔的（.5秒左右），但是Android是没有的，所以这里认为的制造一个间隔
     */
    private String addNewLineCharactersIntoStr(String origionalStr) {
        if (StringUtils.isEmpty(origionalStr)) {
            return origionalStr;
        }

//加\n在模拟器上可以看到停顿,但是在device上则没有任何效果,所以comment out this logic
//        String resultStr = "";
//
//        String[] array = origionalStr.split("(?<=\n)|(?=\n)");  //默认split是干掉\n，而我们希望保留任何\n
//        for (String item: array) {
//            if (item.equals("\n") == false) {
////                item = item + "\n\n";
//                  item = item + "";
//            }
//            resultStr = resultStr + item;
//
//        }

        return origionalStr;

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

        String minus = Character.toString((char)0x2212);  //minus
        resultStr = resultStr.replace(minus, minusStr);

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

        resultStr = resultStr.replaceAll("([a-zA-Z])\\-([a-zA-Z])","$1$2");  //remove - if between two letter like X-ray


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

        if (MutipleTargetHelper.isFullVersion() == false) {
            return;
        }

        if (mIsPlayingCard) {
            return;
        }

        if (((MainActivity)getActivity()).getPackInfoLayoutVisible() ) {
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

        final int requestCodeFinal   = requestCode;
        final int resultCodeFinal    = resultCode;
        final Intent  dataFinal      = data;

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

                    if (requestCodeFinal == Global.REQUEST_CODE_FROM_BACKGROUND) {

                        List<MediaItem> mMediaSelectedList = MediaPickerActivity
                                .getMediaItemSelected(dataFinal);
                        MediaItem item = mMediaSelectedList.get(0);//因为是单选，所以永远是第一个
                        Uri selectedURI = item.getUriOrigin();

                        LOGD(TAG, "onActivityResult: ready to crop");
                        Intent intent = new Intent(getActivity(), CropActivity.class);
                        intent.putExtra("uri",selectedURI);
                        startActivityForResult(intent, Global.REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED);

                    } else if (requestCodeFinal == Global.REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED) {

                        handleCrop(Global.REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED,resultCodeFinal,dataFinal);

                    } else {

                        List<MediaItem> mMediaSelectedList = MediaPickerActivity
                                .getMediaItemSelected(dataFinal);
                        MediaItem item = mMediaSelectedList.get(0);//因为是单选，所以永远是第一个
                        Uri selectedURI = item.getUriOrigin();

                        String decodeUriStr = "";
                        try {
                            decodeUriStr = URLDecoder.decode(selectedURI.toString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if (decodeUriStr.contains("/video")) { //video

                            //step1: get image
                            thumbnailImageFromVideoURL(selectedURI);

                            //step2: get video
                            File toSaveVideoFile = UIHelper.saveVideoToCaches(AppContext.getAppContext(), selectedURI);
                            String savedVideoUriFile = FileOperationHelper.convertToUriFormatFile(toSaveVideoFile);
                            String thumbnailFilePath = "";


                            if (mIsImage2Active) {
                                mImage2.setMultimediaType(FFCMultimediaType.Video);
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr2 = savedVideoUriFile;
                                    thumbnailFilePath = mCurrentCard.question.imageUriFormatStr2;
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr2 = savedVideoUriFile;
                                    thumbnailFilePath = mCurrentCard.answer.imageUriFormatStr2;
                                }
                                mImage2.setVideoUriPath(savedVideoUriFile,thumbnailFilePath);

                            } else {
                                mImage.setMultimediaType(FFCMultimediaType.Video);
                                if (mIsQuestionShowing) {
                                    mCurrentCard.question.movieUriFormatStr = savedVideoUriFile;
                                    thumbnailFilePath = mCurrentCard.question.imageUriFormatStr;
                                } else {
                                    mCurrentCard.answer.movieUriFormatStr = savedVideoUriFile;
                                    thumbnailFilePath = mCurrentCard.answer.imageUriFormatStr;
                                }
                                mImage.setVideoUriPath(savedVideoUriFile,thumbnailFilePath);
                            }

                            if (mIsCreatingCard == false) {
                                mCurrentCard.save(AppContext.getAppContext());
                                if (mIsQuestionShowing) {

                                    Task.delay(460).continueWith(new Continuation<Void, String>() {
                                        @Override
                                        public String then(Task<Void> task) throws Exception {
                                            mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card
                                            takeSnapshotCurrentCard();
                                            return null;
                                        }
                                    },Task.UI_THREAD_EXECUTOR);
                                }
                            }

                        } else if (StringUtils.isEmpty(decodeUriStr) == false) {   //images
                            if (requestCodeFinal == Global.REQUEST_CODE_FROM_LOGO) {

                                ImageSize targetSize = new ImageSize(100, 100);
                                ImageLoader imageLoader = ImageLoader.getInstance();
                                Bitmap scaledBitmap = imageLoader.loadImageSync(selectedURI.toString(),targetSize);

                                if (scaledBitmap == null) {
                                    LOGE(TAG, "handleCrop, scaledBitmap = null");
                                    return;
                                }

                                File toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);

                                mLogoImage.setImageBitmap(scaledBitmap);
                                mCurrentPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                                if (mIsCreatingCard == false) {
                                    mCurrentPack.save(AppContext.getAppContext());
                                    takeSnapshotAll();
                                } else {
                                    mIsTakeSnapshotAllNeeded= true;
                                }

                            } else if (requestCodeFinal == Global.REQUEST_CODE_FROM_IMAGE) {

                                String selectedPath = UIHelper.getRealPathFromURI(getActivity(),selectedURI);
                                boolean isGif = isGif(selectedPath);

                                Bitmap scaledBitmap = null;  //for non-gif
                                File toSaveFile;
                                if (isGif) {
                                    toSaveFile = UIHelper.saveGIFToCaches(new File(selectedPath));
                                } else {

                                    ImageSize targetSize = new ImageSize(400, 400);
                                    ImageLoader imageLoader = ImageLoader.getInstance();
                                    scaledBitmap = imageLoader.loadImageSync(selectedURI.toString(),targetSize);

                                    if (scaledBitmap == null) {
                                        LOGE(TAG, "handleCrop, scaledBitmap = null");
                                        return;
                                    }

                                    toSaveFile = UIHelper.saveImageToCaches(scaledBitmap);
                                }

                                String toSaveFileUrlStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                                if (mIsImage2Active) {

                                    if (isGif) {
                                        mImage2.setAnimitableImage(Uri.fromFile(toSaveFile),isGif,mOnFrescoImageViewLoadCompletionListener);
                                    } else {
                                        mImage2.setStaticImageURI(Uri.parse(toSaveFileUrlStr));
                                    }

                                    if (mIsQuestionShowing) {
                                        mCurrentCard.question.imageUriFormatStr2 = toSaveFileUrlStr;

                                        boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(mCurrentCard.question.movieUriFormatStr2);
                                        if (success == false) {
                                            LOGE(TAG, "failure to delete: " + mCurrentCard.question.movieUriFormatStr2);
                                        }

                                        mCurrentCard.question.movieUriFormatStr2 = "";
                                    } else {
                                        mCurrentCard.answer.imageUriFormatStr2 = toSaveFileUrlStr;

                                        boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(mCurrentCard.answer.movieUriFormatStr2);
                                        if (success == false) {
                                            LOGE(TAG, "failure to delete: " + mCurrentCard.answer.movieUriFormatStr2);
                                        }

                                        mCurrentCard.answer.movieUriFormatStr2 = "";
                                    }
                                } else {

                                    if (isGif) {
                                        mImage.setAnimitableImage(Uri.fromFile(toSaveFile),isGif,mOnFrescoImageViewLoadCompletionListener);
                                    } else {
                                        mImage.setStaticImageURI(Uri.parse(toSaveFileUrlStr));
                                    }

                                    if (mIsQuestionShowing) {
                                        mCurrentCard.question.imageUriFormatStr = toSaveFileUrlStr;

                                        boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(mCurrentCard.question.movieUriFormatStr);
                                        if (success == false) {
                                            LOGE(TAG, "failure to delete: " + mCurrentCard.question.movieUriFormatStr);
                                        }

                                        mCurrentCard.question.movieUriFormatStr = "";
                                    } else {
                                        mCurrentCard.answer.imageUriFormatStr = toSaveFileUrlStr;

                                        boolean success = FileOperationHelper.deleteFileExceptPlaceHolder(mCurrentCard.answer.movieUriFormatStr);
                                        if (success == false) {
                                            LOGE(TAG, "failure to delete: " + mCurrentCard.answer.movieUriFormatStr);
                                        }

                                        mCurrentCard.answer.movieUriFormatStr = "";
                                    }
                                }

                                if (mIsCreatingCard == false) {
                                    mCurrentCard.save(AppContext.getAppContext());
                                    if (mIsQuestionShowing) {

                                        if (isGif) {

                                            Task.callInBackground(new Callable<Object>() {
                                                @Override
                                                public String call() throws Exception {

                                                    if (mIsImage2Active) {
                                                        mLockForScreenshotGif.setTagStr(TAG_IMAGE2);
                                                    } else {
                                                        mLockForScreenshotGif.setTagStr(TAG_IMAGE);
                                                    }



                                                    synchronized (mLockForScreenshotGif) {
                                                        try {
                                                            LOGD(TAG, "mLockForScreenshotGif.wait now");
                                                            mLockForScreenshotGif.wait();
                                                            LOGD(TAG, "mLockForScreenshotGif.wait finished");

                                                            Task.delay(460).continueWith(new Continuation<Void, String>() {
                                                                @Override
                                                                public String then(Task<Void> task) throws Exception {
                                                                    mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card
                                                                    takeSnapshotCurrentCard();

                                                                    return null;
                                                                }
                                                            },Task.UI_THREAD_EXECUTOR);

                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }

                                                    return null;
                                                }
                                            });


                                        } else {

                                            Task.delay(460).continueWith(new Continuation<Void, String>() {
                                                @Override
                                                public String then(Task<Void> task) throws Exception {
                                                    mSnapshotAllCardsSemaphore = -1; //we only need to screenshot curent card
                                                    takeSnapshotCurrentCard();
                                                    return null;
                                                }
                                            },Task.UI_THREAD_EXECUTOR);
                                        }
                                    }
                                }
                            }

                        } else {
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.DIALOG_AlERT))
                                    .setContentText(getString(R.string.DIALOG_UNSUPPORTED_IMAGE_SOURCE))
                                    .show();
                        }
                    }

                }
            }
        }, delay);




    }


    private boolean isGif(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }

        if (path.toLowerCase().contains(".gif")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isLocalVideo(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }

        if (path.toLowerCase().contains(".3gp")) {
            return true;
        } else {
            return false;
        }
    }


    private OnFrescoImageViewLoadCompletionListener mOnFrescoImageViewLoadCompletionListener = new OnFrescoImageViewLoadCompletionListener() {
        @Override
        public void gifLoadSucceeded(View view) {

            String tag = (String) view.getTag();
            if (mLockForScreenshotGif.getTagStr().equals(tag)) {
                synchronized (mLockForScreenshotGif) {
                    try {

                        mLockForScreenshotGif.clearTagStr();
                        LOGD(TAG, "mLockForScreenshotGif.notify now");
                        mLockForScreenshotGif.notify();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        }

        @Override
        public void nonGifLoadSucceeded(View view) {

        }

        @Override
        public void failed(View view) {

        }
    };


}



