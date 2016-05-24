package com.flipflash.fragment;

import android.app.AlertDialog;
import android.content.Intent;
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

    /*
     * we have to disable this function, since it does not work: https://github.com/anjlab/android-inapp-billing-v3/issues/10
     */
    private Button restorePurchaseButton;


    private BillingProcessor mBillingProcessor;

    private static final String DOLLAR_1_PURCHASE_ID = "com.flipflash.flipflashcards.removeads";
    private static final String DOLLAR_5_PURCHASE_ID = "com.flipflash.flipflashcards.full";
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID=null;

    private boolean mLocalizedPriceUpdated = false;
    private boolean mPurchaseRestoreRequest = false;

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
    public void onResume() {
        super.onResume();

        LOGD(TAG, "onResume");

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.add_pack_window_height) + (int)UIHelper.convertDpToPixel(50);
        mContentView.setLayoutParams(params);

        mWebView = (WebView) mContentView.findViewById(R.id.webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.loadUrl("http://www.flipflashcards.com/promo/index.html");

        setupPurchase();
    }


    private void setupPurchase() {

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(getActivity());
        if(!isAvailable) {
            showSimpleAlertDialogWidthMessage("Google In-app Billing is not available now");
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

        mPurchaseRestoreRequest = true;
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
    }

    private void dollar5PurchaseButtonClicked() {

        LOGD(TAG, "dollar5PurchaseButtonClicked");

        if (mLocalizedPriceUpdated == false) {
            showPriceNeedToBeUpdatedDialog();
            return;
        }

        //if already purchased, will still call onProductPurchased, so that's the reason why we can throw restore function 
        mBillingProcessor.purchase(getActivity(), DOLLAR_5_PURCHASE_ID);

    }

    private void dollar1PurchaseButtonClicked() {

        LOGD(TAG, "dollar1PurchaseButtonClicked");

        if (mLocalizedPriceUpdated == false) {
            showPriceNeedToBeUpdatedDialog();
            return;
        }

        //if already purchased, will still call onProductPurchased, so that's the reason why we can throw restore function
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
            showSimpleAlertDialogWidthMessage("Thank you for upgrading, please restart the app to be effective");
        } else {
            showSimpleAlertDialogWidthMessage("In-app billing configuration is not expected, check in Google Console");
        }

    }

    @Override
    public void onPurchaseHistoryRestored() {

        LOGD(TAG, "onPurchaseHistoryRestored");

        {
            TransactionDetails dollar1TransactionDetails = mBillingProcessor.getPurchaseTransactionDetails(DOLLAR_1_PURCHASE_ID);
            if (dollar1TransactionDetails != null) {
                LOGD(TAG, "1 dollar purchased has be restored");
                MutipleTargetHelper.setNoAdVersionFlag(true);
            }
        }

        {
            TransactionDetails dollar5TransactionDetails = mBillingProcessor.getPurchaseTransactionDetails(DOLLAR_5_PURCHASE_ID);
            if (dollar5TransactionDetails != null) {
                LOGD(TAG, "5 dollar purchased has be restored");
                MutipleTargetHelper.setFullVersionFlag(true);
            }
        }

        if (mPurchaseRestoreRequest) {
            showSimpleAlertDialogWidthMessage("Successfully restored, please restart the app to be effective");
            mPurchaseRestoreRequest = false;
        }

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

        LOGD(TAG, "onBillingError with errorCode = " + errorCode);

        //showSimpleAlertDialogWidthMessage("In-app billing error:" + errorCode);

    }

    @Override
    public void onBillingInitialized() {

        LOGD(TAG, "onBillingInitialized");

        if (mBillingProcessor.isPurchased(DOLLAR_1_PURCHASE_ID) && MutipleTargetHelper.isNoAdVersion() == false) {
            MutipleTargetHelper.setNoAdVersionFlag(true);
            showSimpleAlertDialogWidthMessage("Successfully restored, please restart the app to be effective");
            return;
        }

        if (mBillingProcessor.isPurchased(DOLLAR_5_PURCHASE_ID) && MutipleTargetHelper.isFullVersion() == false) {
            MutipleTargetHelper.setFullVersionFlag(true);
            showSimpleAlertDialogWidthMessage("Successfully restored, please restart the app to be effective");
            return;
        }


        ArrayList<String> list = new ArrayList<>(2);
        list.add(DOLLAR_1_PURCHASE_ID);
        list.add(DOLLAR_5_PURCHASE_ID);

        SkuDetails test = mBillingProcessor.getPurchaseListingDetails(DOLLAR_1_PURCHASE_ID);
        if (test != null) {
            Log.d(getTag(),test.priceText);
        }

        List<SkuDetails> skuList =  mBillingProcessor.getPurchaseListingDetails(list);
        if (skuList == null) {
            showSimpleAlertDialogWidthMessage("No in-app products in Google console");
        } else if (skuList.size() != 2) {

            String msg = "";
            for (SkuDetails item : skuList) {
                msg = msg + item.productId;
            }
            LOGD(TAG,msg);

            showSimpleAlertDialogWidthMessage("You can have only 2 in-app product in Google Console, currently, you have: " + skuList.size() );
        } else {

            SkuDetails firstSkuPrice = skuList.get(0);
            SkuDetails secondSkuPrice = skuList.get(1);



            if (firstSkuPrice.productId.equals(DOLLAR_1_PURCHASE_ID) && secondSkuPrice.productId.equals(DOLLAR_5_PURCHASE_ID)) {

                dollar1PurchaseButton.setText("No Ads - " + firstSkuPrice.priceText);
                dollar5PurchaseButton.setText("Full Version - " + secondSkuPrice.priceText);

                mLocalizedPriceUpdated = true;

            } else if (firstSkuPrice.productId.equals(DOLLAR_5_PURCHASE_ID) && secondSkuPrice.productId.equals(DOLLAR_1_PURCHASE_ID)) {

                dollar1PurchaseButton.setText("No Ads - " + secondSkuPrice.priceText);
                dollar5PurchaseButton.setText("Full Version - " + firstSkuPrice.priceText);

                mLocalizedPriceUpdated = true;

            } else {

                showSimpleAlertDialogWidthMessage("IAP configuration is not expected");
            }

        }

    }

    private void showPriceNeedToBeUpdatedDialog() {

        showSimpleAlertDialogWidthMessage("Please wait for price to be fetched");
    }

    private void showSimpleAlertDialogWidthMessage(String msg) {

        LOGD(TAG, "showSimpleAlertDialogWidthMessage with message of" + msg);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setPositiveButton("Close", null);
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
