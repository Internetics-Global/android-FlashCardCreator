package com.flipflash.android_ffc;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.DisplayMetrics;
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

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dropbox.core.v2.files.FileMetadata;
import com.flipflash.UI.PackInfoView;
import com.flipflash.UI.ScaleHelper;
import com.flipflash.UI.SlideInRightWithoutAlphaAnimator;
import com.flipflash.UI.SlideOutRightWithoutAlphaAnimator;
import com.flipflash.data.CSS;
import com.flipflash.event.DownloadCancelEvent;
import com.flipflash.event.FacebookShareFinishEvent;
import com.flipflash.event.MultiMediaFullscreenEvent;
import com.flipflash.event.PurchasedStatusChangeUpdateEvent;
import com.flipflash.event.PurchasedSuccessEvent;
import com.flipflash.event.WebViewMessageEvent;
import com.flipflash.fragment.PurchaseFragment;
import com.flipflash.helper.AWS.AWSShareHelper;
import com.flipflash.helper.AWS.AWSUploadHelper;
import com.flipflash.helper.AWS.AWS_Constant;
import com.flipflash.helper.GoogleDrive.GoogleDriveAuthHelper;
import com.flipflash.helper.GoogleDrive.GoogleDriveShareHelper;
import com.flipflash.helper.GoogleDrive.GoogleDriveUploadHelper;
import com.flipflash.helper.GoogleDrive.GoogleDrive_Constant;
import com.flipflash.helper.Text2SpeechHelper;
import com.flipflash.model.CardListModel;
import com.flipflash.util.MutipleTargetHelper;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.flipflash.cryptor.CryptoHelper;
import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.fragment.CardListFragment;
import com.flipflash.fragment.CreateEditFragment;
import com.flipflash.fragment.SymbolBoxFragment;
import com.flipflash.helper.AWS.SimpleDBHelper;
import com.flipflash.helper.AudioHelper;
import com.flipflash.helper.Dropbox.DropboxAuthHelper;
import com.flipflash.helper.Dropbox.DropboxShareHelper;
import com.flipflash.helper.Dropbox.DropboxUploadHelper;
import com.flipflash.helper.Dropbox.Dropbox_Constant;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.PackBuildHelper;
import com.flipflash.helper.PackDownloadHelper;
import com.flipflash.helper.SQLiteHelper;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;
import com.flipflash.util.TipHelper;
import com.flipflash.util.UIHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.nineoldandroids.animation.Animator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.greenrobot.event.EventBus;

/**
 * MainActivity is the entry for whole app
 * Control both master - detail view
 * Also responsbile for managing Actionbar(or Option Menu)
 */
