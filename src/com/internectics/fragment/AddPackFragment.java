package com.internectics.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Pack;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.*;

import java.io.File;

public class AddPackFragment extends DialogFragment {

	private static AddPackFragment mDialogFragment;
	public View mContentView;
	public Pack pack;
	private int CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY = 1001;

	public static AddPackFragment getInstance() {
		if (mDialogFragment == null) {
			return new AddPackFragment();
		} else {
			return mDialogFragment;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		pack = new Pack();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mContentView = inflater.inflate(R.layout.fragment_add_pack, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView titleTextView = (TextView) mContentView
                .findViewById(R.id.dialog_title);
        titleTextView.setText("Help");
        Button closeButton = (Button) mContentView
				.findViewById(R.id.dialog_head_close_btn);
		Button saveButton = (Button) mContentView
				.findViewById(R.id.dialog_head_save_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

			}
		});
        saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				save();
				dismiss();

			}
		});

		ImageView coverImageView = (ImageView) mContentView
				.findViewById(R.id.fragment_add_pack_coverImage);
		coverImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(
						new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
						CODE_REQUEST_IMAGE_FROM_IMAGE_LIBRARY);

			}
		});

		return mContentView;
	}

	private void save() {
		EditText packNameEditText = (EditText) mContentView
				.findViewById(R.id.fragment_add_pack_pack_name);
		EditText sidebarTitleEditText = (EditText) mContentView
				.findViewById(R.id.fragment_add_pack_sidebar_title);
		EditText creatorEditText = (EditText) mContentView
				.findViewById(R.id.fragment_add_pack_creator);

		pack.packName = packNameEditText.getText().toString();
		pack.sidebarTitle = sidebarTitleEditText.getText().toString();
		pack.creatorNickName = creatorEditText.getText().toString();
		// we set pack.coverImageUriStr in image select or by default
		pack.creatorID = OpenUDID_manager.getOpenUDID();
		pack.userID = Global.USER_ID;
		pack.packID = (int)(System.currentTimeMillis()/1000L);
		pack.save(AppContext.getAppContext());
		
		AppConfig.getInstance(getActivity()).set(Global.packID_Property, String.format("%d", pack.packID));
		AppConfig.getInstance(getActivity()).set(Global.latestPackCreatedDate_Property, StringUtils.getCurrentTimeDate());

        Intent intent = new Intent();
        intent.setAction(Global.BROADCAST_ACTION_UPDATE_MASTER_VIEW);
        intent.putExtra(Global.KEY_FROM, Global.BROADCAST_INTENT_EXTRA_FROM_NEW_PACK);
        getActivity().sendBroadcast(intent);

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
                }else {
                    File toSaveFile = UIHelper.saveImageToCaches(resultBitmap);
                    ImageView coverImageView = (ImageView) mContentView
                            .findViewById(R.id.fragment_add_pack_coverImage);
                    coverImageView.setImageBitmap(resultBitmap);

                    pack.coverImageUriStr = FileOperationHelper.covertToUriFormatString(toSaveFile);
                    Log.d(Global.debugTag, pack.coverImageUriStr);
                }
			}
		}
	}
}
