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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mvpmc.cmyth.java.proglist;

//
// lcm class
//
// This is the starting point for the application.
//
public class lcm extends Activity
{
	// Called when object is created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");

		server = null;

		super.onCreate(savedInstanceState);

		// Do nothing when the app is restarted.
		if (lcm != null) {
			return;
		}

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

		if (lcm == null) {
			lcm = this;
		}

		lcm.start_client();
	}

	public void start_client() {
		if (frontendActivity == null) {
			// Create MythTV frontend activity
			frontendActivity = new Intent(getBaseContext(),
						      frontend.class);
		}

		if (settingsActivity == null) {
			// Create settings activity
			settingsActivity = new Intent(getBaseContext(),
						      settings.class);
		}

		if (statisticsActivity == null) {
			// Create settings activity
			statisticsActivity = new Intent(getBaseContext(),
							statistics.class);
		}

		if (server == null) {
			// Create the backend thread
			String s = prefs.getString("mythtv_server", "");
			String p = prefs.getString("mythtv_port", "6543");
			server = new backend(s, Integer.parseInt(p));
			server.start();
		}

		// Display the MythTV frontend
		startActivity(frontendActivity);
	}

	private class splashThread extends Thread {
		@Override
		public void run() {
			Log.v(TAG, "splashThread.run()");
			super.run();

			while (true) {
				try {
					sleep(100);
				} catch (Exception e) {
				}
			}
		}
	}

	public void update_connection(boolean force) {
		Log.v(TAG, "update_connection()");
	}

	public void update_connection() {
		update_connection(false);
	}

	public void view_settings(boolean error) {
		Log.v(TAG, "view_settings()");

		if (error) {
			settings.set_error("Connection failed!  Please fix your server settings.");
		} else {
			settings.set_error(null);
		}
		startActivity(settingsActivity);
	}

	public void view_statistics() {
		startActivity(statisticsActivity);
	}

	public void server_down(boolean wait) {
		Log.v(TAG, "server_down()");

		view_settings(true);

		if (wait) {
			try {
				wait_for_settings();
			} catch (Exception e) {
			}
		}
	}

	public void wait_for_settings() throws InterruptedException {
		lock.lock();
		in_settings.await();
		lock.unlock();
	}

	public void settings_done() {
		Log.v(TAG, "settings_done()");

		lock.lock();
		in_settings.signal();
		lock.unlock();
	}

	private static final String TAG = "lcm";

	public Intent settingsActivity;
	private Intent frontendActivity;
	public Intent statisticsActivity;

	public backend server;

	public static SharedPreferences prefs;

	public static lcm lcm;

	private Lock lock = new ReentrantLock();

	private Condition in_settings = lock.newCondition();

	static {
		System.loadLibrary("refmem");
		System.loadLibrary("cmyth");
		System.loadLibrary("cppmyth");
		System.loadLibrary("cmyth_java");
	}
}
