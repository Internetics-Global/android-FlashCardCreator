package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.AddPackFragment;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.fragment.CardListFragment;
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
        CardListFragment.Callbacks {

    //Used to diff whether is on card view and card creating
    private boolean mIsCreatingCard = false;
    public boolean  mIsEdittingCard = false;

    public Pack mCurrentPack = new Pack();//mCurrentPack will be automatically refreshed after creating a new card, add a new pack and new pack selected
    public int  mCurrentCardIndex = 0;
    public Card mCurrentCard = new Card();

    //Progress dialog related
    private static final int DIALOG_UPLOADING_PACK = 0;
    private ProgressDialog mDialog;

    private boolean mIsGoingAuthorizationBeforeUpload = false;

    public PopupWindow mPopupWindow;

    private CardDetailFragment mCardDetailFragment;

    private CardDetailFragment mSnapshotCardDetailFragment;

    private ArrayList<CardDetailFragment> mArrayCardDetailFragments;   //speical for snapshot(not include current card)

    private View mCSSToolbar;

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
                Log.d(Global.debugTag, "Button of add card  is clicked");
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
        int menuID;
        if (mIsCreatingCard) {
            menuID = R.menu.actionbar_add_card;
        } else {
            menuID = R.menu.actionbar;
        }
        menu.clear();
        getMenuInflater().inflate(menuID, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (item.getItemId()) {
            case R.id.actionbar_add_pack: {
                DialogFragment dialogFragment = new AddPackFragment();
                dialogFragment.show(getFragmentManager(), "add_pack_fragment");
                break;
            }
            case R.id.actionbar_edit:

                if ((mCurrentPack != null) && (mCurrentPack.cards.size() > 0)) {
                    CardListFragment cardListFragment = (CardListFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_card_list));

                    if (item.getTitle().equals("edit")) {
                        item.setTitle("done");
                        cardListFragment.enterEditStyle(true);
                    } else {
                        item.setTitle("edit");
                        cardListFragment.enterEditStyle(false);
                    }
                }
                break;
            case R.id.actionbar_packs:
                View popupLayout = inflater.inflate(R.layout.pack_list, null, false);
                mPopupWindow = new PopupWindow(600, 390);
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popupwindow_background));
                mPopupWindow.setContentView(popupLayout);
                mPopupWindow.showAsDropDown(findViewById(R.id.actionbar_packs));
                break;

            case R.id.actionbar_change_template_color:
                int defaultIndex = (StringUtils.convertTemplateBackgroundStringToResourceID(mCurrentCard.templateBackground))[0];
                if (mCurrentPack.cards.size() >= 0) {
                    new AlertDialog.Builder(this)
                            .setTitle("Select a template background")
                            .setSingleChoiceItems(new String[]{"Blue", "Coffee", "Gray", "Purple", "Red"}, defaultIndex, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mCardDetailFragment != null) {
                                        dialog.dismiss();
                                        mCardDetailFragment.cardColorTemplateSelectedPostAction(which);
                                    }
                                }
                            })

                            .show();
                }
                break;
            case R.id.actionbar_more:
                startActivity(new Intent(MainActivity.this, MoreActivity.class));
                break;

            case R.id.actionbar_play:
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("packID", mCurrentPack.packID);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
                break;

            case R.id.actionbar_share:
                if (Global.apiReachableWithAlert(MainActivity.this)) {
                    onActionbarShareSelected();
                }
                break;

            case R.id.actionbar_add_card_cancel:
                Log.d(Global.debugTag, "Cancel button is clicked");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                Log.d(Global.debugTag, "Save button is clicked");
                saveNewCreatedCard();
                break;

            case R.id.actionbar_help:
                startActivity(new Intent(MainActivity.this, InstructionActivity.class));
                break;

            case R.id.actionbar_test1:

                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //Step1: download sample pack first
        boolean isDownloaded = AppConfig.sharedInstance().isExamplePackDownloadedBefore();

        boolean isReachable = Global.apiReachable(MainActivity.this);

        if ((!isDownloaded) && (isReachable)) {
            String downloableShareLink = "http://dl.dropbox.com/s/1evrmjjypjisb0o/Pack1366592957-936257718.zip";
            File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
            PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
            packDownloadHelper.mIsFromExamplePackDownload = true;
            packDownloadHelper.execute();
            return;
        }

        //Step2: call from other app or back from Dropbox authorization
        Uri data = getIntent().getData();
        if ((data != null) && (data.getScheme().equalsIgnoreCase("fcc"))) {

            if (Global.apiReachableWithAlert(MainActivity.this)) {
                String downloableShareLink = data.toString().replace("fcc", "http").replace("wwww", "dl");
                File downloadedZipFile = new File(FileOperationHelper.downloadedPackDirectory(), "downloadedPackZip.zip");
                PackDownloadHelper packDownloadHelper = new PackDownloadHelper(MainActivity.this, downloableShareLink, downloadedZipFile.toString());
                packDownloadHelper.execute();
            }
        } else {
            if (true == mIsGoingAuthorizationBeforeUpload) {
                AndroidAuthSession session = DropboxHelper.getDropboxAPI().getSession();
                if (session.authenticationSuccessful()) {
                    session.finishAuthentication(); // Mandatory call to complete the auth
                    // Store it locally in our app for later use
                    TokenPair tokens = session.getAccessTokenPair();
                    DropboxHelper.storeKeys(this, tokens.key, tokens.secret);
                    mIsGoingAuthorizationBeforeUpload = false;
                    uploadingPackAfterLinked();
                }
            }
        }


        initializeCSSToolbar();
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
            mCurrentCard = mCurrentPack.cards.get(mCurrentCardIndex);
            mCardDetailFragment = new CardDetailFragment(mCurrentPack, mCurrentCard, 0);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.card_detail_container, mCardDetailFragment).commit();
        } else {
            mCurrentCardIndex = -1;
            mCurrentCard = null;
            if (mCardDetailFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(mCardDetailFragment).commit();
            }
        }
    }


    /**
     * @param pack, not 100% equal with mCurrentPack in MainActivity.java
     * @param card
     */
    private void prepareSnapShotSelectedCard(Pack pack,Card card) {
        mSnapshotCardDetailFragment = new CardDetailFragment(pack, card, 3);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.detail, mSnapshotCardDetailFragment).commit();

        if (mArrayCardDetailFragments == null) {
            mArrayCardDetailFragments = new ArrayList<CardDetailFragment>();
        }

        mArrayCardDetailFragments.add(mSnapshotCardDetailFragment);
    }


    /**This is called by CardDetailFragment which represent current showing card in detail
     * @param pack,       snapshot all the cards in this pack
     * @param exceptCard, except this
     */
    public void prepareSnapShotAllExceptOne(Pack pack, Card exceptCard) {

        ArrayList<Card> cards = pack.cards;
        for (Card card : cards) {
            if (card.cardID != exceptCard.cardID) {
                prepareSnapShotSelectedCard(pack,card);
            }
        }
    }

    /**
     * This is called by CardDetailFragment which represent current showing card in detail
     */
    public void finishSnapShotAllExceptOne() {

        if (mArrayCardDetailFragments == null)
            return;

        for (CardDetailFragment cardDetailFragment : mArrayCardDetailFragments) {
            getSupportFragmentManager().beginTransaction().remove(cardDetailFragment).commit();
        }
        mArrayCardDetailFragments = null;

    }


    private void startCreateCard() {

        //mCurrentPack = CardListModel.getLatestCreatedPack();//don't need to do here
        boolean result = checkEntryConditionBeforeCreatingNewCard(mCurrentPack);
        if (result == false) {
            return;
        }

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

        mCardDetailFragment = new CardDetailFragment(mCurrentPack, null, 1);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.in_from_right, R.anim.out_to_right)
                .replace(R.id.add_card_frame_layout, mCardDetailFragment)
                .commit();

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
        intent.putExtra(Global.KEY_CARD_INDEX, mCurrentPack.cards.size());
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
        DropboxAPI<AndroidAuthSession> mDBApi = DropboxHelper.getDropboxAPI();
        if (mDBApi.getSession().isLinked()) {
            uploadingPackAfterLinked();
        } else {
            mIsGoingAuthorizationBeforeUpload = true;
            mDBApi.getSession().startAuthentication(MainActivity.this);
        }
    }

    private void uploadingPackAfterLinked() {
        if (PackRecordHelper.checkUploadPackNecessary(MainActivity.this, mCurrentPack)) {
            AndroidAuthSession session = DropboxHelper.getDropboxAPI().getSession();
            if (session.isLinked()) {
                File file = PackBuildHelper.createPackZipFile(mCurrentPack);
                //File file = new File(FileOperationHelper.getTestFile().toString()); test purpose
                PackUploadHelper upload = new PackUploadHelper(this, "/FlashCardCreator/", file, mCurrentPack);
                upload.execute();
            }
        } else {
            String shareLink = PackRecordHelper.getCurrentPackShareLink(mCurrentPack);
            ShareLinkHelper shareLinkHelper = new ShareLinkHelper(this, shareLink, mCurrentPack);
            shareLinkHelper.execShareAction();
        }
    }

    private void initializeCSSToolbar() {

        if (mCSSToolbar!=null) {
            return;
        }

        //get actionbar height ( we can not directly use getActionbar.getHeight)
        TypedValue tv = new TypedValue();
        int actionbarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true)) {
            actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            Log.d(Global.debugTag,"actionbar height is:" + actionbarHeight);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, actionbarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT|Gravity.TOP;
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mCSSToolbar = inflater.inflate(R.layout.css_toolbar, null);
        mCSSToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(Global.debugTag, "touch me");
                return false;
            }
        });
        wm.addView(mCSSToolbar, params);
        mCSSToolbar.setVisibility(View.INVISIBLE);

        Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
        Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
        Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);
        spinnerAlign.setSelected(false);
        spinnerColor.setSelected(false);
        spinnerSize.setSelected(false);


        spinnerAlign.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    mCardDetailFragment.updateCSS(0, position -1);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    mCardDetailFragment.updateCSS(1, position -1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    mCardDetailFragment.updateCSS(2, position -1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void prepareCSSToolbar() {
        if (mCSSToolbar == null) {
            initializeCSSToolbar();
        } else {
            mCSSToolbar.setVisibility(View.VISIBLE);

            Spinner spinnerAlign = (Spinner) mCSSToolbar.findViewById(R.id.spinner_align);
            Spinner spinnerColor = (Spinner) mCSSToolbar.findViewById(R.id.spinner_color);
            Spinner spinnerSize = (Spinner) mCSSToolbar.findViewById(R.id.spinner_size);

            spinnerAlign.setSelection(0);
            spinnerColor.setSelection(0);
            spinnerSize.setSelection(0);
        }
    }

    public void removeCSSToolbar() {
        if (mCSSToolbar == null) {
            return;
        } else {
            mCSSToolbar.setVisibility(View.INVISIBLE);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mCSSToolbar);
        }
    }


}
