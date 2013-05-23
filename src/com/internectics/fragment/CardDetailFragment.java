package com.internectics.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import com.internectics.util.UIHelper;

import java.io.File;

public class CardDetailFragment extends Fragment {

    private Card mCurrentCard;
    private Pack mCurrentPack;

    private View mContentView;

    public static final String ARG_ITEM_ID = "item_id";

    private boolean mIsQuestionShowing = true;

    private EditText mSidebarTitle;
    private EditText mTitle;
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
    private RadioGroup  mRadioGroup;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_card_detail,
                container, false);

        getAllViews();

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mQuestionRadioButton.getId()) {
                    mQuestionRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    mQuestionRadioButton.setTextColor(Color.WHITE);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mAnswerRadioButton.setTextColor(Color.BLACK);
                    mIsQuestionShowing = true;
                    switchToQuestionView();
                }else {
                    mQuestionRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    mQuestionRadioButton.setTextColor(Color.BLACK);
                    mAnswerRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    mAnswerRadioButton.setTextColor(Color.WHITE);
                    mIsQuestionShowing = false;
                    switchToAnswerView();
                }
            }
        });



        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //checkCardEditable();
        updateCommonContent();
        switchToQuestionView();

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
       mTitle        = (EditText) mContentView.findViewById(R.id.title);
       mCreator      = (EditText) mContentView.findViewById(R.id.creator);

       mSubheading   = (EditText) mContentView.findViewById(R.id.subheading);
       mMain         = (EditText) mContentView.findViewById(R.id.main);
       mSub          = (EditText) mContentView.findViewById(R.id.sub);

       mChangeTemplateImage = (ImageView) mContentView.findViewById(R.id.change_template_button);
       mLogoImage           = (ImageView) mContentView.findViewById(R.id.logo_image);
       mLogoURLImage        = (ImageView) mContentView.findViewById(R.id.logo_url_btn);
       mImage               = (ImageView) mContentView.findViewById(R.id.image);

       mQuestionRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_question);
       mAnswerRadioButton   = (RadioButton) mContentView.findViewById(R.id.radio_segment_answer);
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
        mCurrentCard.coverImageUriFormatStr = FileOperationHelper.covertToUriFormatFile(savedFile);

        //Step2: save
        mCurrentCard.save(AppContext.getAppContext());
        Log.d(Global.debugTag, "finish execution of saveNewCreatedCard");
    }
}



