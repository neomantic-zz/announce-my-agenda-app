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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ViewAppInfoActivity extends Activity {

	public static final String KEY_ASSET_URL = "Asset Url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		final Bundle bundle = this.getIntent().getExtras();
		WebView webView = (WebView) this.findViewById(R.id.appWebViewInfo);
		webView.loadUrl(bundle.getString(KEY_ASSET_URL));
	}
}
