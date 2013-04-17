package com.internectics.android_flashcardcreator;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.internectics.helper.SQLiteHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;


/**
 * An activity representing a list of Cards. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link CardDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CardListFragment} and the item details (if present) is a
 * {@link CardDetailFragment}.
 * <p>
 * This activity also implements the required {@link CardListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class CardListActivity extends FragmentActivity implements
		CardListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	
	/**
	 * Dropbox key and secret
	 */
	final static private String APP_KEY = "rl7510fe1641dyl";
    final static private String APP_SECRET = "3twb9tcccje56kg";
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    
    /**
	 * You don't need to change these, leave them alone.
	 */
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    
    private DropboxAPI<AndroidAuthSession> mApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Step1:We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
        //Step2: check 
        //SQLiteHelper.defaultDatabase(this);
		
		setContentView(R.layout.activity_card_list);
	  

		if (findViewById(R.id.card_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((CardListFragment) getSupportFragmentManager().findFragmentById(
					R.id.card_list)).setActivateOnItemClick(true);
		}
		


		// TODO: If exposing deep links into your app, handle intents here.
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.card_list, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		switch (item.getItemId()) {
		case R.id.add_pack:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(inflater.inflate(R.layout.add_pack, null))
			       .setNegativeButton("Cancel", null)
			       .setPositiveButton("Save", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.out.println("Click the Save button");
						
					}
				});
			AlertDialog ad = builder.create();
			ad.setMessage("Add a new pack");
			ad.show();
			break;
		case R.id.menu_edit:
			Toast.makeText(this, "edit", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_packs:
			Toast.makeText(this, "packs", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "add_pack", Toast.LENGTH_SHORT).show();
            View popupLayout = inflater.inflate(R.layout.pack_list, null, false);
            final PopupWindow popupWindow = new PopupWindow(450, 200);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pack_list_background));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setContentView(popupLayout);
            popupWindow.showAsDropDown(findViewById(R.id.add_pack));
			break;

		default:
			break;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	/**
	 * Callback method from {@link CardListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(CardDetailFragment.ARG_ITEM_ID, id);
			CardDetailFragment fragment = new CardDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.card_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, CardDetailActivity.class);
			detailIntent.putExtra(CardDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
}
