//
//  Copyright (C) 2011, Jon Gettler <gettler@mvpmc.org>
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

public class lcm extends Activity
{
	// Called when object is created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Load splash screen
		setContentView(R.layout.splash);
		splashThread splash = new splashThread();
		splash.start();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Create views
		mythActivity = new Intent(getBaseContext(), mythList.class);
		settings = new Intent(getBaseContext(), settings.class);
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
			try {
				int waited = 0;
				while (waited < 5000) {
					sleep(100);
					waited += 100;
				}
			} catch (InterruptedException e) {
				// do nothing
			} finally {
				Log.v(TAG, "splashThread.run() finish");
				startActivity(mythActivity);
				if (!isMythEnabled()) {
					startActivity(settings);
				}
				finish();
			}
		}
	}

	private boolean isMythEnabled() {
		return prefs.getBoolean("mythtv_enable", false);
	}

	private static final String TAG = "lcm";
	private Intent mythActivity;

	public static myth server;
	public static Intent settings;

	private SharedPreferences prefs;
	private boolean enabled;
}
