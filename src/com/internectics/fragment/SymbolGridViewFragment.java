package com.internectics.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.SymbolHelper;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午2:18
 * To change this template use File | Settings | File Templates.
 */
public class SymbolGridViewFragment extends Fragment {

    public View mContentView;
    private GridView mGridView;

    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.symbol_gridview, container, false);

        mGridView = (GridView) mContentView.findViewById(R.id.symbol_gridview);
        mGridView.setAdapter(new SymbolAdapter(getActivity()));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


            }
        });

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

            convertView = mInflater.inflate(R.layout.symbol_view, null);

            ImageView symbolImage = (ImageView) convertView.findViewById(R.id.symbol_image);
            TextView summaryTextView = (TextView) convertView.findViewById(R.id.symbol_summary);

            symbolImage.setImageResource(SymbolHelper.mImageResourceIDArray[position]);
            summaryTextView.setText(SymbolHelper.mDescriptionArray[position]);

            return convertView;
        }
    }
}
