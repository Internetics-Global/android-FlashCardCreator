package com.internectics.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.internectics.android_flashcardcreator.R;

@SuppressWarnings("deprecation")
public class PackListFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Toast.makeText(getActivity(), "this is fragment", Toast.LENGTH_LONG).show();
		View rootView = inflater.inflate(R.layout.fragment_pack_list,
				container, false);
        Gallery g = (Gallery) rootView.findViewById(R.id.pack_list_gallery);
        // Set the adapter to our custom adapter (below)
        g.setAdapter(new ImageAdapter(getActivity()));
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                System.out.println("Index of pack in pack list is:" + position);      
            }
        });
        
		return rootView;
	}
	
	
	public class ImageAdapter extends BaseAdapter {
        private static final int ITEM_WIDTH = 136;
        private static final int ITEM_HEIGHT = 88;

        private final int mGalleryItemBackground;
        private final Context mContext;

        private final Integer[] mImageIds = {
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image,
                R.drawable.pack_cover_default_image
        };

        private final float mDensity;

        public ImageAdapter(Context c) {
            mContext = c;
            // See res/values/attrs.xml for the <declare-styleable> that defines
            // Gallery1.
            TypedArray a = c.obtainStyledAttributes(R.styleable.Gallery1);
            mGalleryItemBackground = a.getResourceId(
                    R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();

            mDensity = c.getResources().getDisplayMetrics().density;
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
		public View getView(int position, View convertView, ViewGroup parent) {
        	System.out.println("getView method in ImageAdapter is called");
        	
        	LinearLayout baseView = new LinearLayout(mContext);
        	baseView.setOrientation(LinearLayout.VERTICAL);
        	
            ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);

                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new Gallery.LayoutParams(
                        (int) (ITEM_WIDTH * mDensity + 0.5f),
                        (int) (ITEM_HEIGHT * mDensity + 0.5f)));
            
                // The preferred Gallery item background
                imageView.setBackgroundResource(mGalleryItemBackground);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(mImageIds[position]);
            baseView.addView(imageView);
            
            TextView packNameView = new TextView(mContext);
            packNameView.setTextColor(Color.WHITE);
            packNameView.setGravity(Gravity.CENTER);
            packNameView.setText("pack name");
            
            baseView.addView(packNameView);

            return baseView;
        }
    }

}
