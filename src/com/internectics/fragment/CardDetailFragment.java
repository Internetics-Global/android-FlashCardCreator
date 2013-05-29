package com.internectics.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.internectics.util.*;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;

public class CardDetailFragment extends Fragment implements FCCEditText.OnKeyboardCloseListener, FCCEditText.OnTouchListener {

    private Card mCurrentCard;
    private Pack mCurrentPack;

    public boolean mIsCreatingCard = false;
    private boolean mIsPlayingCard = false;
    private boolean mIsQuestionShowing = false; //this is only used in play mode

    private View mContentView;

    private LinearLayout mContentBodyLeft;
    private FCCEditText mSidebarTitle;
    private FrameLayout mSidebarBackground;
    private FCCEditText mTitle;
    private LinearLayout mTitleBackground;
    private FCCEditText mCreator;
    private FCCEditText mSubheading;
    private FCCEditText mMain;
    private FCCEditText mSub;
    private ImageView mImage;
    private ImageView mLogoImage;
    private ImageView mChangeTemplateImage;
    private ImageView mLogoURLImage;

    private RadioButton mQuestionRadioButton;
    private RadioButton mAnswerRadioButton;
    private RadioGroup mRadioGroup;

    private InputMethodManager mIMM;

    private int CODE_REQUEST_IMAGE_SOURCE_IS_LOGO = 1001; //when user click on the logo img
    private int CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE = 1002;//when user click on the image img

    private EditText mFocusedEditText;

