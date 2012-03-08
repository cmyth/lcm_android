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

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Comparator;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.net.Uri;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.view.View;
import android.widget.TextView;
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

import org.mvpmc.cmyth.java.refmem;
import org.mvpmc.cmyth.java.connection;
import org.mvpmc.cmyth.java.proglist;
import org.mvpmc.cmyth.java.proginfo;

public class mythList extends ListActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		update_connection();
		Log.v(TAG, "mythList created");
	}

	private void update_connection() {
		server = new myth(this);
		server.start();
		if (isMythEnabled()) {
			server.connect();
		}
		updateList();
	}

	// Called after onCreate() or onRestart()
	@Override
	public void onStart() {
		super.onStart();

		// Register to listen for preference changes
		OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				// Implementation
				Log.v(TAG, "preference changed");
				if (key.equals("mythtv_enable")) {
					update_connection();
				}
				if (key.equals("mythtv_server")) {
					update_connection();
				}
				if (key.equals("mythtv_port")) {
					update_connection();
				}
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	private void updateList() {
		proglist pl = server.getProgList();

		if (pl != progs) {
			progs = pl;
		}

		createList();
	}

	private void createList() {
		if (current == null) {
			createTitleList();
		} else {
			createEpisodeList();
		}
	}

	private List getProgTitles() {
		int i;
		long count = progs.get_count();
		List list = new ArrayList();

		for (i=0; i<count; i++) {
			proginfo prog = progs.get_prog(i);
			String title = prog.title();
			Log.v(TAG, title);
			list.add(title);
			prog.release();
		}

		return list;
	}

	private void createTitleList() {
		List titles;

		if (progs == null) {
			titles = new ArrayList<String>();
		} else {
			Log.v(TAG, "mythList prog count " + progs.get_count());

			titles = getProgTitles();
		}

		SortedSet set = new TreeSet(new Comparator<String>() {
				public int compare(String a, String b) {
					String x = a.toLowerCase();
					String y = b.toLowerCase();
					return x.compareTo(y);
				}
			});
		set.addAll(titles);

		titleList = new ArrayList();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			titleList.add(it.next());
		}

		setListAdapter(new ArrayAdapter<String>(this,
							R.layout.myth_list,
							titleList));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
				current = (String)titleList.get((int)id);
				updateList();
			}
		});
	}

	private void createEpisodeList() {
		List episodes;

		recordings = new ArrayList<proginfo>();

		if (progs == null) {
			episodes = new ArrayList<String>();
		} else {
			long count = progs.get_count();
			int i;

			episodes = new ArrayList();

			for (i=0; i<count; i++) {
				proginfo prog = progs.get_prog(i);
				String title = prog.title();
				if (current.equals(title)) {
					String subtitle = prog.subtitle();
					episodes.add(subtitle);
					recordings.add(prog);
				} else {
					prog.release();
				}
			}
		}

		setListAdapter(new ArrayAdapter<String>(this,
							R.layout.myth_list,
							episodes));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
				Log.v(TAG, "id " + id);
				play_recording(id);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	// Called from stop when the activity comes to the foreground
	@Override
	public void onRestart() {
		Log.v(TAG, "onRestart()");
		super.onRestart();
		updateList();
	}

	// Called when the back button is pressed
	@Override
	public void onBackPressed() {
		if (current != null) {
			current = null;
			updateList();
		} else {
			cleanup();
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			view_about();
			return true;
		case R.id.settings:
			view_settings();
			return false;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void view_about() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.about_string);
                alert.setCancelable(true);
		alert.setNegativeButton(R.string.close,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

		alert.show();
	}

	private void view_settings() {
		Log.v(TAG, "view_settings()");

		startActivity(lcm.settings);
	}

	private void play_recording(long id) {
		Log.v(TAG, "play_recording " + id);

		proginfo prog = (proginfo)recordings.get((int)id);

		if (daemon == null) {
			daemon = new httpd(prog);
			daemon.start();
		} else {
			daemon.setProgram(prog);
		}

		int port = daemon.getPort();
		String name = prog.pathname();
		String url = String.format("http://localhost:%d/%s", port, name);
		Intent i = new Intent(Intent.ACTION_VIEW);  
		i.setData(Uri.parse(url));  
		startActivity(i); 
	}

	public boolean isMythEnabled() {
		return prefs.getBoolean("mythtv_enable", false);
	}

	public String getMythServer() {
		return prefs.getString("mythtv_server", "");
	}

	public int getMythPort() {
		String str = prefs.getString("mythtv_port", "6543");
		Log.v(TAG, "got port " + str);
		return Integer.parseInt(str);
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	private void cleanup() {
		//libcmyth.refDebug();
		if (progs != null) {
			progs.release();
		}
		if (daemon != null) {
			daemon.interrupt();
			daemon.close();
		}
		if (server != null) {
			server.close();
		}
		if (recordings != null) {
			Iterator it = recordings.iterator();
			while (it.hasNext()) {
				proginfo prog = (proginfo)it.next();
				prog.release();
			}
		}
		//libcmyth.refDebug();

		daemon = null;
		progs = null;
		server = null;
		titleList = null;
		current = null;
		recordings = null;
	}

	private static final String TAG = "mythList";

	private SharedPreferences prefs;
	private proglist progs;
	private httpd daemon;
	private myth server;

	private List titleList;
	private String current;
	private List recordings;
}
