package com.flipflash.UI;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flipflash.android_ffc.R;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.model.PackInfoViewAdapter;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;

import java.util.ArrayList;

import static com.flipflash.util.LogUtils.LOGD;


public class PackInfoView extends LinearLayout implements ViewPager.OnPageChangeListener {

    //never to use mCurrentPack.xxx = xxx. Instead, please use setCurrentPack()
    private Pack         mCurrentPack;

    private TextView     mPackTitleTextView;
    private TextView     mPackInfoTextView;
    private TextView     mShareCodeTextView;

    private ImageButton  mBackNavImageButton;
    private ImageButton  mForwardNavImageButton;
    private ImageButton  mPlayImageButton;

    private ViewPager             mViewPager;
    private PackInfoViewAdapter   mAdapter;


    private PackInfoViewDelegate  mPackInfoViewDelegate;

    private Context               mContext;

    private int                   mCurrentPage;


    private static final String TAG = PackInfoView.class.getSimpleName();


    public PackInfoView(Context context) {
        super(context);
        init(context);
    }

    public PackInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PackInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {

        inflate(getContext(), R.layout.pack_info_layout,this);

        mContext = context;

        mPackTitleTextView = (TextView) findViewById(R.id.pack_info_title);
        mPackInfoTextView = (TextView) findViewById(R.id.pack_info_no);
        mShareCodeTextView = (TextView) findViewById(R.id.pack_info_share_code);

        mBackNavImageButton = (ImageButton) findViewById(R.id.back_nav_button);
        mBackNavImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                backPackNavButtonClicked();
            }
        });

        mForwardNavImageButton = (ImageButton) findViewById(R.id.forward_nav_button);
        mForwardNavImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardPackNavButtonClicked();
            }
        });

        mPlayImageButton = (ImageButton) findViewById(R.id.play_nav_button);
        mPlayImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                playPackNavButtonClicked();
            }
        });

        mAdapter = new PackInfoViewAdapter(mContext);
        mViewPager = (ViewPager) findViewById(R.id.pack_info_viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
    }


    public void setCurrentPack(Pack currentPack) {
        this.mCurrentPack = currentPack;

        ArrayList<Pack> packs = User.defaultUser(mContext).packs;
        int i = 0;
        for (Pack item:packs) {
            if (item.packID == mCurrentPack.packID) {
                mCurrentPage = i;
                break;
            }
            i++;
        }
    }

    public void setPackInfoViewDelegate(PackInfoViewDelegate packInfoViewDelegate) {
        mPackInfoViewDelegate = packInfoViewDelegate;
    }


    private void backPackNavButtonClicked() {

        if (mCurrentPage == 0) {
            return;
        }

        ArrayList<Pack> packs = User.defaultUser(mContext).packs;
        if (packs == null || packs.size() == 0) {
            return;
        }

        setCurrentPack(packs.get(mCurrentPage - 1));
        scrollToPage(mCurrentPage,false);

    }

    private void forwardPackNavButtonClicked() {

        ArrayList<Pack> packs = User.defaultUser(mContext).packs;
        if (packs == null || packs.size() == 0) {
            return;
        }

        if (mCurrentPage == packs.size() - 1) {
            return;
        }

        setCurrentPack(packs.get(mCurrentPage + 1));

        scrollToPage(mCurrentPage, false);

    }

    private void playPackNavButtonClicked() {

        if (mPackInfoViewDelegate != null) {
            mPackInfoViewDelegate.playButtonClickedOnPackInfoView();
        }

    }


    private void updatePackMetaData(int page) {

        Pack pack = User.defaultUser(mContext).packs.get(page);


        mPackTitleTextView.setText(pack.packName);
        mPackInfoTextView.setText(String.format("%s:%d", mContext.getString(R.string.Title_Total_Number_Card),pack.cards.size()));

        if (StringUtils.isEmpty(pack.shareLink) == false && ((pack.creatorID).equals(OpenUDID_manager.getOpenUDID()))) {
            Uri uri = Uri.parse(pack.shareLink);
            mShareCodeTextView.setText(String.format("%s: %s",getContext().getString(R.string.Title_Share_Code),uri.getLastPathSegment()));
        } else {
            mShareCodeTextView.setText("");
        }


    }


    private void updatePagerArrowsVisibility(int page) {

        ArrayList<Pack> packs = User.defaultUser(mContext).packs;

        if (packs == null) {
            return;
        }

        if (page > 0) {
            mBackNavImageButton.setEnabled(true);
        } else {
            mBackNavImageButton.setEnabled(false);
        }

        if (page < packs.size() - 1) {
            mForwardNavImageButton.setEnabled(true);
        } else {
            mForwardNavImageButton.setEnabled(false);
        }

    }


    public void refreshWithRebuildViewPager(boolean isRebuildViewPager) {

        LOGD(TAG, "refresh");

        scrollTo(mCurrentPack,isRebuildViewPager);

    }


    public void scrollTo(Pack pack,boolean isRebuildViewPager) {

        if (pack == null) {
            return;
        }

        this.setCurrentPack(pack);

        scrollToPage(mCurrentPage, isRebuildViewPager);

    }

    private void scrollToPage(int page,boolean isRebuildViewPager) {

        if (isRebuildViewPager) {

            //不要使用notifyDataSetChanged(),因为这个方法的前提是,ListView的list指向不能被改变，需要从始至终指向同一个内存
            mViewPager.setAdapter(mAdapter);
        }

        updatePagerArrowsVisibility(page);
        updatePackMetaData(page);
        mViewPager.setCurrentItem(page);

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private int _previousPage = -1;
    @Override
    public void onPageSelected(int position) {

        LOGD(TAG, "onPageSelected with position = " + position);

        ArrayList<Pack> packs = User.defaultUser(mContext).packs;
        if (packs == null || packs.size() == 0) {
            return;
        }

        if (_previousPage == -1 && position == 0) {
            //系统自动调用onPageScrolled,即便没有scroll,这种情况是需要避免的
            return;
        }


        if (_previousPage != position ) {
            _previousPage = position;

            setCurrentPack(User.defaultUser(mContext).packs.get(position));
            updatePagerArrowsVisibility(mCurrentPage);
            updatePackMetaData(mCurrentPage);

            AppConfig.sharedInstance().setPackIDForLastSelected(mCurrentPack.packID);

            mCurrentPack.lastVistDate = Global.currentTimeSeconds();
            mCurrentPack.save(AppContext.getAppContext());

            if (mPackInfoViewDelegate != null) {
                mPackInfoViewDelegate.didScrollToPackOnPackInfoView(mCurrentPack);
            }

        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public interface PackInfoViewDelegate {

        void didScrollToPackOnPackInfoView(Pack pack);

        void playButtonClickedOnPackInfoView();
    }


}
