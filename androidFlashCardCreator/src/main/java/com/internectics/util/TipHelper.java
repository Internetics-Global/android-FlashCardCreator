package com.internectics.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.internectics.android_flashcardcreator.R;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import it.sephiroth.android.library.tooltip.TooltipManager;

/**
 * Created by BourneWang on 24/11/14.
 */
public class TipHelper {

    private static final int POINTER_SIZE = 10;

    private static final int TOOLTIP_TYPE_A_NUMBER = 6;

    public static void showTipForLogo(final Activity activity, View anchorView) {
        TooltipManager.getInstance(activity)
                .create(1)
                .anchor(anchorView, TooltipManager.Gravity.BOTTOM)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_yellow)
                .text("Edit logo image")
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();
    }


    public static void showTipForImage(final Activity activity, View anchorView) {

        TooltipManager.getInstance(activity)
                .create(4)
                .anchor(anchorView, TooltipManager.Gravity.LEFT)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_purple)
                .text("Click to select an image/video from library, or insert a YouTube video linkage")
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);

                    }
                })
                .show();

    }

    public static void showTipForSegmentQuestion(final Activity activity, View anchorView) {

        TooltipManager.getInstance(activity)
                .create(5)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_purple)
                .text("Click here to see the question side of the card")
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForChangeBackground(final Activity activity, View anchorView) {

        TooltipManager.getInstance(activity)
                .create(3)
                .anchor(anchorView, TooltipManager.Gravity.LEFT)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .text("Change background")
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    public static void showTipForRecordSound(final Activity activity, View anchorView) {

        TooltipManager.getInstance(activity)
                .create(2)
                .anchor(anchorView, TooltipManager.Gravity.TOP)
                .closePolicy(TooltipManager.ClosePolicy.TouchInside, 0)
                .withStyleId(R.style.ToolTipLayoutCustomStyle_orange)
                .text("Record sound or voice")
                .toggleArrow(true)
                .maxWidth(500)
                .showDelay(200)
                .activateDelay(300)
                .withCallback(new TooltipManager.onTooltipClosingCallback() {
                    @Override
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);

                    }
                })
                .show();

    }

    public static void showTipForCreateCard(final Activity activity, View anchorView) {

        TooltipManager.getInstance(activity)
                .create(0)
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
                    public void onClosing(int i, boolean b) {
                        TooltipManager.getInstance(activity).remove(i);
                        setFlagIfMeetCondition(activity);
                    }
                })
                .show();

    }

    private static ToolTipView actionbarShare_ToolTipView;

    public static void showTipForActionBarShare(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Share this pack")
                .withTextColor(Color.WHITE)
                .withColor(Color.rgb(173, 173, 45))
                .withArrowHeight(UIHelper.getPixels(310));

        actionbarShare_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarShare_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarShare_ToolTipView.remove();
                actionbarShare_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });

    }

    private static ToolTipView actionbarSetting_ToolTipView;

    public static void showTipForActionBarSetting(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {


        ToolTip toolTip = new ToolTip()
                .withText("App setting")
                .withTextColor(Color.WHITE)
                .withColor(Color.rgb(128, 128, 43))
                .withArrowHeight(UIHelper.getPixels(270))
                ;

        actionbarSetting_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarSetting_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarSetting_ToolTipView.remove();
                actionbarSetting_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });

    }

    private static ToolTipView actionbarHelp_ToolTipView;

    public static void showTipForActionBarHelp(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Toggle help tips on and off")
                .withTextColor(Color.WHITE)
                .withColor(Color.GREEN)
                .withArrowHeight(UIHelper.getPixels(230))
                ;

        actionbarHelp_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarHelp_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarHelp_ToolTipView.remove();
                actionbarHelp_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });

    }

    private static ToolTipView actionbarPalette_ToolTipView;

    public static void showTipForActionBarPalette(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Change the color palette")
                .withTextColor(Color.WHITE)
                .withColor(Color.rgb(255, 125, 43))
                .withArrowHeight(UIHelper.getPixels(190));
                ;

        actionbarPalette_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarPalette_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarPalette_ToolTipView.remove();
                actionbarPalette_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });


    }


    private static ToolTipView actionbarPlay_ToolTipView;

    public static void showTipForActionBarPlay(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Play these cards")
                .withColor(Color.rgb(179, 179, 43))
                .withTextColor(Color.WHITE)
                .withArrowHeight(UIHelper.getPixels(150));

        actionbarPlay_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarPlay_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {

                actionbarPlay_ToolTipView.remove();
                actionbarPlay_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });


    }

    private static ToolTipView actionbarCreatePack_ToolTipView;

    public static void showTipForActionBarCreateNewPack(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Create a new pack")
                .withColor(Color.rgb(128, 0, 128))
                .withTextColor(Color.WHITE)
                .withArrowHeight(UIHelper.getPixels(110));

        actionbarCreatePack_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarCreatePack_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarCreatePack_ToolTipView.remove();
                actionbarCreatePack_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });

    }

    private static ToolTipView actionbarEditPack_ToolTipView;

    public static void showTipForEditPack(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Edit a pack")
                .withColor(Color.rgb(79, 145, 222))
                .withTextColor(Color.WHITE)
                .withArrowHeight(UIHelper.getPixels(70));

        actionbarEditPack_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarEditPack_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {

                actionbarEditPack_ToolTipView.remove();
                actionbarEditPack_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });

    }

    private static ToolTipView actionbarOpenPack_ToolTipView;

    public static void showTipForOpenPack(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Open pack viewer")
                .withTextColor(Color.WHITE)
                .withColor(Color.rgb(255,125,43))
                ;

        actionbarOpenPack_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        actionbarOpenPack_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {
                actionbarOpenPack_ToolTipView.remove();
                actionbarOpenPack_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });
    }

    private static ToolTipView changeTemplate_ToolTipView;

    public static void showTipForChangeTemplate(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Change template")
                .withArrowHeight(UIHelper.getPixels(60))
                .withTextColor(Color.WHITE)
                .withForceShowTop(true)
                .withColor(Color.rgb(255, 125, 43))

                ;

        changeTemplate_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        changeTemplate_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {

                changeTemplate_ToolTipView.remove();
                changeTemplate_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });
    }

    private static ToolTipView segmentAnswer_ToolTipView;

    public static void showTipForSegmentAnswer(final Activity activity,ToolTipRelativeLayout toolTipFrameLayout, View anchor) {

        ToolTip toolTip = new ToolTip()
                .withText("Click here to see the answer  side of the card")
                .withTextColor(Color.WHITE)
                .withForceShowTop(true)
                .withArrowHeight(UIHelper.getPixels(60))
                .withColor(Color.rgb(123, 125, 43))
                ;

        segmentAnswer_ToolTipView = toolTipFrameLayout.showToolTipForView(toolTip, anchor);
        segmentAnswer_ToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
            @Override
            public void onToolTipViewClicked(ToolTipView toolTipView) {

                segmentAnswer_ToolTipView.remove();
                segmentAnswer_ToolTipView = null;
                setFlagIfMeetCondition(activity);

            }
        });
    }

    public static void hideEverthing(Activity activity) {
        for (int i = 0; i < TOOLTIP_TYPE_A_NUMBER; i ++) {
            TooltipManager.getInstance(activity).hide(i);
        }

        if (actionbarCreatePack_ToolTipView != null) {
            actionbarCreatePack_ToolTipView.remove();
            actionbarCreatePack_ToolTipView = null;
        }

        if (actionbarEditPack_ToolTipView != null) {
            actionbarEditPack_ToolTipView.remove();
            actionbarEditPack_ToolTipView = null;
        }

        if (actionbarOpenPack_ToolTipView != null) {
            actionbarOpenPack_ToolTipView.remove();
            actionbarOpenPack_ToolTipView = null;
        }

        if (actionbarHelp_ToolTipView != null) {
            actionbarHelp_ToolTipView.remove();
            actionbarHelp_ToolTipView = null;
        }

        if (actionbarShare_ToolTipView != null) {
            actionbarShare_ToolTipView.remove();
            actionbarShare_ToolTipView = null;
        }

        if (actionbarPalette_ToolTipView != null) {
            actionbarPalette_ToolTipView.remove();
            actionbarPalette_ToolTipView = null;
        }

        if (actionbarPlay_ToolTipView != null) {
            actionbarPlay_ToolTipView.remove();
            actionbarPlay_ToolTipView = null;
        }

        if (actionbarSetting_ToolTipView != null) {
            actionbarSetting_ToolTipView.remove();
            actionbarSetting_ToolTipView = null;
        }

        if (actionbarShare_ToolTipView != null) {
            actionbarShare_ToolTipView.remove();
            actionbarShare_ToolTipView = null;
        }

        if (changeTemplate_ToolTipView != null) {
            changeTemplate_ToolTipView.remove();
            changeTemplate_ToolTipView = null;
        }

        if (segmentAnswer_ToolTipView != null) {
            segmentAnswer_ToolTipView.remove();
            segmentAnswer_ToolTipView = null;
        }


    }

    private static void setFlagIfMeetCondition (Activity activity)  {

        for (int i = 0; i <TOOLTIP_TYPE_A_NUMBER; i ++) {
            if (TooltipManager.getInstance(activity).active(i) == true) {
              return;
            }
        }

        if (segmentAnswer_ToolTipView != null) {
            return;
        }

        if (changeTemplate_ToolTipView != null) {
            return;
        }

        if (actionbarCreatePack_ToolTipView != null) {
            return;
        }

        if (actionbarEditPack_ToolTipView != null) {
            return;
        }

        if (actionbarHelp_ToolTipView != null) {
            return;
        }

        if (actionbarOpenPack_ToolTipView != null) {
            return;
        }

        if (actionbarPalette_ToolTipView != null) {
            return;
        }

        if (actionbarPlay_ToolTipView != null) {
            return;
        }

        if (actionbarSetting_ToolTipView != null) {
            return;
        }
        if (actionbarShare_ToolTipView != null) {
            return;
        }

        AppConfig.sharedInstance().setAllowToShowTooltip(false);



    }


}
