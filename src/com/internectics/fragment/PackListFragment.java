package com.internectics.fragment;

import android.app.Activity;
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
import android.util.TypedValue;
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
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.*;
import java.io.File;
import java.io.FileNotFoundException;

@SuppressWarnings("deprecation")
public class PackListFragment extends Fragment {

    private boolean mIsEditStatus;
    private Gallery mGallery;
    private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;
    private int mIndexOfCurrentPack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pack_list,
                container, false);


        TextView titileTextView = (TextView) rootView.findViewById(R.id.dialog_title);
        titileTextView.setText("Pack List");

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
                ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();

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
                ((MainActivity) getActivity()).mPopupWindow.dismiss();

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

            final int indexOfCurrentPack = position;
            final Pack currentPack = User.defaultUser(AppContext.getAppContext()).packs.get(position);
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
            lp.setMargins(0, UIHelper.getPixels(10), 0, 0);
            packNameView.setLayoutParams(lp);
            baseView.addView(packNameView);

            //part2
            ImageView imageView;
            if (convertView == null) {
                convertView = new ImageView(mContext);
                imageView = (ImageView) convertView;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new Gallery.LayoutParams(
                        UIHelper.getPixels(180), UIHelper.getPixels(150)));

            } else {
                imageView = (ImageView) convertView;
            }


            ContentResolver cResolver = AppContext.getAppContext().getContentResolver();
            String str = currentPack.coverImageUriFormatStr;
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
            imageView.setBackgroundResource(R.drawable.shape_image_round_corner);


            baseView.addView(imageView);

            //part3
            LinearLayout editLayout = new LinearLayout(mContext);
            editLayout.setOrientation(LinearLayout.HORIZONTAL);
            lp = new LinearLayout.LayoutParams(
                    UIHelper.getPixels(180), UIHelper.getPixels(36));
            lp.setMargins(0, 20, 0, 20);
            editLayout.setLayoutParams(lp);

            Button changeCoverImageButton = new Button(mContext);
            changeCoverImageButton.setText("Change");
            changeCoverImageButton.setTextColor(Color.BLACK);
            changeCoverImageButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            changeCoverImageButton.setWidth(UIHelper.getPixels(80));
            changeCoverImageButton.setBackgroundResource(R.drawable.button_gray);
            changeCoverImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIndexOfCurrentPack = indexOfCurrentPack;
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                    startActivityForResult(intent, CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY);
                }
            });

            Button deleteButton = new Button(mContext);
            deleteButton.setText("Delete");
            lp = new LinearLayout.LayoutParams(
                    UIHelper.getPixels(80), LinearLayout.LayoutParams.FILL_PARENT);
            lp.setMargins(20, 0, 0, 0);
            deleteButton.setLayoutParams(lp);
            deleteButton.setBackgroundResource(R.drawable.button_red);
            deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User defaultUser = User.defaultUser(AppContext.getAppContext());
                    defaultUser.removePack(currentPack);
                    int count = defaultUser.packs.size();
                    if (count > 0) {
                        Pack lastPack = defaultUser.packs.get(count - 1);
                        AppConfig.sharedInstance().set(Global.mostRecentPackCreatedID_Property, String.format("%d", lastPack.packID));
                    }
                    ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
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

                if (User.defaultUser(AppContext.getAppContext()).packs.size() <= 1) {
                    deleteButton.setVisibility(View.INVISIBLE);
                } else {
                    if (((MainActivity) getActivity()).mCurrentPack.packID == currentPack.packID) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY) {
            if (resultCode == Activity.RESULT_OK) {

                Uri selectedImageURI = data.getData();

                Bitmap resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
                if (resultBitmap == null) {
                    Log.d(Global.debugTag, "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    Pack currentPack = User.defaultUser(AppContext.getAppContext()).packs.get(mIndexOfCurrentPack);
                    currentPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Log.d(Global.debugTag, currentPack.coverImageUriFormatStr);
                    currentPack.save(AppContext.getAppContext());
                    ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                }
            }
        }
    }

}
