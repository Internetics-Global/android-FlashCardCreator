package com.internectics.util;

import android.app.Activity;
import android.view.View;

import com.internectics.android_flashcardcreator.R;
import it.sephiroth.android.library.tooltip.TooltipManager;

/**
 * Created by BourneWang on 24/11/14.
 */
public class TipHelper {

    private static final int POINTER_SIZE = 10;

    private static final int TOOLTIP_TYPE_A_NUMBER = 15;

    public static void showTipForLogo(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(0)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange_light)
                .text("Edit logo image")
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
                .text("Click to select an image/video from library, or insert a YouTube video linkage")
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
                .text("Click here to see the question side of the card")
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
                .text("Click here to see the answer part of the card")
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
                .text("Change background  ")
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
                .text("Record sound or voice")
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
                .text("Change template")
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
                .text("Create a new card")
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
                .withArrowLenghtWeight(22)
                .text("Share this pack")
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
                .withArrowLenghtWeight(19)
                .text("App setting")
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
                .text("Toggle help tips on and off")
                .withArrowLenghtWeight(16)
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


    public static void showTipForActionBarPalette(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(11)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .withArrowLenghtWeight(13)
                .text("Change the color palette")
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




    public static void showTipForActionBarPlay(final Activity activity, View anchorView) {

        if ((activity == null) || (anchorView == null)) {
            return;
        }

        TooltipManager.getInstance(activity)
                .create(12)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_cyan_light)
                .withArrowLenghtWeight(10)
                .text("Play these cards")
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
                .withArrowLenghtWeight(7)
                .text("Create a new pack")
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
                .withArrowLenghtWeight(4)
                .text("Edit a pack")
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
                .text("Open pack viewer")
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
        for (int i = 0; i < TOOLTIP_TYPE_A_NUMBER; i ++) {
            TooltipManager.getInstance(activity).hide(i);
        }

    }

    private static void setFlagIfMeetCondition (Activity activity)  {

        for (int i = 0; i <TOOLTIP_TYPE_A_NUMBER; i ++) {
            if (TooltipManager.getInstance(activity).active(i) == true) {
              return;
            }
        }

        AppConfig.sharedInstance().setAllowToShowTooltip(false);



    }


}
