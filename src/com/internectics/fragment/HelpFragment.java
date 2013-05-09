package com.internectics.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
public class HelpFragment extends DialogFragment {

    private static HelpFragment mDialogFragment;

    public static HelpFragment getInstance() {
        if (mDialogFragment == null) {
            return new HelpFragment();
        } else {
            return mDialogFragment;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View contentView = inflater.inflate(R.layout.fragment_help,container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Button saveButton = (Button) contentView.findViewById(R.id.dialog_head_save_btn);
        saveButton.setVisibility(View.INVISIBLE);

        Button closeButton = (Button) contentView.findViewById(R.id.dialog_head_close_btn);
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final  WebView webView = (WebView) contentView.findViewById(R.id.help_webview);
        webView.loadUrl("http://www.flipflashcards.com.au");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TextView titleTextView = (TextView)(contentView.findViewById(R.id.dialog_title));
                titleTextView.setText(webView.getTitle());
            }
        });



        return contentView;
    }
}
