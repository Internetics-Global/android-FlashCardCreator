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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import com.internectics.util.UIHelper;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.File;

public class CardDetailFragment extends Fragment {

    private Card mCurrentCard;
    private Pack mCurrentPack;

    private View mContentView;

    private EditText mSidebarTitle;
    private FrameLayout mSidebarBackground;
    private EditText mTitle;
    private LinearLayout mTitleBackground;
    private EditText mCreator;
    private EditText mSubheading;
    private EditText mMain;
    private EditText mSub;
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

    /**
     * Constructor
     * @param currentPack
     * @param currentCard
     */
    public CardDetailFragment(Pack currentPack, Card currentCard) {

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
        mContentView = inflater.inflate(R.layout.fragment_card_detail,
                container, false);

        getAllViews();

        configureSegmentView();
        configureChangeTemplateView();

        mLogoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        CODE_REQUEST_IMAGE_SOURCE_IS_LOGO);

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
        //checkCardEditable();
        updateCommonContent();
        switchToQuestionView();
        configureLogoURLView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(Global.debugTag,"onDestroyView in CardDetailFragment.java");
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
                changeTemplateAction(pos);
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
                changeTemplateAction(pos);
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

    private void changeTemplateAction(int index) {
        if (mQuestionRadioButton.isChecked()) {
            mCurrentCard.question.templateID = index;
        } else {
            mCurrentCard.answer.templateID = index;
        }

    }


    private void switchToQuestionView() {
        updateQuestionContent();
        updateQuestionLayout();
    }


    private void switchToAnswerView() {
        updateAnswerContent();
        updateAnswerLayout();
    }


    private void getAllViews() {
        mSidebarTitle = (EditText) mContentView.findViewById(R.id.sidebar_title);
        mSidebarBackground = (FrameLayout) mContentView.findViewById(R.id.sidebar_background_linearlayout);
        mTitle = (EditText) mContentView.findViewById(R.id.title);
        mTitleBackground = (LinearLayout) mContentView.findViewById(R.id.title_background_linearlayout);
        mCreator = (EditText) mContentView.findViewById(R.id.creator);

        mSubheading = (EditText) mContentView.findViewById(R.id.subheading);
        mMain = (EditText) mContentView.findViewById(R.id.main);
        mSub = (EditText) mContentView.findViewById(R.id.sub);

        mChangeTemplateImage = (ImageView) mContentView.findViewById(R.id.change_template_button);
        mLogoImage = (ImageView) mContentView.findViewById(R.id.logo_image);
        mLogoURLImage = (ImageView) mContentView.findViewById(R.id.logo_url_btn);
        mImage = (ImageView) mContentView.findViewById(R.id.image);

        mQuestionRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_question);
        mAnswerRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_answer);
        mRadioGroup = (RadioGroup) mContentView.findViewById(R.id.radio_segment);

    }


    private void updateCommonContent() {
        mSidebarTitle.setText(mCurrentPack.sidebarTitle);
        if (mQuestionRadioButton.isChecked()) {
            mTitle.setText(mCurrentPack.questionTitle);
        } else {
            mTitle.setText(mCurrentPack.answerTitle);
        }
        mLogoImage.setImageURI(Uri.parse(mCurrentPack.logoImageUriFormatStr));
        mCreator.setText(mCurrentPack.creatorNickName);
    }


    private void updateQuestionContent() {
        mSubheading.setText(mCurrentCard.question.subheading);
        mMain.setText(mCurrentCard.question.main);
        mSub.setText(mCurrentCard.question.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.question.imageUriFormatStr));

    }

    private void updateAnswerContent() {

        mSubheading.setText(mCurrentCard.answer.subheading);
        mMain.setText(mCurrentCard.answer.main);
        mSub.setText(mCurrentCard.answer.sub);
        mImage.setImageURI(Uri.parse(mCurrentCard.answer.imageUriFormatStr));

    }

    private void updateQuestionLayout() {
        mImage.setVisibility(View.INVISIBLE);
    }

    private void updateAnswerLayout() {
        mImage.setVisibility(View.VISIBLE);
    }


    private boolean checkCardEditable() {
        boolean result = false;

        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {
            mLogoURLImage.setVisibility(View.VISIBLE);
            mChangeTemplateImage.setVisibility(View.VISIBLE);

            mTitle.setEnabled(true);
            mSidebarTitle.setEnabled(true);
            mSubheading.setEnabled(true);
            mMain.setEnabled(true);
            mSub.setEnabled(true);
            mCreator.setEnabled(true);
            mImage.setEnabled(true);

            result = true;
        } else {
            mLogoURLImage.setVisibility(View.INVISIBLE);
            mChangeTemplateImage.setVisibility(View.INVISIBLE);

            mTitle.setEnabled(false);
            mSidebarTitle.setEnabled(false);
            mSubheading.setEnabled(false);
            mMain.setEnabled(false);
            mSub.setEnabled(false);
            mCreator.setEnabled(false);
            mImage.setEnabled(false);

            result = false;
        }
        return result;
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

        //Step2: save
        mCurrentCard.save(AppContext.getAppContext());
        Log.d(Global.debugTag, "finish execution of saveNewCreatedCard");
    }

    public void cardColorTemplateSelectedPostAction(int cardColorTemplateID) {
        switch (cardColorTemplateID) {
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
    }
}