public class MainActivity extends FragmentActivity implements
        CardListFragment.Callbacks, PackInfoView.PackInfoViewDelegate, BillingProcessor.IBillingHandler{

    private static final String TAG = MainActivity.class.getSimpleName();


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

    private ProgressDialog    mUploadProgressDialog;
    private ProgressDialog    mSnapShotDialog;
    private ProgressDialog    mZipAndEncryptDialog;

    private ProgressDialog    mUnshortenProgressDialog;

    private ArrayList<CardDetailFragment> mArrayCardDetailFragments;   //Special for snapshot(not include current card)


    public  CardDetailFragment   mCardDetailFragment;


    public  CardDetailFragment   mNewCardDetailFragment;

    public  SymbolBoxFragment    mSymbolBoxFragment;
    private Button               mSymbolKeyboardSwitchButton;

    public int                   packIDForMasterViewPack;

    private PackInfoView         mPackInfoView;

    private AWSUploadHelper          mAmazonUploadHelper ;
    private DropboxUploadHelper      mDropboxUploadHelper ;
    private GoogleDriveUploadHelper  mGoogleDriveUploadHelper ;

    private DonutProgress        mRecordStopProgress;
    private Button               mRecordStopButton;
    private Timer                mRecordCountDownTimer;

    private TextView             mCustomTitleTextView;

    private PackDownloadHelper   mPackDownloadHelper;

    private String[]             mLanguageSpinnerArray;

    private GoogleDriveShareHelper mGoogleDriveShareHelper;
    private DropboxShareHelper     mDropboxShareHelper;
    private AWSShareHelper         mAWSShareHelper;

    private BillingProcessor mBillingProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate: ");

        AppContext mApp = ((AppContext)getApplicationContext());
        mApp.setMainActivity(MainActivity.this);

        //Step1: check table and default user
        SQLiteHelper.defaultDatabase(AppContext.getAppContext());

        //step2: background of to-do-this. we hope to use Uri globally including resource files
        FileOperationHelper.copyResourcesImagesToCache(MainActivity.this);

        //Step3: OpenUDID
        OpenUDID_manager.sync(this);
        if (!OpenUDID_manager.isInitialized()) {
            LOGD(TAG, "onCreate: OpenUDID_manager is not initialized");
        }

        //step3: setup basic view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_card_twopane);

        //step4: setup record button
        mRecordStopProgress = (DonutProgress) findViewById(R.id.record_stop_progress);
        mRecordStopButton = (Button) findViewById(R.id.record_stop_button);
        mRecordStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordStopButtonClicked();
            }
        });

        //step5: setup "creating a new card"
        Button addCardButton = (Button) this.findViewById(R.id.add_card_button);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LOGD(TAG, "onClick: add card button  is clicked");
                createNewCardButtonClicked();
                TipHelper.hideEverything(MainActivity.this);

            }
        });

        if (MutipleTargetHelper.isFullVersion()) {
            addCardButton.setVisibility(View.VISIBLE);
        } else {
            addCardButton.setVisibility(View.INVISIBLE);
        }

        mMasterMaskButton = (Button) findViewById(R.id.master_view_mask);

        mMasterMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissCardCreateWindow();

            }
        });

        //step6: set info view
        mPackInfoView = (PackInfoView) findViewById(R.id.pack_info_layout);
        mPackInfoView.setPackInfoViewDelegate(this);
        mCurrentPack = CardListModel.getLastSelectedPack();
        if (mCurrentPack != null) {
            mPackInfoView.setCurrentPack(mCurrentPack);
            showPackInfoView();
        } else {
            hidePackInfoView();
        }

        //step7: setup symbol box
        mSymbolBoxFragment = (SymbolBoxFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_symbol_box);

        mIsFromRestartApp = true;

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.title_on_actionbar, null);
        mCustomTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mCustomTitleTextView.setText("");
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayShowCustomEnabled(true);

        //EasyTracker.getInstance().setContext(this);

        if (Global.apiReachableWithAlert(MainActivity.this) == false) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getResources().getString(R.string.DIALOG_TITLE_NO_NETWORK))
                    .setMessage(getResources().getString(R.string.DIALOG_PLEASE_CHECK_YOUR_NETWORK))
                    .setPositiveButton(getResources().getString(R.string.DIALOG_OK), null)
                    .show();

        }

        EventBus.getDefault().register(MainActivity.this);


        searchAvailableLanguageSpinnerArray();

    }

    void searchAvailableLanguageSpinnerArray() {
        ArrayList<String> languageList = Text2SpeechHelper.sharedHelper().availableDescriptionList();
        languageList.add(0,getString(R.string.ToolbarItem_Language));
        mLanguageSpinnerArray = languageList.toArray(new String[languageList.size()]);
    }


    public boolean getPackInfoLayoutVisible() {
        LinearLayout         packInfoLayout = (LinearLayout) findViewById(R.id.pack_info_layout);
        if (packInfoLayout != null & packInfoLayout.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
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


        MenuItem packsMenuItem = menu.findItem(R.id.actionbar_packs);
        MenuItem editPackMenuItem = menu.findItem(R.id.actionbar_edit);
//        MenuItem newPackMenuItem = menu.findItem(R.id.actionbar_add_pack);

        MenuItem changeTemplatColorMenuItem = menu.findItem(R.id.actionbar_change_template_color);
        MenuItem helpMenuItem = menu.findItem(R.id.actionbar_help);
        MenuItem moreMenuItem = menu.findItem(R.id.actionbar_more);;

        MenuItem installCodeMenuItem = menu.findItem(R.id.actionbar_install_from_code);
        MenuItem sharePackMenuItem = menu.findItem(R.id.actionbar_share_pack);

        MenuItem playMenuItem = menu.findItem(R.id.actionbar_play);;

        if ((UIHelper.getScreenWidthDPUnit(this) >= 600) && (mIsCreatingCard == false)) {
            changeTemplatColorMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            helpMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            moreMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }

        if (MutipleTargetHelper.isFullVersion() == false) {

            if (sharePackMenuItem != null) {
                sharePackMenuItem.setVisible(false);
            }

//            if (newPackMenuItem != null) {
//                newPackMenuItem.setIcon(R.drawable.pack_add_dimmed);
//            }

            if (changeTemplatColorMenuItem != null) {
                changeTemplatColorMenuItem.setIcon(R.drawable.template_background_change_button_dimmed);
            }

            if (helpMenuItem != null) {
                helpMenuItem.setIcon(R.drawable.helping_button_dimmed);
            }
        } else {

            if (sharePackMenuItem != null) {
                sharePackMenuItem.setVisible(true);
            }

            if (editPackMenuItem != null) {
                editPackMenuItem.setIcon(R.drawable.pack_edit);
            }

//            if (newPackMenuItem != null) {
//                newPackMenuItem.setIcon(R.drawable.pack_add);
//            }

            if (changeTemplatColorMenuItem != null) {
                changeTemplatColorMenuItem.setIcon(R.drawable.template_background_change_button);
            }

            if (helpMenuItem != null) {
                helpMenuItem.setIcon(R.drawable.helping_button);
            }
        }

        //update status
        if (MutipleTargetHelper.isFullVersion()) {

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
        }


        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LOGD(TAG, "onOptionsItemSelected: " + item.toString());

        final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();

        switch (item.getItemId()) {
//            case R.id.actionbar_add_pack: {
//
//                if (MutipleTargetHelper.isFullVersion() == false) {
//                    MutipleTargetHelper.showAlertToUpgradeToFullVersion();
//                    break;
//                }
//
//                DialogFragment dialogFragment = new CreateEditFragment();
//                dialogFragment.show(getSupportFragmentManager(), "add_pack_fragment");
//                break;
//            }
            case R.id.actionbar_edit:

                if (mCurrentPack == null) {
                    break;
                }

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

                break;
            case R.id.actionbar_packs:
                showPackListView();

                break;

            case R.id.actionbar_change_template_color:

                if (MutipleTargetHelper.isFullVersion() == false) {
                    MutipleTargetHelper.showAlertToUpgradeToFullVersion();
                    break;
                }

                if (mCurrentPack == null || activeCardDetailFragment == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.DIALOG_SELECT_CARD_BEFOREHAND), Toast.LENGTH_SHORT).show();
                    break;
                }

                if (!mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
                    new SweetAlertDialog(MainActivity.this)
                            .setTitleText(getString(R.string.DIALOG_AlERT))
                            .setContentText(getString(R.string.DIALOG_YOU_CAN_NOT_CHANGE_TEMPLATE_BACKGROUND))
                            .show();

                }  else {

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
                                                final int finalWhich = which;
                                                dialog.dismiss();
                                                showSnapShotProgressDialog();
                                                Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                //do something on UI thread
                                                                activeCardDetailFragment.cardColorTemplateSelectedPostAction(finalWhich);
                                                            }

                                                        }, 10); // 5000ms delay
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

                play();

                break;


            case R.id.actionbar_share_pack:

                if (MutipleTargetHelper.isFullVersion() == false) {
                    MutipleTargetHelper.showAlertToUpgradeToFullVersion();
                    break;
                }
                if (Global.apiReachableWithAlert(MainActivity.this)) {
                    onActionbarShareItemSelected();
                } else {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.DIALOG_TITLE_NO_NETWORK))
                            .setMessage(getResources().getString(R.string.DIALOG_PLEASE_CHECK_YOUR_NETWORK))
                            .setPositiveButton(getResources().getString(R.string.DIALOG_OK), null)
                            .show();

                }
                break;

            case R.id.actionbar_install_from_code:

                if (MutipleTargetHelper.isFullVersion() == false && MutipleTargetHelper.isNoAdVersion() == false) {
                    MutipleTargetHelper.showAlertToUpgradeToFullVersion();
                } else {

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

                                    if (Global.apiReachableWithAlert(MainActivity.this) == false) {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle(getResources().getString(R.string.DIALOG_TITLE_NO_NETWORK))
                                                .setMessage(getResources().getString(R.string.DIALOG_PLEASE_CHECK_YOUR_NETWORK))
                                                .setPositiveButton(getResources().getString(R.string.DIALOG_OK), null)
                                                .show();
                                        return;

                                    }

                                    final String codeString = codeEditText.getText().toString();

                                    InputMethodManager imm = (InputMethodManager) getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);

                                    if (StringUtils.isEmpty(codeString) == false) {

                                        if (mUnshortenProgressDialog == null) {
                                            mUnshortenProgressDialog = new ProgressDialog(MainActivity.this);
                                            mUnshortenProgressDialog.setMax(100);
                                            mUnshortenProgressDialog.setCancelable(false);
                                            mUnshortenProgressDialog.setCanceledOnTouchOutside(false);
                                            mUnshortenProgressDialog.setMessage(getString(R.string.Title_Process_Share_Code));
                                            mUnshortenProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        }
                                        mUnshortenProgressDialog.show();

                                        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
                                        taskExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {

                                                String shortenURL = String.format("%s%s",Global.TINYURL_SHORTED_BASE_URL,codeString);
                                                String wholeURL = StringUtils.getUnshortedURL(shortenURL);
                                                if (wholeURL == null) {

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            if (mUnshortenProgressDialog !=null) {
                                                                mUnshortenProgressDialog.dismiss();
                                                                mUnshortenProgressDialog = null;
                                                            }

                                                            new SweetAlertDialog(MainActivity.this)
                                                                    .setTitleText(getString(R.string.DIALOG_AlERT))
                                                                    .setContentText(getString(R.string.Title_Share_Code_Not_Right))
                                                                    .show();
                                                        }
                                                    });


                                                } else {

                                                    final Uri packUri = Uri.parse(wholeURL);

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            downloadPack(packUri);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }


                                }
                            })
                            .setNegativeButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }



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

                if (MutipleTargetHelper.isFullVersion() == false) {
                    MutipleTargetHelper.showAlertToUpgradeToFullVersion();
                    break;
                }

                if (TipHelper.isShowingTipForActionBarHelp) {
                    TipHelper.hideEverything(MainActivity.this);
                    Global.isAllowToShowTooltips = true;
                } else {
                    if (Global.isAllowToShowTooltips) {
                        if (activeCardDetailFragment != null) {
                            activeCardDetailFragment.showTooltips();
                            showTooltips();
                        } else {
                            showTooltips();
                        }
                    } else {
                        TipHelper.hideEverything(MainActivity.this);
                        Global.isAllowToShowTooltips = true;
                    }
                }




                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void play() {

        int playOption = AppConfig.sharedInstance().getPlayOption();

        if ((mCurrentPack != null) && (mCurrentPack.cards.size() > 0)) {
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra("packID", mCurrentPack.packID);
            intent.putExtra("oneOffPlayType",playOption);  //manually
            startActivity(intent);
            mIsAllowedToShowPackList = false;
        }  else {

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.DIALOG_AlERT))
                    .setMessage(getString(R.string.DIALOG_NO_CARD_AVAILABLE))
                    .setPositiveButton(getString(R.string.DIALOG_OK), null)
                    .show();
        }
    }

    public void setCurrentPack(Pack mCurrentPack) {
        this.mCurrentPack = mCurrentPack;
        if (mCurrentPack != null) {
//            mCustomTitleTextView.setText(mCurrentPack.packName);
        }
    }


    public CardDetailFragment getActiveCardDetailFragment() {
        CardDetailFragment target;

        if (mIsCreatingCard) {
            target = mNewCardDetailFragment;
        } else {
            target = mCardDetailFragment;
        }

//        if (target == null) {
//            throw new IllegalStateException("getActiveCardDetailFragment should return a non-null value");
//        }

        return target;
    }


    private void recordStopButtonClicked() {

        final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();

        LOGD(TAG, "recordStopButtonClicked");

        if (mRecordCountDownTimer != null) {
            mRecordCountDownTimer.cancel();
            mRecordCountDownTimer = null;
        }

        AudioHelper.isRecordFinished = true;

        activeCardDetailFragment.showCreateSoundView();

        findViewById(R.id.record_button_background_mask_layout).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LOGD(TAG, "onResume");

//if our targetAPI is 23, we have to add this. We can not set targetAPI= 23 since it could raise permission other issues, see my evernote
//        boolean permissionAuthorized = false;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //http://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t
//            if (!Settings.canDrawOverlays(MainActivity.this)) {
//                permissionAuthorized = false;
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, Global.REQUEST_ACTION_MANAGE_OVERLAY_PERMISSION);
//            } else {
//                permissionAuthorized = true;
//            }
//        } else {
//            permissionAuthorized = true;
//        }
//
//        if (permissionAuthorized == false) {
//            return;
//        }

        if (DropboxAuthHelper.sharedHelper().isAuthenticationInProgress()) {

            LOGD(TAG, "onResume: isAuthenticationSuccessful, now try to finishAuthentication and storeAuth");

            // Mandatory call to complete the auth
            boolean success = DropboxAuthHelper.sharedHelper().finishAuthentication();

            if (success) {
                shareToDropbox();
            }

            return;
        } else {
            //for Google Drive, similar logic is located in onActivityResult
        }

        if (mIsNecessaryToRestoreCSSToolbar) {
            initializeCSSToolbar();
            mIsNecessaryToRestoreCSSToolbar = false;
        }

        //Step1: download sample pack first
        boolean isDownloaded = AppConfig.sharedInstance().isExamplePackDownloadedBefore();
        boolean isReachable = Global.apiReachable(MainActivity.this);

        if ((!isDownloaded) && (isReachable) && (mIsFromRestartApp) && Global.isNotAllowDownloadSamplePack == false) {
            mIsFromRestartApp = false;
            String downloableShareLink = Global.SAMPLE_URL;
            File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
            if (mPackDownloadHelper != null) {
                mPackDownloadHelper.cancel(true);
                mPackDownloadHelper = null;
            }
            mPackDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
            mPackDownloadHelper.mIsFromExamplePackDownload = true;
            mPackDownloadHelper.execute();
            return;
        }

        //Step2: call from other app or Dropbox log in
        Uri packUri = getIntent().getData();
        if (packUri != null) {

            if (MutipleTargetHelper.isFullVersion() == false && MutipleTargetHelper.isNoAdVersion() == false) {
                MutipleTargetHelper.showAlertToUpgradeToFullVersion();
                return;
            } else {
                downloadPack(packUri);
            }

        }
        getIntent().setData(null); //in case it will be recalled time and time

        //Used to show pack list
        if (Global.showActionListAgain) {
            Global.showActionListAgain = false;
            reShowShareSocialListAlert();
        } else {
            if (mIsAllowedToShowPackList &&
                    ((mPackDownloadHelper != null && mPackDownloadHelper.isDownloading() == false) || (mPackDownloadHelper == null))) {

                final View appMainView = findViewById(R.id.app_main);

                if (appMainView.getHeight() > 0 && appMainView.getWidth() > 0) {
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
                                    showPackListView();
                                }

                            }, 100); // 100ms delay

                        }
                    });
                }
            }
        }

        Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mIsAllowedToShowPackList = true;

                    }

                }, 1000); // 1000ms delay


        checkAdView();


    }

    private void downloadPack(Uri packUri) {

        if ((packUri != null) && (packUri.getScheme().equalsIgnoreCase("fcc"))) {

            mIsAllowedToShowPackList = false;

            //for download (not include sample pack
            if (Global.apiReachableWithAlert(MainActivity.this)) {

                if (packUri.getHost().contains("google.com")) {
                    //google
                    Global.currentAmazonSimpleDBItemName = packUri.getQueryParameter("id");
                } else {
                    //dropbox
                    String packFileName = packUri.getLastPathSegment();
                    Global.currentAmazonSimpleDBItemName = packFileName.substring(0,packFileName.indexOf(".zip"));
                }

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

                int timeoutCount = 0;    //set timeout = 10 second
                final int kTimeoutThreshold = 500;
                while ((mSemaphore == false) && (timeoutCount <kTimeoutThreshold)) {
                    try {
                        Thread.sleep(20);
                        timeoutCount ++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                if (mUnshortenProgressDialog !=null) {
                    mUnshortenProgressDialog.dismiss();
                    mUnshortenProgressDialog = null;
                }

                if (timeoutCount == kTimeoutThreshold) {
                    Toast.makeText(getApplicationContext(), R.string.DIALOG_NETWORK_TIMEOUT, Toast.LENGTH_LONG).show();

                    return;
                } else {
                    if (mIsAllowDownload) {

                        Global.fccURLForCurrentDownloadingPack =  packUri.toString();

                        String downloableShareLink = packUri.toString().replace("fcc", "https").replace("www", "dl");
                        File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");

                        if (mPackDownloadHelper != null) {
                            mPackDownloadHelper.cancel(true);
                            mPackDownloadHelper = null;
                        }
                        mPackDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
                        mPackDownloadHelper.execute();

                    }   else {
                        Toast.makeText(getApplicationContext(), R.string.DIALOG_REACH_MAX_DOWNLOAD_LIMIT, Toast.LENGTH_LONG).show();
                    }
                }
            } else {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.DIALOG_TITLE_NO_NETWORK))
                        .setMessage(getResources().getString(R.string.DIALOG_PLEASE_CHECK_YOUR_NETWORK))
                        .setPositiveButton(getResources().getString(R.string.DIALOG_OK), null)
                        .show();

            }
        } else {

            if (mUnshortenProgressDialog !=null) {
                mUnshortenProgressDialog.dismiss();
                mUnshortenProgressDialog = null;
            }


            new SweetAlertDialog(MainActivity.this)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.Title_Share_Code_Not_Right))
                    .show();
        }
    }

    public void showTooltips() {

        if (MutipleTargetHelper.isFullVersion() == false) {
            return;
        }

//        //we don't allow to show when screen is too small
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
//        if (dpWidth < 900) {
//            return;
//        }

        LOGD(TAG, "showTooltips");

        Global.isAllowToShowTooltips = false;

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

                TipHelper.showTipForOpenPack(MainActivity.this, editPackButton);
                TipHelper.showTipForEditPack(MainActivity.this,createPackButton);
                TipHelper.showTipForActionBarHelp(MainActivity.this, paletteButton,false);
                TipHelper.showTipForActionBarShare(MainActivity.this, settingButton);
                TipHelper.showTipForActionBarPlay(MainActivity.this, shareButton);
                TipHelper.showTipForActionBarSetting(MainActivity.this, helpButton);
                TipHelper.showTipForActionBarPalette(MainActivity.this, playButton);




            }

        }, 50);
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

        LOGD(TAG, "showPackListView");

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

                    if (AppConfig.sharedInstance().isHelpTipHasBeenShowedFirst() == false) {
                        final Button paletteButton = (Button) findViewById(R.id.tooltip_fake_actionbar_palette); //TODO: we need to match the real meaning with its name
                        TipHelper.showTipForActionBarHelp(MainActivity.this, paletteButton, true);
                        Global.isAllowToShowTooltips = false;
                    }
                }
            });
        }

        if (mPopupWindow.isShowing() == false) {
            View popupLayout =  mPopupWindow.getContentView();
            if (popupLayout == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (mPopupWindow.getContentView() != null) {

                    LOGD(TAG, "mPopupWindow.getContentView() != null");

//                    new SweetAlertDialog(MainActivity.this)
//                        .setTitleText("Debug purpose")
//                        .setContentText("mPopupWindow.getContentView() != null")
//                        .show();


                } else {

                    popupLayout = inflater.inflate(R.layout.pack_list, null, false);
                    if (popupLayout != null) {
                        mPopupWindow.setContentView(popupLayout);
                    } else {
                        LOGE(TAG, "showPackListView: Failed to inflate, please check");
                    }
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
        }
    }

    public void dismissPackListPopupWindow() {
        LOGD(TAG, "dismissPackListPopupWindow");
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
//            mPopupWindow.setContentView(null);
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

        Global.maxDownloadableNoForCurrentDownloadingPack = maxNo;


        if ((Global.currentAmazonSimpleDBItemDownloadCount < maxNo)  || (maxNo == 0)) {  //maxNo = 0 means no record in AmazonSDB
            result = true;
        } else {
            result = false;
        }

        return result;
    }


    @Override
    protected void onStart() {
        super.onStart();

        LOGD(TAG, "onStart");

        setupPurchase();

        //EasyTracker.getInstance().activityStart(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        LOGD(TAG, "onStop");

        if (mPackDownloadHelper != null) {
            mPackDownloadHelper.cancel(true);
            mPackDownloadHelper = null;
        }

        dismissPackListPopupWindow();

        //EasyTracker.getInstance().activityStop(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        LOGD(TAG, "onPause");
        if ((mCSSToolbar != null) && (mCSSToolbar.getParent() != null)) {
            removeCSSToolbar();
            mIsNecessaryToRestoreCSSToolbar = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBillingProcessor = null;

        LOGD(TAG, "onDestroy");

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

        if (mGoogleDriveUploadHelper != null) {
            mGoogleDriveUploadHelper.cancel(true);
            mGoogleDriveUploadHelper = null;
        }

        EventBus.getDefault().unregister(MainActivity.this);
    }


    @Override
    public void onItemSelected(int selectedCardIndex,Pack currentPack,boolean isManuallyClicked) {

        LOGD(TAG, "onItemSelected: " + selectedCardIndex);

        if (currentPack == null) {
            throw new IllegalStateException("when onItemSelected is called, currentPack should never be null");
        }

        mCurrentPack = currentPack;
//        mCustomTitleTextView.setText(mCurrentPack.packName);

        if (selectedCardIndex >= 0) {
            mCurrentCardIndex = selectedCardIndex;
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
        LOGD(TAG, "prepareSnapOnShotSelectedCard");
        CardDetailFragment snapshotCardDetailFragment = new CardDetailFragment();
        snapshotCardDetailFragment.setupParameters(pack, card, 3);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail, snapshotCardDetailFragment).commitAllowingStateLoss();

        if (mArrayCardDetailFragments == null) {
            mArrayCardDetailFragments = new ArrayList<>();
        }
        mArrayCardDetailFragments.add(snapshotCardDetailFragment);
    }


    public void prepareDataForSnapShotAllExceptCurrentCard(Pack pack, Card exceptCard) {
        LOGD(TAG, "prepareDataForSnapShotAllExceptCurrentCard");

        ArrayList<Card> cards = pack.cards;
        for (Card card : cards) {
            if (card.cardID != exceptCard.cardID) {
                prepareSnapOnShotSelectedCard(pack, card);
            }
        }
    }

    public void cleanupDataForSnapShotAllExceptCurrent() {
        LOGD(TAG, "finishDataForSnapShotAllExceptCurrent");

        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];//maybe this number needs to be corrected
        String methodName = e.getMethodName();
        if (methodName.equals("beginScreenshot") == false) {
           throw  new IllegalStateException("cleanupDataForSnapShotAllExceptCurrent only can be called by takeSnapshotCurrentCard") ;
        }

        if (mArrayCardDetailFragments != null) {
            for (CardDetailFragment cardDetailFragment : mArrayCardDetailFragments) {
                getSupportFragmentManager().beginTransaction().remove(cardDetailFragment).commitAllowingStateLoss();
            }
            mArrayCardDetailFragments.clear();
            mArrayCardDetailFragments = null;
        }


        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_SNAPSHOT_ALL);

        if (mIsCreatingCard) {
            intent.putExtra(Global.KEY_CARD_INDEX,mCurrentPack.cards.size()-1);
        } else {
            intent.putExtra(Global.KEY_CARD_INDEX,mCurrentCard.cardSN-1);
        }

        sendBroadcast(intent);

        dismissSnapShotProgressDialog();

        if (mIsCreatingCard) {
            dismissCardCreateWindow();
        }

    }

    private void createNewCardButtonClicked() {

        LOGD(TAG, "startCreateCard");

        if (MutipleTargetHelper.isFullVersion() == false) {
            MutipleTargetHelper.showAlertToUpgradeToFullVersion();
            return;
        }

        //mCurrentPack = CardListModel.getLatestCreatedPack();//don't need to do here
        boolean result = checkEntryConditionBeforeCreatingNewCard(mCurrentPack);
        if (result == false) {
            return;
        }

        hidePackInfoView();

        Pack shadowCopyPack = (Pack) mCurrentPack.clone();
        mNewCardDetailFragment = new CardDetailFragment();
        mNewCardDetailFragment.setupParameters(shadowCopyPack, null, 1);
        if (shadowCopyPack.cards.size() >0) {
            //History of reason, we put templateBackground in Card, rather than Pack. It's not a good design practce anyway.
            mNewCardDetailFragment.mCurrentCard.templateBackground =  shadowCopyPack.cards.get(0).templateBackground;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.add_card_frame_layout, mNewCardDetailFragment)
                .commitAllowingStateLoss();

        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        addCardLayout.setVisibility(View.VISIBLE);

        mMasterMaskButton.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeIn)
                .duration(460)
                .playOn(mMasterMaskButton);

        YoYo.with(new SlideInRightWithoutAlphaAnimator())
                .duration(460)
                .playOn(findViewById(R.id.add_card_frame_layout));


        mIsCreatingCard = true;

        invalidateOptionsMenu();

    }

    private void saveNewCreatedCard() {
        LOGD(TAG, "saveNewCreatedCard:");

        if (mSymbolBoxFragment!=null && mSymbolBoxFragment.isSymbolBoxVisible()) {
            mSymbolBoxFragment.hideSymbolBoxWithAnimation(true);
        }

        mNewCardDetailFragment.saveNewCreatedCard(); //will call dismissCardCreateWindow(); in this


    }

    private void cssSaveToolbarButtonClicked() {

        final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();

        activeCardDetailFragment.dismissKeyboard();
        activeCardDetailFragment.resetVerticalScrollViewBottomMargin();

        if (mSymbolBoxFragment!=null && mSymbolBoxFragment.isSymbolBoxVisible()) {
            mSymbolBoxFragment.hideSymbolBoxWithAnimation(true);
        }
        removeCSSToolbar();

        if (mIsCreatingCard) {
            saveNewCreatedCard();
        } else {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activeCardDetailFragment.saveEditedCard();
                }
            },500);

        }
    }

    public void removeAddCardLayoutIfExisting () {

        FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        if (addCardLayout != null) {
            addCardLayout.setVisibility(View.GONE);
        }
    }

    public void dismissCardCreateWindow() {
        LOGD(TAG, "dismissCardCreateWindow");

        YoYo.with(Techniques.FadeOut)
                .duration(460)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        mMasterMaskButton.setVisibility(View.INVISIBLE);

                        mIsCreatingCard = false;

                        invalidateOptionsMenu();

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                        mMasterMaskButton.setVisibility(View.INVISIBLE);

                        mIsCreatingCard = false;

                        invalidateOptionsMenu();

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(mMasterMaskButton);

        final FrameLayout addCardLayout = (FrameLayout) findViewById(R.id.add_card_frame_layout);
        YoYo.with(new SlideOutRightWithoutAlphaAnimator())
                .duration(460)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        addCardLayout.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().remove(mNewCardDetailFragment).commit();
                        mNewCardDetailFragment = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                        addCardLayout.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().remove(mNewCardDetailFragment).commit();
                        mNewCardDetailFragment = null;

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(addCardLayout);

        removeCSSToolbar();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow(mMasterMaskButton.getApplicationWindowToken(), 0);
        }

        TipHelper.hideEverything(MainActivity.this);

    }

    private boolean checkEntryConditionBeforeCreatingNewCard(Pack currentPack) {
        LOGD(TAG, "checkEntryConditionBeforeCreatingNewCard");
        //case1: check whether pack is empty or not
        if (currentPack == null) {
            Toast.makeText(getApplicationContext(), "Create a pack first before creating a new card", Toast.LENGTH_LONG).show();
            return false;
        }
        //case2: check owner
        if (!currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
            new SweetAlertDialog(MainActivity.this)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.NOT_ALLOW_CREATE_CARD_THAT_IS_NOT_YOU))
                    .show();

            return false;

        }

        return true;
    }


    private void onActionbarShareItemSelected() {
        LOGD(TAG, "onActionbarShareItemSelected");
        if (mCurrentPack == null) {
            Toast.makeText(getApplicationContext(), "NO pack selected", Toast.LENGTH_LONG).show();
            return;
        }

        //check whether to allow to share
        HashMap dict = Hawk.get("isAllowShare");
        boolean isAllowToShare;
        if (dict == null) {
            isAllowToShare = true;
        } else {
            Boolean b = ((Boolean) dict.get(String.format("%d",mCurrentPack.packID)));
            isAllowToShare = (b == null)? true : b.booleanValue();
        }
        if (isAllowToShare == false && (mCurrentPack.creatorID.equals(OpenUDID_manager.getOpenUDID()) == false)) {

            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.DIALOG_AlERT))
                    .setContentText(getString(R.string.DIALOG_SHARE_FUNCTION_FORBIDDEN_BY_CREATOR))
                    .show();
            return;
        }



        if (DropboxAuthHelper.sharedHelper().isLinked()) {
            shareToDropbox();
        } else if (GoogleDriveAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
            shareToGoogleDrive();
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            shareToAWS();
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.DIALOG_STORAGE_SELECTION)
                    .setNeutralButton(R.string.DIALOG_STORAGE_SELECTION_DROPBOX, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DropboxAuthHelper.sharedHelper().startAuthenticationFromActivity(MainActivity.this);
                        }
                    })
                    .setNegativeButton(R.string.DIALOG_STORAGE_SELECTION_GOOGLE_DRIVE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            GoogleDriveAuthHelper.sharedHelper(MainActivity.this).startAuthenticationFromActivity(MainActivity.this);

                        }
                    })
