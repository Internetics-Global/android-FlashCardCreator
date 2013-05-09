package com.internectics.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.DropboxHelper;

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

        TextView titleTextView = (TextView) mContentView
                .findViewById(R.id.dialog_title);
        titleTextView.setText("More");
        Button closeButton = (Button) mContentView
                .findViewById(R.id.dialog_head_close_btn);
        Button saveButton = (Button) mContentView
                .findViewById(R.id.dialog_head_save_btn);
        saveButton.setVisibility(View.INVISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

            }
        });

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
