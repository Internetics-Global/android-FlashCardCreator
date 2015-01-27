package com.internectics.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.SymbolHelper;
import com.internectics.util.FontCache;
import com.internectics.util.Global;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午2:18
 * To change this template use File | Settings | File Templates.
 */
public class SymbolGridViewFragment extends Fragment {

    public View mContentView;

    private Typeface mTypeFace;

    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.symbol_gridview, container, false);

        GridView gridView = (GridView) mContentView.findViewById(R.id.symbol_gridview);
        gridView.setAdapter(new SymbolAdapter(getActivity()));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((MainActivity) getActivity()).mCardDetailFragment.onGridViewItemClicked(position);
            }
        });

        mTypeFace = FontCache.get(Global.fontName_Default, getActivity());

        return mContentView;

    }


    public class SymbolAdapter extends BaseAdapter {
        private Context mContext;

        public SymbolAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return SymbolHelper.getSymbolCount();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // Create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.symbol_view, null);
            }

            TextView summaryTextView = (TextView) convertView.findViewById(R.id.symbol_summary);
            summaryTextView.setTypeface(mTypeFace,Typeface.NORMAL);

            String symbolText = SymbolHelper.mUnicodeArray[position];
            summaryTextView.setText(symbolText);

            ImageView symbolBackgroundImageView = (ImageView) convertView.findViewById(R.id.symbol_background_image);

            if (symbolText.equals("⨯") || symbolText.equals("+") || symbolText.equals("÷") ||
                    symbolText.equals("−") || symbolText.equals("=")) {
                symbolBackgroundImageView.setImageResource(R.drawable.key_orange);

            } else {
                symbolBackgroundImageView.setImageResource(R.drawable.key);
            }

            return convertView;
        }
    }

}
