package com.internectics.android_flashcardcreator;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.internectics.fragment.AddPackFragment;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.fragment.MasterFragment;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
public class MainActivity extends FragmentActivity implements
		MasterFragment.Callbacks {
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
       
        
        //Step2: check table and default user
        SQLiteHelper.defaultDatabase(AppContext.getAppContext());
        
        //Step3: OpenUDID
        OpenUDID_manager.sync(this);
        if (!OpenUDID_manager.isInitialized()) {
            Log.d(Global.debugTag, "OpenUDID_manager is not initialized");	
        }
		
		setContentView(R.layout.activity_card_twopane);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		switch (item.getItemId()) {
		case R.id.actionbar_add_pack:
		{
			DialogFragment dialogFragment = AddPackFragment.getInstance();
			dialogFragment.show(getFragmentManager(), "add_pack_fragment");
			break;
		}
		case R.id.actionbar_edit:
			AddPackFragment newFragment = AddPackFragment.getInstance();  
	        newFragment.show(getFragmentManager(), "dialog");  
			break;
		case R.id.actionbar_packs:
			Toast.makeText(this, "packs", Toast.LENGTH_SHORT).show();
            View popupLayout = inflater.inflate(R.layout.pack_list, null, false);
            final PopupWindow popupWindow = new PopupWindow(450, 200);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pack_list_background));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setContentView(popupLayout);
            popupWindow.showAsDropDown(findViewById(R.id.actionbar_packs));
			break;
			
		case R.id.actionbar_change_template_color:
			new AlertDialog.Builder(this)
			.setTitle("Select a template background")
			.setSingleChoiceItems(new String[]{"Blue","Coffee","Gray","Purple","Red"}, 0, null)
			.setPositiveButton("OK", null)
			.setNegativeButton("Cancel", null)
			.show();
			break;
		case R.id.actionbar_more:
			new AlertDialog.Builder(this)
			.setTitle("More")
			.setItems(new String[]{"Dropbox","Random play","Register","Submit new listing","Help","About"}, null)
			.show();
			break;
			
		case R.id.actionbar_play:
			startActivity(new Intent(MainActivity.this, PlayActivity.class));
			overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
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
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(CardDetailFragment.ARG_ITEM_ID, id);
			CardDetailFragment fragment = new CardDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.card_detail_container, fragment).commit();
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
