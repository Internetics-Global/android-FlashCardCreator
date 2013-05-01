package com.internectics.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.*;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Pack;
import com.internectics.model.CardListModel;
import com.internectics.util.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MasterFragment extends ListFragment {

    MasterFragmentReceiver mReceiver;

    SimpleAdapter listAdapter;

	public Pack currentPack;

	private List<HashMap<String, Object>> cardArrayList;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;

	public MasterFragment() {
		currentPack = new Pack();
		cardArrayList = new ArrayList<HashMap<String, Object>>();;
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
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

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
		
		currentPack = CardListModel.getCurrentPack();
		if (currentPack != null) {
			cardArrayList = CardListModel.getCardList(currentPack);
		}
        listAdapter = new SimpleAdapter(getActivity(),
				cardArrayList, R.layout.card_list_item, new String[] {
						"cardSN", "coverImageUriStr" }, new int[] {
						R.id.card_list_item_card_sn,
						R.id.card_list_item_cover_image });
		listAdapter.setViewBinder(new CardListBinder());
		setListAdapter(listAdapter);
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
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected("need to be updated here, ccaa");
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

    private class MasterFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("intent = " + intent);
            if (intent.getAction().equals(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW)) {
                currentPack = CardListModel.getCurrentPack();
                if (currentPack != null) {
                    cardArrayList.clear();
                    cardArrayList.addAll(CardListModel.getCardList(currentPack));
                }
                listAdapter.notifyDataSetChanged();
            }



        }
    }
}
