package com.flipflash.fragment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.flipflash.UI.SmoothGallery;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.PlayActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.android_ffc.WebViewActivity;
import com.flipflash.data.Pack;
import com.flipflash.data.User;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.OpenUDID_manager;
import com.flipflash.util.StringUtils;
import com.flipflash.util.UIHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.leakcanary.RefWatcher;

import java.io.FileNotFoundException;

import cn.pedant.SweetAlert.SweetAlertDialog;


import static com.flipflash.util.LogUtils.LOGD;

public class PackListFragment extends Fragment {

    private static final String TAG = PackListFragment.class.getName();

    private SmoothGallery mGallery;

    private View mRootView;

    private int  mSortType;

    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User.reset(AppContext.getAppContext(),true);
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

        TextView titleTextView = (TextView) mRootView.findViewById(R.id.dialog_title);
        titleTextView.setText(R.string.Title_Pack_List);

        final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
        editButton.setText(getString(R.string.NavigationBarItem_Create_New_Pack));
        ViewGroup.LayoutParams params = editButton.getLayoutParams();
        params.width = params.width + UIHelper.getPixels(60);
        editButton.setLayoutParams(params);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new CreateEditFragment();
                dialogFragment.show(getActivity().getSupportFragmentManager(), "add_pack_fragment");
                ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                ((MainActivity) getActivity()).dismissPackListPopupWindow();


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

