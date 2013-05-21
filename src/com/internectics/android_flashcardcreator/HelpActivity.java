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
import android.widget.Button;
import android.widget.TextView;
import com.internectics.android_flashcardcreator.R;

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
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        mWebview = new WebView(this);
        setContentView(mWebview);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        mWebview.getSettings().setJavaScriptEnabled(true);

        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                setProgress(newProgress *1000);
            }
        });

        mWebview.loadUrl("http://www.flipflashcards.com.au");
    }

}
