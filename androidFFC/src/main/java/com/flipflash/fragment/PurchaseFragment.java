package com.flipflash.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.flipflash.android_ffc.AppStart;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.util.MutipleTargetHelper;
import com.flipflash.util.UIHelper;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import static com.flipflash.util.LogUtils.LOGD;


public class PurchaseFragment extends android.app.DialogFragment implements BillingProcessor.IBillingHandler {

    private static final String TAG = PurchaseFragment.class.getSimpleName();

    private View mContentView;
    private WebView  mWebView;

    private Button dollar1PurchaseButton;
    private Button dollar5PurchaseButton;
    
    private Button restorePurchaseButton;


    private BillingProcessor mBillingProcessor;

    private static final String DOLLAR_1_PURCHASE_ID = "com.flipflash.flipflashcards.removeads";
    private static final String DOLLAR_5_PURCHASE_ID = "com.flipflash.flipflashcards.full";
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID=null;

    private boolean mLocalizedPriceUpdated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        LOGD(TAG, "onCreateView");

        mContentView = inflater.inflate(R.layout.fragment_purchase, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView titleTextView = (TextView) mContentView
                .findViewById(R.id.dialog_title);
        titleTextView.setText("Purchase");
        final Button closeButton = (Button) mContentView
                .findViewById(R.id.dialog_head_close_btn);
        Button saveButton = (Button) mContentView
                .findViewById(R.id.dialog_head_save_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

            }
        });
        saveButton.setVisibility(View.GONE);


        dollar1PurchaseButton = (Button) mContentView.findViewById(R.id.button_1_dollar);
        dollar5PurchaseButton = (Button) mContentView.findViewById(R.id.button_5_dollar);
        restorePurchaseButton = (Button) mContentView.findViewById(R.id.button_restore);

        if (MutipleTargetHelper.isNoAdVersion()) {
            dollar1PurchaseButton.setEnabled(false);
            dollar1PurchaseButton.setTextColor(Color.GRAY);
        }

        dollar1PurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dollar1PurchaseButtonClicked();
            }
        });

        dollar5PurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dollar5PurchaseButtonClicked();
            }
        });

        restorePurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePurchaseButtonClicked();
            }
        });

        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        LOGD(TAG, "onStart");

        mWebView = (WebView) mContentView.findViewById(R.id.webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.loadUrl("http://www.flipflashcards.com/promo/index.html");

        setupPurchase();
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.add_pack_window_height) + (int)UIHelper.convertDpToPixel(50);
        mContentView.setLayoutParams(params);

        LOGD(TAG, "onResume");

    }


    private void setupPurchase() {

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(getActivity());
        if(!isAvailable) {
            showSimpleAlertDialogWidthMessage(getString(R.string.iap_google_service_not_available));
            return;
        } else {
            LOGD(TAG, "Google In-app Billing is ready");
        }

        String GOOGLE_IAP_LICENCE_KEY = getString(R.string.lvl_public_key);

        mBillingProcessor = new BillingProcessor(getActivity(),MERCHANT_ID,GOOGLE_IAP_LICENCE_KEY,this);

    }

    private void restorePurchaseButtonClicked() {

        LOGD(TAG, "restorePurchaseButtonClicked");

        if (mLocalizedPriceUpdated == false) {
            showPriceNeedToBeUpdatedDialog();
            return;
        }

        boolean result = mBillingProcessor.loadOwnedPurchasesFromGoogle();

        if (result) {

            if (mBillingProcessor.isPurchased(DOLLAR_1_PURCHASE_ID)) {
                MutipleTargetHelper.setNoAdVersionFlag(true);
                showSimpleAlertDialogWidthMessageWithRelaunch(getString(R.string.iap_restore_1dollar_success));
            } else if (mBillingProcessor.isPurchased(DOLLAR_5_PURCHASE_ID)) {
                MutipleTargetHelper.setFullVersionFlag(true);
                showSimpleAlertDialogWidthMessageWithRelaunch(getString(R.string.iap_restore_5dollar_success));
            } else {
                showSimpleAlertDialogWidthMessage(getString(R.string.iap_not_purchased_before));
            }

        } else {
            LOGD(TAG, "mBillingProcessor.loadOwnedPurchasesFromGoogle failed");
        }
    }

    private void dollar5PurchaseButtonClicked() {

        LOGD(TAG, "dollar5PurchaseButtonClicked");

        if (mLocalizedPriceUpdated == false) {
            showPriceNeedToBeUpdatedDialog();
            return;
        }

        mBillingProcessor.purchase(getActivity(), DOLLAR_5_PURCHASE_ID);

    }

    private void dollar1PurchaseButtonClicked() {

        LOGD(TAG, "dollar1PurchaseButtonClicked");

        if (mLocalizedPriceUpdated == false) {
            showPriceNeedToBeUpdatedDialog();
            return;
        }

        mBillingProcessor.purchase(getActivity(), DOLLAR_1_PURCHASE_ID);

    }


    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

        LOGD(TAG, "onProductPurchased");

        boolean result = false;
        if (productId.equals(DOLLAR_1_PURCHASE_ID)) {
            result = true;
            MutipleTargetHelper.setNoAdVersionFlag(true);
        } else if (productId.equals(DOLLAR_5_PURCHASE_ID)) {
            result = true;
            MutipleTargetHelper.setFullVersionFlag(true);
        }

        if (result) {
            showSimpleAlertDialogWidthMessageWithRelaunch(getString(R.string.iap_success_upgrade));
        } else {
            showSimpleAlertDialogWidthMessage(getString(R.string.iap_configuration_error));
        }

    }


    @Override
    public void onPurchaseHistoryRestored() {

        LOGD(TAG, "onPurchaseHistoryRestored");

        /*
         * It's auto called then the billing service is connected for the first time (within
         * the library) and the purchases list was downloaded from google
         * Since we have provided restore button, we comment this logic
         */
//        boolean showDialog = false;
//
//        {
//            TransactionDetails dollar1TransactionDetails = mBillingProcessor.getPurchaseTransactionDetails(DOLLAR_1_PURCHASE_ID);
//            if (dollar1TransactionDetails != null) {
//                LOGD(TAG, "1 dollar purchased has be restored");
//                MutipleTargetHelper.setNoAdVersionFlag(true);
//                showDialog = true;
//            }
//        }
//
//        {
//            TransactionDetails dollar5TransactionDetails = mBillingProcessor.getPurchaseTransactionDetails(DOLLAR_5_PURCHASE_ID);
//            if (dollar5TransactionDetails != null) {
//                LOGD(TAG, "5 dollar purchased has be restored");
//                MutipleTargetHelper.setFullVersionFlag(true);
//                showDialog = true;
//            }
//        }
//
//        if (showDialog) {
//            showSimpleAlertDialogWidthMessage("You have purchased this produc, please restart the app to be effective");
//        }

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

        LOGD(TAG, "onBillingError with errorCode = " + errorCode);

        //showSimpleAlertDialogWidthMessage("In-app billing error:" + errorCode);

    }

    @Override
    public void onBillingInitialized() {

        LOGD(TAG, "onBillingInitialized");



        if (mBillingProcessor.isPurchased(DOLLAR_5_PURCHASE_ID) && MutipleTargetHelper.isFullVersion() == false) {
            MutipleTargetHelper.setFullVersionFlag(true);
            showSimpleAlertDialogWidthMessageWithRelaunch(getString(R.string.iap_restore_5dollar_success));
        } else {

            if (mBillingProcessor.isPurchased(DOLLAR_1_PURCHASE_ID) && MutipleTargetHelper.isNoAdVersion() == false) {
                MutipleTargetHelper.setNoAdVersionFlag(true);
                showSimpleAlertDialogWidthMessageWithRelaunch(getString(R.string.iap_restore_1dollar_success));
            }
        }


        ArrayList<String> list = new ArrayList<>(2);
        list.add(DOLLAR_1_PURCHASE_ID);
        list.add(DOLLAR_5_PURCHASE_ID);

        List<SkuDetails> skuList =  mBillingProcessor.getPurchaseListingDetails(list);
        if (skuList == null || skuList.size() == 0) {

            showSimpleAlertDialogWidthMessage(getString(R.string.iap_configuration_error));

        } else if (skuList.size() < 2) {

            String msg = "";
            for (SkuDetails item : skuList) {
                msg = msg + item.productId + " ";
            }
            LOGD(TAG,"All products:" + msg);

            showSimpleAlertDialogWidthMessage(getString(R.string.iap_configuration_error));
        } else {

            SkuDetails dollar1Sku = null;
            SkuDetails dollar5Sku = null;
            for (SkuDetails item: skuList) {

                LOGD(TAG,"Available product:" + item.productId + " with price:" + item.priceText);

                if (item.productId.equals(DOLLAR_5_PURCHASE_ID)) {
                    dollar5Sku = item;
                }

                if (item.productId.equals(DOLLAR_1_PURCHASE_ID)) {
                    dollar1Sku = item;
                }

                if ((dollar1Sku != null) && (dollar5Sku != null)) {
                    break;
                }

            }

            if ((dollar1Sku != null) && (dollar5Sku != null)) {

                dollar1PurchaseButton.setText("No Ads - " + dollar1Sku.priceText);
                dollar5PurchaseButton.setText("Full Version - " + dollar5Sku.priceText);

                mLocalizedPriceUpdated = true;

            } else {

                showSimpleAlertDialogWidthMessage(getString(R.string.iap_configuration_error));
            }

        }

    }

    private void showPriceNeedToBeUpdatedDialog() {

        showSimpleAlertDialogWidthMessage(getString(R.string.iap_wait_for_update));
    }

    private void showSimpleAlertDialogWidthMessage(String msg) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Close", null);
        alertDialogBuilder
                .setMessage(msg).
                show();

    }

    private void showSimpleAlertDialogWidthMessageWithRelaunch(String msg) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent mStartActivity = new Intent(getActivity(), AppStart.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });
        alertDialogBuilder
                .setMessage(msg).
                show();

    }


    @Override
    public void onDestroy() {

        LOGD(TAG, "onDestroy");

        if (mBillingProcessor != null)
            mBillingProcessor.release();

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        LOGD(TAG, "onActivityResult");

        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }
}
