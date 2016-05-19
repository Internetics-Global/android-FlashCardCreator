package com.flipflash.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.flipflash.android_ffc.R;
import com.flipflash.util.UIHelper;


public class PurchaseFragment extends android.app.DialogFragment {
    private static final String TAG = PurchaseFragment.class.getSimpleName();

    private View mContentView;
    private WebView  mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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


        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.add_pack_window_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.add_pack_window_height) + (int)UIHelper.convertDpToPixel(50);
        mContentView.setLayoutParams(params);

        mWebView = (WebView) mContentView.findViewById(R.id.webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.loadUrl("http://www.flipflashcards.com/promo/index.html");
    }
}
