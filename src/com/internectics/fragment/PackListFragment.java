package com.internectics.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
        titileTextView.setText(R.string.packlist_title);

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
                intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_PACK_SELECTED);
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

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView = inflater.inflate(R.layout.pack_list_item, parent, false);

            TextView packNameView = (TextView) contentView.findViewById(R.id.pack_name_text);
            ImageView imageView = (ImageView) contentView.findViewById(R.id.pack_cover_image);
            Button changeCoverImageButton = (Button) contentView.findViewById(R.id.button_change_cover_image);
            Button deleteButton = (Button) contentView.findViewById(R.id.button_delete_pack);
            LinearLayout editLayout = (LinearLayout) contentView.findViewById(R.id.pack_list_edit_layout);

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


            packNameView.setText(currentPack.packName);


            return contentView;
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
                    Log.w(Global.debugTag, "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    Pack currentPack = User.defaultUser(AppContext.getAppContext()).packs.get(mIndexOfCurrentPack);
                    currentPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Log.d(Global.debugTag, "currentPack.coverImageUriFormatStr is " + currentPack.coverImageUriFormatStr);
                    currentPack.save(AppContext.getAppContext());
                    ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                }
            }
        }
    }

}
