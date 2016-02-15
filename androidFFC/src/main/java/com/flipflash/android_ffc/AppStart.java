package com.flipflash.android_ffc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.flipflash.util.Global;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.orhanobut.hawk.Hawk;

import static com.flipflash.util.LogUtils.LOGD;



public class AppStart extends Activity {
    private static final String TAG = AppStart.class.getSimpleName();

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker         mChecker;

    // Generate 20 random bytes
    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
            -45, 77, -117, -36, -113, -11, 32, -64, 89
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        collectDeviceInfoForDebugging();

        Boolean checkPass = Hawk.get("LVLCheckPass");
        if (checkPass == null || checkPass.booleanValue() == false) {

            if (Global.apiReachableWithAlert(AppStart.this) == false) {

                showDialogAndExit(getString(R.string.DIALOG_WARN),getString(R.string.DIALOG_TITLE_NO_NETWORK));

            } else {

                boolean isGoogleAccountLogged = deviceHasGoogleAccount();
                if (isGoogleAccountLogged == false) {
                    Log.d("lvl","Google account need to be logged in firstly");
                    showDialogAndExit(getString(R.string.DIALOG_WARN),getString(R.string.LVL_Google_Account_Not_Logged));
                    return;
                }

                Log.d("lvl","setup LicenseChecker");

                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                mLicenseCheckerCallback = new MyLicenseCheckerCallback();
                // Construct the LicenseChecker with a Policy.
                mChecker = new LicenseChecker(
                        getApplicationContext(), new ServerManagedPolicy(AppStart.this,
                        new AESObfuscator(SALT, getPackageName(), deviceId)),
                        getString(R.string.lvl_public_key)  // Your public licensing key.key不是随意的,比如你手动更改其中的内容,就会crash app
                );
                mChecker.checkAccess(mLicenseCheckerCallback);
//                mHandler.sendEmptyMessageDelayed(0,200);
            }
        } else {
            Log.d("lvl","lvl check passed and go on");
            mHandler.sendEmptyMessageDelayed(0,200);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChecker != null) {
            mChecker.onDestroy();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(AppStart.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
    };

    private void collectDeviceInfoForDebugging() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        //cardDPHeightInEditMode = screen height -  actionbarHeight -  card top margin - card bottom margin -segment height - segment bottom margin
        float cardDPHeightInEditMode = dpHeight - 40 - 10 - 10 - 24 - 10;
        float cardDPHeightInPlayMode = dpHeight - 10 - 10;
        float ratio = cardDPHeightInPlayMode/cardDPHeightInEditMode;
        if (ratio >Global.scaleInPlayMode) {
            Global.scaleInPlayMode = ratio;  //默认值是1.2，如果比这个数值更大，才进行赋值。这种情况只有在小屏手机中才有这个问题
        }



        LOGD(TAG, "collectDeviceInfoForDebugging: device width = " + dpWidth + " height = " + dpHeight);

        float density = getResources().getDisplayMetrics().density;
        LOGD(TAG, "desnity * dp = px, here density = " + density);
    }



    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int reason) {

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            Hawk.put("LVLCheckPass",Boolean.valueOf(true));


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    AppStart.this);
            alertDialogBuilder.setTitle(getString(R.string.DIALOG_AlERT));
            alertDialogBuilder.setNegativeButton(getString(R.string.DIALOG_CLOSE), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mHandler.sendEmptyMessageDelayed(0,100);
                }
            });
            alertDialogBuilder
                    .setMessage(R.string.LVL_Licence_Granted).show();
        }


        public void dontAllow(int reason) {

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            //有几个注意点:
            //1.模拟器上不能用(始终返回Policy.RETRY),除非模拟器有Google Play service
            //2.必须是联网的.因为the licensing server must be accessible over the network
            //3.测试时,你的Google Play账号,必须登记在developer console网站上


            if (Policy.NOT_LICENSED == reason) {
                Log.d("lvl","Policy.NOT_LICENSED");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = getString(R.string.LVL_Not_Licenced);
                        showDialogAndExit(getString(R.string.DIALOG_WARN),message);
                    }
                });

            } else if (Policy.RETRY == reason) {

                // If the reason received from the policy is RETRY, it was probably
                // Due to a recoverable local or server error, such as when the network is not available to send the request,这就是为什么你需要前置Reachability检测的原因
                Log.d("lvl","Policy.RETRY");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = getString(R.string.LVL_Retry);
                        showDialogAndExit(getString(R.string.DIALOG_WARN),message);
                    }
                });

            }


        }

        //http://stackoverflow.com/questions/10377325/how-do-you-deal-with-licensecheckercallback-error-not-market-managed-error-code
        @Override
        public void applicationError(final int errorCode) {

            Log.d("lvl","applicationError with errorCode = " + errorCode);

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            if (errorCode == LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED) {

                //don't worry if ERROR_NOT_MARKET_MANAGED = 0x3
                //If a end user somehow get a copy of your application (with LVL integrated and uploaded/published in Google Play) from other channel (not purchase via Google Play) and trying to install it on his device (with Google Play client application installed on that device), in this case, LicenseCheckerCallback will go to dontAllow() rather than applicationError(ApplicationErrorCode errorCode).
                //your app has to be on the market and if it already is, you have to have a version code number which is greater or equal than the one already published.

                //only enable when debugging
                if (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    AppStart.this);
                            alertDialogBuilder.setTitle(getString(R.string.DIALOG_AlERT));
                            alertDialogBuilder.setMessage(getString(R.string.LVL_Not_Market_Managed));
                            alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    finish();

                                }
                            });
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder
                                    .show();
                        }
                    });
                } else {
                    mHandler.sendEmptyMessageDelayed(0, 200);
                }



            } else {

//                ERROR_INVALID_PACKAGE_NAME = 1;  //防止别人下载你的app,然后换个名字上传到google play
//                ERROR_NON_MATCHING_UID = 2;  //防止别人下载你的app,然后换个名字上传到google play
//                ERROR_NOT_MARKET_MANAGED = 3; /
//                ERROR_CHECK_IN_PROGRESS = 4;
//                ERROR_INVALID_PUBLIC_KEY = 5; //因为public key是存放在代码中的,破戒者肯定无法更改,导致他永远不可能更改app_id和app_name
//                ERROR_MISSING_PERMISSION = 6;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = getString(R.string.LVL_Not_Licenced);
                        showDialogAndExit(getString(R.string.DIALOG_ERROR),message);

                    }
                });

            }

        }


    }


    private void showDialogAndExit(String title, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                AppStart.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton(getString(R.string.DIALOG_CLOSE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder
                .setMessage(message).show();
    }


    private boolean deviceHasGoogleAccount(){
        AccountManager accMan = AccountManager.get(this);
        Account[] accArray = accMan.getAccountsByType("com.google");
        return accArray.length >= 1 ? true : false;
    }

}