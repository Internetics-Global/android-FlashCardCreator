package com.internectics.android_flashcardcreator;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.*;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.fragment.CardDetailFragment;
import com.internectics.model.CardListModel;
import com.internectics.util.Global;
import com.internectics.util.UIHelper;

import java.util.ArrayList;
import java.util.List;


public class PlayActivity extends FragmentActivity implements SensorEventListener {

    private Pack mCurrentPack;
    private int mPosition;
    private List<Fragment> mFragments;
    private FCCPageAdapter mPageAdapter;

    private static boolean enableSwitch = true;
    private SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int packID = getIntent().getIntExtra("packID", -1);
        mCurrentPack = CardListModel.getPack(packID);

        setContentView(R.layout.play);
        getActionBar().hide();

        mFragments = getFragments();
        mPageAdapter = new FCCPageAdapter(getSupportFragmentManager(), mFragments);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);

        //Keep same size with non-playmode
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) pager.getLayoutParams();
        int margin = (UIHelper.getScreenWidth(this)) / 6 / 2;
        marginLayoutParams.leftMargin = margin;
        marginLayoutParams.rightMargin = margin;
        pager.setLayoutParams(marginLayoutParams);

        pager.setOffscreenPageLimit(2);
        pager.setAdapter(mPageAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if ((mPosition != i) && (i2 == 0)) {
                    Log.i(Global.debugTag, "onPageScrolled, page index=" + i);
                    mPosition = i;

                    ((CardDetailFragment) (mFragments.get(mPosition))).switchToQuestionView();
                    enableSwitch = true;

                }
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }

        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    public class FCCPageAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mFragments;

        public FCCPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mFragments.get(position);
        }

        @Override
        public int getCount() {
            return this.mFragments.size();
        }
    }


    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();

        if (mCurrentPack == null) {
            Log.w(Global.debugTag, "mCurrentPack could not be null in PlayActictiy");
            return fList;
        }

        ArrayList<Card> cardsArray = mCurrentPack.cards;
        for (int i = 0; i < cardsArray.size(); i++) {
            fList.add(new CardDetailFragment(mCurrentPack, cardsArray.get(i), 2));
        }

        return fList;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //range of values is 90 degrees to -90 degrees.
        float roll = event.values[2];

        Log.i(Global.debugTag, "roll angle =" + roll);

        int orientation = getOrientation();
        if (orientation == 1) {
            if ((roll < -15.0) && (enableSwitch)) {
                ((CardDetailFragment) (mFragments.get(mPosition))).switchQuestionAnswerView();
                enableSwitch = false;
            } else if ((Math.abs(roll) < 2) && (!enableSwitch)) {
                enableSwitch = true;
            }
        } else if (orientation == 0) {
            if ((roll > 15.0) && (enableSwitch)) {
                ((CardDetailFragment) (mFragments.get(mPosition))).switchQuestionAnswerView();
                enableSwitch = false;
            } else if ((Math.abs(roll) < 2) && (!enableSwitch)) {
                enableSwitch = true;
            }
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }


    /**
     * -1, other orientation; 0, landscape; 1. reverse landscape
     */
    private int getOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if ((rotation == Surface.ROTATION_0)
                    || (rotation == Surface.ROTATION_90)) {
                Log.d(Global.debugTag, "current rotation is landscape");
                return 0; //landscape (for nexus 7, camera is left side of screen)
            }

            if ((rotation == Surface.ROTATION_180)
                    || (rotation == Surface.ROTATION_270)) {
                Log.d(Global.debugTag, "current rotation is reverselandscape");
                return 1; //reverse landscape   (for nexus 7, camera is right side of screen)
            }

        }

        return -1; //other rotation

    }

}
