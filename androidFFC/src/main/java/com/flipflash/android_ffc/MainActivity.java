package com.flipflash.android_ffc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flipflash.UI.ScaleHelper;
import com.flipflash.data.CSS;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.analytics.tracking.android.EasyTracker;
import com.flipflash.cryptor.CryptoHelper;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.fragment.CardListFragment;
import com.flipflash.fragment.CreateEditFragment;
import com.flipflash.fragment.SymbolBoxFragment;
import com.flipflash.helper.AWS.AWSShareHelper;
import com.flipflash.helper.AWS.AWS_Constant;
import com.flipflash.helper.AWS.AWSUploadHelper;
import com.flipflash.helper.AWS.SimpleDBHelper;
import com.flipflash.helper.AudioHelper;
import com.flipflash.helper.Dropbox.DropboxAuthHelper;
import com.flipflash.helper.Dropbox.DropboxShareHelper;
import com.flipflash.helper.Dropbox.DropboxUploadHelper;
import com.flipflash.helper.Dropbox.Dropbox_Constant;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackBuildHelper;
import com.flipflash.helper.PackDownloadHelper;
import com.flipflash.helper.PackRecordHelper;
import com.flipflash.helper.SQLiteHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;
import com.flipflash.util.TipHelper;
import com.flipflash.util.UIHelper;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * MainActivity is the entry for whole app
 * Control both master - detail view
 * Also responsbile for managing Actionbar(or Option Menu)
 */
