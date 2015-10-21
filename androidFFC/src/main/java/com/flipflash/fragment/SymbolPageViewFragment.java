package com.flipflash.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.helper.SymbolHelper;
import com.flipflash.util.AppContext;
import com.flipflash.util.FontCache;
import com.flipflash.util.Global;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午2:18
 * To change this template use File | Settings | File Templates.
 */
public class SymbolPageViewFragment extends Fragment implements TextView.OnTouchListener {

    private static final String TAG = SymbolPageViewFragment.class.getName();

    public View               mContentView;

    private int              mPageNumber;
    private int              mNumberOfSymbolsBeforePage;

    private Typeface mTypeFace;  //引用它的原因是因为unicode需要特殊字体支持

    private final static int ROW_NUMBER           = 6;
    private final static int COLUMN_NUMBER_TYPE_A = 11;
    private final static int COLUMN_NUMBER_TYPE_B = 12;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle = getArguments();
        int page = bundle.getInt("page_number");
        mPageNumber = page;
        mNumberOfSymbolsBeforePage = numberOfSymbolsBeforePage(mPageNumber);

        mTypeFace = FontCache.get(Global.fontName_Default, getActivity());

        if (page == 0) {
            mContentView = inflater.inflate(R.layout.symbol_box_page_first, container, false);
        } else {
            mContentView = inflater.inflate(R.layout.symbol_box_page_others, container, false);
        }

        setupSymbols();

        return mContentView;

    }

    /**
     * 每当keyboard的布局变化时，需要更新这里
     */
    private void setupSymbols() {

        for (int i =0; i< ROW_NUMBER; i++) {
            String idStr = "symbol_box_row_" + (i+1);
            View myRow = mContentView.findViewById(getResourceID(idStr));

            int MAX_COLUMN;
            if ((mPageNumber == 0) && (i == 0 || i == 1 || i == 2)) {
                MAX_COLUMN = COLUMN_NUMBER_TYPE_A;
            } else {
                MAX_COLUMN = COLUMN_NUMBER_TYPE_B;
            }

            Pattern p = Pattern.compile("\\d");
            for (int j = 0; j < MAX_COLUMN; j++) {
                String symbolIDStr = "symbol_box_column_" + (j+1);
                View symbolView = myRow.findViewById(getResourceID(symbolIDStr));

                ImageView symbolBackgroundImageView = (ImageView) symbolView.findViewById(R.id.symbol_background_image);
                TextView symbolTextView = (TextView) symbolView.findViewById(R.id.symbol_text);

                symbolTextView.setTypeface(mTypeFace,Typeface.BOLD);

                String symbolText = getText(i,j);
                if(symbolText != null) {
                    symbolTextView.setText(symbolText);
                    Matcher m = p.matcher(symbolText);
                    if (m.find() || symbolText.toLowerCase().equals("space bar")) {
                        symbolBackgroundImageView.setImageResource(R.drawable.shape_symbol_background_rounded_corner_highlighted);

                    } else {
                        symbolBackgroundImageView.setImageResource(R.drawable.shape_symbol_background_rounded_corner);
                    }

                    HashMap<Integer,Integer> tag = new HashMap<>();
                    tag.put(Integer.valueOf(i),Integer.valueOf(j));
                    symbolTextView.setTag(tag); //作为标志
                    symbolTextView.setOnTouchListener(this);

                } else {
                    symbolView.setVisibility(View.INVISIBLE);//表示symbol不存在，这时我们需要hidden
                }





            }

        }


    }


    /**
     *
     * @param resourceIDStr: 例如在“R.id.symbol_box_row_1"中指symbol_box_row_1
     * @return
     */
    private int getResourceID(String resourceIDStr) {
        int id = getResources().getIdentifier(resourceIDStr, "id", AppContext.getAppContext().getPackageName());
        return id;
    }

    /**
     * 每当keyboard的布局变化时，需要更新这里
     * 根据所在行列，获取对应字符
     * @param row
     * @param column
     * @return: 当返回为Null,表示越界
     */
    private @Nullable String getText(int row, int column) {
        int index;
        if (mPageNumber == 0) {
            if (row == 0) {
                index = column;
            } else if (row == 1) {
                index = COLUMN_NUMBER_TYPE_A + column;
            } else if (row == 2) {
                index = COLUMN_NUMBER_TYPE_A * 2 + column;
            }else {
                index = 3 *COLUMN_NUMBER_TYPE_A + (row - 3) * COLUMN_NUMBER_TYPE_B + column;
            }

        } else {
           index = row * COLUMN_NUMBER_TYPE_B + column;
        }

        String symbolText;
        if (SymbolHelper.mUnicodeArray.length > index + mNumberOfSymbolsBeforePage) {
            symbolText = SymbolHelper.mUnicodeArray[index + mNumberOfSymbolsBeforePage];
        } else {
            symbolText = null;
        }

        return  symbolText;

    }

    /**
     * 每当keyboard的布局变化时，需要更新这里
     * @param page
     * @return
     */
    private int numberOfSymbolsBeforePage(int page) {
        int count = 0;
        if (page <= 0) {
            count = 0;
        } else if (page == 1) {
            count = (3 * COLUMN_NUMBER_TYPE_A + 3 * COLUMN_NUMBER_TYPE_B);
        } else {
            count = (3 * COLUMN_NUMBER_TYPE_A + 3 * COLUMN_NUMBER_TYPE_B) + (page -1) * COLUMN_NUMBER_TYPE_B * 6;
        }

        return count;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        String text = ((TextView)v).getText().toString();

        ((MainActivity) getActivity()).mCardDetailFragment.onGridViewItemClicked(text);

        return false;
    }
}