                galleryItemClicked(position);

            }
        });


        final Button visitStoreButton = (Button) mRootView.findViewById(R.id.visit_store_btn);
        visitStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.DIALOG_AlERT))
                        .setContentText("Not implemented yet")
                        .setConfirmText(getString(R.string.DIALOG_CLOSE))
                        .show();
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

        final TextView sortCreatedTextView = (TextView) mRootView.findViewById(R.id.sort_created);
        final TextView sortViewedTextView = (TextView) mRootView.findViewById(R.id.sort_viewed);

        sortCreatedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.sortPacks(0);

                sortCreatedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                sortViewedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(Global.sortType,0);
                editor.commit();

                ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
            }
        });

        sortViewedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.sortPacks(2);

                sortViewedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                sortCreatedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.getAppContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(Global.sortType,2);
                editor.commit();

                ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
            }
        });

        switch (mSortType) {
            case 0: {

                sortCreatedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                sortViewedTextView.setPaintFlags(sortViewedTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

                break;
            }
            case 2: {

                sortCreatedTextView.setPaintFlags(sortCreatedTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                sortViewedTextView.setPaintFlags(sortViewedTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                break;
            }
            default:
                break;
        }

        return mRootView;
    }

    private void galleryItemClicked(int position) {

        if (position ==0) {
            DialogFragment dialogFragment = new CreateEditFragment();
            dialogFragment.show(getActivity().getSupportFragmentManager(), "add_pack_fragment");
            ((MainActivity) getActivity()).dismissPackListPopupWindow();
        } else {
            LOGD(TAG, "galleryItemClicked: " + "Index of pack in pack list is:" + position);
            Intent intent = new Intent();
            intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
            intent.putExtra(Global.KEY_FROM, Global.BROADCAST_EXTRA_FROM_PACK_SELECTED);
            int selectedIndex = position-1;
            intent.putExtra("indexOfPack", selectedIndex);
            getActivity().sendBroadcast(intent);

            Pack selectPack = mUser.packs.get(selectedIndex);
            selectPack.lastVistDate = Global.currentTimeSeconds();
            selectPack.save(AppContext.getAppContext());

            AppConfig.sharedInstance().setPackIDForLastSelected(selectPack.packID);

                    ((MainActivity) getActivity()).showPackInfoView();

            ((MainActivity) getActivity()).mPopupWindow.dismiss();
        }
    }


    public class ImageAdapter extends BaseAdapter {

        public ImageAdapter(Context c) {
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

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (position == 0) {
                    convertView = inflater.inflate(R.layout.pack_list_item_add_pack, parent, false);
                } else {
                    convertView = inflater.inflate(R.layout.pack_list_item, parent, false);
                }
            }


            //also share add pack function
            ImageView coverImageView = (ImageView) convertView.findViewById(R.id.pack_cover_image);

            if ((position != 0)&&(mUser.packs.size() > 0)) {

                final Pack currentPack = mUser.packs.get(position -1);



                final ImageButton editButton = (ImageButton) convertView.findViewById(R.id.button_edit);
                final ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.button_delete_pack);
                final ImageView playImageView = (ImageView) convertView.findViewById(R.id.pack_play_image_view);

                playImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        playImageViewButtonClicked(position);

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
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(dataUri.toString(), coverImageView);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }


                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        editButtonClicked(position);


                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteButtonClicked(position);
                    }
                });


                EditText packNameEditText = (EditText) convertView.findViewById(R.id.pack_name_text);
                packNameEditText.setText(mUser.packs.get(position - 1).packName);


                FrameLayout itemLayout = (FrameLayout) convertView.findViewById(R.id.pack_item_layout);

                if (currentPack.packID == AppConfig.sharedInstance().getPackIDForLastSelected()) {
                    itemLayout.setBackgroundResource(R.drawable.shape_pack_list_item_selected_border_green);
                } else {
                    itemLayout.setBackgroundResource(0);
                }

            }


            return convertView;
        }

    }

    private void deleteButtonClicked(int position) {

        final Pack currentPack = mUser.packs.get(position -1);

        if (currentPack.packID == ((MainActivity) getActivity()).packIDForMasterViewPack) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.DIALOG_WARN))
                    .setMessage(getString(R.string.DIALOG_PACK_IS_BEING_USED))
                    .setPositiveButton(getString(R.string.DIALOG_OK), null)
                    .show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.DIALOG_DELETE_PACK))
                    .setPositiveButton(getString(R.string.Title_Delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
                            editButton.setText(getString(R.string.NavigationBarItem_Create_New_Pack));
                            ViewGroup.LayoutParams params = editButton.getLayoutParams();
                            params.width = params.width + UIHelper.getPixels(60);
                            editButton.setLayoutParams(params);
                            mUser.removePack(currentPack);
                            mUser.sortPacks(mSortType);
                            ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(getString(R.string.DIALOG_CANCEL), null)
                    .show();
        }
    }

    private void editButtonClicked(final int position) {

        final Pack currentPack = mUser.packs.get(position -1);

        if (currentPack.creatorID.equals(OpenUDID_manager.getOpenUDID()) == true) {
            gotoPackEditView(position);
        } else {

            final EditText input = new EditText(getActivity());
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.DIALOG_INPUT_ADMIN_PASSWORD)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(input)
                    .setPositiveButton(R.string.DIALOG_DONE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String password = input.getText().toString();
                            String savedPassword = new String(Base64.decode(currentPack.restorePassword,0));
                            if (password.equals(savedPassword)) {

                                currentPack.creatorID = OpenUDID_manager.getOpenUDID();
                                currentPack.save(getActivity());

                                gotoPackEditView(position);

                            } else {

                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.DIALOG_WARN)
                                        .setMessage(R.string.DIALOG_WRONG_PASSWORD)
                                        .setPositiveButton(R.string.DIALOG_OK,null)
                                        .show();
                            }

                        }
                    })
                    .setNegativeButton(R.string.DIALOG_CANCEL, null)
                    .show();

        }
    }

    private void playImageViewButtonClicked(int position) {

        final Pack currentPack = mUser.packs.get(position -1);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.Label_Select)
                .setMessage(R.string.Label_Please_Select)
                .setNegativeButton(R.string.Optional_Play_Manually, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);

                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 0);  //manually
                        startActivity(intent);

                        ((MainActivity) getActivity()).mIsAllowedToShowPackList = false;
                        ((MainActivity) getActivity()).dismissPackListPopupWindow();
                    }
                })
                .setNeutralButton(R.string.Optional_Auto_Play, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);


                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 1);  //manually
                        startActivity(intent);

                        ((MainActivity) getActivity()).mIsAllowedToShowPackList = false;
                        ((MainActivity) getActivity()).dismissPackListPopupWindow();
                    }
                })
                .setPositiveButton(R.string.Optional_Auto_Play_Loop, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);

                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 2);  //manually
                        startActivity(intent);

                        ((MainActivity) getActivity()).mIsAllowedToShowPackList = false;
                        ((MainActivity) getActivity()).dismissPackListPopupWindow();
                    }
                })
                .show();

    }


    private void gotoPackEditView(int position) {

        Pack currentPack = mUser.packs.get(position -1);

        CreateEditFragment dialogFragment = new CreateEditFragment();
        dialogFragment.setPack(currentPack);
        dialogFragment.setIsEditPack(true);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "add_pack_fragment");
        ((MainActivity) getActivity()).dismissPackListPopupWindow();
    }



    @Override
    public void onStop() {
        super.onStop();

        LOGD(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        mGallery.setAdapter(null);
        mGallery.setOnItemClickListener(null);
        super.onDestroy();

        LOGD(TAG, "onDestroy");

//        RefWatcher refWatcher = AppContext.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }
}
