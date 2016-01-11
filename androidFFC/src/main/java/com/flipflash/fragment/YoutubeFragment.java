package com.flipflash.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;

import com.flipflash.android_ffc.R;
import com.flipflash.util.StringUtils;

import java.net.URL;

/**
 * Created by BourneWang on 17/12/2015.
 */
public class YoutubeFragment extends DialogFragment {

    private View mContentView;

    private String mYoutubeLink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.fragment_youtube, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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
        saveButton.setVisibility(View.INVISIBLE);


        return mContentView;

    }

    @Override
    public void onResume() {
        super.onResume();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels - (int)(20 *  displayMetrics.density);
        int height = displayMetrics.heightPixels - (int)(30 *  displayMetrics.density);
        int widthDP = (int) (width/displayMetrics.density);
        int heightDP = (int) (height/displayMetrics.density);
        getDialog().getWindow().setLayout(width,height);

        WebView webView = (WebView) mContentView.findViewById(R.id.youtube_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.setWebViewClient(new WebViewClient());
        //webView.loadUrl("http://www.google.com");

        String youtubeIDStr = StringUtils.getYouTubeIDFromLink(mYoutubeLink);

        String embeddedYoutubeLink = String.format("https://www.youtube.com/embed/%s",youtubeIDStr);   //For example, https://www.youtube.com/embed/_rgzmQ_vpSo

        String playVideo= String.format("<iframe width=\"%d\" height=\"%d\" src=\"%s\" frameborder=\"0\" allowfullscreen></iframe>",widthDP,heightDP,embeddedYoutubeLink);
        webView.loadData(playVideo, "text/html", "utf-8");
    }

    @Override
    public void onStop() {
        super.onStop();

        WebView webView = (WebView) mContentView.findViewById(R.id.youtube_webview);
        webView.onPause();


    }


    public String getYoutubeLink() {
        return mYoutubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        mYoutubeLink = youtubeLink;
    }
}
