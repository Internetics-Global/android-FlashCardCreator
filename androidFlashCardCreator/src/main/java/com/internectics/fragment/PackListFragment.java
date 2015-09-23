package com.internectics.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
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

import com.internectics.UI.SmoothGallery;
import com.internectics.android_flashcardcreator.MainActivity;
import com.internectics.android_flashcardcreator.PlayActivity;
import com.internectics.android_flashcardcreator.R;
import com.internectics.android_flashcardcreator.WebViewActivity;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.OpenUDID_manager;
import com.internectics.util.StringUtils;
import com.internectics.util.UIHelper;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.io.FileNotFoundException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import timber.log.Timber;

public class PackListFragment extends Fragment {

    private SmoothGallery mGallery;

    private View mRootView;

    private int  mSortType;

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

        TextView titleTextView = (TextView) mRootView.findViewById(R.id.dialog_title);
        titleTextView.setText(R.string.packlist_title);

        final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
        editButton.setText("Create New Pack");
        ViewGroup.LayoutParams params = editButton.getLayoutParams();
        params.width = params.width + UIHelper.getPixels(60);
        editButton.setLayoutParams(params);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new CreateEditFragment();
                dialogFragment.show(getActivity().getFragmentManager(), "add_pack_fragment");
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
                        .setTitleText("Alert")
                        .setContentText("Not implemented yet")
                        .setConfirmText("Close")
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
            dialogFragment.show(getActivity().getFragmentManager(), "add_pack_fragment");
            ((MainActivity) getActivity()).dismissPackListPopupWindow();
        } else {
            Timber.tag(Global.debugTag).d("Index of pack in pack list is:" + position);
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
                    .setTitle("Warning")
                    .setMessage("The pack is currently being used")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Are you sure you want to delete?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Button editButton = (Button) mRootView.findViewById(R.id.dialog_head_save_btn);
                            editButton.setText("Create New Pack");
                            ViewGroup.LayoutParams params = editButton.getLayoutParams();
                            params.width = params.width + UIHelper.getPixels(60);
                            editButton.setLayoutParams(params);
                            mUser.removePack(currentPack);
                            mUser.sortPacks(mSortType);
                            ((ImageAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancel", null)
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
                    .setTitle("Input admin password")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(input)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
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
                                        .setTitle("Error")
                                        .setMessage("Wrong password")
                                        .setPositiveButton("OK",null)
                                        .show();
                            }

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        }
    }

    private void playImageViewButtonClicked(int position) {

        final Pack currentPack = mUser.packs.get(position -1);

        new AlertDialog.Builder(getActivity())
                .setTitle("Select")
                .setMessage("Please select one")
                .setNegativeButton("Play Manually", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);

                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 0);  //manually
                        startActivity(intent);

                        ((MainActivity) getActivity()).dismissPackListPopupWindow();
                    }
                })
                .setNeutralButton("Auto Play", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);

                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 1);  //manually
                        startActivity(intent);

                        ((MainActivity) getActivity()).dismissPackListPopupWindow();
                    }
                })
                .setPositiveButton("Auto Play and Loop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AppConfig.sharedInstance().setPackIDForLastSelected(currentPack.packID);

                        Intent intent = new Intent(getActivity(), PlayActivity.class);
                        intent.putExtra("packID", currentPack.packID);
                        intent.putExtra("oneOffPlayType", 2);  //manually
                        startActivity(intent);

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
        dialogFragment.show(getActivity().getFragmentManager(), "add_pack_fragment");
        ((MainActivity) getActivity()).dismissPackListPopupWindow();
    }



    @Override
    public void onStop() {
        super.onStop();

        Timber.tag(Global.debugTag).d("onStop in PackListFragment");
    }

    @Override
    public void onDestroy() {
        mGallery.setAdapter(null);
        super.onDestroy();
        Timber.tag(Global.debugTag).d("onDestory in PackListFragment");
    }
}
