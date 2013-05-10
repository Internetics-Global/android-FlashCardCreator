package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.AddPackFragment;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.fragment.CardListMasterFragment;
import com.internectics.fragment.MoreFragment;
import com.internectics.helper.*;
import com.internectics.util.*;

import java.io.File;
import java.util.ArrayList;

/**
 * MainActivity is the entry for whole app
 * Control both master - detail view
 * Also responsbile for managing Actionbar(or Option Menu)
 */
public class MainActivity extends FragmentActivity implements
        CardListMasterFragment.Callbacks {

    //Used to diff whether is on card view and card creating
    private boolean mIsCreatingCard = false;

    public Pack mCurrentPack = new Pack();//mCurrentPack will be automatically refreshed after creating a new card, add a new pack and new pack selected
    public int mCurrentIndex = 0;
    public Card mCurrentCard = new Card();

    public PopupWindow mPopupWindow;

    //Progress dialog related
    private static final int DIALOG_UPLOADING_PACK = 0;
    ProgressDialog mDialog;

    private boolean mIsUploadingPackAfterLinked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Step1: check table and default user
        SQLiteHelper.defaultDatabase(AppContext.getAppContext());

        //step2: background of to-do-this. we hope to use Uri globally including resource files
        FileOperationHelper.copyResourcesImagesToCache(MainActivity.this);

        //Step2: OpenUDID
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
        if (mIsCreatingCard == true) {
            menu.clear();
            getMenuInflater().inflate(R.menu.actionbar_add_card, menu);
            mIsCreatingCard = false;
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
                PackBuildHelper.buildCardJsonFile(new Card());

                PackBuildHelper.buildPackJsonFile(new Pack());

                PackParserHelper.parsePackJsonFile(FileOperationHelper.getTestFile2().toString());

                PackParserHelper.parseCardJsonFile(FileOperationHelper.getTestFile().toString());

                break;
            case R.id.actionbar_packs:
                Log.d(Global.debugTag, "You have selected menu item of pack");
                View popupLayout = inflater.inflate(R.layout.pack_list, null, false);
                mPopupWindow = new PopupWindow(640, 360);
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popupwindow_background));
                mPopupWindow.setContentView(popupLayout);
                mPopupWindow.showAsDropDown(findViewById(R.id.actionbar_packs));
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
                MoreFragment moreFragment = MoreFragment.getInstance();
                moreFragment.show(getFragmentManager(), "more_fragment");
                break;

            case R.id.actionbar_play:
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
                break;

            case R.id.actionbar_share:
                onActionbarShareSelected();
                break;

            case R.id.actionbar_add_card_cancel:
                Log.d(Global.debugTag, "cancel button is clicked during adding card operation");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                Log.d(Global.debugTag, "save button is clicked during adding card operation");
                saveNewCreatedCard();
                break;

            case R.id.actionbar_help:
                startActivity(new Intent(MainActivity.this, InstructionActivity.class));
                break;

            case R.id.actionbar_test:
                ArrayList <String> file = new ArrayList<String>();
                file.add(FileOperationHelper.getTestFile().toString());
                file.add(FileOperationHelper.getTestFile2().toString());
                File xx = new File(FileOperationHelper.cacheDirectory(),"fuck.zip");
                try {
                    ZipFileHelper.zipFiles(xx.toString(),file);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
//                String downloadURL = "http://dl.dropbox.com/s/zejhwnb3x87d0vz/card1361506195.8733811651867036.zip";
//                PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this,downloadURL);
//                packDownloadHelper.execute();
                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Uri data = getIntent().getData();
        if ((data != null) && (data.getScheme().equalsIgnoreCase("fcc"))) {
            //called from outside app like browser
            //TODO
            String downloableShareLink = data.toString().replace("fcc","http").replace("wwww","dl");
            PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this,downloableShareLink);
            packDownloadHelper.execute();


        } else {
            if (true == mIsUploadingPackAfterLinked) {
                AndroidAuthSession session = DropboxHelper.getDropboxAPI(this).getSession();
                if (session.authenticationSuccessful()) {
                    session.finishAuthentication(); // Mandatory call to complete the auth
                    // Store it locally in our app for later use
                    TokenPair tokens = session.getAccessTokenPair();
                    DropboxHelper.storeKeys(this, tokens.key, tokens.secret);
                    mIsUploadingPackAfterLinked = false;
                    uploadingPackAfterLinked();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Callback method from {@link com.internectics.fragment.CardListMasterFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int index) {

        mCurrentIndex = index;
        mCurrentCard = mCurrentPack.cards.get(mCurrentIndex);
        CardDetailFragment fragment = new CardDetailFragment(mCurrentPack, mCurrentCard);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_detail_container, fragment).commit();
    }


    private void startCreateCard() {

        //mCurrentPack = CardListModel.getCurrentPack();//don't need to do here
        boolean result = checkEntryConditionBeforeCreatingNewCard(mCurrentPack);
        if (result == false) {
            return;
        }

        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.VISIBLE);

        Button masterMaskButton = (Button) findViewById(R.id.master_view_mask);
        masterMaskButton.setVisibility(View.VISIBLE);
        final Animation animAlphaUp = new AlphaAnimation(0.0f, 1.0f);
        animAlphaUp.setDuration(500);
        masterMaskButton.startAnimation(animAlphaUp);
        masterMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCardCreateWindow();
            }
        });

        CardDetailFragment fragment = new CardDetailFragment(mCurrentPack, null);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.out_to_right)
                .replace(R.id.add_card_frame_layout, fragment)
                .commit();

        mIsCreatingCard = true;
        invalidateOptionsMenu();

    }

    private void saveNewCreatedCard() {
        //1. do save action
        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_SAVE_NEW_CARD);
        sendBroadcast(intent);

        //2. dismiss windows
        dismissCardCreateWindow();

        //3. notify CardListMasterFragment view to update
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_INTENT_EXTRA_FROM_NEW_CARD);
        sendBroadcast(intent);

    }

    private void dismissCardCreateWindow() {
        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.INVISIBLE);

        Button masterMaskButton = (Button) findViewById(R.id.master_view_mask);
        masterMaskButton.setVisibility(View.INVISIBLE);
        final Animation animAlphaUp = new AlphaAnimation(1.0f, 0.0f);
        animAlphaUp.setDuration(500);
        masterMaskButton.startAnimation(animAlphaUp);

        mIsCreatingCard = false;
        invalidateOptionsMenu();

    }

    private boolean checkEntryConditionBeforeCreatingNewCard(Pack currentPack) {

        //case1: check whether pack is empty or not
        if (currentPack == null) {
            Toast.makeText(this, "Create a pack first before creating a new card", 1).show();
            return false;
        }
        //case2: check owner
        if (!currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
            Toast.makeText(this, "You cannot create a card in pack you haven't created yourself.", 1).show();


            return false;

        }

        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);
        switch (id) {
            case DIALOG_UPLOADING_PACK: {
                mDialog = new ProgressDialog(this);
                mDialog.setMessage("Please wait while loading...");
                mDialog.setIndeterminate(false);
                mDialog.setMax(100);
                mDialog.setCancelable(false);
                return mDialog;
            }
        }
        return null;
    }

    private void onActionbarShareSelected() {
        if (mCurrentPack == null) {
            Toast.makeText(this, "NO pack selected", Toast.LENGTH_LONG).show();
            return;
        }
        DropboxAPI<AndroidAuthSession> mDBApi = DropboxHelper.getDropboxAPI(this);
        if (mDBApi.getSession().isLinked()) {
            uploadingPackAfterLinked();
        } else {
            mIsUploadingPackAfterLinked = true;
            mDBApi.getSession().startAuthentication(MainActivity.this);
        }
    }

    private void uploadingPackAfterLinked() {
        if (PackRecordHelper.checkUploadPackNecessary(MainActivity.this,mCurrentPack)) {
            AndroidAuthSession session = DropboxHelper.getDropboxAPI(this).getSession();
            if (session.isLinked()) {
                File file = PackBuildHelper.createPackZipFile(mCurrentPack);
                //File file = new File(FileOperationHelper.getTestFile().toString()); test purpose
                PackUploadHelper upload = new PackUploadHelper(this, "/FlashCardCreator/", file, mCurrentPack);
                upload.execute();
            }
        } else {
            String shareLink = AppConfig.getInstance(this).getCurrentPackShareLink(mCurrentPack);
            ShareLinkHelper shareLinkHelper = new ShareLinkHelper(this,shareLink,mCurrentPack);
            shareLinkHelper.execShareAction();
        }
    }




}
