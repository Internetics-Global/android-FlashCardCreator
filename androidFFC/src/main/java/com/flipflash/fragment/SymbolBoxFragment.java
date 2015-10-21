package com.flipflash.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.flipflash.android_ffc.R;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午12:29
 * To change this template use File | Settings | File Templates.
 */
public class SymbolBoxFragment extends Fragment {

    private static final String TAG = SymbolBoxFragment.class.getName();

    public View mContentView;
    private final int  NUMBER_PAGE = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.fragment_symbol_box, container);
        CircleIndicator defaultIndicator = (CircleIndicator) mContentView.findViewById(R.id.symbol_indicator);
        ViewPager pager = (ViewPager) mContentView.findViewById(R.id.symbol_pager);
        SymbolPageAdapter pageAdapter = new SymbolPageAdapter(getFragmentManager(), getGridViewFragments());
        pager.setAdapter(pageAdapter);
        defaultIndicator.setViewPager(pager);
        hideSymbolBoxWithAnimation(false);
        return mContentView;
    }


    public void hideSymbolBoxWithAnimation(boolean animation) {
        if ((animation) && (mContentView.getVisibility() == View.VISIBLE)) {
            final Animation animAlphaUp = new AlphaAnimation(1.0f, 0.0f);
            animAlphaUp.setDuration(500);
            mContentView.startAnimation(animAlphaUp);
        }
        mContentView.setVisibility(View.INVISIBLE);
    }

    public void showSymbolBoxWithAnimation(boolean animation) {
        if ((animation) && (mContentView.getVisibility() == View.INVISIBLE)) {
            final Animation animAlphaUp = new AlphaAnimation(0.0f, 1.0f);
            animAlphaUp.setDuration(500);
            mContentView.startAnimation(animAlphaUp);
        }
        mContentView.setVisibility(View.VISIBLE);

    }

    public boolean isSymbolBoxVisible() {
        boolean bool = mContentView.getVisibility() == View.VISIBLE;
        return bool;
    }



    private List<SymbolPageViewFragment> getGridViewFragments() {
        List<SymbolPageViewFragment> fList = new ArrayList<SymbolPageViewFragment>();

        for (int i = 0; i< NUMBER_PAGE; i++) {
            SymbolPageViewFragment symbolPageViewFragment = new SymbolPageViewFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("page_number",i);
            symbolPageViewFragment.setArguments(bundle);
            fList.add(symbolPageViewFragment);

        }

        return fList;
    }


    public class SymbolPageAdapter extends FragmentStatePagerAdapter {

        private List<SymbolPageViewFragment> mSymbolGridViewFragments;

        public SymbolPageAdapter(FragmentManager fm, List<SymbolPageViewFragment> gridviews) {
            super(fm);
            this.mSymbolGridViewFragments = gridviews;
        }

        @Override
        public SymbolPageViewFragment getItem(int position) {
            return this.mSymbolGridViewFragments.get(position);
        }

        @Override
        public int getCount() {
            return this.mSymbolGridViewFragments.size();
        }
    }

}
