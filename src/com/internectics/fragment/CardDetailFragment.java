package com.internectics.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.util.AppContext;
import com.internectics.util.Global;

public class CardDetailFragment extends Fragment {

    CardDetailReceiver mCardDetailReceiver;

    private Card mCurrentCard;
    private Pack mCurrentPack;

	public static final String ARG_ITEM_ID = "item_id";


    public CardDetailFragment(Pack currentPack,Card currentCard) {

        if (currentCard == null) {
            Log.d(Global.debugTag,"Creating a new card is going on");
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
		View rootView = inflater.inflate(R.layout.fragment_card_detail,
				container, false);

		return rootView;
	}


    /**
     * to initialized card during creating a new card
     */
    private void initilizeNewCard() {
        mCurrentCard =  new Card();
        mCurrentCard.packID = mCurrentPack.packID;
        mCurrentCard.cardSN = mCurrentPack.cards.size() + 1;
    }


    @Override
    public void onResume() {
        super.onResume();
        mCardDetailReceiver = new CardDetailReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.BROADCAST_ACTION_SAVE_NEW_CARD);
        getActivity().registerReceiver(mCardDetailReceiver,filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mCardDetailReceiver);
    }

    private class CardDetailReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("intent = " + intent);
            if (intent.getAction().equals(Global.BROADCAST_ACTION_SAVE_NEW_CARD)) {
                saveNewCreatedCard();
            }

        }
    }

    private void saveNewCreatedCard() {
        mCurrentCard.save(AppContext.getAppContext());
        Log.d(Global.debugTag,"finish execution of saveNewCreatedCard");
    }
}



