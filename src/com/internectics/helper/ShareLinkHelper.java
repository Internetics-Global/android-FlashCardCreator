package com.internectics.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.internectics.util.Global;

/**
 * Used to
 * 1. create share linkage
 * 2. execute share action
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;

    public ShareLinkHelper(Context context) {
        mContext = context;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DropboxAPI.DropboxLink link = DropboxHelper.getDropboxAPI(mContext).share("/dd.html");

            Log.d(Global.debugTag, link.url);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link.url);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Something to say:");
            mContext.startActivity(Intent.createChooser(intent, "Share current pack to"));


        } catch (DropboxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return false;
    }

}
