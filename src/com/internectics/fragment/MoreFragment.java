package com.internectics.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.helper.DropboxHelper;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.*;

import java.io.File;

public class MoreFragment extends DialogFragment {

	private static MoreFragment mDialogFragment;
	public View mContentView;

	public static MoreFragment getInstance() {
		if (mDialogFragment == null) {
			return new MoreFragment();
		} else {
			return mDialogFragment;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mContentView = inflater.inflate(R.layout.fragment_more, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ToggleButton dropboxToggleButton = (ToggleButton)mContentView.findViewById(R.id.dropbox_toggleButton);
        final boolean isLinked =DropboxHelper.getDropboxAPI(getActivity()).getSession().isLinked();
        dropboxToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLinked) {
                    DropboxHelper.logOut(getActivity());
                } else {
                    DropboxHelper.getDropboxAPI(getActivity()).getSession().startAuthentication(getActivity());
                }
            }
        });
        if (isLinked) {
            dropboxToggleButton.setChecked(true);
        } else {
            dropboxToggleButton.setChecked(false);
        }

		return mContentView;
	}
}
