package com.internectics.android_flashcardcreator;

import com.internectics.data.Pack;
import com.internectics.model.CardListModel;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

public class CursorDSLV extends FragmentActivity {

    private SimpleDragSortCursorAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dragsortlistview);

        String[] cols = {"card_sn","cover_image"};
        int[] ids = {R.id.card_list_item_card_sn,R.id.card_list_item_cover_image};
        adapter = new MAdapter(this,
                R.layout.card_list_item, null, cols, ids, 0);

        DragSortListView dslv = (DragSortListView) findViewById(android.R.id.list);
        dslv.setAdapter(adapter);

        // build a cursor from the String array
        List<HashMap<String, Object>> mCardArrayList;
        Pack mCurrentPack = CardListModel.getCurrentPack();
        mCardArrayList = CardListModel.getCardList(mCurrentPack);

        MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "card_sn","cover_image"});
        for (int i = 0; i < mCardArrayList.size(); i++) {
            cursor.newRow()
                    .add(i)
                    .add(mCardArrayList.get(i).get("cardSN"))
                    .add(mCardArrayList.get(i).get("coverImageUriFormatStr").toString());

        }
        adapter.changeCursor(cursor);
    }

    private class MAdapter extends SimpleDragSortCursorAdapter {
        private Context mContext;

        public MAdapter(Context ctxt, int rmid, Cursor c, String[] cols, int[] ids, int something) {
            super(ctxt, rmid, c, cols, ids, something);
            mContext = ctxt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            View tv = v.findViewById(R.id.card_list_item_cover_image);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "text clicked", Toast.LENGTH_SHORT).show();
                }
            });
            return v;
        }
    }
}