    /**
     * Constructor
     *
     * @param currentPack
     * @param currentCard
     * @param source,     0 ordinary; 1 creating new card; 2. play mode
     */
    public CardDetailFragment(Pack currentPack, Card currentCard, int source) {

        if (source == 1) {
            mIsCreatingCard = true;
        } else if (source == 2) {
            mIsPlayingCard = true;
            mIsCreatingCard = false;
        } else {
            mIsPlayingCard = false;
            mIsCreatingCard = false;
        }

        if (currentCard == null) {
            Log.d(Global.debugTag, "Creating a new card is going on");
            mCurrentPack = currentPack;
            initilizeNewCard();

        } else {
            mCurrentCard = currentCard;
            mCurrentPack = currentPack;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIMM = (InputMethodManager) (getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mIsPlayingCard) {
            //need to hide queston/answer segment raido group
            mContentView = inflater.inflate(R.layout.card, container, false);
        } else {
            mContentView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        }

        getAllViews();

        if (!mIsPlayingCard) {
            configureSegmentView();
        }
        configureChangeTemplateView();
        configureLogoURLView();

        mLogoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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

        return mContentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Global.debugTag, "onViewCreated in CardDetailFragment");

        updateCommonContent();
        switchToQuestionView();

        if (mIsPlayingCard) {
            disableCardEditable();
        } else {
            configureCardStatus();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Global.debugTag, "onResume in CardDetailFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(Global.debugTag, "onDestroyView in CardDetailFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImageURI = data.getData();

            Bitmap resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
            if (resultBitmap == null) {
                Log.d(Global.debugTag, "resultBitmap is null");
            } else {
                File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);

                if (requestCode == CODE_REQUEST_IMAGE_SOURCE_IS_LOGO) {
                    mLogoImage.setImageBitmap(resultBitmap);
                    mCurrentPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);

                } else if (requestCode == CODE_REQUEST_IMAGE_SOURCE_IS_IMAGE) {
                    mImage.setImageBitmap(resultBitmap);
                    if (mQuestionRadioButton.isChecked()) {
                        mCurrentCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    } else {
                        mCurrentCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    }
                }
            }
        }
    }

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

    private void configureChangeTemplateView() {
        ActionItem questionActionItem0 = new ActionItem(0, null, getResources().getDrawable(R.drawable.question_templatescreenshot0));
        ActionItem questionActionItem1 = new ActionItem(1, null, getResources().getDrawable(R.drawable.question_templatescreenshot1));
        ActionItem questionActionItem2 = new ActionItem(2, null, getResources().getDrawable(R.drawable.question_templatescreenshot2));
        ActionItem questionActionItem3 = new ActionItem(3, null, getResources().getDrawable(R.drawable.question_templatescreenshot3));
        ActionItem questionActionItem4 = new ActionItem(4, null, getResources().getDrawable(R.drawable.question_templatescreenshot4));

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
                if (mQuestionRadioButton.isChecked()) {
                    questionQuickAction.show(mChangeTemplateImage);
                } else {
                    answerQuickAction.show(mChangeTemplateImage);
                }

            }
        });

    }

    private void configureLogoURLView() {
        mLogoURLImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputEditText = new EditText(getActivity());
                inputEditText.setSingleLine(true);
                inputEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Set URL")
                        .setMessage("Please input a valid URL")
                        .setView(inputEditText)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurrentPack.logoURL = inputEditText.getText().toString();
                                if (mIMM.isActive()) {
                                    mIMM.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }

    private void changeTemplateNotification(int index) {
        if (mQuestionRadioButton.isChecked()) {
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
        mTitle = (FCCEditText) mContentView.findViewById(R.id.title);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (FCCEditText) mContentView.findViewById(R.id.creator);

        mContentBodyLeft = (LinearLayout) mContentView.findViewById(R.id.content_body_left);

        mSubheading = (FCCEditText) mContentView.findViewById(R.id.subheading);
        mMain = (FCCEditText) mContentView.findViewById(R.id.main);
        mSub = (FCCEditText) mContentView.findViewById(R.id.sub);

        mChangeTemplateImage = (ImageView) mContentView.findViewById(R.id.change_template_button);
        mLogoImage = (ImageView) mContentView.findViewById(R.id.logo_image);
        mLogoURLImage = (ImageView) mContentView.findViewById(R.id.logo_url_btn);
        mImage = (ImageView) mContentView.findViewById(R.id.image);

        if (!mIsPlayingCard) {
            mQuestionRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_question);
            mAnswerRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_answer);
            mRadioGroup = (RadioGroup) mContentView.findViewById(R.id.radio_segment);
        }

        mTitle.mCallbacks = this;
        mSidebarTitle.mCallbacks = this;
        mSubheading.mCallbacks = this;
        mMain.mCallbacks = this;
        mSub.mCallbacks = this;

        mSubheading.setOnTouchListener(this);
        mMain.setOnTouchListener(this);
        mSub.setOnTouchListener(this);
    }


    private void updateCommonContent() {
        mSidebarTitle.setText(mCurrentPack.sidebarTitle + "(" + mCurrentCard.cardSN + ")");

        mLogoImage.setImageURI(Uri.parse(mCurrentPack.logoImageUriFormatStr));
        mCreator.setText(mCurrentPack.creatorNickName);

        int sidebarBGResourceID = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[1];
        mSidebarBackground.setBackgroundResource(sidebarBGResourceID);
        int titleBGResourceID = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[2];
        mTitleBackground.setBackgroundResource(titleBGResourceID);

        if (!mIsPlayingCard) {
            if (mQuestionRadioButton.isChecked()) {
                mTitle.setText(mCurrentPack.questionTitle);
            } else {
                mTitle.setText(mCurrentPack.answerTitle);
            }
        }

    }


    private void updateQuestionContent() {
        mTitle.setText(mCurrentPack.questionTitle);
        mSubheading.setText(mCurrentCard.question.subheading);
        mMain.setText(mCurrentCard.question.main);
        mSub.setText(mCurrentCard.question.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));

    }

    private void updateAnswerContent() {
        mTitle.setText(mCurrentPack.answerTitle);
        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));

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
        mCreator.setEnabled(true);
        mImage.setEnabled(true);
    }

    private void disableCardEditable() {
        mLogoURLImage.setVisibility(View.INVISIBLE);
        mChangeTemplateImage.setVisibility(View.INVISIBLE);

        mTitle.setEnabled(false);
        mSidebarTitle.setEnabled(false);
        mSubheading.setEnabled(false);
        mMain.setEnabled(false);
        mSub.setEnabled(false);
        mCreator.setEnabled(false);
        mImage.setEnabled(false);
    }


    /**
     * to initialized card during creating a new card
     */
    private void initilizeNewCard() {
        mCurrentCard = new Card();
        mCurrentCard.packID = mCurrentPack.packID;
        mCurrentCard.cardSN = mCurrentPack.cards.size() + 1;
    }

    public void saveNewCreatedCard() {

        //Step1: take card screenshot
        View cardView = mContentView.findViewById(R.id.card);
        Bitmap bitmap = UIHelper.loadBitmapFromView(cardView);
        File savedFile = UIHelper.saveImageToCaches(bitmap);
        mCurrentCard.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(savedFile);


        //Step4: save
        mCurrentCard.save(AppContext.getAppContext());
        Log.d(Global.debugTag, "finish execution of saveNewCreatedCard");
    }


    public void cardColorTemplateSelectedPostAction(int cardColorTemplateIndex) {
        switch (cardColorTemplateIndex) {
            case 0:
                mSidebarBackground.setBackgroundResource(R.drawable.card_sidebar_bg_blue);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_blue);
                break;
            case 1:
                mSidebarBackground.setBackgroundResource(R.drawable.card_sidebar_bg_coffee);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_coffee);
                break;
            case 2:
                mSidebarBackground.setBackgroundResource(R.drawable.card_sidebar_bg_gray);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_gray);
                break;
            case 3:
                mSidebarBackground.setBackgroundResource(R.drawable.card_sidebar_bg_purple);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_purple);
                break;
            case 4:
                mSidebarBackground.setBackgroundResource(R.drawable.card_sidebar_bg_red);
                mTitleBackground.setBackgroundResource(R.drawable.card_title_bg_red);
                break;
            default:
                Log.i(Global.debugTag, "Out of range");
        }

        String templateBackground = StringUtils.convertTemplateBackgroundIndexToString(cardColorTemplateIndex);
        mCurrentCard.templateBackground = templateBackground;

        if (!mIsCreatingCard) {

            for (Card card : mCurrentPack.cards) {
                card.templateBackground = templateBackground;
                //TODO, screenshot all cards
                card.save(AppContext.getAppContext());
            }

        }

        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_INTENT_EXTRA_FROM_CURRENT_PACK_UPDATE);
        getActivity().sendBroadcast(intent);
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

        switch (templateID) {
            case 0:
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
            default:
                Log.i(Global.debugTag, "mCurrentCard.question.templateID is out of scope");
        }

    }

    private void updateAnswerViewTemplate() {

        int templateID = mCurrentCard.answer.templateID;
        LinearLayout.LayoutParams params;

        switch (templateID) {
            case 0:
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);

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
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 500f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 210f;
                mImage.setLayoutParams(params);

                //part2:text
                params = (LinearLayout.LayoutParams) mSubheading.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 60;
                mSubheading.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mMain.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 290;
                mMain.setLayoutParams(params);

                params = (LinearLayout.LayoutParams) mSub.getLayoutParams();
                params.width = LinearLayout.LayoutParams.FILL_PARENT;
                params.weight = 60;
                mSub.setLayoutParams(params);

                break;
            case 2:
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);

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
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 710f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 0f;
                mImage.setLayoutParams(params);

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
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 360f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 350f;
                mImage.setLayoutParams(params);

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
                //part1: image
                params = (LinearLayout.LayoutParams) mContentBodyLeft.getLayoutParams();
                params.weight = 0f;
                mContentBodyLeft.setLayoutParams(params);
                params = (LinearLayout.LayoutParams) mImage.getLayoutParams();
                params.weight = 710f;
                mImage.setLayoutParams(params);

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
                Log.i(Global.debugTag, "mCurrentCard.answer.templateID is out of scope");
        }
    }

    private void updateQuestionCSS() {
        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subheadingAlign));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.mainAlign));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.question.css.subAlign));

        //step2: size
        mSubheading.setTextSize(mCurrentCard.question.css.subheadingSize);
        mMain.setTextSize(mCurrentCard.question.css.mainSize);
        mSub.setTextSize(mCurrentCard.question.css.subSize);

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.question.css.subColor));
    }

    private void updateAnswerCSS() {
        //step1: alignment
        mSubheading.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subheadingAlign));
        mMain.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.mainAlign));
        mSub.setGravity(StringUtils.convertGravityStringToInt(mCurrentCard.answer.css.subAlign));

        //step2: size
        mSubheading.setTextSize(mCurrentCard.answer.css.subheadingSize);
        mMain.setTextSize(mCurrentCard.answer.css.mainSize);
        mSub.setTextSize(mCurrentCard.answer.css.subSize);

        //step3: color
        mSubheading.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subheadingColor));
        mMain.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.mainColor));
        mSub.setTextColor(StringUtils.convertColorStringToInt(mCurrentCard.answer.css.subColor));
    }


    @Override
    public void onKeyboardClose(EditText editText) {

        if ((mIMM.isActive() == false) || (mIsPlayingCard)) {
            //It will be called when press the back button, even no keyboard is shown on now.That's the reason why we need to check
            return;
        }

        Log.d(Global.debugTag, "Keyboard is closed");
        int id = editText.getId();
        switch (id) {
            case R.id.sidebar_title:
                mCurrentPack.sidebarTitle = editText.getText().toString();
                break;
            case R.id.title:
                if (mQuestionRadioButton.isChecked()) {
                    mCurrentPack.questionTitle = editText.getText().toString();
                } else {
                    mCurrentPack.answerTitle = editText.getText().toString();
                }
                break;
            case R.id.creator:
                mCurrentPack.creatorNickName = editText.getText().toString();
                break;
            case R.id.subheading:
                if (mQuestionRadioButton.isChecked()) {
                    mCurrentCard.question.subheading = editText.getText().toString();
                } else {
                    mCurrentCard.answer.subheading = editText.getText().toString();
                }
                break;
            case R.id.main:
                if (mQuestionRadioButton.isChecked()) {
                    mCurrentCard.question.main = editText.getText().toString();
                } else {
                    mCurrentCard.answer.main = editText.getText().toString();
                }
                break;
            case R.id.sub:
                if (mQuestionRadioButton.isChecked()) {
                    mCurrentCard.question.sub = editText.getText().toString();
                } else {
                    mCurrentCard.answer.sub = editText.getText().toString();
                }

                break;
            default:
                Log.i(Global.debugTag, "Out of our scope");
        }

        //Save logic if not creating a new card
        if (mIsCreatingCard) {
            //we will do that when we click the save button
        } else {
            //We do here
            mCurrentPack.save(AppContext.getAppContext());
            mCurrentCard.save(AppContext.getAppContext());
        }

        //Update actionbar
        getActivity().getActionBar().show();
        ((MainActivity) getActivity()).mIsEdittingCard = false;
        getActivity().invalidateOptionsMenu();

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.d(Global.debugTag, "onTouch happened");
        getActivity().getActionBar().show();
        ((MainActivity) getActivity()).mIsEdittingCard = true;
        getActivity().invalidateOptionsMenu();

        mFocusedEditText = (EditText) v;

        return false;
    }

    public void updateCSS(int menuID, int subMenuID) {
        CSS currentCSS;

        //Step1: question or answer view now
        boolean isQuestionShowing = mQuestionRadioButton.isChecked();

        //Step2: determine operaton target
        int editTextTag = Integer.parseInt((String) mFocusedEditText.getTag());
        if (isQuestionShowing) {
            currentCSS = mCurrentCard.question.css;
        } else {
            currentCSS = mCurrentCard.answer.css;
        }

        //Step3: fill values
        int[] sizeArray = getResources().getIntArray(R.array.css_size);
        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        switch (menuID) {
            case 0:   //stand for align

                if (editTextTag == 1001) {
                    currentCSS.subheadingAlign = alignArray[subMenuID];
                } else if (editTextTag == 1002) {
                    currentCSS.mainAlign = alignArray[subMenuID];
                } else if (editTextTag == 1003) {
                    currentCSS.subAlign = alignArray[subMenuID];
                }

                switch (subMenuID) {
                    case 0:
                        mFocusedEditText.setGravity(Gravity.LEFT);
                        break;
                    case 1:
                        mFocusedEditText.setGravity(Gravity.CENTER);
                        break;
                    case 2:
                        mFocusedEditText.setGravity(Gravity.RIGHT);
                        break;
                    default:
                        Log.d(Global.debugTag, "Out of range of subMenuID");
                }
                break;

            case 1:   //stand for size

                //you can find the tag definition(1001,1002,1003) in card.xml
                if (editTextTag == 1001) {
                    currentCSS.subheadingSize = sizeArray[subMenuID];
                } else if (editTextTag == 1002) {
                    currentCSS.mainSize = sizeArray[subMenuID];
                } else if (editTextTag == 1003) {
                    currentCSS.subSize = sizeArray[subMenuID];
                }

                switch (subMenuID) {
                    case 0:
                        mFocusedEditText.setTextSize(sizeArray[0]);
                        break;
                    case 1:
                        mFocusedEditText.setTextSize(sizeArray[1]);
                        break;
                    case 2:
                        mFocusedEditText.setTextSize(sizeArray[2]);
                        break;
                    case 3:
                        mFocusedEditText.setTextSize(sizeArray[3]);
                        break;
                    case 4:
                        mFocusedEditText.setTextSize(sizeArray[4]);
                        break;
                    default:
                        Log.d(Global.debugTag, "Out of range of subMenuID");
                }
                break;
            case 2:   //stand for color

                if (editTextTag == 1001) {
                    currentCSS.subheadingColor = colorArray[subMenuID];
                } else if (editTextTag == 1002) {
                    currentCSS.mainColor = colorArray[subMenuID];
                } else if (editTextTag == 1003) {
                    currentCSS.subColor = colorArray[subMenuID];
                }

                switch (subMenuID) {
                    case 0:
                        mFocusedEditText.setTextColor(Color.RED);
                        break;
                    case 1:
                        mFocusedEditText.setTextColor(Color.BLUE);
                        break;
                    case 2:
                        mFocusedEditText.setTextColor(Color.BLACK);
                        break;
                    case 3:
                        mFocusedEditText.setTextColor(Color.YELLOW);
                        break;
                    case 4:
                        mFocusedEditText.setTextColor(Color.GREEN);
                        break;
                    default:
                        Log.d(Global.debugTag, "Out of range of subMenuID");
                }
                break;
            default:
                Log.d(Global.debugTag, "Out of range of menuID");
        }


        if (!mIsCreatingCard) {
            if (isQuestionShowing) {
                mCurrentCard.question.css.save(AppContext.getAppContext());
            } else {
                mCurrentCard.answer.css.save(AppContext.getAppContext());
            }
        }
    }

}



