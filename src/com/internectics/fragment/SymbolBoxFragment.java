package com.internectics.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.SymbolHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午12:29
 * To change this template use File | Settings | File Templates.
 */
public class SymbolBoxFragment extends Fragment {

    public View mContentView;
    private SymbolPageAdapter mPageAdapter;
    private ViewPager mPager;
    private List<SymbolGridViewFragment> mSymbolGridViewFragments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSymbolGridViewFragments = getGridViewFragments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.fragment_symbol_box, container);
        mPager = (ViewPager) mContentView.findViewById(R.id.symbol_pager);
        mPageAdapter = new SymbolPageAdapter(getFragmentManager(), getGridViewFragments());
        mPager.setAdapter(mPageAdapter);
        hideSymbolBox();
        return mContentView;
    }


    public void hideSymbolBox() {
        mContentView.setVisibility(View.INVISIBLE);
    }

    public void showSymbolBox() {
        mContentView.setVisibility(View.VISIBLE);
    }



    private List<SymbolGridViewFragment> getGridViewFragments() {
        List<SymbolGridViewFragment> fList = new ArrayList<SymbolGridViewFragment>();
        int size = SymbolHelper.getSymbolCount();

        for (int i =0; i<size; i++) {
            fList.add(new SymbolGridViewFragment());
        }

        return fList;
    }


    public class SymbolPageAdapter extends FragmentStatePagerAdapter {

        private List<SymbolGridViewFragment> mSymbolGridViewFragments;

        public SymbolPageAdapter(FragmentManager fm, List<SymbolGridViewFragment> gridviews) {
            super(fm);
            this.mSymbolGridViewFragments = gridviews;
        }

        @Override
        public SymbolGridViewFragment getItem(int position) {
            return this.mSymbolGridViewFragments.get(position);
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

}