//                    .setPositiveButton(R.string.DIALOG_STORAGE_SELECTION_AWS, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                            new SweetAlertDialog(MainActivity.this)
//                                    .setTitleText(getString(R.string.DIALOG_AlERT))
//                                    .setContentText(getString(R.string.AWS_DRIVE_LOGIN_REQUIRED))
//                                    .show();
//
//
//                        }
//                    })
                    .show();
        }




    }


    private void shareToDropbox() {
        LOGD(TAG, "share");

        Global.activeShareStorage = 1;

        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

            setPasswordAndUpload();

        } else {
            mDropboxShareHelper = new DropboxShareHelper(this,mCurrentPack,null,true);
            mDropboxShareHelper.share();

        }
    }


    private void shareToGoogleDrive() {
        LOGD(TAG, "share");

        Global.activeShareStorage = 0;

        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

            setPasswordAndUpload();

        } else {
            mGoogleDriveShareHelper = new GoogleDriveShareHelper(this,mCurrentPack,true,null);
            mGoogleDriveShareHelper.share();

        }
    }

    private void shareToAWS() {
        LOGD(TAG, "share");

        Global.activeShareStorage = 2;

        if ((mCurrentPack.creatorID).equals(OpenUDID_manager.getOpenUDID())) {

            setPasswordAndUpload();

        } else {
            mAWSShareHelper = new AWSShareHelper(this,mCurrentPack,true);
            mAWSShareHelper.share();

        }
    }




    private void setPasswordAndUpload() {
        LOGD(TAG, "setPasswordAndUpload");
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        passwordEditText.setSingleLine();
        new AlertDialog.Builder(this)
                .setTitle(R.string.DIALOG_SET_PASSWORD)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(passwordEditText)
                .setNegativeButton(R.string.DIALOG_SET, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        String password = passwordEditText.getText().toString();
                        passwordSetAlertViewClickedWithPassword(password);

                    }
                })
                .setNeutralButton(R.string.Keyboard_No_Needed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                        passwordSetAlertViewClickedWithPassword("");

                    }
                })
                .setPositiveButton(R.string.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    public void passwordSetAlertViewClickedWithPassword(String password) {

        ZippingAndEncryptTask myTask = new ZippingAndEncryptTask(password);
        myTask.execute("ZippingAndEncryptTask");
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


    public void prepareCSSToolbar() {
        if ((mCSSToolbar == null) || (mCSSToolbar.getParent() == null)) {
            initializeCSSToolbar();

        }
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
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
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
        final Spinner spinnerLanguage = (Spinner) mCSSToolbar.findViewById(R.id.spinner_language);

        HighLightArrayAdapter adapterFont = new HighLightArrayAdapter(this,
                R.layout.spinner,getResources().getTextArray(R.array.css_font_nominal));
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

        if (mLanguageSpinnerArray == null || mLanguageSpinnerArray.length <=1) {
            searchAvailableLanguageSpinnerArray();
        }
        final HighLightArrayAdapter adapterLanguage = new HighLightArrayAdapter(this,
                R.layout.spinner,mLanguageSpinnerArray);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapterLanguage);

        spinnerFont.setSelection(0);
        spinnerAlign.setSelection(0);
        spinnerColor.setSelection(0);
        spinnerSize.setSelection(0);
        spinnerLanguage.setSelection(0);

        final Button cssSaveButton = (Button) mCSSToolbar.findViewById(R.id.csstoolbar_save_btn);
        cssSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cssSaveToolbarButtonClicked();
            }
        });
        Button cssCancelButton = (Button) mCSSToolbar.findViewById(R.id.csstoolbar_close_btn);
        cssCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();

                if (mSymbolBoxFragment!=null) {
                    mSymbolBoxFragment.hideSymbolBoxWithAnimation(true);
                }
                activeCardDetailFragment.dismissKeyboard();
                activeCardDetailFragment.resetVerticalScrollViewBottomMargin();

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

                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();
                final int selectionStart = activeCardDetailFragment.mCurrentFocusedCardContentText.getSelectionStart();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                if (mIsKeyboardVisible) {

                    if (activeCardDetailFragment.isCurrentFocusedCardContentTextUsingDefaultFont() == false){
                        Toast.makeText(getApplicationContext(),R.string.DIALOG_SYMBOL_NOT_SUPPORTED_BY_FONT,Toast.LENGTH_LONG).show();
                    } else {
                        setAsSymbolStatus();
                        mIsKeyboardVisible = false;

                        Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        activeCardDetailFragment.mCurrentFocusedCardContentText.setSelection(selectionStart);
                                    }

                                }, 400);
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

                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();

                if (position > 0) //this is necessary, since default will be automatically executed
                {
                    activeCardDetailFragment.updateCSS(3, position - 1, null);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spinnerAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();
                if (position > 0) //this is necessary, since default will be automatically executed
                    activeCardDetailFragment.updateCSS(0, position - 1, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();
                if (position > 0)
                    activeCardDetailFragment.updateCSS(1, position - 1, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();
                if (position > 0)
                    activeCardDetailFragment.updateCSS(2, position - 1,null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final CardDetailFragment activeCardDetailFragment = getActiveCardDetailFragment();
                if (position > 0) {
                    ArrayList selectedList = adapterLanguage.getSelectedList();
                    activeCardDetailFragment.updateCSS(4, position - 1, selectedList);
                    if (selectedList.contains(Integer.valueOf(position))) {
                        spinnerLanguage.setSelection(0);
                    }
                }
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


    public void showCSSToolbar(CSS css,String tag) {
        LOGD(TAG, "showCSSToolbar");
        CSS currentCSS = css;

        if ((mCSSToolbar != null) && (mCSSToolbar.getParent() != null)) {
            mCSSToolbar.setVisibility(View.VISIBLE);

            //Rest spinner title when touch another TextField
            Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
            Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
            Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
            Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);
            Spinner spinnerLanguage = (Spinner) mCSSToolbar.findViewById(R.id.spinner_language);

            Button saveButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_save_btn);
            Button cancelButton = (Button)mCSSToolbar.findViewById(R.id.csstoolbar_close_btn);

            mIsKeyboardVisible = true;

            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            spinnerFont.setSelection(0);
            spinnerAlign.setSelection(0);
            spinnerColor.setSelection(0);
            spinnerSize.setSelection(0);
            spinnerLanguage.setSelection(0);

            updateSpinnersHighlightedItem(currentCSS,tag);

        }
    }


    public void updateSpinnersHighlightedItem(CSS css,String tag) {
        LOGD(TAG, "updateSpinnersHighlightedItem");
        CSS currentCSS = css;

        Spinner spinnerFont = (Spinner) mCSSToolbar.findViewById(R.id.spinner_font);
        Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
        Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
        Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);
        Spinner spinnerLanguage = (Spinner) mCSSToolbar.findViewById(R.id.spinner_language);

        String[] alignArray = getResources().getStringArray(R.array.css_align);
        String[] colorArray = getResources().getStringArray(R.array.css_color);
        String[] fontArray = getResources().getStringArray(R.array.css_font);
        int[]    sizeArray = ScaleHelper.getRealSizeIntArray(MainActivity.this);

        int alignHorizontalIndex = -1; //no selected by default
        int alignVerticalIndex = -1; //no selected by default
        int colorIndex = -1; //no selected by default
        int fontIndex = 1;  // by default, it's default color, so it's 1
        int sizeIndex = -1;
        int languageIndex = -1;

        boolean semiTransparent = false;

        if (tag.equals(CardDetailFragment.TAG_SUBHEADING)) {
            alignHorizontalIndex = Arrays.asList(alignArray).indexOf(currentCSS.subheadingAlign);

            if (currentCSS.subheadingAlignVertical.equals(getString(R.string.ToolbarItem_Align_Vertical))) {
                alignVerticalIndex = 4;
            } else {
                alignVerticalIndex = 5;
            }
            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.subheadingColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.subheadingFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.subheadingSize);

            languageIndex = Text2SpeechHelper.sharedHelper().availableLanguageLocalStringList().indexOf(currentCSS.subheadingText2SpeechSound) + 1;

            semiTransparent = currentCSS.subheadingSemiTransparent;
        } else if (tag.equals(CardDetailFragment.TAG_MAIN)) {

            alignHorizontalIndex = Arrays.asList(alignArray).indexOf(currentCSS.mainAlign);

            if (currentCSS.mainAlignVertical.equals(getString(R.string.ToolbarItem_Align_Vertical))) {
                alignVerticalIndex = 4;
            } else {
                alignVerticalIndex = 5;
            }

            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.mainColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.mainFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.mainSize);

            languageIndex = Text2SpeechHelper.sharedHelper().availableLanguageLocalStringList().indexOf(currentCSS.mainText2SpeechSound) + 1;

            semiTransparent = currentCSS.mainSemiTransparent;
        } else if (tag.equals(CardDetailFragment.TAG_SUB)) {

            alignHorizontalIndex = Arrays.asList(alignArray).indexOf(currentCSS.subAlign);

            if (currentCSS.subAlignVertical.equals(getString(R.string.ToolbarItem_Align_Vertical))) {
                alignVerticalIndex = 4;
            } else {
                alignVerticalIndex = 5;
            }

            colorIndex = Arrays.asList(colorArray).indexOf(currentCSS.subColor);
            fontIndex = Arrays.asList(fontArray).indexOf(currentCSS.subFont);
            sizeIndex = searchNearestIndex(sizeArray, (int) currentCSS.subSize);

            languageIndex = Text2SpeechHelper.sharedHelper().availableLanguageLocalStringList().indexOf(currentCSS.subText2SpeechSound) + 1;

            semiTransparent = currentCSS.subSemiTransparent;

        }

        if (fontIndex == -1) {
            fontIndex = 1;
        }

