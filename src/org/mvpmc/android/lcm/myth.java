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
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.os.SystemClock;

import org.mvpmc.cmyth.java.connection;
import org.mvpmc.cmyth.java.proglist;
import org.mvpmc.cmyth.java.event;

public class myth extends Thread {

	public myth(SharedPreferences sp, String s, int p) {
		Log.v(TAG, "myth()");

		prefs = sp;
		server = s;
		port = p;

		sync = new ReentrantLock();
	}

	@Override
	public void run() {
		Log.v(TAG, "run()");
		super.run();
		connect();

		while (true) {
			if (conn != null) {
				event ev;

				ev = conn.get_event();

				Log.v(TAG, "event: " + ev.name());

				if (ev.name().equals("connection closed")) {
					conn = null;
				}
			} else {
				try {
					sleep(500);
				} catch (Exception e) {
				}
			}
		}
	}

	public int connect() {
		Log.v(TAG, "connect()");
		sync.lock();
		Log.v(TAG, "connect to server: " + server);
		Log.v(TAG, "connect to port: " + port);
		try {
			conn = new connection(server);
		} catch (Exception e) {
			Log.v(TAG, "connect() failed!");
			sync.unlock();
			return -1;
		}
		Log.v(TAG, "connect() finished");
		progs = conn.get_proglist();
		Log.v(TAG, "connect(): program count " + progs.get_count());
		sync.unlock();
		return 0;
	}

	public int reconnect() {
		Log.v(TAG, "reconnect()");
		if (conn == null) {
			connect();
		}
		return 0;
	}

	public void disconnect() {
		Log.v(TAG, "disconnect()");
		conn = null;
		progs = null;
	}

	public long getProgCount() {
		long n = 0;
		if (progs != null) {
			n = progs.get_count();
			if (n < 0) {
				if (connect() == 0) {
					n = progs.get_count();
				}
			}
		}

		return n;
	}

	public proglist getProgList() {
		sync.lock();
		proglist p = progs;
		sync.unlock();

		return p;
	}

	public void close() {
		if (conn != null) {
			conn.release();
		}
		if (progs != null) {
			progs.release();
		}
	}

	public int conn_wait(int timeout) {
		long start = SystemClock.elapsedRealtime();
		long now;

		timeout = timeout * 1000;

		while (true) {
			try {
				if (sync.tryLock(10, TimeUnit.MILLISECONDS)) {
					sync.unlock();
					if (conn != null) {
						return 0;
					}
				}
				now = SystemClock.elapsedRealtime();
				if ((now - start) >= timeout) {
					break;
				}
				sleep(100);
			} catch (Exception e) {
			}
		}

		return -1;
	}

	public int conn_wait() {
		return conn_wait(5);
	}

	private static final String TAG = "myth";
	private SharedPreferences prefs;
	private boolean enabled;

	private connection conn;
	private proglist progs;

	private String server;
	private int port;

	private ReentrantLock sync;
}
