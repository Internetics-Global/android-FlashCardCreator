package com.flipflash.model;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flipflash.android_ffc.R;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BourneWang on 12/06/2016.
 */
public class PackInfoViewAdapter extends PagerAdapter {

    private Context mContext;

    private List<Pack> mPackList;

    public PackInfoViewAdapter(Context c)
    {
        mContext = c;
    }

    @Override
    public int getCount() {

        mPackList = User.defaultUser(mContext).packs;

        if (mPackList == null) {
            return 0;
        } else {
            return mPackList.size();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pack_info_scrollview_item,container, false);
        container.addView(view);

        Pack pack = mPackList.get(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        if (StringUtils.isEmptyOrPlaceHolder(pack.coverImageUriFormatStr) ||
                StringUtils.isValidImageFile(pack.coverImageUriFormatStr) == false) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_pack_cover_image_transparent));
        } else {
            imageView.setImageURI(Uri.parse(pack.coverImageUriFormatStr));
        }


        return view;
    }


}