//        if (languageIndex == 0) {
//            String defaultValue = Text2SpeechHelper.sharedHelper().getSelectedLanguageLocalString();
//            languageIndex = Text2SpeechHelper.sharedHelper().availableLanguageLocalStringList().indexOf(defaultValue) + 1;
//        }

        HighLightArrayAdapter adapterFont = (HighLightArrayAdapter) spinnerFont.getAdapter();
        HighLightArrayAdapter adapterSize = (HighLightArrayAdapter) spinnerSize.getAdapter();
        HighLightArrayAdapter adapterAlign = (HighLightArrayAdapter) spinnerAlign.getAdapter();
        HighLightArrayAdapter adapterColor = (HighLightArrayAdapter) spinnerColor.getAdapter();
        HighLightArrayAdapter adapterLanguage = (HighLightArrayAdapter) spinnerLanguage.getAdapter();

        ArrayList fontIndexList = new ArrayList<Integer>();
        fontIndexList.add(Integer.valueOf(fontIndex));
        adapterFont.setSelection(fontIndexList);

        ArrayList sizeIndexList = new ArrayList<Integer>();
        sizeIndexList.add(Integer.valueOf(sizeIndex+1));
        adapterSize.setSelection(sizeIndexList);

        ArrayList colorIndexList = new ArrayList<Integer>();
        colorIndexList.add(Integer.valueOf(colorIndex));
        if (semiTransparent) {
            colorIndexList.add(Integer.valueOf(colorArray.length - 1));
        }
        adapterColor.setSelection(colorIndexList);

        ArrayList alignIndexList = new ArrayList<Integer>();
        alignIndexList.add(Integer.valueOf(alignHorizontalIndex));
        alignIndexList.add(Integer.valueOf(alignVerticalIndex));
        adapterAlign.setSelection(alignIndexList);

        ArrayList languageIndexList = new ArrayList<Integer>();
        languageIndexList.add(Integer.valueOf(languageIndex));
        adapterLanguage.setSelection(languageIndexList);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {

        LOGD(TAG, "onKeyDown: keycode is " + keyCode + " key event is " + event.toString());

        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {

            if ((mSymbolBoxFragment !=null) && (mSymbolBoxFragment.isSymbolBoxVisible())) {
                mSymbolBoxFragment.hideSymbolBoxWithAnimation(false);
                return false;
            }

            if (mIsCreatingCard == true) {
                dismissCardCreateWindow();
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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            finishAffinity();
                        } else {
                            System.exit(0);
                        }
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

        LOGD(TAG, "showPackInfoView");

        if (isPackInfoViewVisible() == false) {
            findViewById(R.id.card_detail_container).setVisibility(View.GONE);
            mPackInfoView.setVisibility(View.VISIBLE);
        }

        mPackInfoView.setCurrentPack(mCurrentPack);
        mPackInfoView.scrollTo(mCurrentPack,true);


    }

    public void refreshPackInfoView() {

        if (isPackInfoViewVisible() == false) {
            findViewById(R.id.card_detail_container).setVisibility(View.GONE);
            mPackInfoView.setVisibility(View.VISIBLE);
        }

        mPackInfoView.refreshWithRebuildViewPager(false);

    }

    private void hidePackInfoView() {

        LOGD(TAG, "hidePackInfoView");

        mPackInfoView.setVisibility(View.GONE);
        findViewById(R.id.card_detail_container).setVisibility(View.VISIBLE);

    }

    public boolean isPackInfoViewVisible() {

        if (mPackInfoView.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }

    }

    private void removeAdView() {
        final ImageView imageView = (ImageView) findViewById(R.id.ad_image_view);
        imageView.setVisibility(View.GONE);
    }

    private void removeAdViewIfAllowed() {
        final ImageView imageView = (ImageView) findViewById(R.id.ad_image_view);

        if (MutipleTargetHelper.isFullVersion() == false && MutipleTargetHelper.isNoAdVersion() == false) {

        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public void checkAdView() {

        if (mPackDownloadHelper == null || mPackDownloadHelper.isDownloading() == false) {

            if (MutipleTargetHelper.isFullVersion() == false && MutipleTargetHelper.isNoAdVersion() == false) {
                showAdView();
                return;
            }
        }

        final ImageView imageView = (ImageView) findViewById(R.id.ad_image_view);
        imageView.setVisibility(View.GONE);
        return;

    }

    private void showAdView() {

        final ImageView imageView = (ImageView) findViewById(R.id.ad_image_view);

        if (MutipleTargetHelper.isFullVersion() || MutipleTargetHelper.isNoAdVersion()) {
        }

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://www.flipflashcards.com/promo/upgrade.png",imageView, defaultOptions,new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                imageView.setVisibility(View.VISIBLE);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MutipleTargetHelper.showPurchaseView();
            }
        });

    }

    private  int COUNTDOWN_SECOND_FOR_RECORDING = 30;
    private  int COUNTDOWN_SECOND_FOR_PREPARE = 6;
    public void dismissCreateSoundFragment(boolean is_to_recording) {
        View view = findViewById(R.id.record_button_background_mask_layout);

        COUNTDOWN_SECOND_FOR_PREPARE = 6;
        COUNTDOWN_SECOND_FOR_RECORDING = 30;

        mRecordStopProgress.setProgress(0);

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

                    if (COUNTDOWN_SECOND_FOR_PREPARE == 1) {

                        if (COUNTDOWN_SECOND_FOR_RECORDING == 30) {
                            AudioHelper.startRecord(); //only execute once
                        }

                        COUNTDOWN_SECOND_FOR_RECORDING--;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mRecordStopButton.setText("Stop");

                                mRecordStopProgress.setProgress((int)((30-COUNTDOWN_SECOND_FOR_RECORDING)/30.0*100));
                            }
                        });
                        if (COUNTDOWN_SECOND_FOR_RECORDING ==0) {
                            AudioHelper.stopRecord();
                            mRecordCountDownTimer.cancel();
                            mRecordCountDownTimer = null;

                        }

                    } else {

                        COUNTDOWN_SECOND_FOR_PREPARE = COUNTDOWN_SECOND_FOR_PREPARE -1;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecordStopButton.setText(String.format("%d",COUNTDOWN_SECOND_FOR_PREPARE));
                            }
                        });

                    }


                }
            },0,1000);


        }

    }


    public void showSnapShotProgressDialog() {

        LOGD(TAG, "showSnapShotProgressDialog");

        if (mSnapShotDialog == null) {
            mSnapShotDialog = new ProgressDialog(MainActivity.this);
            mSnapShotDialog.setMax(100);
            mSnapShotDialog.setCancelable(false);
            mSnapShotDialog.setCanceledOnTouchOutside(false);
            mSnapShotDialog.setMessage(getString(R.string.DIALOG_APPLY_TO_ALL_CARD));
            mSnapShotDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        if (mSnapShotDialog.isShowing() == false) {
            mSnapShotDialog.show();
        }
    }

    public void dismissSnapShotProgressDialog() {

        LOGD(TAG, "dismissSnapShotProgressDialog");

        if (mSnapShotDialog != null && mSnapShotDialog.isShowing()) {
            mSnapShotDialog.dismiss();
        }
    }

    private void updateScreenshotProgressDialogWithProgress(int progress) {

        final int finalProgress = progress;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSnapShotDialog.isShowing() == false) {
                    mSnapShotDialog.show();
                }
                mSnapShotDialog.setProgress(finalProgress);
            }
        });
    }




    //TODO:  lint This Handler class should be static or leaks might occur (null)
    private final Handler mDropboxUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Dropbox_Constant.UPLOAD_SUCCEED: {
                    String filePathInDropbox = (String) msg.obj;

                    Toast.makeText(getApplicationContext(), R.string.DIALOG_UPLOAD_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

                    mDropboxShareHelper = new DropboxShareHelper(MainActivity.this,mCurrentPack,filePathInDropbox,false);
                    mDropboxShareHelper.execute();

                    break;
                }


                case Dropbox_Constant.UPLOAD_FAILED: {

                    Exception exception = (Exception) msg.obj;
                    if (exception != null) {
                        String message = exception.getMessage();
                        if (message != null && message.toLowerCase().contains("invalid_access_token")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    MainActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setNegativeButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.DIALOG_DROPBOX_TOKEN_ERROR_PLEASE_LOG_IN_AGAIN).show();
                        } else if (message != null && message.toLowerCase().contains("insufficient_space")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    MainActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setNegativeButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.DIALOG_ERROR_DROPBOX_QUOTA_FULL).show();
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    MainActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setNegativeButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.DIALOG_UPLOAD_FAILURE).show();
                        }
                    }

                    break;
                }

            }
        }
    };


    //TODO:  lint This Handler class should be static or leaks might occur (null)
    private final Handler mGoogleDriveUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GoogleDrive_Constant.UPLOAD_SUCCEED: {

                    String googleDriveShareLink = (String) msg.obj;

                    Toast.makeText(getApplicationContext(), R.string.DIALOG_UPLOAD_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

                    mGoogleDriveShareHelper = new GoogleDriveShareHelper(MainActivity.this,mCurrentPack,false,googleDriveShareLink);
                    mGoogleDriveShareHelper.execute();

                    break;
                }


                case GoogleDrive_Constant.UPLOAD_FAILED: {
                    break;
                }

            }
        }
    };


    //TODO: lint This Handler class should be static or leaks might occur (null)
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
                        mUploadProgressDialog.setCancelable(false);
                        mUploadProgressDialog.setCanceledOnTouchOutside(false);
                        mUploadProgressDialog.setMessage(getString(R.string.Indicator_Upload));
                        mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mUploadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.DIALOG_CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mAmazonUploadHelper != null) {
                                    mAmazonUploadHelper.stop();
                                }
                            }
                        });
                    }

                    if (flag == 0) {
                        mUploadProgressDialog.dismiss();

                        Toast.makeText(getApplicationContext(), R.string.DIALOG_UPLOAD_SUCCESSFULLY, Toast.LENGTH_SHORT).show();

                        mAWSShareHelper = new AWSShareHelper(MainActivity.this, mCurrentPack,false);
                        mAWSShareHelper.execute();

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
        LOGD(TAG, "onActivityResult with request code = " + requestCode);

        if (requestCode == Global.REQUEST_LOGIN) {
            //old parse logic here
        } else if (requestCode == 2061984) {  //defined in "https://github.com/anjlab/android-inapp-billing-v3"
            android.app.Fragment fragment = getFragmentManager().findFragmentByTag("PurchaseFragment");
            if (fragment != null)
            {
                ((PurchaseFragment)fragment).onActivityResult(requestCode, resultCode,data);
            }

        } else if (requestCode == Global.REQUEST_ACTION_MANAGE_OVERLAY_PERMISSION) {
//            if (Settings.canDrawOverlays(this)) {
//                // continue here - permission was granted
//            } else {
//                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
//                    .setTitleText("Alert")
//                    .setContentText("Sorry but you have to  give it permission to access. Otherwise the app would not work well.")
//                    .setConfirmText("Close")
//                    .show();
//            }
        } else if (requestCode == Global.REQUEST_CODE_GOOGLE_ACCOUNT_PICKER) {

            if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    GoogleDriveAuthHelper.sharedHelper(MainActivity.this).finishAuthentication(accountName);
                    shareToGoogleDrive();
                }
            }

        } else if (requestCode == Global.REQUEST_CODE_GOOGLE_DRIVE_REQUEST_PERMISSION) {

            shareToGoogleDrive();

        }else {

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

    public void updateEditPackNavIcon() {
        invalidateOptionsMenu();
    }



    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

        if (mBillingProcessor.isPurchased(Global.DOLLAR_5_PURCHASE_ID) && MutipleTargetHelper.isFullVersion() == false) {
            MutipleTargetHelper.setFullVersionFlag(true);
            removeAdViewIfAllowed();
        } else {

            if (mBillingProcessor.isPurchased(Global.DOLLAR_1_PURCHASE_ID) && MutipleTargetHelper.isNoAdVersion() == false) {
                MutipleTargetHelper.setNoAdVersionFlag(true);
                removeAdViewIfAllowed();
            }
        }

    }


    private void setupPurchase() {

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(MainActivity.this);
        if(!isAvailable) {
            return;
        } else {
            LOGD(TAG, "Google In-app Billing is ready");
        }

        String GOOGLE_IAP_LICENCE_KEY = getString(R.string.lvl_public_key);

        mBillingProcessor = new BillingProcessor(MainActivity.this,Global.MERCHANT_ID,GOOGLE_IAP_LICENCE_KEY,this);

    }


    class HighLightArrayAdapter extends ArrayAdapter<CharSequence> {

        private ArrayList mSelectedList;

        public ArrayList getSelectedList() {
            return mSelectedList;
        }


        public void setSelection(ArrayList list) {
            mSelectedList =  list;
            notifyDataSetChanged();
        }

        public HighLightArrayAdapter(Context context, int resource, CharSequence[] objects) {
            super(context, resource, objects);
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View itemView =  super.getDropDownView(position, convertView, parent);

            if (mSelectedList!= null && mSelectedList.contains(Integer.valueOf(position))) {
                itemView.setBackgroundColor(Color.rgb(56,184,226));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            return itemView;
        }
    }



    public class ZippingAndEncryptTask extends AsyncTask<String, Void, Integer> {

        private String  mPassword;
        private File    mZippedFile;

        public ZippingAndEncryptTask(String password) {
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {

            if (mZipAndEncryptDialog == null) {
                mZipAndEncryptDialog = new ProgressDialog(MainActivity.this);
                mZipAndEncryptDialog.setMax(100);
                mZipAndEncryptDialog.setCancelable(false);
                mZipAndEncryptDialog.setCanceledOnTouchOutside(false);
                mZipAndEncryptDialog.setMessage(getString(R.string.Indicator_Share_Process_Processing));
                mZipAndEncryptDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }

            if (mZipAndEncryptDialog.isShowing() == false) {
                mZipAndEncryptDialog.show();
            }

        }

        @Override
        protected Integer doInBackground(String... params) {

            mZippedFile = PackBuildHelper.createPackZipFile(MainActivity.this, mCurrentPack, mPassword);
            if (mZippedFile == null) {
                return -1;

            } else {
                boolean result = CryptoHelper.encryptFileWithSameOutput(mZippedFile);
                if (result == false) {
                    return -2;

                } else {
                    return 0;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {

            if (mZipAndEncryptDialog != null && mZipAndEncryptDialog.isShowing()) {
                mZipAndEncryptDialog.dismiss();
            }

            if (integer == -1) {
                Toast.makeText(getApplicationContext(), R.string.DIALOG_CREATE_ZIPPED_SHARE_FILE_FAILED, Toast.LENGTH_LONG).show();
            } else if (integer == -2) {
                Toast.makeText(getApplicationContext(), R.string.DIALOG_ENCRPT_ZIPPED_SHARE_FILED_FAILED, Toast.LENGTH_LONG).show();
            } else {

                //步骤： upload -- > 设置最大分享数 --> 创建短连接 --> 分享
                if (DropboxAuthHelper.sharedHelper().isLinked()) {

                    mDropboxUploadHelper = new DropboxUploadHelper(MainActivity.this, Global.DROPBOX_FOLDER, mZippedFile,mDropboxUploadHandler);
                    mDropboxUploadHelper.execute(mZippedFile.toString(), Global.GOOGLE_DRIVE_FOLDER_NAME);

                } else if (GoogleDriveAuthHelper.sharedHelper(MainActivity.this).isLinked()) {
                    mGoogleDriveUploadHelper = new GoogleDriveUploadHelper(MainActivity.this, Global.GOOGLE_DRIVE_FOLDER_NAME, mZippedFile, mGoogleDriveUploadHandler);
                    mGoogleDriveUploadHelper.execute();
                } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    mAmazonUploadHelper = new AWSUploadHelper(MainActivity.this, mAmazonUploadHandler);
                    mAmazonUploadHelper.upload(mZippedFile);
                } else {
                    LOGE(TAG, "onPostExecute, and should not be here, please have a check");
                }
            }
        }



    }

    public void onEventMainThread(MultiMediaFullscreenEvent event) {

        mIsAllowedToShowPackList = false;

    }

    public void onEventMainThread(FacebookShareFinishEvent event) {
        reShowShareSocialListAlert();
    }

    private void reShowShareSocialListAlert() {
        if (Global.activeShareStorage == 0) {
            if (mGoogleDriveShareHelper != null) {
                mGoogleDriveShareHelper.showShareSocialListAlert();
            }
        } else if (Global.activeShareStorage == 1) {
            if (mDropboxShareHelper != null) {
                mDropboxShareHelper.showShareSocialListAlert();
            }

        } else if (Global.activeShareStorage == 2) {
            if (mAWSShareHelper != null) {
                mAWSShareHelper.showShareSocialListAlert();
            }
        }
    }


    public void onEventMainThread(WebViewMessageEvent event) {

        mIsAllowedToShowPackList = false;

        String urlStr = event.ffcURLToDownload;
        final Uri packUri = Uri.parse(urlStr);
        Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        downloadPack(packUri);
                    }

                }, 500); // 5000ms delay

    }

    public void onEventMainThread(PurchasedSuccessEvent event) {

        removeAdView();

    }

    public void onEventMainThread(PurchasedStatusChangeUpdateEvent event) {

        removeAdViewIfAllowed();

    }

    public void onEventMainThread(DownloadCancelEvent event) {

        if (mPackDownloadHelper != null) {
            mPackDownloadHelper.cancel(true);
            mPackDownloadHelper = null;
        }

        if (AppConfig.sharedInstance().isHelpTipHasBeenShowedFirst() == false) {
            final Button paletteButton = (Button) findViewById(R.id.tooltip_fake_actionbar_palette); //TODO: we need to match the real meaning with its name
            TipHelper.showTipForActionBarHelp(MainActivity.this, paletteButton,true);
            Global.isAllowToShowTooltips = false;
        }

        checkAdView();

    }

    /*
     * delegate of PackInfoViewDelegate
     */
    @Override
    public void didScrollToPackOnPackInfoView(Pack pack) {
        //update list view
        setCurrentPack(pack);
        CardListFragment cardListFragment = (CardListFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_card_list));
        cardListFragment.setCurrentPack(pack);
        cardListFragment.updateListView(-1,false);

    }

    /*
     * delegate of PackInfoViewDelegate
     */
    @Override
    public void playButtonClickedOnPackInfoView() {
        play();
    }


}
