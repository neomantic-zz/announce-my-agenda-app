/** "Announce My Agenda" Android App
    Copyright (C) 2014 Chad Albers

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

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
