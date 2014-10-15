package com.internectics.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.internectics.UI.FCCEditText;
import com.internectics.UI.RoundedBottomRightImageView;
import com.internectics.UI.ScaleHelper;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.android_flashcardcreator.VideoViewActivity;
import com.internectics.android_flashcardcreator.WebViewActivity;
import com.internectics.data.Answer;
import com.internectics.data.CSS;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.Question;
import com.internectics.helper.AudioHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackRecordHelper;
import com.internectics.helper.SymbolHelper;
import com.internectics.util.*;
import com.soundcloud.android.crop.Crop;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

enum IMAGE_SOURCE {
    IMAGE_SOURCE_IS_LOGO,
    IMAGE_SOURCE_IS_IMAGE, //包括IMAGE1和IMAGE2,之间的区分用：mIsImage2Active
    IMAGE_SOURCE_IS_BACKGROUND
}


public class CardDetailFragment extends Fragment implements FCCEditText.OnKeyboardCloseListener, FCCEditText.OnTouchListener {

    public Card mCurrentCard;
    public Pack mCurrentPack;

    public View mContentView;


    private LinearLayout mContentBodyType1;  //第一种布局 （这是同ios非常不同的一点），通过此对象，控制subheading,main,sub的显示与否，而不用每个单独控制
    private LinearLayout mContentBodyType2;  //第二种布局  （这是同ios非常不同的一点），通过此对象，控制subheading,main,sub的显示与否，而不用每个单独控制

    //左边，mContentBodyLeft是frameLayout，包含mContentBodyLeftText和mImage2
    private LinearLayout mContentBodyLeftLayout;
    private LinearLayout mContentBodyLeftTextMainSubLayout;//包含main and sub
    private FrameLayout mContentBodyLeftMainSubFrameLayout;//包含了mContentBodyLeftTextMainSubLayout
    public ImageView mImage2;

    //右边，mContentBodyRightFrameLayout，包含mImage和sub_replace_image
    private FrameLayout mContentBodyRightFrameLayout;
    public ImageView mImage;


    private FCCEditText mSidebarTitle;
    private FrameLayout mSidebarBackground;
    public  TextView mCardSN;
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

    private IMAGE_SOURCE  mActiveImageSource;

    public boolean mIsCreatingCard = false;
    private boolean mIsPlayingCard = false;

    private boolean mIsSnapShotNotCurrent = false;//as to snapshot,we have different stragegy on current showing card and other cards

    public boolean mIsQuestionShowing = true;

    private boolean mIsTakeSnapshotAllNeeded = false; //when fields that belong to current pack(like title) changes, it will be set true

    private static int mSemaphore = 0; //used to indicate all snapshots are done

    //切换过程中
    private boolean mIsSwitchingQuestionAnswerView = false;

    private boolean mIsImage2Active = false; //我们有两个image(image和image2),这个变量用于区分

    //用于
    // 1. onStop时，是否需要进行写入到数据库；
    // 2. resize完毕后，是否需要暂存prepareToSavingTextFontSizeInfo
    private static boolean mIsSaveNeededAfterResize = false;

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


