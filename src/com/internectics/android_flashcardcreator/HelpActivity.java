package com.internectics.android_flashcardcreator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 2/05/13
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class HelpActivity extends Activity {

    private WebView mWebview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        mWebview = new WebView(this);
        setContentView(mWebview);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                setProgress(newProgress *1000);
            }
        });

        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(HelpActivity.this, "Error! " + "Fail to load webpage", Toast.LENGTH_SHORT).show();
            }
        });

        mWebview.loadUrl("http://www.flipflashcards.com.au");
    }

}
