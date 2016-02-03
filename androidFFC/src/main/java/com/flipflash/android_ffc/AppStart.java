package com.flipflash.android_ffc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;

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

    //key不是随意的,比如你手动更改其中的内容,就会crash app
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh35nQt2ttqMr0RsfhS3vqDQiaaBnNLyZ3n20owNpzGMOAClyK7UpST2MALzZT7G3pOwGuxQB7fHUPXDeI4ZVrcPB0m0ZeBwJ+5xe3isxdwQMVkZkAFJnCsfrthAjBUBvxJ1w/RX8HyQs9Pqts3XVZRiDu3Pc3RVwmIjQUx8Kksoy8ks79BLmed3Ar9tG+JVjWuOtUzoGQz+1LjetNVQlg7wfLXQuGfDT+k+rO/lt62SyPr0bSS4Fj6JwBlBN8f7fM2x44/UFQV0w/zwGnDJrSLP/bMD5UJ2rXBnbJe3D0gaSK9OBg1wuSbIUrUES8FDImju3ZqxyS16pFFQKLU8qvQIDAQAB";
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
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                mLicenseCheckerCallback = new MyLicenseCheckerCallback();
                // Construct the LicenseChecker with a Policy.
                mChecker = new LicenseChecker(
                        getApplicationContext(), new ServerManagedPolicy(AppStart.this,
                        new AESObfuscator(SALT, getPackageName(), deviceId)),
                        BASE64_PUBLIC_KEY  // Your public licensing key.
                );
                mChecker.checkAccess(mLicenseCheckerCallback);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mChecker.onDestroy();
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
            mHandler.sendEmptyMessageDelayed(0,200);
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
                LOGD(TAG, "Policy.NOT_LICENSED");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = "Unauthorised source of the app, please download it from Google Play";
                        showDialogAndExit(getString(R.string.DIALOG_WARN),message);
                    }
                });

            } else if (Policy.RETRY == reason) {

                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service,这就是为什么你需要前置Reachability检测的原因

                LOGD(TAG, "Policy.RETRY");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String message = "Unauthorised source of the app, or restart to check again";
                        showDialogAndExit(getString(R.string.DIALOG_WARN),message);
                    }
                });

            }


        }

        //http://stackoverflow.com/questions/10377325/how-do-you-deal-with-licensecheckercallback-error-not-market-managed-error-code
        @Override
        public void applicationError(final int errorCode) {

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            if (errorCode == LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED) {

                //don't worry if ERROR_NOT_MARKET_MANAGED = 0x3
                //If a end user somehow get a copy of your application (with LVL integrated and uploaded/published in Google Play) from other channel (not purchase via Google Play) and trying to install it on his device (with Google Play client application installed on that device), in this case, LicenseCheckerCallback will go to dontAllow() rather than applicationError(ApplicationErrorCode errorCode).
                //your app has to be on the market and if it already is, you have to have a version code number which is greater or equal than the one already published.

                //only enable when debugging
                if (false) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    AppStart.this);
                            alertDialogBuilder.setTitle(getString(R.string.DIALOG_AlERT));
                            alertDialogBuilder.setMessage("LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED");
                            alertDialogBuilder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mHandler.sendEmptyMessageDelayed(0, 200);

                                }
                            });
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

                        String message = "Unauthorised source of the app, please download it from Google Play";
                        showDialogAndExit(getString(R.string.DIALOG_ERROR),message);

                    }
                });

            }

            LOGD(TAG, "applicationError with errorCode = " + errorCode);

        }


    }


    private void showDialogAndExit(String title, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                AppStart.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setNegativeButton(getString(R.string.DIALOG_CLOSE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder
                .setMessage(message).show();
    }

}