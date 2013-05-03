package com.internectics.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
				Log.d(Global.debugTag, selectedImageURI.toString()); // format like
															// "content://media/external/images/media/25
				ContentResolver cResolver = getActivity().getContentResolver();
				try {
					//1. scale image
					Bitmap bitmap = BitmapFactory.decodeStream(cResolver
							.openInputStream(selectedImageURI));
					ImageView coverImageView = (ImageView) mContentView
							.findViewById(R.id.fragment_add_pack_coverImage);
					Bitmap resizeBitmap = FileOperationHelper.resizeBitmap(
							bitmap, 400, 400);
					//2. set UI
					coverImageView.setImageBitmap(resizeBitmap);
					//3. save image
					File toSaveFile = FileOperationHelper.generateUniqueImageFilePath();
					FileOutputStream fOutputStream = new FileOutputStream(toSaveFile);
					try {
						resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOutputStream);
						fOutputStream.flush();
						fOutputStream.close();
					} catch (Exception oException) {
						oException.printStackTrace();
					}
					//4. update data (but not do persistence)
					pack.coverImageUriStr = FileOperationHelper.covertToUriFormatString(toSaveFile);
					Log.d(Global.debugTag, pack.coverImageUriStr);
					
					
					 
				} catch (FileNotFoundException e) {
					Log.e("Exception", e.getMessage(), e);
				}

			}
		}
	}
}
