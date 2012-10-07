package com.neomantic.calendar_out_loud;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class UnsupportedLanguageAlertFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.alert_title_unsupported_language)
			.setMessage(R.string.alert_btn_unsupported_language_msg)
			.setPositiveButton(R.string.alert_btn_unsupported_language_ok, 
					new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							MainActivity m = ((MainActivity) getActivity());
							m.disableSpeechButton();
							m.disableListView();
						}
					})
			.create();
	}

	public static DialogFragment newInstance() {
		return new UnsupportedLanguageAlertFragment();
	}

}