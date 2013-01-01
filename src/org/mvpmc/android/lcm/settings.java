//
//  Copyright (C) 2011-2013, Jon Gettler <gettler@mvpmc.org>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//

package org.mvpmc.android.lcm;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

public class settings extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (error != null) {
			show_error(error);
		}
		global = lcm.lcm;
	}

	@Override
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed()");
		super.onBackPressed();
		global.settings_done();
	}

	public void show_error(String msg) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(msg);
                alert.setCancelable(true);
		alert.setNegativeButton("Close",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

		alert.show();
	}

	public static void set_error(String msg) {
		error = msg;
	}

	private static final String TAG = "settings";
	public static SharedPreferences prefs;
	private static String error = null;

	private lcm global;
}
