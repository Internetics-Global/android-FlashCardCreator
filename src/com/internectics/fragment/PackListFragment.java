package com.internectics.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.internectics.UI.SmoothGallery;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.PlayActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.android_flashcardcreator.WebViewActivity;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.*;

import java.io.File;
import java.io.FileNotFoundException;

public class PackListFragment extends Fragment {

    private boolean mIsEditStatus;
    private SmoothGallery mGallery;
    private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;
    private int mIndexOfCurrentPack;

    private View mRootView;

    private int  mSortType;

    private int mSelectedItemIndex = -1;

    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = User.defaultUser(AppContext.getAppContext());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
        mSortType = sp.getInt(Global.sortType,2);
        mUser.sortPacks(mSortType);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_pack_list,
                container, false);


        TextView titileTextView = (TextView) mRootView.findViewById(R.id.dialog_title);
        titileTextView.setText(R.string.packlist_title);

        final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
        editButton.setText("Create New Pack");
        ViewGroup.LayoutParams params = editButton.getLayoutParams();
        params.width = params.width + UIHelper.getPixels(60);
        editButton.setLayoutParams(params);
        mIsEditStatus = false;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editButton.getText().equals("Create New Pack")) {
                    ((MainActivity) getActivity()).mPopupWindow.dismiss();
                    DialogFragment dialogFragment = new AddPackFragment();
                    dialogFragment.show(getActivity().getFragmentManager(), "add_pack_fragment");
                } else {
                    editButton.setText("Create New Pack");
                    ViewGroup.LayoutParams params = editButton.getLayoutParams();
                    params.width = params.width + UIHelper.getPixels(60);;
                    editButton.setLayoutParams(params);
                    mIsEditStatus = false;
                    mSelectedItemIndex = -1;
                }
                ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();

            }
        });

        Button closeButton = (Button) mRootView.findViewById(R.id.dialog_head_close_btn);
        closeButton.setVisibility(View.INVISIBLE);


        mGallery = (SmoothGallery) mRootView.findViewById(R.id.pack_list_gallery);
        // Set the adapter to our custom adapter (below)
        mGallery.setAdapter(new ImageAdapter(getActivity()));
        if (mUser.packs.size() >0) {
            mGallery.setSelection(1); //when set this, everytime after notifyDataSetChanged finishe, getView(1) will be called one more
        }
        mGallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (position ==0) {
                    ((MainActivity) getActivity()).mPopupWindow.dismiss();
                    DialogFragment dialogFragment = new AddPackFragment();
                    dialogFragment.show(getActivity().getFragmentManager(), "add_pack_fragment");
                } else {
                    Log.d(Global.debugTag, "Index of pack in pack list is:" + position);
                    Intent intent = new Intent();
                    intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
                    intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_PACK_SELECTED);
                    int selectedIndex = position-1;
                    intent.putExtra("indexOfPack", selectedIndex);
                    getActivity().sendBroadcast(intent);

                    Pack selectPack = mUser.packs.get(selectedIndex);
                    selectPack.lastVistDate = (int)System.currentTimeMillis();
                    selectPack.save(AppContext.getAppContext());

                    ((MainActivity) getActivity()).mPopupWindow.dismiss();
                }

            }
        });


        final Button newUserButton = (Button) mRootView.findViewById(R.id.new_user_btn);
        newUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra("url", "http://www.youtube.com.au");
                startActivity(intent);
            }
        });

        final Button sortButton = (Button) mRootView.findViewById(R.id.sort_btn);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Integer.parseInt((String)sortButton.getTag())) {
                    case 0: {
                        sortButton.setTag("1");
                        mUser.sortPacks(0);
                        sortButton.setText("Sorted by recently created first");

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt(Global.sortType,0);
                        editor.commit();

                        ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();

                        break;
                    }
                    case 1: {
                        sortButton.setTag("0");
                        mUser.sortPacks(2);
                        sortButton.setText("Sorted by recently viewed first");

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt(Global.sortType,2);
                        editor.commit();

                        ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();

                        break;
                    }
                }
            }
        });

        switch (mSortType) {
            case 0: {
                sortButton.setText("Sorted by recently created first");
                break;
            }
            case 2: {
                sortButton.setText("Sorted by recently visited first");
                break;
            }
            default:
                break;
        }

        return mRootView;
    }


    public class ImageAdapter extends BaseAdapter {

        private final Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mUser.packs.size() +1;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView;
            if (position == 0) {
                contentView = inflater.inflate(R.layout.pack_list_item_add_pack, parent, false);
            } else {
                contentView = inflater.inflate(R.layout.pack_list_item, parent, false);
            }


            //also share add pack function
            ImageView coverImageView = (ImageView) contentView.findViewById(R.id.pack_cover_image);


            if ((position != 0)&&(mUser.packs.size() > 0)) {

                final Pack currentPack = mUser.packs.get(position -1);

                //also share the edit cards function
                Button changeCoverImageButton = (Button) contentView.findViewById(R.id.button_change_cover_image);

                final Button deleteButton = (Button) contentView.findViewById(R.id.button_delete_pack);

                ImageView playImageView = (ImageView) contentView.findViewById(R.id.pack_play);
                playImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ((MainActivity)getActivity()).mIsAllowedToShowPackList = false;

                        currentPack.lastVistDate = (int)System.currentTimeMillis();
                        currentPack.save(AppContext.getAppContext());

                        ((MainActivity) getActivity()).mPopupWindow.dismiss();
                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        startActivity(intent);
                    }
                });

                ContentResolver cResolver = AppContext.getAppContext().getContentResolver();
                String str = currentPack.coverImageUriFormatStr;
                if (StringUtils.isNumeric(str)) {
                    coverImageView.setImageResource(Integer.parseInt(str));
                } else {
                    Uri dataUri = Uri.parse(str);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(cResolver
                                .openInputStream(dataUri));
                        coverImageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                final EditText packNameView = (EditText) contentView.findViewById(R.id.pack_name_text);
                packNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {

                            currentPack.packName = packNameView.getText().toString();
                            currentPack.save(AppContext.getAppContext());
                        }
                        return false;
                    }
                });

                changeCoverImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mIsEditStatus) {
                            if (position == 0) {

                            } else {
                                mIndexOfCurrentPack = position -1;
                                Intent intent = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                                startActivityForResult(intent, CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY);
                            }
                        } else {
                            mSelectedItemIndex = position;
                            mIsEditStatus = true;
                            final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
                            editButton.setText("Done");
                            ViewGroup.LayoutParams params = editButton.getLayoutParams();
                            params.width = params.width - UIHelper.getPixels(60);;
                            editButton.setLayoutParams(params);
                            ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                        }
                    }
                });


                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialog.Builder(mContext)
                                .setTitle("Are you sure to delete?")
                                .setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
                                        editButton.setText("Create New Pack");
                                        ViewGroup.LayoutParams params = editButton.getLayoutParams();
                                        params.width = params.width + UIHelper.getPixels(60);
                                        editButton.setLayoutParams(params);
                                        mIsEditStatus = false;
                                        mIndexOfCurrentPack = -1;
                                        mUser.removePack(currentPack);
                                        mUser.sortPacks(mSortType);
                                        ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("Cancel",null)
                                .show();



                    }
                });




                if ((mIsEditStatus) && (mSelectedItemIndex == position)) {

                    changeCoverImageButton.setText("Change Image");

                    if (mUser.packs.get(position -1).creatorID.equals(OpenUDID_manager.getOpenUDID())) {
                        changeCoverImageButton.setVisibility(View.VISIBLE);
                        packNameView.setTextColor(Color.BLACK);
                        packNameView.setEnabled(true);
                        packNameView.setFocusable(true);
                        packNameView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    } else {
                        changeCoverImageButton.setVisibility(View.INVISIBLE);
                        packNameView.setBackgroundColor(Color.TRANSPARENT);
                        packNameView.setTextColor(Color.WHITE);
                        packNameView.setEnabled(false);
                        packNameView.setFocusable(false);
                    }

                    if (mUser.packs.size() <= 1) {
                        deleteButton.setVisibility(View.INVISIBLE);
                    } else {
                    }



                } else {
                    packNameView.setEnabled(false);
                    packNameView.setFocusable(false);
                    packNameView.setBackgroundColor(Color.TRANSPARENT);
                    packNameView.setTextColor(Color.WHITE);
                    deleteButton.setVisibility(View.INVISIBLE);

                    changeCoverImageButton.setText("Edit Cards");
                }

                if (((mSelectedItemIndex >=0 ) && (mSelectedItemIndex != position) && mIsEditStatus)
                        ||((currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID()) == false)&&(currentPack.packID == ((MainActivity)getActivity()).packIDForMasterViewPack))){

                    changeCoverImageButton.setVisibility(View.INVISIBLE);
                }
                //Log.d(Global.debugTag3,currentPack.creatorID + "====" + OpenUDID_manager.getOpenUDID() + "----" + position + "packID: " + currentPack.packID + "PackID2: " + ((MainActivity)getActivity()).packIDForMasterViewPack);

                packNameView.setText(mUser.packs.get(position -1).packName);

                if (currentPack.packID == ((MainActivity)getActivity()).packIDForMasterViewPack) {
                    deleteButton.setVisibility(View.INVISIBLE);
                }

            }


            return contentView;
        }

        private void deleteCurrentPack() {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY) {
            if (resultCode == Activity.RESULT_OK) {

                Bitmap resultBitmap = null;
                Uri selectedImageURI = data.getData();

                //step1: get image
                final String[] filePathColumn = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };
                Cursor cursor = getActivity().getContentResolver().query(selectedImageURI, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex;
                    // if it is a picasa image on newer devices with OS 3.0 and up
                    if ((selectedImageURI.toString().startsWith("content://com.google.android.gallery3d"))
                            ||(selectedImageURI.toString().startsWith("content://com.sec.android.gallery3d"))){
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                        if (columnIndex != -1) {
                            final Uri picasaUri = selectedImageURI;
                            resultBitmap = UIHelper.getResized400SizeBitmapFromPicasa(getActivity(), picasaUri);
                        }
                    } else { // it is a regular local image file
                        resultBitmap = UIHelper.resizeImageTo400(getActivity(), selectedImageURI);
                    }
                    cursor.close();
                }

                if (resultBitmap == null) {
                    Log.w(Global.debugTag, "resultBitmap is null");
                } else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    Pack currentPack = mUser.packs.get(mIndexOfCurrentPack);
                    currentPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(toSaveFile);
                    Log.d(Global.debugTag, "currentPack.coverImageUriFormatStr is " + currentPack.coverImageUriFormatStr);
                    currentPack.save(AppContext.getAppContext());
                    mUser.sortPacks(mSortType);
                    ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("ccaa","onStop in PackListFragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ccaa","onDestory in PackListFragment");
    }
}
