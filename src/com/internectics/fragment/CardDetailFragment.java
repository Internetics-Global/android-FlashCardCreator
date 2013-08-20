package com.internectics.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.internectics.UI.FCCEditText;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.android_flashcardcreator.WebViewActivity;
import com.internectics.data.CSS;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackRecordHelper;
import com.internectics.helper.SymbolHelper;
import com.internectics.util.*;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;

public class CardDetailFragment extends Fragment implements FCCEditText.OnKeyboardCloseListener, FCCEditText.OnTouchListener {

    public Card mCurrentCard;
    public Pack mCurrentPack;

    public View mContentView;


    private LinearLayout mContentBodyType1;
    private LinearLayout mContentBodyType2;

    private LinearLayout mContentBodyLeft;
    private FCCEditText mSidebarTitle;
    private FrameLayout mSidebarBackground;
    public  TextView mCardSN;
    private FCCEditText mTitle;
    private LinearLayout mTitleBackground;
    private FCCEditText mCreator;
    private FCCEditText mSubheading;
    private FCCEditText mMain;
    private FCCEditText mSub;
    private ImageView mImage;
    private FCCEditText mSubheading2;
    private FCCEditText mMain2;
    private FCCEditText mSub2;
    private ImageView mImage2;
    private ImageView mLogoImage;
    private ImageView mChangeTemplateImage;
    private ImageView mLogoURLImage;
    private RadioButton mQuestionRadioButton;
    private RadioButton mAnswerRadioButton;
    private RadioGroup mRadioGroup;

    private InputMethodManager mIMM;
    private EditText mCurrentFocusedCardContentText;  // only applicable to subheading, main and sub text

    private int CODE_REQUEST_IMAGE_SOURCE_IS_LOGO = 1001; //when user click on the logo img
    private int CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE = 1002;//when user click on the image img

    public boolean mIsCreatingCard = false;
    private boolean mIsPlayingCard = false;

    private boolean mIsSnapShotNotCurrent = false;//as to snapshot,we have different stragegy on current showing card and other cards

    private boolean mIsQuestionShowing = true;

    private boolean mIsTakeSnapshotAllNeeded = false; //when fields that belong to current pack(like title) changes, it will be set true

    private static int mSemaphore = 0; //used to indicate all snapshots are done

    private static boolean isSaveNeededAfterResize = false;


    private Handler myHandler;


