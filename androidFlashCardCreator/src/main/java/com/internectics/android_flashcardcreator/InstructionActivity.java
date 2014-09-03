package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;


public class InstructionActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("Help");
        setContentView(R.layout.instruction);

        ViewFlow viewFlow = (ViewFlow) findViewById(R.id.instruction_viewflow);
        viewFlow.setAdapter(new ImageAdapter(this), 5);
        CircleFlowIndicator circleFlowIndicator = (CircleFlowIndicator) findViewById(R.id.instruction_viewflowindic);
        viewFlow.setFlowIndicator(circleFlowIndicator);

    }

    private class ImageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private final int[] ids = {R.drawable.help1, R.drawable.help2, R.drawable.help3};

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
            ((TextView) convertView.findViewById(R.id.txtView)).setText((getResources().getStringArray(R.array.help_instruction))[position]);
            return convertView;
        }

    }
}