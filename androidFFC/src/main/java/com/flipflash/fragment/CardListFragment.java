package com.flipflash.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.model.CardListModel;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.flipflash.util.LogUtils.LOGD;


/**
 * CardListFragment manage ListAdapter including updating ListView
 * the order of card shown in CardListFragment is consisitent with cardSN order
 */
public class CardListFragment extends Fragment {
    private static final String TAG = CardListFragment.class.getName();

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

    public boolean mIsListViewEditable = false;

    public boolean mIsListViewUpdating = false;

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

        mDSLVListView.setDividerHeight(0);

        mDSLVListView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                LOGD(TAG, "remove: Card list item is removed" + which);
                        removeListItem(which);
            }
        });

        mDSLVListView.setDragListener(new DragSortListView.DragListener() {
            @Override
            public void drag(int from, int to) {
                dragListItem(from, to);
            }
        });

        mDSLVListView.setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);

        //Register broadcast
        mReceiver = new MasterFragmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        getActivity().registerReceiver(mReceiver, filter);

        if (mCurrentPack != null) {
            ((MainActivity)getActivity()).packIDForMasterViewPack = mCurrentPack.packID;
        }

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Step1: update cursor and refresh UI
        MatrixCursor cursor = rebuildCursor();
        adapter.changeCursor(cursor);

        //Step2: Finally, send back currentPack to activity
        ((MainActivity) getActivity()).setCurrentPack(mCurrentPack);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);

