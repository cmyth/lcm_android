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

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;

import org.mvpmc.cmyth.java.connection;
import org.mvpmc.cmyth.java.proglist;

public class myth extends Thread {

	public myth(mythList ml) {
		list = ml;
	}

	@Override
	public void run() {
		super.run();
		Log.v(TAG, "run()");
		prefs = list.getPrefs();
	}

	public int connect() {
		Log.v(TAG, "connect()");
		String server = list.getMythServer();
		Log.v(TAG, "connect to server: " + server);
		int port = list.getMythPort();
		Log.v(TAG, "connect to port: " + port);
		try {
			conn = new connection(server);
		} catch (Exception e) {
			Log.v(TAG, "connect() failed!");
			return -1;
		}
		Log.v(TAG, "connect() finished");
		progs = conn.get_proglist();
		Log.v(TAG, "connect(): program count " + progs.get_count());
		return 0;
	}

	public void disconnect() {
		Log.v(TAG, "disconnect()");
		conn = null;
		progs = null;
	}

	public long getProgCount() {
		if (progs != null) {
			return progs.get_count();
		} else {
			return 0;
		}
	}

	public proglist getProgList() {
		return progs;
	}

	public void close() {
		if (conn != null) {
			conn.release();
		}
		if (progs != null) {
			progs.release();
		}
	}

	private static final String TAG = "myth";
	private SharedPreferences prefs;
	private boolean enabled;

	private connection conn;
	private proglist progs;

	private mythList list;
}