public class MainActivity extends FragmentActivity implements
        CardListFragment.Callbacks {

    private static final String TAG = MainActivity.class.getName();

    private static final int LOGIN_REQUEST = 0;

    private boolean           mIsCreatingCard = false;
    public  boolean           mIsEdittingCard = false;
    private boolean           mIsNecessaryToRestoreCSSToolbar = false;
    private boolean           mIsFromRestartApp = false;
    public  boolean           mIsAllowedToShowPackList = true;
    public  boolean           mIsKeyboardVisible; //we can NOT judge by imm.isActive
    private boolean           mIsAllowDownload;
    private boolean           mSemaphore;

    public Pack               mCurrentPack = new Pack();//mCurrentPack will be automatically refreshed after creating a new card, add a new pack and new pack selected
    public int                mCurrentCardIndex = 0;
    public Card               mCurrentCard = new Card();

    public  PopupWindow       mPopupWindow;
    private View              mCSSToolbar;
    private Button            mMasterMaskButton;
    private View              mMasterViewUpdatingLayout;

    private ProgressDialog    mUploadProgressDialog;

    private ArrayList<CardDetailFragment> mArrayCardDetailFragments;   //Special for snapshot(not include current card)

    public  CardDetailFragment   mCardDetailFragment;
    public  SymbolBoxFragment    mSymbolBoxFragment;
    private Button               mSymbolKeyboardSwitchButton;

    public int                   packIDForMasterViewPack;

    private LinearLayout         mPackInfoLayout;

    private AWSUploadHelper      mAmazonUploadHelper ;
    private DropboxUploadHelper  mDropboxUploadHelper ;

    private DonutProgress      mRecordStopProgress;
    private Button             mRecordStopButton;
    private Timer              mRecordCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate: ");

        //Step1: check table and default user
        SQLiteHelper.defaultDatabase(AppContext.getAppContext());

        //step2: background of to-do-this. we hope to use Uri globally including resource files
        FileOperationHelper.copyResourcesImagesToCache(MainActivity.this);

        //Step2: OpenUDID
        OpenUDID_manager.sync(this);
        if (!OpenUDID_manager.isInitialized()) {
            LOGD(TAG, "onCreate: OpenUDID_manager is not initialized");
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_card_twopane);

        mRecordStopProgress = (DonutProgress) findViewById(R.id.record_stop_progress);
        mRecordStopButton = (Button) findViewById(R.id.record_stop_button);
        mRecordStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordStopButtonClicked();
            }
        });


        Button addCardButton = (Button) this.findViewById(R.id.add_card_button);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LOGD(TAG, "onClick: add card button  is clicked");
                startCreateCard();
                TipHelper.hideEverthing(MainActivity.this);

            }
        });


        mMasterMaskButton = (Button) findViewById(R.id.master_view_mask);
        mMasterViewUpdatingLayout = findViewById(R.id.master_view_updating_layout);

        mPackInfoLayout = (LinearLayout) findViewById(R.id.pack_info_layout);
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
                //item.setTitle("done");
                item.setIcon(getResources().getDrawable(R.drawable.pack_edit_finished));
            } else {
                //item.setTitle("edit");
                item.setIcon(getResources().getDrawable(R.drawable.pack_edit));
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

                    new SweetAlertDialog(this)
                            .setTitleText(getResources().getString(R.string.DIALOG_AlERT))
                            .setContentText(getResources().getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();

                } else {
                    if ((mCurrentPack != null) && (mCurrentPack.cards.size() > 0)) {
                        CardListFragment cardListFragment = (CardListFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_card_list));
                        if (cardListFragment.getEditStyle() == false) {
                            //item.setTitle("done");
                            item.setIcon(getResources().getDrawable(R.drawable.pack_edit_finished));
                            cardListFragment.enterEditStyle(true);
                        } else {
                            //item.setTitle("edit");
                            item.setIcon(getResources().getDrawable(R.drawable.pack_edit));
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
                    new SweetAlertDialog(this)
                            .setTitleText(getString(R.string.DIALOG_AlERT))
                            .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();

                }  else {
                    if (mCardDetailFragment == null) {
                        Toast.makeText(this, getString(R.string.DIALOG_SELECT_CARD_BEFOREHAND), Toast.LENGTH_SHORT).show();
                        break;
                    }

                    int defaultIndex = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[0];
                    if (mCurrentPack.cards.size() >= 0) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.Title_Select_Template)
                                .setSingleChoiceItems(new String[]{getResources().getString(R.string.Optional_Blue),
                                        getResources().getString(R.string.Optional_Coffee), getResources().getString(R.string.Optional_Gray),
                                        getResources().getString(R.string.Optional_Purple), getResources().getString(R.string.Optional_Red)}, defaultIndex,
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

            case R.id.actionbar_play_auto:
                if (mCurrentPack.cards.size() > 0) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("packID", mCurrentPack.packID);
                    intent.putExtra("oneOffPlayType",1);  //manually
                    startActivity(intent);
                    mIsAllowedToShowPackList = false;
                }  else {

                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.DIALOG_AlERT))
                            .setMessage(getString(R.string.DIALOG_NO_CARD_AVAILABLE))
                            .setPositiveButton(getString(R.string.DIALOG_OK), null)
                            .show();
                }
                break;

            case R.id.actionbar_play_auto_loop:
                if (mCurrentPack.cards.size() > 0) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("packID", mCurrentPack.packID);
                    intent.putExtra("oneOffPlayType",2);  //manually
                    startActivity(intent);
                    mIsAllowedToShowPackList = false;
                }  else {

                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.DIALOG_AlERT))
                            .setMessage(getString(R.string.DIALOG_NO_CARD_AVAILABLE))
                            .setPositiveButton(getString(R.string.DIALOG_OK), null)
                            .show();
                }
                break;

            case R.id.actionbar_play_manually:

                if ((mCurrentPack != null) && (mCurrentPack.cards.size() > 0)) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("packID", mCurrentPack.packID);
                    intent.putExtra("oneOffPlayType",0);  //manually
                    startActivity(intent);
                    mIsAllowedToShowPackList = false;
                }  else {

                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.DIALOG_AlERT))
                            .setMessage(getString(R.string.DIALOG_NO_CARD_AVAILABLE))
                            .setPositiveButton(getString(R.string.DIALOG_OK), null)
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
                        .setTitle(R.string.DIALOG_INPUT_DOWNLOAD_CODE)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(codeEditText)
                        .setPositiveButton(R.string.DIALOG_DONE, new DialogInterface.OnClickListener() {
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
                        .setNegativeButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                break;

            case R.id.actionbar_add_card_cancel:
                LOGD(TAG, "onOptionsItemSelected: Cancel button is clicked");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                LOGD(TAG, "onOptionsItemSelected: Save button is clicked");
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


    private void recordStopButtonClicked() {

        if (mRecordCountDownTimer != null) {
            mRecordCountDownTimer.cancel();
            mRecordCountDownTimer = null;
        }

        AudioHelper.isRecordFinished = true;

        mCardDetailFragment.showCreateSoundView();

        findViewById(R.id.record_button_background_mask_layout).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LOGD(TAG, "onResume");

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

        //Step2: call from other app or Dropbox log in
        Uri data = getIntent().getData();
        if ((data != null) && (data.getScheme().equalsIgnoreCase("fcc"))) {

            mIsAllowedToShowPackList = false;

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
                    Toast.makeText(this, R.string.DIALOG_NETWORK_TIMEOUT, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (mIsAllowDownload) {
                        String downloableShareLink = data.toString().replace("fcc", "https").replace("www", "dl");
                        File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
                        PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
                        packDownloadHelper.execute();
                    }   else {
                        Toast.makeText(this, R.string.DIALOG_REACH_MAX_DOWNLOAD_LIMIT, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        getIntent().setData(null); //in case it will be recalled time and time

        //Used to show pack list
        if (mIsAllowedToShowPackList) {

            final View appMainView = findViewById(R.id.app_main);

            if (appMainView.getHeight() > 0 && appMainView.getWidth() > 0) {
                //如果已经渲染完毕
                showPackListView();
            } else {
                appMainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            appMainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            appMainView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showPackListView();//放在Handler中是一个trick，实际发现，如果没有这个，则不会显示pack list
                            }

                        }, 100); // 100ms delay

                    }
                });
            }
        }


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
                TipHelper.showTipForCreateCard(MainActivity.this, addCardButton);


                TipHelper.showTipForOpenPack(MainActivity.this, openPackButton);
                TipHelper.showTipForEditPack(MainActivity.this, editPackButton);
                TipHelper.showTipForActionBarCreateNewPack(MainActivity.this, createPackButton);
                TipHelper.showTipForActionBarPlay(MainActivity.this, playButton);
                TipHelper.showTipForActionBarPalette(MainActivity.this, paletteButton);
                TipHelper.showTipForActionBarHelp(MainActivity.this, helpButton);
                TipHelper.showTipForActionBarSetting(MainActivity.this, settingButton);
                TipHelper.showTipForActionBarShare(MainActivity.this, shareButton);


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
            mPopupWindow = new PopupWindow(UIHelper.getScreenWidth(this)-50, getResources().getDimensionPixelSize(R.dimen.pack_list_window_height));
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popupwindow_background));
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mPopupWindow = null;

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fm = fragmentManager.findFragmentByTag("tag_pack_list_fragment");
                    fragmentManager.beginTransaction().remove(fm).commitAllowingStateLoss();
                    fragmentManager.executePendingTransactions();

                    if (AppConfig.sharedInstance().isAllowToShowTooltip()) {
                        showTooltips();
                    }
                }
            });
        }

        if (mPopupWindow.isShowing() == false) {
            View popupLayout =  mPopupWindow.getContentView();
            if (popupLayout == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                popupLayout = inflater.inflate(R.layout.pack_list, null, false);
                if (popupLayout != null) {
                    mPopupWindow.setContentView(popupLayout);
                } else {
                    LOGE(TAG, "showPackListView: Failed to inflate, please check");
                }
            }

            View actionbarPacks = findViewById(R.id.actionbar_packs);
            if ((popupLayout != null) && (actionbarPacks != null)) {
                //mPopupWindow.showAsDropDown(actionbarPacks);
                View appMainView = findViewById(R.id.app_main);
                int actionbarHeight = UIHelper.getActionbarHeight(MainActivity.this);
                mPopupWindow.showAtLocation(appMainView,Gravity.LEFT|Gravity.TOP,25,actionbarHeight + 2);
            }

        } else {
           dismissPackListPopupWindow();
        }
    }

    public void dismissPackListPopupWindow() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }


    private boolean checkDownloadable(String itemName) {
        LOGD(TAG, "checkDownloadable: Now begin to execute checkDownloadable");

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

        if (mRecordCountDownTimer != null) {
            mRecordCountDownTimer.cancel();
            mRecordCountDownTimer = null;
        }

        if (mAmazonUploadHelper != null) {
            mAmazonUploadHelper.stop();
        }

        if (mDropboxUploadHelper != null) {
            mDropboxUploadHelper.cancel(true);
            mDropboxUploadHelper = null;
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
                mCardDetailFragment.setupParameters(mCurrentPack, mCurrentCard, 0);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.card_detail_container, mCardDetailFragment).commitAllowingStateLoss();
            } else {
                LOGE(TAG, "onItemSelected: Out of index of array during executing onItemSelected");
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
    private void prepareSnapOnShotSelectedCard(Pack pack, Card card) {
        CardDetailFragment snapshotCardDetailFragment = new CardDetailFragment();
        snapshotCardDetailFragment.setupParameters(pack, card, 3);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail, snapshotCardDetailFragment).commitAllowingStateLoss();

        if (mArrayCardDetailFragments == null) {
            mArrayCardDetailFragments = new ArrayList<>();
        }
        mArrayCardDetailFragments.add(snapshotCardDetailFragment);
    }


    /**
     * This is called by CardDetailFragment which represent current showing card in detail
     *
     * @param pack,       snapshot all the cards in this pack
     * @param exceptCard, except this
     */
    public void prepareSnapShotAllExceptCurrentCard(Pack pack, Card exceptCard) {

        ArrayList<Card> cards = pack.cards;
        for (Card card : cards) {
            if (card.cardID != exceptCard.cardID) {
                prepareSnapOnShotSelectedCard(pack, card);
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

        //we don't need to consider "during creating card" since we have disabled that
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

        LOGD(TAG, "finishSnapShot: " + String.format("FinishSnapShot on cardSN = %d",fragment.mCurrentCard.cardID));
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
        mCardDetailFragment.setupParameters(mCurrentPack, null, 1);
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
            imm.hideSoftInputFromWindow(mMasterMaskButton.getApplicationWindowToken(), 0);
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
            new SweetAlertDialog(this)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.NOT_ALLOW_CREATE_CARD_THAT_IS_NOT_YOU))
                    .show();

            return false;

        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        LOGD(TAG, "onStart");

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LOGD(TAG, "onStop");

        EasyTracker.getInstance().activityStop(this);
    }



    private void onActionbarShareItemSelected() {
        if (mCurrentPack == null) {
            Toast.makeText(this, "NO pack selected", Toast.LENGTH_LONG).show();
            return;
        }


        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {

            share();

        } else {

            parseUserAuth();
        }



    }

    private void parseUserAuth() {

        mIsAllowedToShowPackList = false;

        ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                MainActivity.this);
        Intent parseLoginIntent = loginBuilder.setParseLoginEnabled(true)
                .setParseLoginEmailAsUsername(false)
                .setParseSignupButtonText("Create account")
                .setParseSignupMinPasswordLength(4)
                .setAppLogo(R.drawable.sign_in_logo)
                .build();
        startActivityForResult(parseLoginIntent, LOGIN_REQUEST);


    }

    private void share() {

        if (DropboxAuthHelper.sharedHelper(MainActivity.this).isLinked()) {

            if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

                if (PackRecordHelper.checkUploadPackNecessary(MainActivity.this, mCurrentPack)) {
                    setPasswordAndUpload();
                } else {
                    DropboxShareHelper dropboxShareHelper = new DropboxShareHelper(this,mCurrentPack,true);
                    dropboxShareHelper.execute();
                }

            } else {
                AWSShareHelper AWSShareHelper = new AWSShareHelper(this,mCurrentPack,true);
                AWSShareHelper.share();

            }

        } else {

            if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

                if (PackRecordHelper.checkUploadPackNecessary(MainActivity.this, mCurrentPack)) {
                    setPasswordAndUpload();
                } else {
                    if (DropboxAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
                        DropboxShareHelper dropboxShareHelper = new DropboxShareHelper(this,mCurrentPack,true);
                        dropboxShareHelper.execute();
                    } else {
                        AWSShareHelper AWSShareHelper = new AWSShareHelper(this,mCurrentPack,true);
                        AWSShareHelper.share();
                    }
                }

            } else {
                if (DropboxAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
                    DropboxShareHelper dropboxShareHelper = new DropboxShareHelper(this,mCurrentPack,true);
                    dropboxShareHelper.execute();
                } else {
                    AWSShareHelper AWSShareHelper = new AWSShareHelper(this,mCurrentPack,true);
                    AWSShareHelper.share();
                }

            }

        }
    }


    private void setPasswordAndUpload() {
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        passwordEditText.setSingleLine();
        new AlertDialog.Builder(this)
                .setTitle(R.string.DIALOG_SET_PASSWORD)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(passwordEditText)
                .setPositiveButton(R.string.DIALOG_SET, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        String password = passwordEditText.getText().toString();
                        File file = PackBuildHelper.createPackZipFile(MainActivity.this, mCurrentPack, password);
                        if (file == null) {
                            Toast.makeText(MainActivity.this, "Failed to zip pack", Toast.LENGTH_LONG).show();
                        } else {

                            boolean result = CryptoHelper.encryptFileWithSameOutput(file);
                            if (result == false) {
                                Toast.makeText(MainActivity.this, "Failed to encrypt pack", Toast.LENGTH_LONG).show();
                            } else {
                                //步骤： upload -- > 设置最大分享数 --> 创建短连接 --> 分享
                                if (DropboxAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
                                    mDropboxUploadHelper = new DropboxUploadHelper(MainActivity.this, Global.DROPBOX_FOLDER, file,mDropboxUploadHandler);
                                    mDropboxUploadHelper.execute();
                                } else {
                                    mAmazonUploadHelper = new AWSUploadHelper(MainActivity.this, mAmazonUploadHandler);
                                    mAmazonUploadHelper.upload(file);
                                }
                            }
                        }

                    }
                })
                .setNegativeButton(R.string.Keyboard_No_Needed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        File file = PackBuildHelper.createPackZipFile(MainActivity.this, mCurrentPack, "");
                        if (file == null) {
                            Toast.makeText(MainActivity.this, R.string.DIALOG_CREATE_ZIPPED_SHARE_FILE_FAILED, Toast.LENGTH_LONG).show();
                        } else {

                            boolean result = CryptoHelper.encryptFileWithSameOutput(file);
                            if (result == false) {
                                Toast.makeText(MainActivity.this, R.string.DIALOG_ENCRPT_ZIPPED_SHARE_FILED_FAILED, Toast.LENGTH_LONG).show();
                            } else {
                                //步骤： upload -- > 设置最大分享数 --> 创建短连接 --> 分享
                                if (DropboxAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
                                    mDropboxUploadHelper = new DropboxUploadHelper(MainActivity.this, Global.DROPBOX_FOLDER, file,mDropboxUploadHandler);
                                    mDropboxUploadHelper.execute();
                                } else {
                                    mAmazonUploadHelper = new AWSUploadHelper(MainActivity.this, mAmazonUploadHandler);
                                    mAmazonUploadHelper.upload(file);
                                }

                            }
                        }

                    }
                })
                .show();
    }

    private void initializeCSSToolbar() {

        LOGD(TAG, "initializeCSSToolbar");

        if (mCSSToolbar != null) {
            return;
        }

        //get actionbar height ( we can not directly use getActionbar.getHeight)
        TypedValue tv = new TypedValue();
        int actionbarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            LOGD(TAG, "initializeCSSToolbar: actionbar height is:" + actionbarHeight);
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
                LOGD(TAG, "onTouch: touching mCSSToolbar");
                return false;
            }
        });
        wm.addView(mCSSToolbar, params);
        mCSSToolbar.setVisibility(View.GONE);

        Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
        Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
        Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
        Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

        HighLightArrayAdapter adapterFont = new HighLightArrayAdapter(this,
                R.layout.spinner,getResources().getTextArray(R.array.css_font));
        adapterFont.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(adapterFont);

        HighLightArrayAdapter adapterAlign = new HighLightArrayAdapter(this,
                R.layout.spinner,getResources().getStringArray(R.array.css_align));
        adapterAlign.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlign.setAdapter(adapterAlign);

        HighLightArrayAdapter adapterColor = new HighLightArrayAdapter(this,
                R.layout.spinner,getResources().getStringArray(R.array.css_color));
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adapterColor);

        HighLightArrayAdapter adapterSize = new HighLightArrayAdapter(this,
                R.layout.spinner,getResources().getStringArray(R.array.css_size));
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
                                    mCardDetailFragment.saveEditedCard();
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
                        Toast.makeText(getApplicationContext(),R.string.DIALOG_SYMBOL_NOT_SUPPORTED_BY_FONT,Toast.LENGTH_LONG).show();
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
                    mCardDetailFragment.updateCSS(3, position - 1);
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
        mSymbolKeyboardSwitchButton.setText(getString(R.string.ToolbarItem_Keyboard));
    }

    public void setAsKeyboardStatus() {
        mSymbolBoxFragment.hideSymbolBoxWithAnimation(false);
        if (mSymbolKeyboardSwitchButton != null) {
            mSymbolKeyboardSwitchButton.setText(getString(R.string.ToolbarItem_Symbol));
        }

    }

    public void prepareCSSToolbar() {
        if ((mCSSToolbar == null) || (mCSSToolbar.getParent() == null)) {
            initializeCSSToolbar();
            
        }
    }


    public void showCSSToolbar(CSS css,String tag) {

        CSS currentCSS = css;

        if ((mCSSToolbar != null) && (mCSSToolbar.getParent() != null)) {
            mCSSToolbar.setVisibility(View.VISIBLE);

            //Rest spinner title when touch another textfield
            Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
            Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
            Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
            Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

            Button saveButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_save_btn);
            Button cancelButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_close_btn);

            mIsKeyboardVisible = true;

            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            spinnerFont.setSelection(0);
            spinnerAlign.setSelection(0);
            spinnerColor.setSelection(0);
            spinnerSize.setSelection(0);

            updateSpinnersHighlightedItem(css,tag);

        }
    }

    /*
     * 高亮显示当前选中的spinner item
     */
    public void updateSpinnersHighlightedItem(CSS css,String tag) {

        CSS currentCSS = css;

        Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
        Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
        Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
        Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        String[] fontArray = getResources().getStringArray(R.array.css_font);
        int[]    sizeArray = ScaleHelper.getRealSizeIntArray(MainActivity.this);

        int alignIndex = -1; //no selected by default
        int colorIndex = -1; //no selected by default
        int fontIndex = 1;  // by default, it's default color, so it's 1
        int sizeIndex = -1;
        if (tag.equals(CardDetailFragment.TAG_SUBHEADING)) {
            alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.subheadingAlign);
            if (alignIndex == -1) {
                alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.subheadingAlignVertical);
            }
            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.subheadingColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.subheadingFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.subheadingSize);

        } else if (tag.equals(CardDetailFragment.TAG_MAIN)) {

            alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.mainAlign);
            if (alignIndex == -1) {
                alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.mainAlignVertical);
            }
            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.mainColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.mainFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.mainSize);

        } else if (tag.equals(CardDetailFragment.TAG_SUB)) {

            alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.subAlign);
            if (alignIndex == -1) {
                alignIndex = Arrays.asList(alignArray).indexOf(currentCSS.subAlignVertical);
            }
            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.subColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.subFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.subSize);

        }

        if (fontIndex == -1) {
            fontIndex = 1;  //我们必须这么做，因为我们希望默认是选择default，而不是什么都不选中
        }

        HighLightArrayAdapter adapterFont = (HighLightArrayAdapter) spinnerFont.getAdapter();
        HighLightArrayAdapter adapterSize = (HighLightArrayAdapter) spinnerSize.getAdapter();
        HighLightArrayAdapter adapterAlign = (HighLightArrayAdapter) spinnerAlign.getAdapter();
        HighLightArrayAdapter adapterColor = (HighLightArrayAdapter) spinnerColor.getAdapter();
        adapterFont.setSelection(fontIndex);
        adapterSize.setSelection(sizeIndex+1); //因为我们之前剔除掉了
        adapterColor.setSelection(colorIndex);
        adapterAlign.setSelection(alignIndex);
    }

    public void removeCSSToolbar() {
        if (mCSSToolbar == null) {
            LOGD(TAG, "removeCSSToolbar: mCSSToolbar is null when executing removeCSSToolbar");
            return;
        } else {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (mCSSToolbar.getParent() != null) {
                mCSSToolbar.setVisibility(View.GONE);
                wm.removeView(mCSSToolbar);
                mCSSToolbar = null;
                LOGD(TAG, "removeCSSToolbar: removeCSSToolbar is called");
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
        builder.setMessage(getString(R.string.DIALOG_ARE_YOU_SURE_YOU_WANT_TO_EXIT));
        builder.setTitle(getString(R.string.DIALOG_AlERT));
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(getString(R.string.DIALOG_OK),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton(R.string.DIALOG_CANCEL,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    public void showPackInfoView() {
        mPackInfoLayout.setVisibility(View.VISIBLE);
        updatePackInfoView();
        findViewById(R.id.card_detail_container).setVisibility(View.GONE);
    }

    private void hidePackInfoView() {

        mPackInfoLayout.setVisibility(View.GONE);
        findViewById(R.id.card_detail_container).setVisibility(View.VISIBLE);



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
        packCoverTextView.setText(String.format("%s:%d", getString(R.string.Title_Total_Number_Card),mCurrentPack.cards.size()));

        TextView  shareCodeTextView = (TextView) findViewById(R.id.pack_info_share_code);
        if (StringUtils.isEmpty(mCurrentPack.shareLink) == false) {
            Uri uri = Uri.parse(mCurrentPack.shareLink);
            shareCodeTextView.setText(String.format("%s: %s",getString(R.string.Title_Share_Code),uri.getLastPathSegment()));
        } else {
            shareCodeTextView.setText("");
        }
    }

    private  int COUNTDOWN_SECOND_FOR_RECORDING = 30;
    public void dismissCreateSoundFragment(boolean is_to_recording) {
        View view = findViewById(R.id.record_button_background_mask_layout);

        if (is_to_recording == false) {
            view.setVisibility(View.INVISIBLE);

        } else {
            view.setVisibility(View.VISIBLE);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            if (mRecordCountDownTimer !=null) {
                mRecordCountDownTimer.cancel();
                mRecordCountDownTimer = null;
            }

            mRecordCountDownTimer = new Timer();
            mRecordCountDownTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    COUNTDOWN_SECOND_FOR_RECORDING--;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecordStopProgress.setProgress((int)((30-COUNTDOWN_SECOND_FOR_RECORDING)/30.0*100));
                        }
                    });
                    if (COUNTDOWN_SECOND_FOR_RECORDING ==0) {
                        AudioHelper.stopRecord();
                        mRecordCountDownTimer.cancel();
                        mRecordCountDownTimer = null;
                    }
                }
            },0,1000);


        }

    }

    private final Handler mDropboxUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Dropbox_Constant.UPLOAD_SUCCEED: {
                    File file = (File) msg.obj;

                    Toast.makeText(MainActivity.this, R.string.DIALOG_UPLOAD_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

                    DropboxShareHelper dropboxShareHelper = new DropboxShareHelper(MainActivity.this,mCurrentPack,false);
                    dropboxShareHelper.execute();

                    break;
                }


                case Dropbox_Constant.UPLOAD_FAILED: {
                    break;
                }

            }
        }
    };


    private final Handler mAmazonUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AWS_Constant.UPLOAD_PROGRESS: {
                    File file = (File) msg.obj;
                    int flag = msg.arg1; //indicate whether upload is finished or not
                    int percent = msg.arg2;

                    if (mUploadProgressDialog == null) {
                        mUploadProgressDialog = new ProgressDialog(MainActivity.this);
                        mUploadProgressDialog.setMax(100);
                        mUploadProgressDialog.setMessage(getString(R.string.Indicator_Upload));
                        mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    }

                    if (flag == 0) {
                        mUploadProgressDialog.dismiss();

                        Toast.makeText(MainActivity.this, R.string.DIALOG_UPLOAD_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

                        AWSShareHelper AWSShareHelper = new AWSShareHelper(MainActivity.this, mCurrentPack,false);
                        AWSShareHelper.execute();

                    } else {
                        if (mUploadProgressDialog.isShowing() == false) {
                            mUploadProgressDialog.show();
                        }
                        mUploadProgressDialog.setProgress(percent);
                    }
                    break;
                }


            }
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Parse暂时不支持区分sign up或sign in
        //https://github.com/ParsePlatform/ParseUI-Android/issues/79

        if (requestCode == LOGIN_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                final ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.getUsername().length() > 20) { //表明这是一个系统生成的user name，而不是二次用户生成

                        final EditText passwordEditText = new EditText(MainActivity.this);
                        passwordEditText.setSingleLine(true);
                        passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.DIALOG_CREATE_ACCOUNT_ALERT_MESSAGE)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setView(passwordEditText)
                                .setPositiveButton(R.string.DIALOG_DONE, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String username = passwordEditText.getText().toString().trim().toLowerCase();//bucket name必须小写

                                        if (username.length() == 0) {
                                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText(getString(R.string.DIALOG_ACCOUNT_USERNAME_EMPTY_ERROR))
                                                    .show();
                                            return;
                                        }

                                        currentUser.setUsername(username);
                                        currentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    share();
                                                } else {
                                                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                            .setTitleText(getString(R.string.DIALOG_ERROR))
                                                            .setContentText(getString(R.string.DIALOG_ACCOUNT_USERNAME_HAS_BEEN_REGISTERED))
                                                            .show();

                                                }

                                            }
                                        });


                                    }
                                })
                                .setNegativeButton(R.string.DIALOG_CANCEL, null)
                                .show();

                    } else {

                        share();
                    }
                } else {
                    new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.DIALOG_ERROR))
                            .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_LOG_IN_FAILURE))
                            .show();
                    LOGD(TAG, "onActivityResult: sign up or sign in failure.currentUser should exist");
                }


            } else if (resultCode == Activity.RESULT_CANCELED) {

            } else {

                new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.DIALOG_ERROR))
                        .setContentText(getString(R.string.DIALOG_SOCIAL_MEDIA_LOG_IN_FAILURE))
                        .show();
                LOGE(TAG, "onActivityResult: " + "sign up or sign in failure with resultCode = " + resultCode);
            }
        } else {

        }

    }

    class HighLightArrayAdapter extends ArrayAdapter<CharSequence> {

        private int mSelectedIndex = -1;


        public void setSelection(int position) {
            mSelectedIndex =  position;
            notifyDataSetChanged();
        }

        public HighLightArrayAdapter(Context context, int resource, CharSequence[] objects) {
            super(context, resource, objects);
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View itemView =  super.getDropDownView(position, convertView, parent);

            if (position == mSelectedIndex) {
                itemView.setBackgroundColor(Color.rgb(56,184,226));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            return itemView;
        }
    }


    int searchNearestIndex(int[] array, int searchNumber) {
        int pos = Arrays.binarySearch(array, searchNumber);
        if (pos >= 0)
            return pos;
        else {
            int insertionPoint = -pos - 1;
            if (insertionPoint > 0 && insertionPoint < array.length) {
                if ((searchNumber - array[insertionPoint - 1]) < (array[insertionPoint] - searchNumber)) {
                    return insertionPoint - 1;
                } else {
                    return insertionPoint;
                }

            } else {

                return insertionPoint == 0 ? 0 : array.length - 1;
            }
        }
    }


}
