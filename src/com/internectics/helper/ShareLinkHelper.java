package com.internectics.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.internectics.util.Global;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mShareLink;


    /*
      @param shareLink must enter a valid value when directly sharing; enter anything when creating share link first
     */
    public ShareLinkHelper(Context context,String shareLink) {
        mContext = context;
        mShareLink = shareLink;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DropboxAPI.DropboxLink link = DropboxHelper.getDropboxAPI(mContext).share("/dd.html");
            mShareLink = link.toString();
            Log.d(Global.debugTag, mShareLink);
            execShareAction();

        } catch (DropboxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

    public void execShareAction() {
        if (mShareLink == null) {
            Log.d(Global.debugTag, "You need to provide a share link URL firstly");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mShareLink);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Something to say:");
        mContext.startActivity(Intent.createChooser(intent, "Share current pack to"));
    }

}
