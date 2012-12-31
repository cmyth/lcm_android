//
//  Copyright (C) 2011-2012, Jon Gettler <gettler@mvpmc.org>
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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.Preference;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import org.mvpmc.cmyth.java.proglist;

public class lcm extends Activity
{
	// Called when object is created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");

		server = null;

		super.onCreate(savedInstanceState);

		if (lcm != null) {
			return;
		}

		lcm = this;

		// Load preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Load splash screen
		setContentView(R.layout.splash);
		splashThread splash = new splashThread();
		splash.start();
	}

	@Override
	public void onStart() {
		Log.v(TAG, "onStart()");

		super.onStart();

		if (mythActivity == null) {
			// Create views
			mythActivity = new Intent(getBaseContext(),
						  mythList.class);
			settings = new Intent(getBaseContext(), settings.class);

			// Connect to the MythTV server
			update_connection(true);

			// Wait for the connection to happen (in another thread)
			if (server.conn_wait() != 0) {
				Log.v(TAG, "onStart(): connection failed");
			}
		}

		// Display the recordings list
		startActivity(mythActivity);
	}

	// Called from stop when the activity comes to the foreground
	@Override
	public void onRestart() {
		Log.v(TAG, "onRestart()");
		super.onRestart();
	}

	// Called from pause when the activity comes to the foreground
	@Override
	public void onResume() {
		Log.v(TAG, "onResume()");
		super.onResume();
	}

	private class splashThread extends Thread {
		@Override
		public void run() {
			Log.v(TAG, "splashThread.run()");
			super.run();
		}
	}

	public void update_connection(boolean force) {
		Log.v(TAG, "update_connection()");
		if ((server == null) || (force == true)) {
			Log.v(TAG, "connect to server");
			String s = prefs.getString("mythtv_server", "");
			String p = prefs.getString("mythtv_port", "6543");
			server = new myth(prefs, s, Integer.parseInt(p));
			server.start();
		}
	}

	public void update_connection() {
		update_connection(false);
	}

	private static final String TAG = "lcm";

	public Intent settings;
	private static Intent mythActivity;

	public myth server;

	private SharedPreferences prefs;

	public static lcm lcm;

	static {
		System.loadLibrary("refmem");
		System.loadLibrary("cmyth");
		System.loadLibrary("cppmyth");
		System.loadLibrary("cmyth_java");
	}
}
