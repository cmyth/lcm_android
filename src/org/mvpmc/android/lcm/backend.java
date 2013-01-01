//
//  Copyright (C) 2012-2013, Jon Gettler <gettler@mvpmc.org>
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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

import org.mvpmc.cmyth.java.connection;
import org.mvpmc.cmyth.java.event;
import org.mvpmc.cmyth.java.proglist;

public class backend extends Thread
{
	public backend(String s, int p) {
		Log.v(TAG, "backend(" + s + ", " + p + ")");

		global = lcm.lcm;

		prefs = global.prefs;
		server = s;
		port = p;

		sync = new ReentrantLock();

		connected = false;
	}

	@Override
	public void run() {
		Log.v(TAG, "run()");

		super.run();

		while (true) {
			while (connect() == false) {
				global.server_down(true);
			}

			monitor();
		}
	}

	private boolean connect() {
		sync.lock();

		server = prefs.getString("mythtv_server", server);
		String p = prefs.getString("mythtv_port", "6543");
		port = Integer.parseInt(p);

		Log.v(TAG, "connect to backend at " + server + ":" + port);

		try {
			conn = new connection(server);
			connected = true;
			Log.v(TAG, "connection succeeded");
		} catch (Exception e) {
			connected = false;
			Log.v(TAG, "connection failed");
		}

		sync.unlock();

		return connected;
	}

	private void monitor() {
		event ev;

		while (true) {
			ev = conn.get_event();

			if (ev.name().equals("connection closed")) {
				return;
			}
		}
	}

	public proglist get_progs() {
		proglist rc;

		sync.lock();

		if (conn == null) {
			rc = null;
		} else {
			rc = conn.get_proglist();
		}

		sync.unlock();

		return rc;
	}

	private static final String TAG = "backend";

	private SharedPreferences prefs;

	private String server;
	private int port;

	private connection conn;

	private boolean connected;

	private ReentrantLock sync;

	private lcm global;
}
