package com.flipflash.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.flipflash.android_ffc.BuildConfig;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.fragment.PurchaseFragment;
import com.orhanobut.hawk.Hawk;

/**
 * Created by BourneWang on 18/05/2016.
 */
public class MutipleTargetHelper {

    private static final String K_Full_Version_Flag  = "K_Full_Version_Flag";
    private static final String K_No_Ad_Version_Flag  = "K_No_Ad_Version_Flag";

    public static boolean isFullVersion() {
        return Hawk.get(K_Full_Version_Flag,false);

    }
    public static boolean isNoAdVersion() {

        return Hawk.get(K_No_Ad_Version_Flag,false);

    }

    public static void showAlertToUpgradeToFullVersion() {

        AppContext app = (AppContext) AppContext.getAppContext();
        final MainActivity mainActivity = app.getMainActivity();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(app.getMainActivity());
        alertDialog.setTitle("This is a FlipFlashCard PRO function");
        alertDialog.setMessage("You can upgrade the app to get it!");
        alertDialog.setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                //do nothing here
            }
        });
        alertDialog.setPositiveButton("More details", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                showPurchaseView();
            }
        });
        alertDialog.show();
    }
    public static void showPurchaseView() {

        AppContext app = (AppContext) AppContext.getAppContext();
        final MainActivity mainActivity = app.getMainActivity();

        android.app.DialogFragment dialogFragment = new PurchaseFragment();
        dialogFragment.show(mainActivity.getFragmentManager(),"PurchaseFragment");


    }

    public static void setFullVersionFlag(boolean flag) {

        Hawk.put(K_Full_Version_Flag,flag);

    }
    public static void setNoAdVersionFlag(boolean flag) {

        Hawk.put(K_No_Ad_Version_Flag,flag);

    }
}
