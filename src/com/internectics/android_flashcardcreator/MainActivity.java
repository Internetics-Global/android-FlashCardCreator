package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.internectics.fragment.AddPackFragment;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.fragment.MasterFragment;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;

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

    //Used to diff between card view and card creating
    private boolean isCreatingCard = false;

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

        Button addCardButton = (Button) this.findViewById(R.id.add_card_button);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Global.debugTag, "the add card button is clicked");

                startCreateCard();

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isCreatingCard == true) {
            menu.clear();
            getMenuInflater().inflate(R.menu.actionbar_add_card, menu);
            isCreatingCard = false;
        } else {
            menu.clear();
            getMenuInflater().inflate(R.menu.actionbar, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (item.getItemId()) {
            case R.id.actionbar_add_pack: {
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
                        .setSingleChoiceItems(new String[]{"Blue", "Coffee", "Gray", "Purple", "Red"}, 0, null)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
            case R.id.actionbar_more:
                new AlertDialog.Builder(this)
                        .setTitle("More")
                        .setItems(new String[]{"Dropbox", "Random play", "Register", "Submit new listing", "Help", "About"}, null)
                        .show();
                break;

            case R.id.actionbar_play:
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
                break;

            case R.id.actionbar_add_card_cancel:
                Log.d(Global.debugTag, "cancel button is clicked during adding card operation");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                Log.d(Global.debugTag,"save button is clicked during adding card operation");
                saveNewCreatedCard();
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


    private void startCreateCard() {

        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.VISIBLE);

        Button masterMaskButton = (Button) findViewById(R.id.master_view_mask);
        masterMaskButton.setVisibility(View.VISIBLE);
        masterMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCardCreateWindow();
            }
        });

        Bundle arguments = new Bundle();
        arguments.putString("item_id", "3");  //temp value
        CardDetailFragment fragment = new CardDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.add_card_frame_layout, fragment).commit();

        isCreatingCard = true;
        invalidateOptionsMenu();

    }

    private void saveNewCreatedCard() {
        dismissCardCreateWindow();

    }

    private void dismissCardCreateWindow() {
        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.INVISIBLE);

        Button masterMaskButton = (Button) findViewById(R.id.master_view_mask);
        masterMaskButton.setVisibility(View.INVISIBLE);

        isCreatingCard = false;
        invalidateOptionsMenu();

    }
}
