package com.internectics.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.internectics.data.Pack;
import com.internectics.util.Global;

/**
 * 1. create share linkage
 * 2. invoke share intent
 */
public class ShareLinkHelper extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private String mFilePathInDropbox;
    private Pack mCurentPack;


    /*
      @param shareLink must enter a valid value when directly sharing; enter anything when creating share link first
     */
    public ShareLinkHelper(Context context, String file, Pack currentPack) {
        mContext = context;
        mFilePathInDropbox = file;
        mCurentPack = currentPack;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DropboxAPI.DropboxLink link = DropboxHelper.getDropboxAPI().share(mFilePathInDropbox);
            String shareLink = link.url;
            Log.d(Global.debugTag, "the shareLink is: " + shareLink);
            PackRecordHelper.savePackUploadRecord(mContext, mCurentPack, shareLink);
            execShareAction();

        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void execShareAction() {
        String shareLink = PackRecordHelper.getCurrentPackShareLink(mCurentPack);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareLink);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Something to say:");
        mContext.startActivity(Intent.createChooser(intent, "Share current pack to"));
    }

}
