package com.internectics.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.model.CardListModel;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * CardListFragment manage ListAdapter including updating ListView
 */
public class CardListFragment extends Fragment {

    //View related
    public View mContentView;
    private DragSortListView mDSLVListView;

    //Adapter
    private SimpleDragSortCursorAdapter adapter;

    //Modular related
    public Pack mCurrentPack;
    private List<HashMap<String, Object>> mCardArrayList;

    //Callback
    private Callbacks mCallbacks;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public boolean isListViewEditable = false;

    MasterFragmentReceiver mReceiver;

    public CardListFragment() {
        mCurrentPack = CardListModel.getLatestCreatedPack();
        if (mCurrentPack != null) {
            mCardArrayList = CardListModel.getCardList(mCurrentPack);
        } else {
            mCardArrayList = new ArrayList<HashMap<String, Object>>();
        }
    }


    public interface Callbacks {
        public void onItemSelected(int index);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Step1: get content view
        mContentView = inflater.inflate(R.layout.dragsortlistview, container);

        //Step2: set DragSortListView and FCCdapter
        String[] cols = {"card_sn", "cover_image"};
        int[] ids = {R.id.card_list_item_card_sn, R.id.card_list_item_cover_image};
        adapter = new FCCdapter(getActivity(),
                R.layout.card_list_item, null, cols, ids, 0);

        mDSLVListView = (DragSortListView) mContentView.findViewById(android.R.id.list);
        mDSLVListView.setAdapter(adapter);

        mDSLVListView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                Log.d(Global.debugTag, "Card list item is removed" + which);
                removeListItem(which);
            }
        });

        mDSLVListView.setDragListener(new DragSortListView.DragListener() {
            @Override
            public void drag(int from, int to) {
                Log.d(Global.debugTag, String.format("Move card list item from %d to %d", from, to));
                dragListItem(from, to);
            }
        });



        return mContentView;
    }


    @Override
    public void onResume() {
        super.onResume();

        //Step1: register broadcast
        mReceiver = new MasterFragmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        getActivity().registerReceiver(mReceiver, filter);

        //Step2: update cursor and refresh UI
        MatrixCursor cursor = rebuildCursor();
        adapter.changeCursor(cursor);

        //Step3: Finally, send back currenPack to activity
        ((MainActivity) getActivity()).mCurrentPack = mCurrentPack;
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }


    private MatrixCursor rebuildCursor() {

        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "card_sn", "cover_image"});

        if (mCurrentPack == null)
            return cursor;

        for (int i = 0; i < mCardArrayList.size(); i++) {
            cursor.newRow()
                    .add(i)
                    .add(mCardArrayList.get(i).get("cardSN"))
                    .add(mCardArrayList.get(i).get("coverImageUriFormatStr").toString());
        }

        return cursor;
    }


    public void enterEditStyle(boolean isEditingStyle) {

        isListViewEditable = isEditingStyle;

        updateListView(0);
    }

    private class FCCdapter extends SimpleDragSortCursorAdapter {

        public FCCdapter(Context ctxt, int rmid, Cursor c, String[] cols, int[] ids, int something) {
            super(ctxt, rmid, c, cols, ids, something);
            mContext = ctxt;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            View tv = v.findViewById(R.id.card_list_item_cover_image);

            ImageView drageImage = (ImageView) v.findViewById(R.id.card_list_item_drag_handle);
            ImageView removeImage = (ImageView) v.findViewById(R.id.card_list_item_click_remove);
            TextView cardSNText = (TextView) v.findViewById(R.id.card_list_item_card_sn);

            Animation alphaOut = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in);
            Animation alphaIn = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in);
            if (isListViewEditable) {
                drageImage.setVisibility(View.VISIBLE);
                removeImage.setVisibility(View.VISIBLE);
                cardSNText.setVisibility(View.GONE);

                drageImage.startAnimation(alphaIn);
                removeImage.startAnimation(alphaIn);
            } else {
                drageImage.setVisibility(View.GONE);
                removeImage.setVisibility(View.GONE);
                cardSNText.setVisibility(View.VISIBLE);

                cardSNText.startAnimation(alphaIn);
            }

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(Global.debugTag, "card item is clicked:" + position);
                    mCallbacks.onItemSelected(position);
                }
            });
            return v;
        }
    }


    /**
     * Deal with broadcast and update view
     */
    private class MasterFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW)) {

                //step1: setmCurrentPack;
                String extraStr = intent.getExtras().getString(Global.KEY_FROM);
                int    extraInt = intent.getExtras().getInt("cardIndex");

                if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_NEW_PACK)) {
                    mCurrentPack = CardListModel.getLatestCreatedPack();
                } else if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_NEW_CARD)) {
                    mCurrentPack = CardListModel.updateCurrentPack(mCurrentPack);
                } else if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_CURRENT_PACK_UPDATE)) {
                    mCurrentPack = CardListModel.updateCurrentPack(mCurrentPack);
                } else if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED)) {
                    int index = intent.getExtras().getInt("indexOfPack");
                    mCurrentPack = CardListModel.getAllPacks().get(index);
                } else if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_PACK_DOWNLOADED)) {
                    mCurrentPack = CardListModel.getLastPack();
                }

                //step2: update listview
                updateListView(extraInt);
            }
        }

    }


    private void dragListItem(int from, int to) {

        if (from == to) {
            return;
        } else if (from < to) {

            for (int i = from + 1; i <= to; i++) {
                Card card = mCurrentPack.cards.get(i);
                card.cardSN = i;
                card.save(AppContext.getAppContext());
            }

        } else {

            for (int i = to; i < from; i++) {
                Card card = mCurrentPack.cards.get(i);
                card.cardSN = i + 2;
                card.save(AppContext.getAppContext());
            }
        }

        //Step3: update list view
        updateListView(0);
    }


    private void removeListItem(int which) {

        //Step1: remove current card from mCurrentPack and database
        Card removedCard = mCurrentPack.cards.get(which);
        mCurrentPack.cards.remove(which);
        removedCard.destroy(AppContext.getAppContext());

        //Step2: reorder all cards' SN and save to database
        for (int i = 0; i < mCurrentPack.cards.size(); i++) {
            Card card = mCurrentPack.cards.get(i);
            card.cardSN = i + 1;
            card.save(AppContext.getAppContext());
        }

        //Step3: update list view
        updateListView(0);

    }

    private void updateListView(int selectedItemIndex) {

        //Step1: update mCardArrayList
        if (mCurrentPack != null) {
            mCardArrayList.clear();
            mCardArrayList.addAll(CardListModel.getCardList(mCurrentPack));
        }

        //Step2: update cursor
        MatrixCursor cursor = rebuildCursor();
        adapter.changeCursor(cursor);

        //Step3: Send back currenPack to activity
        ((MainActivity) getActivity()).mCurrentPack = mCurrentPack;

        //Step4: Update detail view
        if (mCardArrayList.size() >0) {
            mCallbacks.onItemSelected(selectedItemIndex);
            mDSLVListView.setItemChecked(selectedItemIndex, true);
            mDSLVListView.smoothScrollToPosition(selectedItemIndex);
        }  else {
            mCallbacks.onItemSelected(-1);
        }
    }
}
