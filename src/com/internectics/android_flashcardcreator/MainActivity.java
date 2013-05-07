package com.internectics.android_flashcardcreator;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.*;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.PackTransferHelper;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.AppContext;
import com.internectics.helper.DropboxHelper;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import org.json.JSONException;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * MainActivity is the entry for whole app
 * Control both master - detail view
 * Also responsbile for managing Actionbar(or Option Menu)
 */
public class MainActivity extends FragmentActivity implements
        CardListMasterFragment.Callbacks {

    //Used to diff between card view and card creating
    private boolean mIsCreatingCard = false;

    public Pack mCurrentPack = new Pack();//mCurrentPack will be automatically refreshed after creating a new card, add a new pack and new pack selected
    public int  mCurrentIndex = 0;
    public Card mCurrentCard = new Card();

    public PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //Step1:We create a new AuthSession so that we can use the Dropbox API.


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
                try {
                    PackTransferHelper.buildCardJsonFile(new Card(),FileOperationHelper.getTestFile().toString());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                try {
                    PackTransferHelper.buildPackJsonFile(new Pack(),FileOperationHelper.getTestFile2().toString());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                try {
                    PackTransferHelper.parsePackJsonFile(FileOperationHelper.getTestFile2().toString());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ParseException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                try {
                    PackTransferHelper.parseCardJsonFile(FileOperationHelper.getTestFile().toString());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ParseException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

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

//                new AlertDialog.Builder(this)
//                        .setTitle("More")
//                        .setItems(new String[]{"Dropbox", "Random play", "Register", "Submit new listing", "Help", "About"}, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                switch (which) {
//                                    case 0:
//                                        break;
//                                    case 1:
//                                        break;
//                                    case 2:
//                                        break;
//                                    case 3:
//                                        break;
//                                    case 4:
//                                        HelpFragment helpFragment = HelpFragment.getInstance();
//                                        helpFragment.show(getFragmentManager(),"help_dialog");
//                                        break;
//                                    case 5:
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            }
//                        })
//                        .show();
                break;

            case R.id.actionbar_play:
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_above);
                break;

            case R.id.actionbar_share:

                DropboxAPI<AndroidAuthSession> mDBApi = DropboxHelper.getDropboxAPI(this);
                if (!mDBApi.getSession().isLinked()) {
                    mDBApi.getSession().startAuthentication(MainActivity.this);
                }

                //Intent intent = new Intent(Intent.ACTION_SEND);
                //intent.setType("text/plain");
                //intent.putExtra(Intent.EXTRA_TEXT, "htt://www.microsoft.com");
                //intent.putExtra(Intent.EXTRA_SUBJECT, "Something to say:");
                //startActivity(Intent.createChooser(intent, "Share current pack to"));
                break;

            case R.id.actionbar_add_card_cancel:
                Log.d(Global.debugTag, "cancel button is clicked during adding card operation");
                dismissCardCreateWindow();
                break;

            case R.id.actionbar_add_card_save:
                Log.d(Global.debugTag,"save button is clicked during adding card operation");
                saveNewCreatedCard();
                break;

            case R.id.actionbar_help:
                startActivity(new Intent(MainActivity.this,InstructionActivity.class));
                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Step1: deal with dropbox
        AndroidAuthSession session = DropboxHelper.getDropboxAPI(this).getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                DropboxHelper.storeKeys(this,tokens.key, tokens.secret);
                Toast.makeText(this, "Build session successfully", Toast.LENGTH_SHORT)
                        .show();





            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                .show();
                Log.d(Global.debugTag, "Error authenticating", e);
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
        CardDetailFragment fragment = new CardDetailFragment(mCurrentPack,mCurrentCard);
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

        CardDetailFragment fragment = new CardDetailFragment(mCurrentPack,null);
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
           Toast.makeText(this,"Create a pack first before creating a new card", 1).show();
           return false;
       }
       //case2: check owner
       if (!currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
           Toast.makeText(this,"You cannot create a card in pack you haven't created yourself.", 1).show();


           return false;

       }

       return  true;
    }
}
