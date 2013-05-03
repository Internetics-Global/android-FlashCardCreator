package com.internectics.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import com.internectics.util.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class PackListFragment extends Fragment {

    private boolean mIsEditStatus;
    private Gallery mGallery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pack_list,
                container, false);


        final Button editButton = (Button) rootView.findViewById(R.id.dialog_head_save_btn);
        editButton.setText("Edit");
        mIsEditStatus = false;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editButton.getText().equals("Edit")) {
                    editButton.setText("Done");
                    mIsEditStatus = true;
                } else {
                    editButton.setText("Edit");
                    mIsEditStatus = false;
                }
                ((ImageAdapter)mGallery.getAdapter()).notifyDataSetChanged();

            }
        });

        Button closeButton = (Button) rootView.findViewById(R.id.dialog_head_close_btn);
        closeButton.setVisibility(View.INVISIBLE);


        mGallery = (Gallery) rootView.findViewById(R.id.pack_list_gallery);
        // Set the adapter to our custom adapter (below)
        mGallery.setAdapter(new ImageAdapter(getActivity()));
        mGallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(Global.debugTag, "Index of pack in pack list is:" + position);
                Intent intent = new Intent();
                intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                intent.putExtra(Global.KEY_FROM, Global.BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED);
                intent.putExtra("indexOfPack", position);  //id begin from 0
                getActivity().sendBroadcast(intent);
                ((MainActivity)getActivity()).mPopupWindow.dismiss();

            }
        });



        return rootView;
    }


    public class ImageAdapter extends BaseAdapter {

        private final Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return User.defaultUser(AppContext.getAppContext()).packs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final Pack currentPack =  User.defaultUser(AppContext.getAppContext()).packs.get(position);
            LinearLayout baseView = new LinearLayout(mContext);
            baseView.setOrientation(LinearLayout.VERTICAL);

            //part1
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

            //part2
            ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);
                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

            //part3
            LinearLayout editLayout = new LinearLayout(mContext);
            editLayout.setOrientation(LinearLayout.HORIZONTAL);
            lp = new LinearLayout.LayoutParams(
                    UIHelper.getPixels(180), UIHelper.getPixels(40));
            lp.setMargins(0,20,0,20);
            editLayout.setLayoutParams(lp);

            Button changeCoverImageButton = new Button(mContext);
            changeCoverImageButton.setText("Change");
            changeCoverImageButton.setWidth(UIHelper.getPixels(80));
            changeCoverImageButton.setBackgroundResource(R.drawable.graybutton);

            Button deleteButton = new Button(mContext);
            deleteButton.setText("Delete");
            lp = new LinearLayout.LayoutParams(
                    UIHelper.getPixels(80), LinearLayout.LayoutParams.FILL_PARENT);
            lp.setMargins(20,0,0,0);
            deleteButton.setLayoutParams(lp);
            deleteButton.setBackgroundResource(R.drawable.redbutton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User defaultUser = User.defaultUser(AppContext.getAppContext());
                    defaultUser.removePack(currentPack);
                    int count = defaultUser.packs.size();
                    if (count >0) {
                        Pack lastPack = defaultUser.packs.get(count-1);
                        AppConfig.getInstance(getActivity()).set(Global.packID_Property, String.format("%d", lastPack.packID));
                    }
                    ((ImageAdapter)mGallery.getAdapter()).notifyDataSetChanged();
                }
            });


            editLayout.addView(changeCoverImageButton);
            editLayout.addView(deleteButton);
            baseView.addView(editLayout);

            if (mIsEditStatus) {
                editLayout.setVisibility(View.VISIBLE);

                if (currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID())) {
                    changeCoverImageButton.setVisibility(View.VISIBLE);
                } else {
                    changeCoverImageButton.setVisibility(View.INVISIBLE);
                }

                if (User.defaultUser(AppContext.getAppContext()).packs.size()<=1) {
                    deleteButton.setVisibility(View.INVISIBLE);
                } else {
                    if (((MainActivity)getActivity()).mCurrentPack.packID == currentPack.packID) {
                        deleteButton.setVisibility(View.INVISIBLE);
                    } else {
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }


            } else {
                editLayout.setVisibility(View.INVISIBLE);
            }



            return baseView;
        }
    }

}