//        RefWatcher refWatcher = AppContext.getRefWatcher(getActivity());
//        refWatcher.watch(this);
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

    public boolean getEditStyle() {
        return mIsListViewEditable;
    }


    public void enterEditStyle(boolean isEditingStyle) {

        mIsListViewEditable = isEditingStyle;

        updateListView(-1);
    }

    private class FCCdapter extends SimpleDragSortCursorAdapter {

        private int selectedPosition = -1;

        public FCCdapter(Context ctxt, int rmid, Cursor c, String[] cols, int[] ids, int something) {
            super(ctxt, rmid, c, cols, ids, something);
            mContext = ctxt;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            ImageView coverImage = (ImageView) v.findViewById(R.id.card_list_item_cover_image);

            ImageView coverImageMask = (ImageView) v.findViewById(R.id.card_list_item_cover_image_mask);


            View background = v.findViewById(R.id.card_list_item_background);

            ImageView drageImage = (ImageView) v.findViewById(R.id.card_list_item_drag_handle);
            ImageView removeImage = (ImageView) v.findViewById(R.id.card_list_item_click_remove);
            TextView cardSNText = (TextView) v.findViewById(R.id.card_list_item_card_sn);

            //Animation alphaOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            //Animation alphaIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            if (mIsListViewEditable) {
                drageImage.setVisibility(View.VISIBLE);
                removeImage.setVisibility(View.VISIBLE);
                cardSNText.setVisibility(View.GONE);

                //drageImage.startAnimation(alphaIn);
                //removeImage.startAnimation(alphaIn);
            } else {
                drageImage.setVisibility(View.GONE);
                removeImage.setVisibility(View.GONE);
                cardSNText.setVisibility(View.VISIBLE);

                //cardSNText.startAnimation(alphaIn);
            }


            coverImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LOGD(TAG, "onClick: " +  "card item is clicked:" + position);
                    mCallbacks.onItemSelected(position);
                    ((FCCdapter) adapter).setSelectedPosition(position);
                    adapter.notifyDataSetChanged();
                }
            });


            //highlight clor
            if (selectedPosition == position) {
                background.setBackgroundColor(Color.rgb(67, 67, 67));
                coverImageMask.setImageDrawable(getResources().getDrawable(R.drawable.card_cover_image_mask_blue));
            } else {
                background.setBackgroundColor(Color.TRANSPARENT);
                coverImageMask.setImageDrawable(getResources().getDrawable(R.drawable.card_cover_image_mask_black));
            }

            //check whether cover image is empty or not
            if (coverImage.getDrawable() == null) {
                coverImage.setImageDrawable(getResources().getDrawable(R.drawable.card_cover_image_placeholder));
            }


            return v;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }
    }


    /**
     * Deal with broadcast and update view
     */
    private class MasterFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW)) {

                //step1: set mCurrentPack;
                String extraFrom = intent.getExtras().getString(Global.KEY_FROM);
                int extraCardIndex = 0;

                if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_NEW_PACK)) {
                    extraCardIndex = 0;
                    mCurrentPack = CardListModel.getLatestCreatedPack();
                } else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_EDIT_PACK)) {
                    //still use current pack
                }else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_NEW_CARD)) {
                    extraCardIndex = intent.getExtras().getInt(Global.KEY_CARD_INDEX);
                    mCurrentPack = User.getPack(AppContext.getAppContext(),mCurrentPack.packID);
                } else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE)) {
                    extraCardIndex = -1;
                    mCurrentPack = User.getPack(AppContext.getAppContext(),mCurrentPack.packID);
                } else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_PACK_SELECTED)) {
                    extraCardIndex = 0;
                    int packIndex = intent.getExtras().getInt("indexOfPack");
                    mCurrentPack = CardListModel.getAllPacks().get(packIndex);
                } else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_PACK_DOWNLOADED)) {
                    extraCardIndex = 0;
                    mCurrentPack = CardListModel.getLatestCreatedPack();
                    ((MainActivity)getActivity()).showPackListView();
                } else if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_SNAPSHOT_ALL)) {
                    mCurrentPack = User.getPack(AppContext.getAppContext(),mCurrentPack.packID);
                    extraCardIndex = mCurrentPack.cards.size() -1;
                }

                ((MainActivity)getActivity()).packIDForMasterViewPack = mCurrentPack.packID;

                //step2: update list view
                if (extraFrom.equals(Global.BROADCAST_EXTRA_FROM_PACK_SELECTED)) {
                    updateListView(-1);
                } else {
                    updateListView(extraCardIndex);
                }
            }
        }

    }


    private void dragListItem(int from, int to) {

        Card card;
        if (from == to) {
            return;
        } else if (from < to) {

            for (int i = from + 1; i <= to; i++) {
                card = mCurrentPack.cards.get(i);
                card.cardSN = i;
                card.save(AppContext.getAppContext());
            }

            card = mCurrentPack.cards.get(from);
            card.cardSN = to + 1;
            card.save(AppContext.getAppContext());

        } else {

            for (int i = to; i < from; i++) {
                card = mCurrentPack.cards.get(i);
                card.cardSN = i + 2;
                card.save(AppContext.getAppContext());
            }

            card = mCurrentPack.cards.get(from);
            card.cardSN = to + 1;
            card.save(AppContext.getAppContext());

        }

        //Step3: reorder
        Collections.sort(mCurrentPack.cards, new Comparator<Card>() {
            @Override
            public int compare(Card lhs, Card rhs) {
                return (lhs.cardSN - rhs.cardSN);
            }
        });
    }


    private void removeListItem(int which) {

        //Step1: remove current card from mCurrentPack and database
        Card removedCard = mCurrentPack.cards.get(which);
        mCurrentPack.removeCard(AppContext.getAppContext(),removedCard);

        //Step2: reorder all cards' SN
        int index = 0;
        for (Card card:mCurrentPack.cards) {
            card.cardSN = index + 1;
            index ++;
        }

        //Step3: update list view
        updateListView(0);

        LOGD(TAG, "removeListItem: test point 0");
        //Step4: save change
        mCurrentPack.saveAllCards(AppContext.getAppContext());

        LOGD(TAG, "removeListItem: test point 1");

    }

    /**
     * @param selectedItemIndex, Don't update detail view when -1
     */
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
        ((MainActivity) getActivity()).setCurrentPack(mCurrentPack);

        //Step4: Update detail view
        if ((mCardArrayList.size() > 0) && (selectedItemIndex >= 0)) {


            mCallbacks.onItemSelected(selectedItemIndex);
            ((FCCdapter) adapter).setSelectedPosition(selectedItemIndex);
            adapter.notifyDataSetChanged();
            mDSLVListView.smoothScrollToPosition(selectedItemIndex);
        } else if (selectedItemIndex == -1) {
            //do nothing
        } else {
            //Clear detail view
            mCallbacks.onItemSelected(-1);
        }

        ((MainActivity) getActivity()).clearMaskButtonForContentUpdating();
    }

}
