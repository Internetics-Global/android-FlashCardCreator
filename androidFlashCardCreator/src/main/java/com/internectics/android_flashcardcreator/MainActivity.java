package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.CreateEditFragment;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.fragment.CardListFragment;
import com.internectics.fragment.SymbolBoxFragment;
import com.internectics.helper.AWS.AWSUtils;
import com.internectics.helper.AWS.AWS_Constant;
import com.internectics.helper.AWS.S3UploadHelper;
import com.internectics.helper.AWS.SimpleDBHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackBuildHelper;
import com.internectics.helper.PackDownloadHelper;
import com.internectics.helper.PackRecordHelper;
import com.internectics.helper.SQLiteHelper;
import com.internectics.helper.ShareHelper;
import com.internectics.helper.SymbolHelper;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import com.internectics.util.StringUtils;
import com.internectics.util.TipHelper;
import com.internectics.util.UIHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * MainActivity is the entry for whole app
 * Control both master - detail view
 * Also responsbile for managing Actionbar(or Option Menu)
 */
public class MainActivity extends FragmentActivity implements
        CardListFragment.Callbacks {

    private boolean          mIsCreatingCard = false;
    public boolean           mIsEdittingCard = false;
    private boolean          mIsNecessaryToRestoreCSSToolbar = false;
    private boolean          mIsFromRestartApp = false;
    public boolean           mIsAllowedToShowPackList = true;
    public boolean           mIsKeyboardVisible; //we can NOT judge by imm.isActive
    private boolean          mIsAllowDownload;
    private boolean          mSemaphore;

    public Pack              mCurrentPack = new Pack();//mCurrentPack will be automatically refreshed after creating a new card, add a new pack and new pack selected
    public int               mCurrentCardIndex = 0;
    public Card              mCurrentCard = new Card();

    public PopupWindow       mPopupWindow;
    private View             mCSSToolbar;
    private Button           mMasterMaskButton;
    private View             mMasterViewUpdatingLayout;

    private ProgressDialog   mUploadProgressDialog;

    private ArrayList<CardDetailFragment> mArrayCardDetailFragments;   //Special for snapshot(not include current card)

    public CardDetailFragment  mCardDetailFragment;
    public SymbolBoxFragment   mSymbolBoxFragment;
    private Button             mSymbolKeyboardSwitchButton;

    public int                 packIDForMasterViewPack;

    private FrameLayout        mPackInfoLayout;

    private S3UploadHelper     uploadHelper ;

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
            Timber.tag(Global.debugTag).w("OpenUDID_manager is not initialized");
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_card_twopane);


        Button addCardButton = (Button) this.findViewById(R.id.add_card_button);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.tag(Global.debugTag).d("add card button  is clicked");
                startCreateCard();
                TipHelper.hideEverthing(MainActivity.this);

            }
        });


        mMasterMaskButton = (Button) findViewById(R.id.master_view_mask);
        mMasterViewUpdatingLayout = findViewById(R.id.master_view_updating_layout);

        mPackInfoLayout = (FrameLayout) findViewById(R.id.pack_info_layout);
        showPackInfoView();

        mSymbolBoxFragment = (SymbolBoxFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_symbol_box);

        mIsFromRestartApp = true;

        EasyTracker.getInstance().setContext(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int menuID;
        if (mIsCreatingCard) {
            menuID = R.menu.actionbar_add_card;
        } else {
            menuID = R.menu.actionbar;

        }

        menu.clear();
        getMenuInflater().inflate(menuID, menu);

        if ((UIHelper.getScreenWidthDPUnit(this) >= 600) && (mIsCreatingCard == false)) {
            MenuItem item = menu.findItem(R.id.actionbar_change_template_color);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            item = menu.findItem(R.id.actionbar_help);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            item = menu.findItem(R.id.actionbar_more);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }

        //update status
        MenuItem item = menu.findItem(R.id.actionbar_edit);
        if (item != null) {
            CardListFragment cardListFragment = (CardListFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_card_list));
            if (cardListFragment.getEditStyle()) {
                item.setTitle("done");
            } else {
                item.setTitle("edit");
            }
        }


        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (item.getItemId()) {
            case R.id.actionbar_add_pack: {
                DialogFragment dialogFragment = new CreateEditFragment();
                dialogFragment.show(getFragmentManager(), "add_pack_fragment");
                break;
            }
            case R.id.actionbar_edit:

                if (mCurrentPack == null) {
                    break;
                }

                if (!mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
                    Toast.makeText(this, "You can only make changes to cards you have created yourself", Toast.LENGTH_LONG).show();

                } else {
                    if ((mCurrentPack != null) && (mCurrentPack.cards.size() > 0)) {
                        CardListFragment cardListFragment = (CardListFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_card_list));
                        if (item.getTitle().toString().toUpperCase().equals("EDIT")) {
                            item.setTitle("done");
                            cardListFragment.enterEditStyle(true);
                        } else {
                            item.setTitle("edit");
                            cardListFragment.enterEditStyle(false);
                        }
                    }
                }

                break;
            case R.id.actionbar_packs:
                showPackListView();

                break;

            case R.id.actionbar_change_template_color:

                if (!mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
                    Toast.makeText(this, "You can only make changes to cards you have created yourself", Toast.LENGTH_LONG).show();

                }  else {
                    if (mCardDetailFragment == null) {
                        Toast.makeText(this, "You need to select a card beforehand", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    int defaultIndex = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[0];
                    if (mCurrentPack.cards.size() >= 0) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.change_title)
                                .setSingleChoiceItems(new String[]{getResources().getString(R.string.change_blue),
                                        getResources().getString(R.string.change_coffee), getResources().getString(R.string.change_gray),
                                        getResources().getString(R.string.change_purple), getResources().getString(R.string.change_red)}, defaultIndex,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                setMaskButtonForContentUpdating();
                                                mCardDetailFragment.cardColorTemplateSelectedPostAction(which);
                                            }
                                        })

                                .show();
                    }
                }
                break;
            case R.id.actionbar_more:
                startActivity(new Intent(MainActivity.this, MoreActivity.class));
                mIsAllowedToShowPackList = false;
                break;

            case R.id.actionbar_play:

                if (mCurrentPack.cards.size() > 0) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("packID", mCurrentPack.packID);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
                    mIsAllowedToShowPackList = false;
                }  else {

                    new AlertDialog.Builder(this)
                            .setTitle("Alert")
                            .setMessage("No card available")
                            .setPositiveButton("OK", null)
                            .show();
                }



                break;

            case R.id.actionbar_share_pack:
                if (Global.apiReachableWithAlert(MainActivity.this)) {
                    onActionbarShareItemSelected();
                }
                break;

            case R.id.actionbar_install_from_code:

                final EditText codeEditText = new EditText(this);
                codeEditText.setHint("lzupcb1");
                codeEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                codeEditText.setSingleLine();
                new AlertDialog.Builder(this)
                        .setTitle("Input download code")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(codeEditText)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String codeString = codeEditText.getText().toString();
                                if ((codeString != null) && (codeString.length() > 0)) {
                                    String finalURL = "http://tinyurl.com/" + codeString;
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalURL));
                                    startActivity(browserIntent);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                break;

            case R.id.actionbar_add_card_cancel:
                Timber.tag(Global.debugTag).d("Cancel button is clicked");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                Timber.tag(Global.debugTag).d("Save button is clicked");
                saveNewCreatedCard();
                break;

            case R.id.actionbar_help:

                boolean isAllowToShowTooltip = AppConfig.sharedInstance().isAllowToShowTooltip();
                if (isAllowToShowTooltip == false) {
                    AppConfig.sharedInstance().setAllowToShowTooltip(true);
                    if (mCardDetailFragment != null) {
                        mCardDetailFragment.showTooltips();
                        showTooltips();
                    } else {
                        showTooltips();
                    }
                } else {
                    AppConfig.sharedInstance().setAllowToShowTooltip(false);
                    TipHelper.hideEverthing(MainActivity.this);
                }


                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCurrentPack(Pack mCurrentPack) {
        this.mCurrentPack = mCurrentPack;
        updatePackInfoView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsNecessaryToRestoreCSSToolbar) {
            initializeCSSToolbar();
            mIsNecessaryToRestoreCSSToolbar = false;
        }

        //Step1: download sample pack first
        boolean isDownloaded = AppConfig.sharedInstance().isExamplePackDownloadedBefore();

        boolean isReachable = Global.apiReachable(MainActivity.this);

        if ((!isDownloaded) && (isReachable) && (mIsFromRestartApp)) {
            mIsFromRestartApp = false;
            String downloableShareLink = Global.SAMPLE_URL;
            File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
            PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
            packDownloadHelper.mIsFromExamplePackDownload = true;
            packDownloadHelper.execute();
            return;
        }

        //Step2: call from other app
        Uri data = getIntent().getData();
        if ((data != null) && (data.getScheme().equalsIgnoreCase("fcc"))) {
            //for download (not include sample pack
            if (Global.apiReachableWithAlert(MainActivity.this)) {

                String packFileName = data.getLastPathSegment();
                Global.currentAmazonSimpleDBItemName = packFileName.substring(0,packFileName.indexOf(".zip"));
                mSemaphore = false;

                //The reason why we design this is: network operation could not be done on main thread
                new Thread()
                {
                    @Override
                    public void run() {
                        mIsAllowDownload = checkDownloadable(Global.currentAmazonSimpleDBItemName);
                        mSemaphore = true;
                    }
                }.start();

                int timeoutCount = 0;    //set timeout = 5 second
                final int kTimeoutThreshold = 250;
                while ((mSemaphore == false) && (timeoutCount <kTimeoutThreshold)) {
                    try {
                        Thread.sleep(20);
                        timeoutCount ++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (timeoutCount == kTimeoutThreshold) {
                    Toast.makeText(this, "Network timeout, please try again", Toast.LENGTH_LONG).show();
                } else {
                    if (mIsAllowDownload) {
                        String downloableShareLink = data.toString().replace("fcc", "https").replace("www", "dl");
                        File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
                        PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
                        packDownloadHelper.execute();
                    }   else {
                        Toast.makeText(this, "You have reached the limit of downloads for this pack", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        getIntent().setData(null); //in case it will be recalled time and time

        //Used to show pack list
        if (mIsAllowedToShowPackList) {
            ShowPacklistAfterViewDidAppearTask dTask = new ShowPacklistAfterViewDidAppearTask();
            dTask.execute(100);
        }

        mIsAllowedToShowPackList = true;



    }

    public void showTooltips() {

        final Button addCardButton = (Button) this.findViewById(R.id.add_card_button);
        final Button shareButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_share);
        final Button settingButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_setting);
        final Button helpButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_help);
        final Button paletteButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_palette);
        final Button playButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_play);
        final Button createPackButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_create_pack);
        final Button editPackButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_edit_pack);
        final Button openPackButton = (Button) this.findViewById(R.id.tooltip_fake_actionbar_open_pack);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                TipHelper.showTipForCreateCard(MainActivity.this,addCardButton);


                TipHelper.showTipForOpenPack(MainActivity.this,openPackButton);
                TipHelper.showTipForEditPack(MainActivity.this,editPackButton);
                TipHelper.showTipForActionBarCreateNewPack(MainActivity.this,createPackButton);
                TipHelper.showTipForActionBarPlay(MainActivity.this,playButton);
                TipHelper.showTipForActionBarPalette(MainActivity.this,paletteButton);
                TipHelper.showTipForActionBarHelp(MainActivity.this,helpButton);
                TipHelper.showTipForActionBarSetting(MainActivity.this,settingButton);
                TipHelper.showTipForActionBarShare(MainActivity.this,shareButton);



            }

        }, 200);
    }

    public static int getActionBarSize(final Context context) {

        final int[] attrs;

        attrs = new int[]{android.R.attr.actionBarSize};


        TypedArray values = context.getTheme().obtainStyledAttributes(attrs);
        try {
            return values.getDimensionPixelSize(0, 0);
        } finally {
            values.recycle();
        }
    }

    public void showPackListView() {

        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(UIHelper.getScreenWidth(this)- 50, getResources().getDimensionPixelSize(R.dimen.pack_list_window_height));
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popupwindow_background));
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mPopupWindow.dismiss();

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fm = fragmentManager.findFragmentByTag("tag_pack_list_fragment");
                    fragmentManager.beginTransaction().remove(fm).commit();

                    if (AppConfig.sharedInstance().isAllowToShowTooltip()) {
                        showTooltips();
                    }
                }
            });


        }

        if ((mPopupWindow != null) &&(mPopupWindow.isShowing() == false)) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupLayout = inflater.inflate(R.layout.pack_list, null, false);
            if (popupLayout != null) {
                mPopupWindow.setContentView(popupLayout);
                View actionbarPacks = findViewById(R.id.actionbar_packs);
                if (actionbarPacks != null) {
                    mPopupWindow.showAsDropDown(actionbarPacks);
                }
            }
        }
    }


    private boolean checkDownloadable (String itemName) {
        Timber.tag(Global.debugTag).d("Now begin to execute checkDownloadable");

        boolean result = false;

        HashMap<String,String> rowData = SimpleDBHelper.getAttributesForItem(Global.amazon_sdb_domain_name, itemName);

        if (rowData.containsKey("currentNo")){
            Global.currentAmazonSimpleDBItemDownloadCount = Integer.parseInt(rowData.get("currentNo"));
        } else {
            Global.currentAmazonSimpleDBItemDownloadCount = 0;
        }

        int maxNo;
        if (rowData.containsKey("maxNo")) {
            maxNo = Integer.parseInt(rowData.get("maxNo"));
        } else {
            maxNo = 1000000; //as big as possible
        }


        if ((Global.currentAmazonSimpleDBItemDownloadCount < maxNo)  || (maxNo == 0)) {  //maxNo = 0 means no record in AmazonSDB
            result = true;
        } else {
            result = false;
        }

        return result;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if ((mCSSToolbar != null) && (mCSSToolbar.getParent() != null)) {
            removeCSSToolbar();
            mIsNecessaryToRestoreCSSToolbar = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (uploadHelper != null) {
            uploadHelper.stop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * @param index (index<0) is used to clear master and detail views
     */
    @Override
    public void onItemSelected(int index) {

        if (index >= 0) {
            mCurrentCardIndex = index;
            if (mCurrentPack.cards.size() > mCurrentCardIndex) {
                mCurrentCard = mCurrentPack.cards.get(mCurrentCardIndex);
                mCardDetailFragment = new CardDetailFragment();
                mCardDetailFragment.configureParameters(mCurrentPack, mCurrentCard, 0);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.card_detail_container, mCardDetailFragment).commitAllowingStateLoss();
            }  else {
                Timber.tag(Global.debugTag).e("Out of index of array during executing onItemSelected");
            }

        } else {
            mCurrentCardIndex = -1;
            mCurrentCard = null;
            if (mCardDetailFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(mCardDetailFragment).commitAllowingStateLoss();
            }
        }

        hidePackInfoView();
    }


    /**
     * @param pack, not 100% equal with mCurrentPack in MainActivity.java
     * @param card
     */
    private void prepareSnapShotSelectedCard(Pack pack, Card card) {
        CardDetailFragment snapshotCardDetailFragment = new CardDetailFragment();
        snapshotCardDetailFragment.configureParameters(pack, card, 3);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail, snapshotCardDetailFragment).commitAllowingStateLoss();

        if (mArrayCardDetailFragments == null) {
            mArrayCardDetailFragments = new ArrayList<CardDetailFragment>();
        }
        mArrayCardDetailFragments.add(snapshotCardDetailFragment);
    }


    /**
     * This is called by CardDetailFragment which represent current showing card in detail
     *
     * @param pack,       snapshot all the cards in this pack
     * @param exceptCard, except this
     */
    public void prepareSnapShotAllExceptOne(Pack pack, Card exceptCard) {

        ArrayList<Card> cards = pack.cards;
        for (Card card : cards) {
            if (card.cardID != exceptCard.cardID) {
                prepareSnapShotSelectedCard(pack, card);
            }
        }
    }

    /**
     * This is called by CardDetailFragment which represent current showing card in detail
     */
    public void finishSnapShotAllExceptCurrent() {

        if (mArrayCardDetailFragments == null)
            return;

        for (CardDetailFragment cardDetailFragment : mArrayCardDetailFragments) {
            getSupportFragmentManager().beginTransaction().remove(cardDetailFragment).commitAllowingStateLoss();
        }

        mArrayCardDetailFragments.clear();
        mArrayCardDetailFragments = null;

        //we don't need to conisder "during creating card" since we have disabled that
        //this used to free memory since we "except current" in finishSnapShotAllExceptCurrent
        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_SNAPSHOT_ALL);
        Bundle extraBundle = new Bundle();
        extraBundle.putInt("EXTRA_PACK_ID",mCurrentPack.packID);
        extraBundle.putInt("EXTRA_INDEX",mCurrentCardIndex);
        intent.putExtra("BUNDLE", extraBundle);
        sendBroadcast(intent);

    }

    public void finishSnapShot(CardDetailFragment fragment) {
        if ((fragment == null) || (fragment.mCurrentCard == null)) {
            return;
        }

        getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();

        Timber.tag(Global.debugTag).w(String.format("FinishSnapShot on cardSN = %d",fragment.mCurrentCard.cardID));
    }


    private void startCreateCard() {

        //mCurrentPack = CardListModel.getLatestCreatedPack();//don't need to do here
        boolean result = checkEntryConditionBeforeCreatingNewCard(mCurrentPack);
        if (result == false) {
            return;
        }

        hidePackInfoView();

        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.VISIBLE);

        mMasterMaskButton.setVisibility(View.VISIBLE);
        final Animation animAlphaUp = new AlphaAnimation(0.0f, 1.0f);
        animAlphaUp.setDuration(500);
        mMasterMaskButton.startAnimation(animAlphaUp);

        mMasterMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCardCreateWindow();

            }
        });


        mCardDetailFragment = new CardDetailFragment();
        mCardDetailFragment.configureParameters(mCurrentPack, null, 1);
        if (mCurrentPack.cards.size() >0) {
            //History of reason, we put templateBackground in Card, rather than Pack. It's not a good design practce anyway.
            mCardDetailFragment.mCurrentCard.templateBackground =  mCurrentPack.cards.get(0).templateBackground;
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.out_to_right)
                .replace(R.id.add_card_frame_layout, mCardDetailFragment)
                .commitAllowingStateLoss();

        mIsCreatingCard = true;
        invalidateOptionsMenu();

    }

    private void saveNewCreatedCard() {

        //1. do save action
        mCardDetailFragment.saveNewCreatedCard();

        //2. dismiss windows
        dismissCardCreateWindow();

        //3. notify CardListFragment view to update
        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_NEW_CARD);
        intent.putExtra(Global.KEY_CARD_INDEX, (mCurrentPack.cards.size()-1));
        sendBroadcast(intent);

    }

    private void dismissCardCreateWindow() {
        getSupportFragmentManager().beginTransaction().remove(mCardDetailFragment).commit();
        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.GONE);

        Button masterMaskButton = (Button) findViewById(R.id.master_view_mask);
        masterMaskButton.setVisibility(View.INVISIBLE);
        final Animation animAlphaUp = new AlphaAnimation(1.0f, 0.0f);
        animAlphaUp.setDuration(500);
        masterMaskButton.startAnimation(animAlphaUp);

        mIsCreatingCard = false;
        invalidateOptionsMenu();

        removeCSSToolbar();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow(mMasterMaskButton.getApplicationWindowToken() , 0 );
        }

        mCardDetailFragment = null;
        TipHelper.hideEverthing(MainActivity.this);

    }

    private boolean checkEntryConditionBeforeCreatingNewCard(Pack currentPack) {

        //case1: check whether pack is empty or not
        if (currentPack == null) {
            Toast.makeText(this, "Create a pack first before creating a new card", Toast.LENGTH_LONG).show();
            return false;
        }
        //case2: check owner
        if (!currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
            Toast.makeText(this, "You cannot create a card in pack you haven't created yourself.", Toast.LENGTH_LONG).show();
            return false;

        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }



    private void onActionbarShareItemSelected() {
        if (mCurrentPack == null) {
            Toast.makeText(this, "NO pack selected", Toast.LENGTH_LONG).show();
            return;
        }

        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

            if (PackRecordHelper.checkUploadPackNecessary(MainActivity.this, mCurrentPack)) {
                setPasswordAndUpload();
            } else {
                ShareHelper shareHelper = new ShareHelper(this,mCurrentPack,false);
                shareHelper.share();
            }

        } else {
            ShareHelper shareHelper = new ShareHelper(this, mCurrentPack,true);
            shareHelper.execute();

        }
    }


    private void setPasswordAndUpload() {
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        passwordEditText.setSingleLine();
        new AlertDialog.Builder(this)
                .setTitle("Set a password?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(passwordEditText)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        String password = passwordEditText.getText().toString();
                        File file = PackBuildHelper.createPackZipFile(MainActivity.this, mCurrentPack, password);
                        if (file == null) {
                            Toast.makeText(MainActivity.this, "Failed to zip pack", Toast.LENGTH_LONG).show();
                        } else {

                            //步骤： upload -- > 设置最大分享数 --> 创建短连接 --> 分享
                            uploadHelper = new S3UploadHelper(MainActivity.this,mUploadHandler);
                            uploadHelper.upload(file);
                        }

                    }
                })
                .setNegativeButton("Not needed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        File file = PackBuildHelper.createPackZipFile(MainActivity.this, mCurrentPack, "");
                        if (file == null) {
                            Toast.makeText(MainActivity.this, "Failed to zip pack", Toast.LENGTH_LONG).show();
                        } else {
                            //步骤： upload -- > 设置最大分享数 --> 创建短连接 --> 分享
                            uploadHelper = new S3UploadHelper(MainActivity.this,mUploadHandler);
                            uploadHelper.upload(file);
                        }

                    }
                })
                .show();
    }

    private void initializeCSSToolbar() {

        if (mCSSToolbar != null) {
            return;
        }

        //get actionbar height ( we can not directly use getActionbar.getHeight)
        TypedValue tv = new TypedValue();
        int actionbarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            Timber.tag(Global.debugTag).d("actionbar height is:" + actionbarHeight);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, actionbarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mCSSToolbar = inflater.inflate(R.layout.css_toolbar, null);
        mCSSToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Timber.tag(Global.debugTag).d("touching mCSSToolbar");
                return false;
            }
        });
        wm.addView(mCSSToolbar, params);
        mCSSToolbar.setVisibility(View.GONE);

        Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
        Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
        Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
        Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

        ArrayAdapter<CharSequence> adapterFont = ArrayAdapter.createFromResource(this,
                R.array.css_font, R.layout.spinner);
        adapterFont.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(adapterFont);

        ArrayAdapter<CharSequence> adapterAlign = ArrayAdapter.createFromResource(this,
                R.array.css_align, R.layout.spinner);
        adapterAlign.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlign.setAdapter(adapterAlign);

        ArrayAdapter<CharSequence> adapterColor = ArrayAdapter.createFromResource(this,
                R.array.css_color, R.layout.spinner);
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapterColor);

        ArrayAdapter<CharSequence> adapterSize = ArrayAdapter.createFromResource(this,
                R.array.css_size, R.layout.spinner);
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(adapterSize);

        spinnerFont.setSelection(0);
        spinnerAlign.setSelection(0);
        spinnerColor.setSelection(0);
        spinnerSize.setSelection(0);

        Button cssSaveButton = (Button) mCSSToolbar.findViewById(R.id.csstoolbar_save_btn);
        cssSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCardDetailFragment.dismissKeyboard();
                if (mSymbolBoxFragment!=null) {
                    mSymbolBoxFragment.hideSymbolBoxWithAnimation(true);
                }
                removeCSSToolbar();

                if (mIsCreatingCard) {
                    saveNewCreatedCard();
                } else {
                    new Thread() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mCardDetailFragment.saveEdittedCard();
                                }
                            });
                        };
                    }.start();
                }
            }
        });
        Button cssCancelButton = (Button) mCSSToolbar.findViewById(R.id.csstoolbar_close_btn);
        cssCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSymbolBoxFragment!=null) {
                    mSymbolBoxFragment.hideSymbolBoxWithAnimation(true);
                }
                mCardDetailFragment.dismissKeyboard();
                removeCSSToolbar();

                if (mIsCreatingCard) {
                    dismissCardCreateWindow();
                }
            }
        });

        mSymbolKeyboardSwitchButton = (Button) mCSSToolbar.findViewById(R.id.csstoolbar_symbol_btn);
        mSymbolKeyboardSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                if (mIsKeyboardVisible) {

                    if (mCardDetailFragment.isCurrentFocusedCardContentTextUsingDefaultFont() == false){
                        Toast.makeText(getApplicationContext(),"ymbol could possibly not be supported by selected font, please check",Toast.LENGTH_LONG).show();
                    } else {
                        setAsSymbolStatus();
                        mIsKeyboardVisible = false;
                    }



                } else {
                    setAsKeyboardStatus();
                    mIsKeyboardVisible = true;
                }

            }
        });

        spinnerFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) //this is necessary, since default will be automatically executed
                {
                    if (SymbolHelper.isSymbolIncluded(mCardDetailFragment.mCurrentFocusedCardContentText.getText().toString())) {
                        Toast.makeText(getApplicationContext(),"You can not change font once text includes symbol",Toast.LENGTH_LONG).show();

                    } else {
                        mCardDetailFragment.updateCSS(3, position - 1);
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spinnerAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) //this is necessary, since default will be automatically executed
                    mCardDetailFragment.updateCSS(0, position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0)
                    mCardDetailFragment.updateCSS(1, position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0)
                    mCardDetailFragment.updateCSS(2, position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    public void setAsSymbolStatus() {
        mSymbolBoxFragment.showSymbolBoxWithAnimation(false);
        mSymbolKeyboardSwitchButton.setText("Keyboard");
    }

    public void setAsKeyboardStatus() {
        mSymbolBoxFragment.hideSymbolBoxWithAnimation(false);
        if (mSymbolKeyboardSwitchButton != null) {
            mSymbolKeyboardSwitchButton.setText("Symbol");
        }

    }

    public void prepareCSSToolbar() {
        if ((mCSSToolbar == null) || (mCSSToolbar.getParent() == null)) {
            initializeCSSToolbar();
            Timber.tag(Global.debugTag).d("initializeCSSToolbar is called");
        }
    }


    public void showCSSToolbar() {
        if ((mCSSToolbar != null) && (mCSSToolbar.getParent() != null)) {
            mCSSToolbar.setVisibility(View.VISIBLE);

            //Rest spinner title when touch another textfield
            Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
            Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
            Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
            Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

            Button saveButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_save_btn);
            Button cancelButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_close_btn);

            spinnerFont.setSelection(0);
            spinnerAlign.setSelection(0);
            spinnerColor.setSelection(0);
            spinnerSize.setSelection(0);

            Timber.tag(Global.debugTag).d("prepareCSSToolbar is called");

            mIsKeyboardVisible = true;

            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    public void removeCSSToolbar() {
        if (mCSSToolbar == null) {
            Timber.tag(Global.debugTag).w("rmCSSToolbar is null when executing removeCSSToolbar");
            return;
        } else {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (mCSSToolbar.getParent() != null) {
                mCSSToolbar.setVisibility(View.GONE);
                wm.removeView(mCSSToolbar);
                mCSSToolbar = null;
                Timber.tag(Global.debugTag).d("removeCSSToolbar is called");
            }

            mIsKeyboardVisible = false;

        }
    }

    public void setMaskButtonForContentUpdating() {
        mMasterViewUpdatingLayout.setVisibility(View.VISIBLE);

    }

    public void clearMaskButtonForContentUpdating() {
        mMasterViewUpdatingLayout.setVisibility(View.INVISIBLE);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mIsCreatingCard == true) {
            dismissCardCreateWindow();
            return false;
        }

        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {

            if ((mSymbolBoxFragment !=null) && (mSymbolBoxFragment.isSymbolBoxVisible())) {
                mSymbolBoxFragment.hideSymbolBoxWithAnimation(false);
                return false;
            }

            dialog_Exit(MainActivity.this);
        }
        return false;

    }


    public void dialog_Exit(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to exit?");
        builder.setTitle("Alert");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton("Cancel",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    private void showPackInfoView() {
        mPackInfoLayout.setVisibility(View.VISIBLE);
    }

    private void hidePackInfoView() {

        mPackInfoLayout.setVisibility(View.INVISIBLE);

    }

    private void updatePackInfoView() {

        if (mCurrentPack == null) {
            hidePackInfoView();
            return;
        }

        ImageView packCoverImageView = (ImageView) findViewById(R.id.pack_info_cover_image);
        if (mCurrentPack.coverImageUriFormatStr.contains("default")
                || mCurrentPack.coverImageUriFormatStr.contains("placeholder")
                     || mCurrentPack.coverImageUriFormatStr.length() == 0) {
            packCoverImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_pack_cover_image_transparent));
        } else {
            packCoverImageView.setImageURI(Uri.parse(mCurrentPack.coverImageUriFormatStr));
        }

        TextView  packCoverTextView = (TextView) findViewById(R.id.pack_info_title);
        packCoverTextView.setText(mCurrentPack.packName);
    }


    private final Handler mUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AWS_Constant.UPLOAD_PROGRESS:
                    File file = (File) msg.obj;
                    int flag = msg.arg1; //indicate whether upload is finished or not
                    int percent = msg.arg2;

                    if (mUploadProgressDialog == null) {
                        mUploadProgressDialog = new ProgressDialog(MainActivity.this);
                        mUploadProgressDialog.setMax(100);
                        mUploadProgressDialog.setMessage("Uploading...");
                        mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    }

                    if (flag == 0) {
                        mUploadProgressDialog.dismiss();

                        Toast.makeText(MainActivity.this, "Pack successfully uploaded", Toast.LENGTH_SHORT).show();

                        String fullPath_S3 = AWSUtils.fullPath_S3(file.getName());
                        PackRecordHelper.save(MainActivity.this, mCurrentPack, null, fullPath_S3);  //因为这时还没有share link，所以设置为null

                        ShareHelper shareHelper = new ShareHelper(MainActivity.this, mCurrentPack,false);
                        shareHelper.execute();

                    } else {
                        if (mUploadProgressDialog.isShowing() == false) {
                            mUploadProgressDialog.show();
                        }
                        mUploadProgressDialog.setProgress(percent);
                    }
                    break;
            }
        }
    };




    /**
     *
     */
    class ShowPacklistAfterViewDidAppearTask extends AsyncTask<Integer, Integer, String> {

        final View myView = findViewById(R.id.card_list);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {

            while (myView.getHeight() == 0 || myView.getWidth() == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showPackListView();

        }

    }


}
