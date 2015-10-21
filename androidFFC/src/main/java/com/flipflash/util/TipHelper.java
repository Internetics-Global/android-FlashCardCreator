package com.flipflash.util;

import android.app.Activity;
import android.view.View;

import com.flipflash.android_ffc.R;

import it.sephiroth.android.library.tooltip.TooltipManager;

/**
 * Created by BourneWang on 24/11/14.
 */
public class TipHelper {

    private static final String TAG = TipHelper.class.getName();

    private static final int POINTER_SIZE = 10;

    private static final int TOOLTIP_TYPE_A_NUMBER = 16;

    public static void showTipForLogo(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(0)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange_light)
                .text(activity.getString(R.string.Tip_Edit_Logo))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForImage(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(1)
                .anchor(anchorView, TooltipManager.Gravity.LEFT)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange_light)
                .text(activity.getString(R.string.Tip_Image))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForSegmentQuestion(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(2)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan)
                .text(activity.getString(R.string.Tip_Question))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForSegmentAnswer(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(3)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_purple)
                .withArrowLenghtWeight(4)
                .text(activity.getString(R.string.Tip_Answer))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }

    public static void showTipForChangeBackground(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(4)
                .anchor(anchorView, TooltipManager.Gravity.LEFT)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withArrowLenghtWeight(3)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_green)
                .text(activity.getString(R.string.Tip_Background))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForRecordSound(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(5)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_pink)
                .text(activity.getString(R.string.Tip_Record))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForChangeTemplate(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(6)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_blue)
                .withArrowLenghtWeight(4)
                .text(activity.getString(R.string.Tip_Template))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }

    public static void showTipForCreateCard(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(7)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .text(activity.getString(R.string.Tip_Create_Card))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }


    public static void showTipForActionBarShare(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(8)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan_light)
                .withArrowLenghtWeight(16.8f)
                .text(activity.getString(R.string.Tip_Share))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForActionBarSetting(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(9)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan_dark)
                .withArrowLenghtWeight(14.5f)
                .text(activity.getString(R.string.Tip_App_Setting))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForActionBarHelp(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(10)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_green)
                .text(activity.getString(R.string.Tip_Toggle))
                .withArrowLenghtWeight(12.2f)
                .toggleArrow(true)
                .maxWidth(1000)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForActionBarPalette(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(11)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .withArrowLenghtWeight(9.9f)
                .text(activity.getString(R.string.Tip_Palette))
                .toggleArrow(true)
                .maxWidth(1000)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }




    public static void showTipForActionBarPlay(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(12)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan_light)
                .withArrowLenghtWeight(7.6f)
                .text(activity.getString(R.string.Tip_Play))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b, boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForActionBarCreateNewPack(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(13)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_purple)
                .withArrowLenghtWeight(5.3f)
                .text(activity.getString(R.string.Tip_Create_New_Pack))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b,boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForEditPack(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(14)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan)
                .withArrowLenghtWeight(3)
                .text(activity.getString(R.string.Tip_Edit_Pack))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b,boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForOpenPack(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(15)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .text(activity.getString(R.string.Tip_Open_Pack_Viewer))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b,boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForLinkButton(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(16)
                .anchor(anchorView, TooltipManager.Gravity.LEFT)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_blue)
                .text(activity.getString(R.string.Tip_Edit_Link))
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b,boolean b2) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }



    public static void hideEverthing(Activity activity) {
        for (int i = 0; i <= TOOLTIP_TYPE_A_NUMBER; i ++) {
            TooltipManager.getInstance(activity).hide(i);
        }

    }

    private static void setFlagIfMeetCondition (Activity activity)  {

        for (int i = 0; i <= TOOLTIP_TYPE_A_NUMBER; i ++) {
            if (TooltipManager.getInstance(activity).active(i) == true) {
              return;
            }
        }

        AppConfig.sharedInstance().setAllowToShowTooltip(false);



    }


}
