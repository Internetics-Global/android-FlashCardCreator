package com.internectics.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Pack;
import com.internectics.model.CardListModel;
import com.internectics.util.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * CardListMasterFragment manage ListAdapter including updating ListView
 */
public class CardListMasterFragment extends ListFragment {

    MasterFragmentReceiver mReceiver;

	public Pack mCurrentPack;

	private List<HashMap<String, Object>> mCardArrayList;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;

	public CardListMasterFragment() {
		mCurrentPack = new Pack();
		mCardArrayList = new ArrayList<HashMap<String, Object>>();;
	}

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(int index);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public void onResume() {
		super.onResume();

        mReceiver = new MasterFragmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        getActivity().registerReceiver(mReceiver,filter);
		
		mCurrentPack = CardListModel.getCurrentPack();
		if (mCurrentPack != null) {
			mCardArrayList = CardListModel.getCardList(mCurrentPack);
		}
        SimpleAdapter listAdapter = new SimpleAdapter(getActivity(),
                mCardArrayList, R.layout.card_list_item, new String[] {
						"cardSN", "coverImageUriStr" }, new int[] {
                R.id.card_list_item_card_sn,
						R.id.card_list_item_cover_image });
		listAdapter.setViewBinder(new CardListBinder());
		setListAdapter(listAdapter);
        //Finally, send back currenPack to activity
        ((MainActivity) getActivity()).mCurrentPack = mCurrentPack;

	}

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
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
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		mCallbacks.onItemSelected(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

    /**
     * deal with broadcast and update view
     */
    private class MasterFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW)) {

                //step1: setmCurrentPack;
                String extraStr = intent.getExtras().getString(Global.KEY_FROM);

                if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_NEW_CARD) ||
                        extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_NEW_PACK)) {
                    mCurrentPack = CardListModel.getCurrentPack();
                } else if (extraStr.equals(Global.BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED)) {
                    int index = intent.getExtras().getInt("indexOfPack");
                    mCurrentPack =  CardListModel.getAllPacks().get(index);
                }

                //step2: update listview
                updateListView();


            } else if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //step1: set mCurrentPack
                //PackParserHelper.assembleCards();

                //step2: update listview
                updateListView();
            }



        }

        private void updateListView() {
            if (mCurrentPack != null) {
                mCardArrayList.clear();
                mCardArrayList.addAll(CardListModel.getCardList(mCurrentPack));
            }
            ((SimpleAdapter)getListAdapter()).notifyDataSetChanged();

            //Send back currenPack to activity
            ((MainActivity) getActivity()).mCurrentPack = mCurrentPack;

            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mCallbacks.onItemSelected(0);
            getListView().setItemChecked(0,true);
        }
    }
}
