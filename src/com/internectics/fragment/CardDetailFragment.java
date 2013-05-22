package com.internectics.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.UIHelper;

import java.io.File;

public class CardDetailFragment extends Fragment {

    private Card mCurrentCard;
    private Pack mCurrentPack;

    private View mContentView;

    public static final String ARG_ITEM_ID = "item_id";

    private boolean mIsQuestionShowing = true;

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
        EditText sidebarEditText = (EditText) mContentView.findViewById(R.id.sidebar_title);
        sidebarEditText.setText(String.format("%d", mCurrentCard.cardSN));

        final RadioButton questionRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_question);
        final RadioButton answerRadioButton = (RadioButton) mContentView.findViewById(R.id.radio_segment_answer);

        RadioGroup radioGroup = (RadioGroup) mContentView.findViewById(R.id.radio_segment);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == questionRadioButton.getId()) {
                    questionRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    questionRadioButton.setTextColor(Color.WHITE);
                    answerRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    answerRadioButton.setTextColor(Color.BLACK);
                    mIsQuestionShowing = true;
                }else {
                    questionRadioButton.setBackgroundResource(R.drawable.button_segment_unselected);
                    questionRadioButton.setTextColor(Color.BLACK);
                    answerRadioButton.setBackgroundResource(R.drawable.button_segment_selected);
                    answerRadioButton.setTextColor(Color.WHITE);
                    mIsQuestionShowing = false;
                }
            }
        });


        return mContentView;
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