    public boolean isCurrentFocusedCardContentTextUsingDefaultFont() {

        if (mCurrentFocusedCardContentText == null) {
            return true;
        }

        String fontStr = "";
        int editTextTag = Integer.parseInt((String) mCurrentFocusedCardContentText.getTag());
        if (editTextTag == 1001) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.subheadingFont;
            } else {
                fontStr = mCurrentCard.answer.css.subheadingFont;
            }
        } else if (editTextTag == 1002) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.mainFont;
            } else {
                fontStr = mCurrentCard.answer.css.mainFont;
            }
        } else if (editTextTag == 1003) {
            if (mIsQuestionShowing) {
                fontStr = mCurrentCard.question.css.subFont;
            } else {
                fontStr = mCurrentCard.answer.css.subFont;
            }
        }

        if ((fontStr == null) || (fontStr.length() == 0)|| (fontStr.toLowerCase().contains("default"))) {
            return true;
        } else {
            return false;
        }

    }


    public void configureParameters(Pack currentPack, Card currentCard, int source)
    {

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
            Log.i(Global.debugTag, "Creating a new card is going on");
            mCurrentPack = currentPack;
            initilizeNewCard();

        } else {
            mCurrentCard = currentCard;
            mCurrentPack = currentPack;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >=18) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        } else {

        }

        mIMM = (InputMethodManager) (getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

        if (mIsPlayingCard) {
            //need to hide queston/answer segment raido group
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

//        Typeface typeFace = FontCache.get(Global.fontName_Default, getActivity());
//        mSubheading.setTypeface(typeFace,Typeface.BOLD);
//        mMain.setTypeface(typeFace,Typeface.BOLD);
//        mSub.setTypeface(typeFace,Typeface.BOLD);
//        mSubheading2.setTypeface(typeFace,Typeface.BOLD);
//        mMain2.setTypeface(typeFace,Typeface.BOLD);
//        mSub2.setTypeface(typeFace,Typeface.BOLD);

        return mContentView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Global.debugTag, "onViewCreated in CardDetailFragment is called, cardSN=" + mCurrentCard.cardSN);

        updateCommonContent();
        switchToQuestionView(false);

        if (mIsPlayingCard) {
            disableCardEditable();
        } else {
            configureCardStatus();
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

    }


    @Override
    public void onResume() {
        super.onResume();
        mIsTakeSnapshotAllNeeded = false;  //necessary

        //need to be put onResume, see http://stackoverflow.com/questions/13721063/aftertextchanged-being-called-without-the-text-being-actually-changed

        setEditTextListener();

        Log.d(Global.debugTag, "onResume in CardDetailFragment");
    }


    @Override
    public void onDestroy() {
        Log.d(Global.debugTag, String.format("onDestroy in CardDetailFragment, cardSN = %d",mCurrentCard.cardSN));
        super.onDestroy();


    }

    @Override
    public void onStop() {
        Log.d(Global.debugTag, String.format("onStop in CardDetailFragment, cardSN = %d",mCurrentCard.cardSN));
        super.onStop();

        mVtoSubheading.removeGlobalOnLayoutListener(mVtoSubheadingListener);
        mVtoMain.removeGlobalOnLayoutListener(mVtoMainListener);
        mVtoSub.removeGlobalOnLayoutListener(mVtoSubListener);

        //当当前card移除时，比如进入下一卡片，如果进行过resize操作，则保存一下
        if (((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) && (mIsSaveNeededAfterResize)) {
            mIsSaveNeededAfterResize = false;
            //prepareToSavingTextFontSizeInfo,由于resize后，会主动执行一下，所以这里没有必要了
            mCurrentCard.save(AppContext.getAppContext());
            Log.d(Global.debugTag2, "Saving to database after triggerResizeTextToFitFrame in onStop");
        }


    }


    private void playAudio(String path) {

        AudioHelper.playAudio(path);

    }

    private void playVideo() {
        String targetStr =  "";
        if (mIsQuestionShowing) {
            if (mIsImage2Active) {
                if  (mCurrentCard.question.movieUriFormatStr2.length() >0) {
                    targetStr = mCurrentCard.question.movieUriFormatStr2;
                }
            } else {
                if  (mCurrentCard.question.movieUriFormatStr.length() >0) {
                    targetStr = mCurrentCard.question.movieUriFormatStr;
                }
            }
        } else {
            if (mIsImage2Active) {
                if  (mCurrentCard.answer.movieUriFormatStr2.length() >0) {
                    targetStr = mCurrentCard.answer.movieUriFormatStr2;

                }
            } else {
                if  (mCurrentCard.answer.movieUriFormatStr.length() >0) {
                    targetStr = mCurrentCard.answer.movieUriFormatStr;

                }
            }
        }

        if (targetStr.length() > 0) {

            if (Build.FINGERPRINT.startsWith("generic")) {
                Toast.makeText(getActivity(),"Don't support to play on simulator",Toast.LENGTH_LONG).show();
                return;
            }

            if (targetStr.contains("http://") || targetStr.contains("https://")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(targetStr));
                startActivity(i);

            } else {
                String videoPath =  FileOperationHelper.deleteUriSchemeHeader(targetStr);
                Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                intent.putExtra("videoPath", videoPath);
                startActivity(intent);
            }

        } else {
            Toast.makeText(getActivity(),"Not available video file",Toast.LENGTH_LONG).show();
        }


    }


    private void showYoutubeLinkageInputDialog() {
        final EditText textInput = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle("Input a valid YouTube url")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(textInput)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
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
                            Toast.makeText(getActivity(), "Invalid YouTube url, it must be a full url - for example: http://www.youtube.com/watch?v=3-EaGGPGiJY", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void selectImageOrVideoFromLibrary() {
        if (mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
//            Intent intent = new Intent(Intent.ACTION_PICK, null);
//            intent.setType("video/*, images/*");
//            startActivityForResult(intent, CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE);
            Crop.pickImageWithFragment(CardDetailFragment.this, false);
        } else {
            if (mIsQuestionShowing) {
                if (mIsImage2Active) {
                    if  (mCurrentCard.question.movieUriFormatStr2.length() >0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Play video in play mode");
                        builder.setTitle("Alert");
                        builder.create().show();
                    }
                } else {
                    if  (mCurrentCard.question.movieUriFormatStr.length() >0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Play video in play mode");
                        builder.setTitle("Alert");
                        builder.create().show();
                    }
                }
            } else {
                if (mIsImage2Active) {
                    if  (mCurrentCard.answer.movieUriFormatStr2.length() >0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Play video in play mode");
                        builder.setTitle("Alert");
                        builder.create().show();

                    }
                } else {
                    if  (mCurrentCard.answer.movieUriFormatStr.length() >0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Play video in play mode");
                        builder.setTitle("Alert");
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
            mCurrentCard.question.css.subheadingSize =  UIHelper.pixelsToSp((float)(mSubheading.getTextSize()/scaleVal));
            mCurrentCard.question.css.mainSize =  UIHelper.pixelsToSp((float)(mMain.getTextSize()/scaleVal));
            mCurrentCard.question.css.subSize = UIHelper.pixelsToSp((float)(mSub.getTextSize()/scaleVal));

        } else {
            mCurrentCard.answer.css.subheadingSize =  UIHelper.pixelsToSp((float)(mSubheading.getTextSize()/scaleVal));
            mCurrentCard.answer.css.mainSize =  UIHelper.pixelsToSp((float)(mMain.getTextSize()/scaleVal));
            mCurrentCard.answer.css.subSize = UIHelper.pixelsToSp((float)(mSub.getTextSize()/scaleVal));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //whatever RESULT_OK or RESULT_CANCELED, we need to do this first
        ((MainActivity)getActivity()).mIsAllowedToShowPackList = false;

        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(requestCode,resultCode, data);
        } else {

            if (resultCode == Activity.RESULT_OK) {

                Uri selectedURI = data.getData();

                if (selectedURI.toString().contains("/video")) { //video

                    //step1: get image
                    thumbnailImageFromURL(selectedURI);

                    //step2: get video
                    File toSaveVideoFile = UIHelper.saveVideoToCaches(AppContext.getAppContext(),selectedURI);

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

                } else {   //images

                    beginCrop(data.getData());

                }
            } else {
            }
        }


    }

    private void beginCrop(Uri source) {
        Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        new Crop(source).output(outputUri).asSquare().start(getActivity(),CardDetailFragment.this);
    }

    private void handleCrop(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
        }

        Uri selectedURI = Crop.getOutput(data);

        Bitmap resultBitmap = null;

        //step1: get image
        try {
            resultBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedURI));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //step2: do next
        if (resultBitmap == null) {
            Log.e(Global.debugTag, "resultBitmap is null");
        } else {

            File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);

            if (mActiveImageSource == IMAGE_SOURCE.IMAGE_SOURCE_IS_LOGO) {
                mLogoImage.setImageBitmap(resultBitmap);
                mCurrentPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                if (mIsCreatingCard == false) {
                    mCurrentPack.save(AppContext.getAppContext());
                    ((MainActivity) getActivity()).setMaskButtonForContentUpdating();
                    takeSnapshotAll();
                }

            } else if (mActiveImageSource == IMAGE_SOURCE.IMAGE_SOURCE_IS_IMAGE) {

                if (mIsImage2Active) {
                    mImage2.setImageBitmap(resultBitmap);
                    if (mIsQuestionShowing) {
                        mCurrentCard.question.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    } else {
                        mCurrentCard.answer.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    }
                } else {
                    mImage.setImageBitmap(resultBitmap);
                    if (mIsQuestionShowing) {
                        mCurrentCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    } else {
                        mCurrentCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
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
            } else if (mActiveImageSource == IMAGE_SOURCE.IMAGE_SOURCE_IS_BACKGROUND) {
                setCardBackgroundImageWithBitmap(resultBitmap);
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
            }
        }

    }

    /*
    通过uri，获取video的thumbnail
     */
    private void thumbnailImageFromURL (Uri selectedURI) {

        Bitmap resultBitmap = UIHelper.getVideoThumbnail(AppContext.getAppContext(),selectedURI);

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
                    switchToQuestionView(false);
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
                .setTitle("Select")
                .setMessage("Image/video selection")
                .setPositiveButton("Insert a YouTube url", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showYoutubeLinkageInputDialog();
                    }
                })
                .setNegativeButton("Select from library", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActiveImageSource = IMAGE_SOURCE.IMAGE_SOURCE_IS_IMAGE;
                        selectImageOrVideoFromLibrary();
                    }
                })
                .show();
    }


    /*
    配置logo imageview的click listner
     */
    private void configureLogoImageView() {

        mLogoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mIsPlayingCard == false) {
                    if (isEditableMode()) {
                        mActiveImageSource = IMAGE_SOURCE.IMAGE_SOURCE_IS_LOGO;
                        Crop.pickImageWithFragment(CardDetailFragment.this, true);
//                        startActivityForResult(
//                                new Intent(
//                                        Intent.ACTION_PICK,
//                                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
//                                CODE_REQUEST_IMAGE_SOURCE_IS_LOGO);

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
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentPack.logoURL)));
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Alert")
                                .setMessage("Uncorrect website or mail address")
                                .setPositiveButton("OK", null)
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
                    Toast.makeText(getActivity(),"You can only edit card that you have created it.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     配置background change imageview的click listner
     */
    private void configureBackgroundChangeImageView() {
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

                    if (string.length() >0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Edit/Remove")
                                .setPositiveButton("Remove background image", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mIsQuestionShowing) {
                                            mCurrentCard.question.backgroundImageUriFormatStr = "";
                                        } else {
                                            mCurrentCard.answer.backgroundImageUriFormatStr = "";
                                        }
                                        setCardBackgroundImageDefault();
                                    }
                                })
                                .setNegativeButton("Change background image", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mActiveImageSource = IMAGE_SOURCE.IMAGE_SOURCE_IS_BACKGROUND;
                                        Crop.pickImageWithFragment(CardDetailFragment.this, true);
//                                        startActivityForResult(
//                                                new Intent(
//                                                        Intent.ACTION_PICK,
//                                                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
//                                                CODE_REQUEST_IMAGE_SOURCE_IS_BACKGROUND);
                                    }
                                })
                                .show();
                    } else {
                        mActiveImageSource = IMAGE_SOURCE.IMAGE_SOURCE_IS_BACKGROUND;
                        Crop.pickImageWithFragment(CardDetailFragment.this, true);
//                        startActivityForResult(
//                                new Intent(
//                                        Intent.ACTION_PICK,
//                                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
//                                CODE_REQUEST_IMAGE_SOURCE_IS_BACKGROUND);
                    }

                } else {
                    Toast.makeText(getActivity(),"You can only edit card that you have created it.",Toast.LENGTH_LONG).show();
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

        if ((!mIsPlayingCard)&&(isEditableMode())) {  // popup a choice dialog: youtube linkage or library
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
                        Toast.makeText(getActivity(),"Video play is only available in play mode",Toast.LENGTH_LONG).show();

                    }
                });

            }
        }

        //step2: configure image2

        if ((!mIsPlayingCard)&&(isEditableMode())) {  // popup a choice dialog: youtube linkage or library
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
                        Toast.makeText(getActivity(),"Video play is only available in play mode",Toast.LENGTH_LONG).show();

                    }
                });

            }
        }

    }

    /**
     配置template change imageview的click listner
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
        questionQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateNotification(pos);
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
        answerQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateNotification(pos);
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
                        Toast.makeText(getActivity(),"You can only edit card that you have created it.",Toast.LENGTH_LONG).show();
                    }

                }
            });
        }



    }

    /**
     配置logo url view的click listner
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
                        .setTitle(R.string.logourl_title)
                        .setMessage(R.string.logourl_message)
                        .setView(inputEditText)
                        .setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
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
                        .setNegativeButton(R.string.button_cancel, null)
                        .show();
            }
        });

    }

    /**
     *打开录制声音的view
     */
    private void showCreateSoundView() {

        CreateSoundFragment dialogFragment = new CreateSoundFragment();
        dialogFragment.mIsCreatingCard = mIsCreatingCard;
        dialogFragment.mCurrentCard = mCurrentCard;
        dialogFragment.mCurrentPack = mCurrentPack;
        dialogFragment.mIsQuestionShowing = mIsQuestionShowing;
        dialogFragment.show(getActivity().getFragmentManager(), "create_sound_fragment");
    }

    private void changeTemplateNotification(int index) {
        if (mIsQuestionShowing) {
            mCurrentCard.question.templateID = index;
            switchToQuestionView(true);

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
                                saveEdittedCard();
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
            switchToQuestionView(false);
            mIsQuestionShowing = true;
        }

        if (mIsPlayingCard) {
            disableCardEditable();
        }


    }

    /**
     * *Set public since play modes need it
     * @param excludeTitle,mTitle will trigger takeSnapAll function is it is set
     */
    public void switchToQuestionView(boolean excludeTitle) {
        mIsSwitchingQuestionAnswerView = true;
        mIsQuestionShowing = true;
        if (!excludeTitle) {
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
     * @param excludeTitle,mTitle will trigger takeSnapAll function is it is set
     */
    private void switchToAnswerView(boolean excludeTitle) {

        mIsSwitchingQuestionAnswerView = true;
        mIsQuestionShowing = false;
        if (!excludeTitle) {
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



    private void getAllViews() {
        mSidebarTitle = (FCCEditText) mContentView.findViewById(R.id.sidebar_title);
        mSidebarBackground = (FrameLayout) mContentView.findViewById(R.id.sidebar_background_linearlayout);
        mCardSN = (TextView) mContentView.findViewById(R.id.card_sn);

        mTitle = (FCCEditText) mContentView.findViewById(R.id.title);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (FCCEditText) mContentView.findViewById(R.id.creator);
        mJobTitle = (FCCEditText) mContentView.findViewById(R.id.job_title);

        LinearLayout creatorLayout = (LinearLayout) mContentView.findViewById(R.id.creator_layout);

        mContentBodyLeftLayout = (LinearLayout) mContentView.findViewById(R.id.content_body_left);
        mContentBodyLeftTextMainSubLayout =  (LinearLayout) mContentView.findViewById(R.id.content_body_left_text_main_sub_layout);
        mContentBodyLeftMainSubFrameLayout = (FrameLayout) mContentView.findViewById(R.id.content_body_left_B_C);

        mContentBodyRightFrameLayout = (FrameLayout) mContentView.findViewById(R.id.content_body_right);

        mContentBodyType1 = (LinearLayout) mContentView.findViewById(R.id.content_body_type1);
        mContentBodyType2 = (LinearLayout) mContentView.findViewById(R.id.content_body_type2);


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


        mSubheading.mCallbacks = this;
        mMain.mCallbacks = this;
        mSub.mCallbacks = this;

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

    /*
    由于mSubheading，mMain，mSub在不同的template下，指向的view不一样，所以每次更改template时，则都需要调用
     */
    private void updateContentViewsPointers(int templateID) {

        //1. 重新定向
        if ((mContentBodyType1.getVisibility() == View.VISIBLE)) {
            mSubheading = (FCCEditText) mContentView.findViewById(R.id.subheading);
            mMain = (FCCEditText) mContentView.findViewById(R.id.main);

            if (mIsQuestionShowing) {
                if (templateID == 11) {
                    mSub = (FCCEditText) mContentView.findViewById(R.id.sub_replace_image);
                } else {
                    mSub = (FCCEditText) mContentView.findViewById(R.id.sub);
                }
            } else {
                if (templateID == 9) {
                    mSub = (FCCEditText) mContentView.findViewById(R.id.sub_replace_image);
                } else {
                    mSub = (FCCEditText) mContentView.findViewById(R.id.sub);
                }
            }

            mImage = (ImageView) mContentView.findViewById(R.id.image);
            mImage2 = (ImageView) mContentView.findViewById(R.id.image2);
            mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));
            mImage2.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr2));

        } else {
            mSubheading = (FCCEditText) mContentView.findViewById(R.id.subheading_BodyType2);
            mMain = (FCCEditText) mContentView.findViewById(R.id.main_BodyType2);
            mSub = (FCCEditText) mContentView.findViewById(R.id.sub_BodyType2);
            mImage = (ImageView) mContentView.findViewById(R.id.image_BodyType2);
            mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));
        }

        //2. text的重新OnTouchListener
        if (!mIsPlayingCard) {
            mSubheading.setOnTouchListener(this);
            mMain.setOnTouchListener(this);
            mSub.setOnTouchListener(this);

        }

        //3. text的重新 EditorActionListener
        setEditTextListener();

        //4. text的重新 OnKeyboardCloseListener
        mSubheading.mCallbacks = this;
        mMain.mCallbacks = this;
        mSub.mCallbacks = this;

        //5. image的重新OnClickListener
        setImageVideoClickListener();


        //TODO:由于重新指向，一些格式化数据比如size,font会丢失掉，所以需要后续修正

        //6. 数据重新填充
        if (mIsQuestionShowing) {
            mSubheading.setText(mCurrentCard.question.subheading);
            mMain.setText(mCurrentCard.question.main);
            mSub.setText(mCurrentCard.question.sub);

        } else {
            mSubheading.setText(mCurrentCard.answer.subheading);
            mMain.setText(mCurrentCard.answer.main);
            mSub.setText(mCurrentCard.answer.sub);
        }




    }


    private void triggerResizeTextToFitFrame(final EditText v) {

        synchronized (v) {
            if (v.getText().length() == 0) {
                return;
            }

            int noOfLines = v.getLineCount(); //this is very important, when setTextSize execute, getLineCount could possibly be zero
            int textHeight = noOfLines * v.getLineHeight();
            int viewHeight = v.getHeight();
            int lineHeight = v.getLineHeight();
            if ((textHeight > viewHeight) && (viewHeight > 1) && (noOfLines > 0)) {

                int cursorPosition = v.getSelectionStart();

                if ((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) {
                    // resize action
                    float textSize = v.getTextSize();
                    float newTextSize = 0;

                    if (textSize >200) {
                        newTextSize =  v.getTextSize() - 30;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX,newTextSize);

                    } else if ((textSize >100) && (textSize <= 200)){
                        newTextSize =  v.getTextSize() - 20;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX,newTextSize);
                    } else if ((textSize >50) && (textSize <= 100)) {
                        newTextSize =  v.getTextSize() - 5;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX,newTextSize);
                    } else if ((textSize >30) && (textSize <= 50)) {
                        newTextSize =  v.getTextSize() - 2;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX,newTextSize);
                    } else if (textSize <= 30) {
                        newTextSize =  v.getTextSize() - 1;
                        v.setTextSize(TypedValue.COMPLEX_UNIT_PX,newTextSize);
                    } else {
                        newTextSize =  v.getTextSize();
                    }

                    Log.d(Global.debugTag2, "Reading to resize" + v.getText().toString());

                    mIsSaveNeededAfterResize = true;


                } else {

                    if (textHeight < viewHeight + lineHeight) {
                        //we only do this during editable mode
                        String text = v.getText().toString();
                        int index = text.length() - 1;
                        Log.d(Global.debugTag, text + index);
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

                //恢复可见性
                v.setVisibility(View.VISIBLE);

                //仅在如下情况起作用：
                //1. read only
                //2. mIsSaveNeededAfterResize
                //3. 不再question/answer切换中
                if (((mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) == false) && (mIsSaveNeededAfterResize) && (mIsSwitchingQuestionAnswerView == false)) {
                    //mIsSaveNeededAfterResize = false;，不能置false，因为我们在onstop时需要写入数据库

                    prepareToSavingTextFontSizeInfo();

                    Log.d(Global.debugTag2, "keep data after triggerResizeTextToFitFrame in onStop.CardSN=" + mCurrentCard.cardSN);
                }


            }
        }

    }


    private void setEditTextListener() {

        Log.d(Global.debugTag, "setEditTextListener in CardDetailFragment is called, cardSN=" + mCurrentCard.cardSN);

        //由于需要字体自适应，自适应的过程会在界面显示出（字体变大或变小），这种体验不好，所以先hide
        if (mMain.getText().toString().length() >0) {
            mMain.setVisibility(View.INVISIBLE);
        }
        if (mSubheading.getText().toString().length() >0) {
            mSubheading.setVisibility(View.INVISIBLE);
        }
        if (mSub.getText().toString().length() >0) {
            mSub.setVisibility(View.INVISIBLE);
        }

        if (mIsPlayingCard == false) {
            mSidebarTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEdittedCard();
                    }
                    return false;
                }
            });
            mCreator.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEdittedCard();
                    }
                    return false;
                }
            });
            mJobTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEdittedCard();
                    }
                    return false;
                }
            });

            mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveEdittedCard();
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
                    Log.d(Global.debugTag, "mTitle has changed");
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

                    Log.d(Global.debugTag, "mCreator has changed");

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

                    Log.d(Global.debugTag, "mJobTitle has changed");

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

                    Log.d(Global.debugTag, "mSidebarTitle has changed");

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
                if (mIsQuestionShowing) {
                    mCurrentCard.question.subheading = mSubheading.getText().toString();
                } else {
                    mCurrentCard.answer.subheading = mSubheading.getText().toString();
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mSubheading);
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
                if (mIsQuestionShowing) {
                    mCurrentCard.question.main = mMain.getText().toString();
                } else {
                    mCurrentCard.answer.main = mMain.getText().toString();
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mMain);
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
                if (mIsQuestionShowing) {
                    mCurrentCard.question.sub = mSub.getText().toString();
                } else {
                    mCurrentCard.answer.sub = mSub.getText().toString();
                }

                if (isEditableMode() == false) {
                    triggerResizeTextToFitFrame(mSub);
                }

            }
        };
        mSub.addTextChangedListener(mSubTextWatcher);


        mVtoSubheadingListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSubheading);
            }
        };
        mVtoSubheading = mSubheading.getViewTreeObserver();
        mVtoSubheading.addOnGlobalLayoutListener(mVtoSubheadingListener);

        mVtoMainListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mMain);
            }
        };
        mVtoMain = mMain.getViewTreeObserver();
        mVtoMain.addOnGlobalLayoutListener(mVtoMainListener);

        mVtoSubListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSub);
            }
        };
        mVtoSub = mSub.getViewTreeObserver();
        mVtoSub.addOnGlobalLayoutListener(mVtoSubListener);

    }



    private void updateCommonContent() {
        if (mCurrentPack.sidebarTitle.contains("null")) {
            mSidebarTitle.setText("");
        } else {
            mSidebarTitle.setText(mCurrentPack.sidebarTitle);
        }
        mCardSN.setText(String.format("%d", mCurrentCard.cardSN));

        mLogoImage.setImageURI(Uri.parse(mCurrentPack.logoImageUriFormatStr));
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
        mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));
        mImage2.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr2));

        if (mCurrentCard.question.backgroundImageUriFormatStr.length() >0) {
            setCardBackgroundImageWithUri(mCurrentCard.question.backgroundImageUriFormatStr);
        } else {
            setCardBackgroundImageDefault();
        }

    }

    private void updateAnswerContent() {
        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));
        mImage2.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr2));

        if (mCurrentCard.answer.backgroundImageUriFormatStr.length() > 0) {
            setCardBackgroundImageWithUri(mCurrentCard.answer.backgroundImageUriFormatStr);
        } else {
            setCardBackgroundImageDefault();
        }

    }


    /**
     * Apply according to card editable or not
     */
    private void configureCardStatus() {

        if (isEditableMode()) {
            enableCardEditable();
        } else {
            disableCardEditable();
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
     * to initialized card during creating a new card
     */
    private void initilizeNewCard() {
        mCurrentCard = new Card();
        mCurrentCard.packID = mCurrentPack.packID;
        mCurrentCard.cardSN = mCurrentPack.cards.size() + 1;
        mCurrentCard.cardID = Global.generateNoRepeatInt();
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

        mCurrentPack.addCard(AppContext.getAppContext(),mCurrentCard);
        Log.d(Global.debugTag, "finish execution of saveNewCreatedCard");

        mIsTakeSnapshotAllNeeded = false;


    }


    /**
     * do save when editting curent card
     * do NOT save when creating a new card
     * do NOT refresh card list view
     */
    private void takeSnapshotCurrentCard() {

        boolean toggle = false;

        if (isEditableMode()) {
            disableCardEditable();
        }

        if (mIsQuestionShowing == false) {
            switchToQuestionView(false);
            toggle = true;
        }

        //hide logo image if its placeholder
        if (mCurrentPack.logoImageUriFormatStr.contains("placeholder") == true) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }

        View cardView = mContentView.findViewById(R.id.card);
        Bitmap bitmap = UIHelper.loadBitmapFromView(cardView);
        File savedFile = UIHelper.saveImageToCaches(bitmap);
        if(bitmap != null && !bitmap.isRecycled()) {
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
                Log.w(Global.debugTag, "Out of range");
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


    private void updateQuestionViewTemplate() {

        int templateID = mCurrentCard.question.templateID;

        LinearLayout.LayoutParams paramsA = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
        paramsA.weight = 0f;
        mContentBodyRightFrameLayout.setLayoutParams(paramsA);

        LinearLayout.LayoutParams paramsB = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
        paramsB.weight = 710f;
        mContentBodyLeftLayout.setLayoutParams(paramsB);

        //we don't need to set font size here since it will be done in CSS constructor


        LinearLayout.LayoutParams params;
        switch (templateID) {
            case 0:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //no image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 330;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 330;
                mMain.setLayoutParams(params);

                //5.sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 1:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. no image
                mImage.setVisibility(View.INVISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  getResources().getDimensionPixelSize(R.dimen.question_template_1_margin_right);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 320;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 160;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 160;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 2:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. not show image
                mImage.setVisibility(View.INVISIBLE);

                //3. suheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 280;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 100;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 3:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //not show image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 390;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 200;
                mMain.setLayoutParams(params);

                //5. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 190;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 4:

                //1.type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //not show image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4.main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                mMain.setLayoutParams(params);

                //5. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 5:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 0f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 710f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 6:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 7:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2.left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 8:
                //1. type
                mContentBodyType1.setVisibility(View.INVISIBLE);
                mContentBodyType2.setVisibility(View.VISIBLE);
                updateContentViewsPointers(templateID);

                //use default in card.xml is OK

                //2:NOT show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 9:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2: left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3:subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5:show image2
                mImage2.setVisibility(View.VISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.INVISIBLE);
                break;

            case 10:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;


            case 11:

                //第一步是，更新sub，因为在这里sub的指向在updateContentViewsPointers发生了变化
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSub.setLayoutParams(params);

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2.left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.INVISIBLE);
                mSub.setVisibility(View.VISIBLE); //这时sub代替了image

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;


            default:
                Log.w(Global.debugTag, "mCurrentCard.question.templateID is out of scope");
        }

    }

    private void updateAnswerViewTemplate() {

        LinearLayout.LayoutParams paramsA = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
        paramsA.weight = 0f;
        mContentBodyRightFrameLayout.setLayoutParams(paramsA);

        LinearLayout.LayoutParams paramsB = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
        paramsB.weight = 710f;
        mContentBodyLeftLayout.setLayoutParams(paramsB);

        int templateID = mCurrentCard.answer.templateID;


        //we don't need to set font size here since it will be done in CSS construct

        LinearLayout.LayoutParams params;
        switch (templateID) {
            case 10:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //no image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 330;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 330;
                mMain.setLayoutParams(params);

                //5.sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 8:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. no image
                mImage.setVisibility(View.INVISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  getResources().getDimensionPixelSize(R.dimen.question_template_1_margin_right);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 320;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 160;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 160;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 7:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. not show image
                mImage.setVisibility(View.INVISIBLE);

                //3. suheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 280;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 100;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 6:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //not show image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 390;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 200;
                mMain.setLayoutParams(params);

                //5. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 190;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 3:

                //1.type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //not show image
                mImage.setVisibility(View.INVISIBLE);

                //2. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //3. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //4.main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                mMain.setLayoutParams(params);

                //5. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;
            case 5:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 0f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 710f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 0:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 4:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2.left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 1:
                //1. type
                mContentBodyType1.setVisibility(View.INVISIBLE);
                mContentBodyType2.setVisibility(View.VISIBLE);
                updateContentViewsPointers(templateID);

                //use default in card.xml is OK

                //2:NOT show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;

            case 11:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2: left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3:subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 70;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 360;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5:show image2
                mImage2.setVisibility(View.VISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.INVISIBLE);
                break;

            case 2:

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2. left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4. layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 215;
                mMain.setLayoutParams(params);

                //6. sub
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                //7. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;


            case 9:

                //第一步是，更新sub，因为在这里sub的指向在updateContentViewsPointers发生了变化
                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSub.setLayoutParams(params);

                //1. type
                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);
                updateContentViewsPointers(templateID);

                //2.left and right
                params = (LinearLayout.LayoutParams) mContentBodyLeftLayout.getLayoutParams();
                params.weight = 360f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftLayout.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mContentBodyRightFrameLayout.getLayoutParams();
                params.weight = 350f;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyRightFrameLayout.setLayoutParams(params);
                mImage.setVisibility(View.INVISIBLE);
                mSub.setVisibility(View.VISIBLE); //这时sub代替了image

                //3. subheading
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                params.rightMargin =  UIHelper.getPixels(4);
                mSubheading.setLayoutParams(params);

                //4.layout for main and sub
                params = (LinearLayout.LayoutParams) mContentBodyLeftMainSubFrameLayout.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                params.rightMargin =  UIHelper.getPixels(4);
                mContentBodyLeftMainSubFrameLayout.setLayoutParams(params);

                //5. main
                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                //6. not show image2
                mImage2.setVisibility(View.INVISIBLE);
                mContentBodyLeftTextMainSubLayout.setVisibility(View.VISIBLE);

                break;


            default:
                Log.w(Global.debugTag, "mCurrentCard.answer.templateID is out of scope");
        }
    }

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
            scaleVal = (float)1.0;
        }

        mSubheading.setTextSize((mCurrentCard.question.css.subheadingSize *scaleVal));
        mMain.setTextSize((mCurrentCard.question.css.mainSize *scaleVal));
        mSub.setTextSize((mCurrentCard.question.css.subSize * scaleVal));

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subColor));


        mSubheading.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.subheadingFont),Typeface.BOLD);
        mMain.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.mainFont),Typeface.BOLD);
        mSub.setTypeface(FontHelper.fontFromName(getActivity(), mCurrentCard.question.css.subFont),Typeface.BOLD);
    }

    private void updateAnswerCSS() {

        float scaleVal;
        if (mIsPlayingCard) {
            scaleVal = Global.scaleInPlayMode;
        } else {
            scaleVal = (float)1.0;
        }

        mTitle.setTextColor(Color.RED);

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign ) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subheadingAlignVertical));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.mainAlignVertical));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign) | StringUtils.convertVerticalGravityStringToInt(mCurrentCard.answer.css.subAlignVertical));

        //step2: size
        mSubheading.setTextSize((mCurrentCard.answer.css.subheadingSize *scaleVal));
        mMain.setTextSize((mCurrentCard.answer.css.mainSize *scaleVal));
        mSub.setTextSize((mCurrentCard.answer.css.subSize *scaleVal));

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subColor));


        mSubheading.setTypeface(FontHelper.fontFromName(getActivity(),mCurrentCard.answer.css.subheadingFont),Typeface.BOLD);
        mMain.setTypeface(FontHelper.fontFromName(getActivity(),mCurrentCard.answer.css.mainFont),Typeface.BOLD);
        mSub.setTypeface(FontHelper.fontFromName(getActivity(),mCurrentCard.answer.css.subFont),Typeface.BOLD);
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
        ((MainActivity) getActivity()).prepareSnapShotAllExceptOne(mCurrentPack, mCurrentCard);
    }


    public void updateCSS(int menuID, int subMenuID) {
        CSS currentCSS;

        if ((mCurrentFocusedCardContentText == null) || (mCurrentFocusedCardContentText.getTag() == null)) {
            Log.e(Global.debugTag,"mCurrentFocusedCardContentText or mCurrentFocusedCardContentText.getTag()  is null during execution on updateCSS");
            return;
        }


        //Step2: determine operaton target
        int editTextTag = Integer.parseInt((String) mCurrentFocusedCardContentText.getTag());
        if (mIsQuestionShowing) {
            currentCSS = mCurrentCard.question.css;
        } else {
            currentCSS = mCurrentCard.answer.css;
        }




        //Step3: fill values
        String[] sizeArray = ScaleHelper.realSizeArray(getActivity()); //我们不能从R.array.css_size获取，因为它仅仅是名义值，而不是真实的值


        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        String[] fontArray = getResources().getStringArray(R.array.css_font);
        switch (menuID) {
            case 0:   //stand for align

                //由于CSS存储时，只有两个值Vertical或者空，所以对于Vertical Alignment，我们需要特殊化一下（兼容ios）
                boolean isVerticalAlign = false;
                if (subMenuID == 3) {
                    isVerticalAlign = true;
                    alignArray[subMenuID +1] = "Vertical";
                } else if (subMenuID == 4) {
                    isVerticalAlign = true;
                    alignArray[subMenuID +1] = "";
                }

                int horizontalGravity;
                int verticallGravity;

                if (editTextTag == 1001) {
                    if (isVerticalAlign) {
                        currentCSS.subheadingAlignVertical = alignArray[subMenuID +1];
                    } else {
                        currentCSS.subheadingAlign = alignArray[subMenuID +1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.subheadingAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.subheadingAlignVertical);

                } else if (editTextTag == 1002) {
                    if (isVerticalAlign) {
                        currentCSS.mainAlignVertical = alignArray[subMenuID +1];
                    } else {
                        currentCSS.mainAlign = alignArray[subMenuID +1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.mainAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.mainAlignVertical);

                } else if (editTextTag == 1003) {
                    if (isVerticalAlign) {
                        currentCSS.subAlignVertical = alignArray[subMenuID +1];
                    } else {
                        currentCSS.subAlign = alignArray[subMenuID +1];
                    }

                    horizontalGravity = StringUtils.convertGravityStringToInt(currentCSS.subAlign);
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt(currentCSS.subAlignVertical);
                } else {
                    horizontalGravity = StringUtils.convertGravityStringToInt("");
                    verticallGravity = StringUtils.convertVerticalGravityStringToInt("");
                }

                mCurrentFocusedCardContentText.setGravity(horizontalGravity|verticallGravity);

                break;

            case 1:   //stand for size

                float size = Float.parseFloat(sizeArray[subMenuID]);  //是个纯font size的数组，不带Size描述

                //you can find the tag definition(1001,1002,1003) in card.xml
                if (editTextTag == 1001) {
                    currentCSS.subheadingSize = size;
                } else if (editTextTag == 1002) {
                    currentCSS.mainSize = size;
                } else if (editTextTag == 1003) {
                    currentCSS.subSize = size;
                }

                double scaleVal;
                if (mIsPlayingCard) {
                    scaleVal = Global.scaleInPlayMode;
                } else {
                    scaleVal = 1.0;
                }




                mCurrentFocusedCardContentText.setTextSize((int)(size *scaleVal));

                break;
            case 2:   //stand for color

                if (editTextTag == 1001) {
                    currentCSS.subheadingColor = colorArray[subMenuID +1];
                } else if (editTextTag == 1002) {
                    currentCSS.mainColor = colorArray[subMenuID +1];
                } else if (editTextTag == 1003) {
                    currentCSS.subColor = colorArray[subMenuID +1];
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
                        Log.w(Global.debugTag, "Out of range of subMenuID");
                }
                break;
            case 3:   //font



                if (editTextTag == 1001) {
                    currentCSS.subheadingFont = fontArray[subMenuID +1];
                } else if (editTextTag == 1002) {
                    currentCSS.mainFont = fontArray[subMenuID +1];
                } else if (editTextTag == 1003) {
                    currentCSS.subFont = fontArray[subMenuID +1];
                }

                mCurrentFocusedCardContentText.setTypeface(FontHelper.fontFromArrayIndex(AppContext.getAppContext(),subMenuID));

                break;
            default:
                Log.w(Global.debugTag, "Out of range of menuID");
        }


        if (!mIsCreatingCard) {
            if (mIsQuestionShowing) {
                mCurrentCard.question.css.save(AppContext.getAppContext());
            } else {
                mCurrentCard.answer.css.save(AppContext.getAppContext());
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.d(Global.debugTag, "onTouch happened, event.getAction=" + event.getAction());


        if ((v.getTag() != null) && (event.getAction() == MotionEvent.ACTION_DOWN)) {

            ((MainActivity) getActivity()).mIsKeyboardVisible = true;
            ((MainActivity) getActivity()).setAsKeyboardStatus();

            int tag = Integer.parseInt((String) v.getTag());

            if ((tag == 1001) || (tag == 1002) || (tag == 1003)) {


                //this is quite a trick in order to make EditText scrollable
                EditText text = (EditText) v;
                int lineHeight = text.getLineHeight();
                int lineCount = text.getLineCount();
                int textHeight = text.getHeight();
                int maxNoOfLines = (2* textHeight)/lineHeight;
                int addedNoOfLines = maxNoOfLines - lineCount;

                if ((addedNoOfLines > 0) && (lineCount > 1) && (lineCount * lineHeight < 2 *textHeight)) {

                    String addedStr = "";
                    for (int i = 0; i< addedNoOfLines; i ++) {
                        addedStr = addedStr + "\n";
                    }
                    text.setText(text.getText().toString() + addedStr);
                }

                //check card.xml for tag
                ((MainActivity) getActivity()).mIsEdittingCard = true;

                ((MainActivity) getActivity()).prepareCSSToolbar();
                ((MainActivity) getActivity()).showCSSToolbar();

                mCurrentFocusedCardContentText = (EditText) v;
            }
            else {
                ((MainActivity)getActivity()).removeCSSToolbar();
            }
        }

        return false; //don't set to false;
    }

    /**
     * put save here when editting a current card
     * put save in saveNewCreatedCard when creating a new card
     */
    @Override
    public void onKeyboardClose(EditText editText) {
        if (mIsPlayingCard == false) {
            ((MainActivity) getActivity()).removeCSSToolbar();
        }
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


    /**
     * simply close keyboard and do nothing
     */
    public void dismissKeyboard() {
        if (mCurrentFocusedCardContentText != null) {
            mIMM.hideSoftInputFromWindow(mCurrentFocusedCardContentText.getWindowToken(), 0);
        } else {
            Log.d(Global.debugTag, "mCurrentFocusedCardContentText is null");
        }

    }


    /**
     * simply close keyboard and do nothing
     */
    public void dismissKeyboard2() {
        if (mCurrentFocusedCardContentText != null) {
            mIMM.hideSoftInputFromInputMethod(mCurrentFocusedCardContentText.getWindowToken(), 0);
        } else {
            Log.d(Global.debugTag, "mCurrentFocusedCardContentText is null");
        }

    }

    public void saveEdittedCard() {

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

    public void onGridViewItemClicked(int index) {
        Log.d(Global.debugTag,"index of symobol/emotion is:" + index);
        int start = mCurrentFocusedCardContentText.getSelectionStart();

        String beforeString = mCurrentFocusedCardContentText.getText().toString().substring(0,start);
        String afterString = mCurrentFocusedCardContentText.getText().toString().substring(start);

        mCurrentFocusedCardContentText.setText(beforeString + SymbolHelper.mUnicodeArray[index] + afterString);

        Log.d(Global.debugTag,"the result is:" + mCurrentFocusedCardContentText.getText().toString());

        mCurrentFocusedCardContentText.setSelection(start +1);

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

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        setCardBackgroundImageWithBitmap(bitmap);

    }


    private void setCardBackgroundImageDefault () {
        RoundedBottomRightImageView backgroundImageView = (RoundedBottomRightImageView) mContentView.findViewById(R.id.card_background_image);
        backgroundImageView.setImageBitmap(null);

    }

    //used in edit card mode
    private void setCardBackgroundMaskGray () {
        View card = mContentView.findViewById(R.id.card_mask);
        card.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_gray));
    }

    //used in play mode and create card mode
    private void setCardBackgroundMaskBlack () {
        View card = mContentView.findViewById(R.id.card_mask);
        card.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_black));
    }


    public ArrayList textToSpeechContentArray() {
        ArrayList<String> arrayList = new ArrayList<String>();

        if (mIsQuestionShowing) {

            Question question = mCurrentCard.question;
            if (question.subheading.length() >0) {

                arrayList.add(replaceBasicSymbol(question.subheading));
            }
            if (question.main.length() >0) {
                arrayList.add(replaceBasicSymbol(question.main));
            }
            if (question.sub.length() >0) {
                arrayList.add(replaceBasicSymbol(question.sub));
            }

        } else {
            Answer answer = mCurrentCard.answer;
            if (answer.subheading.length() >0) {
                arrayList.add(replaceBasicSymbol(answer.subheading));
            }
            if (answer.main.length() >0) {
                arrayList.add(replaceBasicSymbol(answer.main));
            }
            if (answer.sub.length() >0) {
                arrayList.add(replaceBasicSymbol(answer.sub));
            }
        }

        return arrayList;
    }



    private String replaceBasicSymbol (String str) {
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

        resultStr = str.replace("+",plusStr);
        resultStr = resultStr.replace("⨯",timesStr);
        resultStr = resultStr.replace("÷",dividedByStr);
        resultStr = resultStr.replace("-",minusStr);

        resultStr = resultStr.replace("cm²",squareCentimetresStr);
        resultStr = resultStr.replace("cm³",cubicCentimetresStr);
        resultStr = resultStr.replace("mm²",squareMillimetresStr);
        resultStr = resultStr.replace("mm³",cubicMillimetresStr);


        resultStr = resultStr.replace("m³",cubicMetresStr);
        resultStr = resultStr.replace("m²",squareMetresStr);
        resultStr = resultStr.replace("ft²",squareFeetStr);

        resultStr = resultStr.replace("ft³",cubicFeetStr);
        resultStr = resultStr.replace("in²",squareInchesStr);
        resultStr = resultStr.replace("in³",cubicInchesStr);

        resultStr = resultStr.replace("°C",degreesCelsiusStr);

        resultStr = resultStr.replace("°F",degreesFahrenheitStr);
        resultStr = resultStr.replace("°R",degreesRankinStr);
        resultStr = resultStr.replace("°K",degresssKelvinStr);
        resultStr = resultStr.replace("CO₂",carbonDioxideStr);

        resultStr = resultStr.replace("N₂",nitrogenStr);
        resultStr = resultStr.replace("O₂",oxygenStr);
        resultStr = resultStr.replace("π",pieStr);
        resultStr = resultStr.replace("r²",squareRdiusStr);

        resultStr = resultStr.replace("O₃",OzoneStr);
        resultStr = resultStr.replace("∕",perStr);
        resultStr = resultStr.replace("/",perStr);//not unicode
        resultStr = resultStr.replace("mb=",millibarStr);//mb = millibar
        resultStr = resultStr.replace("mb =",millibarStr);//mb = millibar
        resultStr = resultStr.replace("%",percentStr); //not unicode

        resultStr = resultStr.replace("r=",radiusStr);
        resultStr = resultStr.replace("r =",radiusStr);
        resultStr = resultStr.replace("d=",diameterStr);
        resultStr = resultStr.replace("d =",diameterStr);
        resultStr = resultStr.replace(">",greaterThenStr);
        resultStr = resultStr.replace("<",lessThenStr);

        resultStr = resultStr.replace("√",squareRootStr);

        resultStr = resultStr.replace(" = ",equalsStr);// , not unicode  //need to put the last


        return resultStr;

    }


}