    /**
     * @param currentPack
     * @param currentCard
     * @param source:     1, create new card; 2, playing card; 3.
     */
    public CardDetailFragment(Pack currentPack, Card currentCard, int source) {

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

        mIMM = (InputMethodManager) (getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

        if (mIsPlayingCard) {
            //need to hide queston/answer segment raido group
            mContentView = inflater.inflate(R.layout.card, container, false);
        } else {
            mContentView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        }

        getAllViews();
        setEditTextListener();

        if (!mIsPlayingCard) {
            configureSegmentView();
        }
        configureChangeTemplateView();
        configureLogoURLView();

        mLogoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mIsPlayingCard == false) {
                    if (isEditableMode()) {
                        startActivityForResult(
                                new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                CODE_REQUEST_IMAGE_SOURCE_IS_LOGO);
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
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCurrentPack.logoURL)));
                    }
                }

            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE);

            }
        });

        mImage2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE);

            }
        });


        if (mIsSnapShotNotCurrent == true) {
            mContentView.setVisibility(View.INVISIBLE);

            ViewDidAppearTask dTask = new ViewDidAppearTask();
            dTask.execute(100);
        }

        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "DejaVuSans.ttf");
        mSubheading.setTypeface(typeFace,Typeface.BOLD);
        mMain.setTypeface(typeFace,Typeface.BOLD);
        mSub.setTypeface(typeFace,Typeface.BOLD);
        mSubheading2.setTypeface(typeFace,Typeface.BOLD);
        mMain2.setTypeface(typeFace,Typeface.BOLD);
        mSub2.setTypeface(typeFace,Typeface.BOLD);

        return mContentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Global.debugTag, "onViewCreated in CardDetailFragment is called");

        updateCommonContent();
        switchToQuestionView();

        if (mIsPlayingCard) {
            disableCardEditable();
        } else {
            configureCardStatus();
        }


        //we have to disable because of performance issue
        if (mIsCreatingCard) {
            mSidebarTitle.setEnabled(false);
            mTitle.setEnabled(false);
            mCreator.setEnabled(false);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        mIsTakeSnapshotAllNeeded = false;  //necessary

        Log.d(Global.debugTag, "onResume in CardDetailFragment");
    }


    @Override
    public void onDestroy() {
        Log.d(Global.debugTag, String.format("onDestroy in CardDetailFragment, cardSN = %d",mCurrentCard.cardSN));
        super.onDestroy();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImageURI = data.getData();

            Bitmap resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
            if (resultBitmap == null) {
                Log.w(Global.debugTag, "resultBitmap is null");
            } else {
                File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);

                if (requestCode == CODE_REQUEST_IMAGE_SOURCE_IS_LOGO) {
                    mLogoImage.setImageBitmap(resultBitmap);
                    mCurrentPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                    if (mIsCreatingCard == false) {
                        mCurrentPack.save(AppContext.getAppContext());
                        ((MainActivity) getActivity()).setMaskButtonForContentUpdating();
                        takeSnapshotAll();
                    }

                } else if (requestCode == CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE) {
                    mImage.setImageBitmap(resultBitmap);
                    mImage2.setImageBitmap(resultBitmap);
                    if (mIsQuestionShowing) {
                        mCurrentCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    } else {
                        mCurrentCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    }

                    if (mIsCreatingCard == false) {
                        mCurrentCard.save(AppContext.getAppContext());
                        if (mIsQuestionShowing) {
                            Intent intent = new Intent();
                            intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE);
                            getActivity().sendBroadcast(intent);
                        }
                    }
                }
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
                    mQuestionRadioButton.setTextColor(Color.WHITE);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mAnswerRadioButton.setTextColor(Color.BLACK);
                    switchToQuestionView();
                } else {
                    mQuestionRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mQuestionRadioButton.setTextColor(Color.BLACK);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    mAnswerRadioButton.setTextColor(Color.WHITE);
                    switchToAnswerView();
                }
            }
        });
    }

    /**
     * Confugure template view view in onCreateView
     */
    private void configureChangeTemplateView() {
        ActionItem questionActionItem0 = new ActionItem(0, null, getResources().getDrawable(R.drawable.question_templatescreenshot0));
        ActionItem questionActionItem1 = new ActionItem(1, null, getResources().getDrawable(R.drawable.question_templatescreenshot1));
        ActionItem questionActionItem2 = new ActionItem(2, null, getResources().getDrawable(R.drawable.question_templatescreenshot2));
        ActionItem questionActionItem3 = new ActionItem(3, null, getResources().getDrawable(R.drawable.question_templatescreenshot3));
        ActionItem questionActionItem4 = new ActionItem(4, null, getResources().getDrawable(R.drawable.question_templatescreenshot4));
        ActionItem questionActionItem5 = new ActionItem(5, null, getResources().getDrawable(R.drawable.question_templatescreenshot5));

        ActionItem answerActionItem0 = new ActionItem(0, null, getResources().getDrawable(R.drawable.answer_templatescreenshot0));
        ActionItem answerActionItem1 = new ActionItem(1, null, getResources().getDrawable(R.drawable.answer_templatescreenshot1));
        ActionItem answerActionItem2 = new ActionItem(2, null, getResources().getDrawable(R.drawable.answer_templatescreenshot2));
        ActionItem answerActionItem3 = new ActionItem(3, null, getResources().getDrawable(R.drawable.answer_templatescreenshot3));
        ActionItem answerActionItem4 = new ActionItem(4, null, getResources().getDrawable(R.drawable.answer_templatescreenshot4));
        ActionItem answerActionItem5 = new ActionItem(5, null, getResources().getDrawable(R.drawable.answer_templatescreenshot5));

        final QuickAction questionQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);
        final QuickAction answerQuickAction = new QuickAction(getActivity(), QuickAction.VERTICAL);

        questionQuickAction.addActionItem(questionActionItem0);
        questionQuickAction.addActionItem(questionActionItem1);
        questionQuickAction.addActionItem(questionActionItem2);
        questionQuickAction.addActionItem(questionActionItem3);
        questionQuickAction.addActionItem(questionActionItem4);
        questionQuickAction.addActionItem(questionActionItem5);
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
        answerQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                changeTemplateNotification(pos);
            }
        });


        mChangeTemplateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsQuestionShowing) {
                    questionQuickAction.show(mChangeTemplateImage);
                } else {
                    answerQuickAction.show(mChangeTemplateImage);
                }

            }
        });

    }

    /**
     * Confugure logo url view in onCreateView
     */
    private void configureLogoURLView() {
        mLogoURLImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputEditText = new EditText(getActivity());
                inputEditText.setSingleLine(true);
                inputEditText.setText(mCurrentPack.logoURL);
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

    private void changeTemplateNotification(int index) {
        if (mIsQuestionShowing) {
            mCurrentCard.question.templateID = index;
            updateQuestionViewTemplate();

        } else {
            mCurrentCard.answer.templateID = index;
            updateAnswerViewTemplate();
        }


    }

    public void switchQuestionAnswerView() {

        if (mIsQuestionShowing) {
            switchToAnswerView();
            mIsQuestionShowing = false;
        } else {
            switchToQuestionView();
            mIsQuestionShowing = true;
        }
    }


    /**
     * Set public since play modes need it
     */
    public void switchToQuestionView() {
        mIsQuestionShowing = true;
        updateQuestionContent();
        updateQuestionViewTemplate();
        updateQuestionCSS();
    }


    private void switchToAnswerView() {
        mIsQuestionShowing = false;
        updateAnswerContent();
        updateAnswerViewTemplate();
        updateAnswerCSS();
    }



    private void getAllViews() {
        mSidebarTitle = (FCCEditText) mContentView.findViewById(R.id.sidebar_title);
        mSidebarBackground = (FrameLayout) mContentView.findViewById(R.id.sidebar_background_linearlayout);
        mCardSN = (TextView) mContentView.findViewById(R.id.card_sn);

        mTitle = (FCCEditText) mContentView.findViewById(R.id.title);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (FCCEditText) mContentView.findViewById(R.id.creator);

        mContentBodyLeft = (LinearLayout) mContentView.findViewById(R.id.content_body_left);

        mContentBodyType1 = (LinearLayout) mContentView.findViewById(R.id.content_body_type1);
        mContentBodyType2 = (LinearLayout) mContentView.findViewById(R.id.content_body_type2);

        mSubheading = (FCCEditText) mContentView.findViewById(R.id.subheading);
        mMain = (FCCEditText) mContentView.findViewById(R.id.main);
        mSub = (FCCEditText) mContentView.findViewById(R.id.sub);
        mImage = (ImageView) mContentView.findViewById(R.id.image);

        mSubheading2 = (FCCEditText) mContentView.findViewById(R.id.subheading2);
        mMain2 = (FCCEditText) mContentView.findViewById(R.id.main2);
        mSub2 = (FCCEditText) mContentView.findViewById(R.id.sub2);
        mImage2 = (ImageView) mContentView.findViewById(R.id.image2);

        mChangeTemplateImage = (ImageView) mContentView.findViewById(R.id.change_template_button);
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

        mSubheading2.mCallbacks = this;
        mMain2.mCallbacks = this;
        mSub2.mCallbacks = this;

        if (!mIsPlayingCard) {
            mSubheading.setOnTouchListener(this);
            mMain.setOnTouchListener(this);
            mSub.setOnTouchListener(this);
            mSubheading2.setOnTouchListener(this);
            mMain2.setOnTouchListener(this);
            mSub2.setOnTouchListener(this);
            mCreator.setOnTouchListener(this);
            mSidebarTitle.setOnTouchListener(this);
            mTitle.setOnTouchListener(this);
        }

        ViewTreeObserver vtoSubheading = mSubheading.getViewTreeObserver();
        vtoSubheading.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSubheading);
            }
        });


        ViewTreeObserver vtoMain = mMain.getViewTreeObserver();
        vtoMain.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mMain);
            }
        });

        ViewTreeObserver vtoSub = mSub.getViewTreeObserver();
        vtoSub.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSub);
            }
        });

        ViewTreeObserver vtoSubheading2 = mSubheading2.getViewTreeObserver();
        vtoSubheading2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSubheading2);
            }
        });


        ViewTreeObserver vtoMain2 = mMain2.getViewTreeObserver();
        vtoMain2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mMain2);
            }
        });

        ViewTreeObserver vtoSub2 = mSub2.getViewTreeObserver();
        vtoSub2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                triggerResizeTextToFitFrame(mSub2);
            }
        });






    }


    private void triggerResizeTextToFitFrame(final EditText v) {

        //Log.d("ccaa2", "Entering triggerResizeTextToFitFrame on EditText" + v.getText().toString());

        if (v.getText().length() == 0) {
            return;
        }

        int noOfLines = v.getLineCount(); //this is very important, when setTextSize execute, getLineCount could possibly be zero
        int textHeight = noOfLines * v.getLineHeight();
        //Log.d("ccaa2", String.format("before resizing textHeight=%d, v.getLineCount()=%d,v.getHeight() = %d",textHeight,noOfLines,v.getHeight()));

        if ((textHeight > v.getHeight()) && (v.getHeight() > 1) && (noOfLines > 0)) {

            //Log.d("ccaa2", String.format("Now executing textHeight=%d, v.getHeight=%d, v.getTextSize=%f",textHeight,v.getHeight(), v.getTextSize()));

            float textSize = v.getTextSize();

            if (textSize >200) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,(v.getTextSize() - 30));

            } else if ((textSize >100) && (textSize <= 200)){
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,(v.getTextSize() - 20));
            } else if ((textSize >50) && (textSize <= 100)) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,(v.getTextSize() - 5));
            } else if ((textSize >30) && (textSize <= 50)) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,(v.getTextSize() - 2));
            } else if (textSize <= 30) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX,(v.getTextSize() - 1));
            }

            isSaveNeededAfterResize = true;
        } else {
            if ((isSaveNeededAfterResize) && (mCurrentCard != null)) {

            }
        }
    }


    private void setEditTextListener() {

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

        mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveEdittedCard();
                }
                return false;
            }
        });


        mTitle.addTextChangedListener(new TextWatcher() {
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
        });

        mCreator.addTextChangedListener(new TextWatcher() {
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
        });

        mSidebarTitle.addTextChangedListener(new TextWatcher() {
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
        });


        mSubheading.addTextChangedListener(new TextWatcher() {
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

                Log.d(Global.debugTag, "mSubheading has changed");
                triggerResizeTextToFitFrame(mSubheading);
            }
        });

        mMain.addTextChangedListener(new TextWatcher() {
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

                Log.d(Global.debugTag, "mMain has changed");
                triggerResizeTextToFitFrame(mMain);
            }
        });

        mSub.addTextChangedListener(new TextWatcher() {
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

                Log.d(Global.debugTag, "mSub has changed");
                triggerResizeTextToFitFrame(mSub);
            }
        });


        mSubheading2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsQuestionShowing) {
                    mCurrentCard.question.subheading = mSubheading2.getText().toString();
                } else {
                    mCurrentCard.answer.subheading = mSubheading2.getText().toString();
                }

                Log.d(Global.debugTag, "mSubheading2 has changed");
                triggerResizeTextToFitFrame(mSubheading2);
            }
        });

        mMain2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsQuestionShowing) {
                    mCurrentCard.question.main = mMain2.getText().toString();
                } else {
                    mCurrentCard.answer.main = mMain2.getText().toString();
                }

                Log.d(Global.debugTag, "mMain2 has changed");
                triggerResizeTextToFitFrame(mMain2);
            }
        });

        mSub2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsQuestionShowing) {
                    mCurrentCard.question.sub = mSub2.getText().toString();
                } else {
                    mCurrentCard.answer.sub = mSub2.getText().toString();
                }

                Log.d(Global.debugTag, "mSub2 has changed");
                triggerResizeTextToFitFrame(mSub2);
            }
        });
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
        mTitle.setText(mCurrentPack.questionTitle);
        mSubheading.setText(mCurrentCard.question.subheading);
        mMain.setText(mCurrentCard.question.main);
        mSub.setText(mCurrentCard.question.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));

        mSubheading2.setText(mCurrentCard.question.subheading);
        mMain2.setText(mCurrentCard.question.main);
        mSub2.setText(mCurrentCard.question.sub);
        mImage2.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));

    }

    private void updateAnswerContent() {
        mTitle.setText(mCurrentPack.answerTitle);
        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));

        mSubheading2.setText(mCurrentCard.answer.subheading);
        mMain2.setText(mCurrentCard.answer.main);
        mSub2.setText(mCurrentCard.answer.sub);
        mImage2.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));

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
        mChangeTemplateImage.setVisibility(View.VISIBLE);

        mTitle.setEnabled(true);
        mSidebarTitle.setEnabled(true);
        mSubheading.setEnabled(true);
        mMain.setEnabled(true);
        mSub.setEnabled(true);
        mSubheading2.setEnabled(true);
        mMain2.setEnabled(true);
        mSub2.setEnabled(true);
        mCreator.setEnabled(true);
        mImage.setEnabled(true);
        mImage2.setEnabled(true);

        mCreator.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSubheading.setBackgroundResource(R.drawable.shape_edittext_editable);
        mMain.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSub.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSubheading2.setBackgroundResource(R.drawable.shape_edittext_editable);
        mMain2.setBackgroundResource(R.drawable.shape_edittext_editable);
        mSub2.setBackgroundResource(R.drawable.shape_edittext_editable);

    }

    private void disableCardEditable() {
        mLogoURLImage.setVisibility(View.INVISIBLE);
        mChangeTemplateImage.setVisibility(View.INVISIBLE);

        mTitle.setEnabled(false);
        mCreator.setEnabled(false);
        mSidebarTitle.setEnabled(false);
        mSubheading.setEnabled(false);
        mMain.setEnabled(false);
        mSub.setEnabled(false);
        mImage.setEnabled(false);
        mSubheading2.setEnabled(false);
        mMain2.setEnabled(false);
        mSub2.setEnabled(false);
        mImage2.setEnabled(false);

        mCreator.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSubheading.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mMain.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSub.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSubheading2.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mMain2.setBackgroundResource(R.drawable.shape_edittext_no_editable);
        mSub2.setBackgroundResource(R.drawable.shape_edittext_no_editable);

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

        mCurrentCard.save(AppContext.getAppContext());
        Log.d(Global.debugTag, "finish execution of saveNewCreatedCard");

        mIsTakeSnapshotAllNeeded = false;


    }


    /**
     * do save when editting curent card
     * do NOT save when creating a new card
     */
    private void takeSnapshotCurrentCard() {

        if (isEditableMode()) {
            disableCardEditable();
        }

        //hide logo image if its placeholder
        if (mCurrentPack.logoImageUriFormatStr.contains("placeholder") == true) {
            mLogoImage.setVisibility(View.INVISIBLE);
        }

        View cardView = mContentView.findViewById(R.id.card);
        Bitmap bitmap = UIHelper.loadBitmapFromView(cardView);
        File savedFile = UIHelper.saveImageToCaches(bitmap);
        bitmap.recycle();
        System.gc();
        mCurrentCard.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(savedFile);

        //restore and show logo image
        mLogoImage.setVisibility(View.VISIBLE);

        if (isEditableMode()) {
            enableCardEditable();
        }

        if (mIsCreatingCard == false) {
            mCurrentCard.save(AppContext.getAppContext());
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

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
        params.weight = 0f;
        mImage.setLayoutParams(params);

        params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
        params.weight = 710f;
        mContentBodyLeft.setLayoutParams(params);

        //we don't need to set font size here since it will be done in CSS constructor

        switch (templateID) {
            case 0:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                mImage.setVisibility(View.INVISIBLE);

                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 50;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 350;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                break;
            case 1:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                mImage.setVisibility(View.INVISIBLE);

                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 50;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 180;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 160;
                mSub.setLayoutParams(params);
                break;
            case 2:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                mImage.setVisibility(View.INVISIBLE);

                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 280;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 100;
                mSub.setLayoutParams(params);
                break;
            case 3:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                mImage.setVisibility(View.INVISIBLE);

                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 200;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 190;
                mSub.setLayoutParams(params);
                break;
            case 4:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                mImage.setVisibility(View.INVISIBLE);

                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 380;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            case 5:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 0f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 710f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            default:
                Log.w(Global.debugTag, "mCurrentCard.question.templateID is out of scope");
        }

    }

    private void updateAnswerViewTemplate() {

        int templateID = mCurrentCard.answer.templateID;
        LinearLayout.LayoutParams params;

        //we don't need to set font size here since it will be done in CSS constructo

        switch (templateID) {
            case 0:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 80;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 320;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);

                break;
            case 1:

                mContentBodyType1.setVisibility(View.INVISIBLE);
                mContentBodyType2.setVisibility(View.VISIBLE);

                //use default in card.xml is OK

                break;
            case 2:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 60;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 350;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            case 3:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 710f;
                mContentBodyLeft.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 0f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.INVISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            case 4:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 420;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            case 5:

                mContentBodyType1.setVisibility(View.VISIBLE);
                mContentBodyType2.setVisibility(View.INVISIBLE);

                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 0f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 710f;
                mImage.setLayoutParams(params);
                mImage.setVisibility(View.VISIBLE);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 0;
                mSub.setLayoutParams(params);
                break;
            default:
                Log.w(Global.debugTag, "mCurrentCard.answer.templateID is out of scope");
        }
    }

    private void updateQuestionCSS() {

        mTitle.setTextColor(Color.parseColor("#0910FF"));

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subheadingAlign) | Gravity.CENTER_VERTICAL);
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.mainAlign) | Gravity.TOP);
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subAlign) | Gravity.TOP);

        //step2: size
        mSubheading.setTextSize(mCurrentCard.question.css.subheadingSize);
        mMain.setTextSize(mCurrentCard.question.css.mainSize);
        mSub.setTextSize(mCurrentCard.question.css.subSize);

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subColor));

        //step1: alignment
        mSubheading2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subheadingAlign) | Gravity.CENTER_VERTICAL);
        mMain2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.mainAlign) | Gravity.TOP);
        mSub2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subAlign) | Gravity.TOP);

        //step2: size
        mSubheading2.setTextSize(mCurrentCard.question.css.subheadingSize);
        mMain2.setTextSize(mCurrentCard.question.css.mainSize);
        mSub2.setTextSize(mCurrentCard.question.css.subSize);

        //step3: color
        mSubheading2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subheadingColor));
        mMain2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.mainColor));
        mSub2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subColor));
    }

    private void updateAnswerCSS() {

        mTitle.setTextColor(Color.RED);

        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign ) | Gravity.CENTER_VERTICAL);
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign) | Gravity.TOP);
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign) | Gravity.TOP);

        //step2: size
        mSubheading.setTextSize(mCurrentCard.answer.css.subheadingSize);
        mMain.setTextSize(mCurrentCard.answer.css.mainSize);
        mSub.setTextSize(mCurrentCard.answer.css.subSize);

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subColor));

        //step1: alignment
        mSubheading2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign ) | Gravity.CENTER_VERTICAL);
        mMain2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign) | Gravity.TOP);
        mSub2.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign) | Gravity.TOP);

        //step2: size
        mSubheading2.setTextSize(mCurrentCard.answer.css.subheadingSize);
        mMain2.setTextSize(mCurrentCard.answer.css.mainSize);
        mSub2.setTextSize(mCurrentCard.answer.css.subSize);

        //step3: color
        mSubheading2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subheadingColor));
        mMain2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.mainColor));
        mSub2.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subColor));
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
        }

        //Step2: determine operaton target
        int editTextTag = Integer.parseInt((String) mCurrentFocusedCardContentText.getTag());
        if (mIsQuestionShowing) {
            currentCSS = mCurrentCard.question.css;
        } else {
            currentCSS = mCurrentCard.answer.css;
        }

        //Step3: fill values
        String[] sizeArray = getResources().getStringArray(R.array.css_size);
        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        switch (menuID) {
            case 0:   //stand for align

                if (editTextTag == 1001) {
                    currentCSS.subheadingAlign = alignArray[subMenuID +1];
                } else if (editTextTag == 1002) {
                    currentCSS.mainAlign = alignArray[subMenuID +1];
                } else if (editTextTag == 1003) {
                    currentCSS.subAlign = alignArray[subMenuID + 1];
                }

                switch (subMenuID) {
                    case 0:
                        mCurrentFocusedCardContentText.setGravity(Gravity.LEFT);
                        break;
                    case 1:
                        mCurrentFocusedCardContentText.setGravity(Gravity.CENTER_HORIZONTAL);
                        break;
                    case 2:
                        mCurrentFocusedCardContentText.setGravity(Gravity.RIGHT);
                        break;
                    default:
                        Log.w(Global.debugTag, "Out of range of subMenuID");
                }
                break;

            case 1:   //stand for size

                int size = Integer.parseInt(sizeArray[subMenuID +1]);

                //you can find the tag definition(1001,1002,1003) in card.xml
                if (editTextTag == 1001) {
                    currentCSS.subheadingSize = size;
                } else if (editTextTag == 1002) {
                    currentCSS.mainSize = size;
                } else if (editTextTag == 1003) {
                    currentCSS.subSize = size;
                }

                mCurrentFocusedCardContentText.setTextSize(size);

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
                    default:
                        Log.w(Global.debugTag, "Out of range of subMenuID");
                }
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
        ((MainActivity) getActivity()).removeCSSToolbar();
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

        mCurrentFocusedCardContentText.setSelection(mCurrentFocusedCardContentText.getText().length());

    }


}



