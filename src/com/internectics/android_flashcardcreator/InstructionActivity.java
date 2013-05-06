package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 6/05/13
 * Time: 10:33 上午
 * To change this template use File | Settings | File Templates.
 */
public class InstructionActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Help");
        setContentView(R.layout.instruction);

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.instruction_viewflow);
        viewFlow.setAdapter(new ImageAdapter(this), 5);
        CircleFlowIndicator circleFlowIndicator = (CircleFlowIndicator) findViewById(R.id.instruction_viewflowindic);
        viewFlow.setFlowIndicator(circleFlowIndicator);

    }

    private class ImageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private final int[] ids = { R.drawable.help1, R.drawable.help2, R.drawable.help3, R.drawable.help4,
                R.drawable.help5, R.drawable.help6, R.drawable.help7 };

        public ImageAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return ids.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.instruction_image_item, null);
            }
            ((ImageView) convertView.findViewById(R.id.imgView)).setImageResource(ids[position]);
            return convertView;
        }

    }
}