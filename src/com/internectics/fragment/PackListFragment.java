package com.internectics.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
import com.internectics.util.UIHelper;

import java.io.FileNotFoundException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class PackListFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pack_list,
                container, false);


        Button editButton = (Button) rootView.findViewById(R.id.dialog_head_save_btn);
        editButton.setText("Edit");

        Button closeButton = (Button) rootView.findViewById(R.id.dialog_head_close_btn);
        closeButton.setVisibility(View.INVISIBLE);


        Gallery g = (Gallery) rootView.findViewById(R.id.pack_list_gallery);
        // Set the adapter to our custom adapter (below)
        g.setAdapter(new ImageAdapter(getActivity()));
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(Global.debugTag, "Index of pack in pack list is:" + position);
                Intent intent = new Intent();
                intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                intent.putExtra(Global.KEY_FROM, Global.BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED);
                intent.putExtra("indexOfPack", id);  //id begin from 0
                getActivity().sendBroadcast(intent);
                ((MainActivity)getActivity()).mPopupWindow.dismiss();

            }
        });



        return rootView;
    }


    public class ImageAdapter extends BaseAdapter {

        private final Context mContext;

        private ArrayList<Pack> packs;

        public ImageAdapter(Context c) {
            mContext = c;
            packs = User.defaultUser(AppContext.getAppContext()).packs;
        }

        public int getCount() {
            return packs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Pack currentPack =  packs.get(position);

            LinearLayout baseView = new LinearLayout(mContext);
            baseView.setOrientation(LinearLayout.VERTICAL);

            ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);
                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(new Gallery.LayoutParams(
                        UIHelper.getPixels(180),UIHelper.getPixels(150)));

            } else {
                imageView = (ImageView) convertView;
            }

            ContentResolver cResolver = AppContext.getAppContext().getContentResolver();
            String str = currentPack.coverImageUriStr;
            if (StringUtils.isNumeric(str)) {
                imageView.setImageResource(Integer.parseInt(str));
            } else {
                Uri dataUri = Uri.parse(str);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(cResolver
                            .openInputStream(dataUri));
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }


            baseView.addView(imageView);

            TextView packNameView = new TextView(mContext);
            packNameView.setTextColor(Color.WHITE);
            packNameView.setText(currentPack.packName);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
            packNameView.setGravity(Gravity.CENTER);
            packNameView.setWidth(UIHelper.getPixels(180));
            packNameView.setHeight(UIHelper.getPixels(30));
            lp.setMargins(0,UIHelper.getPixels(10),0,0);
            packNameView.setLayoutParams(lp);

            baseView.addView(packNameView);

            return baseView;
        }
    }

}
