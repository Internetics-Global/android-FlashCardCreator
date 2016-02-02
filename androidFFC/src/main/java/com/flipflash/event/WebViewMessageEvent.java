package com.flipflash.event;

/**
 * Created by BourneWang on 2/02/2016.
 */
public class WebViewMessageEvent {

    public final String ffcURLToDownload;

    public WebViewMessageEvent(String urlStr) {
        this.ffcURLToDownload = urlStr;
    }
}