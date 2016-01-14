package com.flipflash.android_ffc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

import com.flipflash.util.AppContext;

public class WebViewActivity extends Activity {
    private static final String TAG = WebViewActivity.class.getSimpleName();

    private WebView mWebview;

    private String mURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mWebview = new WebView(this);
        setContentView(mWebview);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                setProgress(newProgress * 1000);
            }
        });

        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Error! " + "Fail to load webpage", Toast.LENGTH_SHORT).show();
            }
        });

        mURL = getIntent().getExtras().getString("url");
        if (mURL == null) {
            mURL = "http://www.flipflashcards.com.au";
        }
        mWebview.loadUrl(mURL);
    }

}
