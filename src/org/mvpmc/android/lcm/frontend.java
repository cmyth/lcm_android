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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Comparator;

import org.mvpmc.cmyth.java.connection;
import org.mvpmc.cmyth.java.proglist;
import org.mvpmc.cmyth.java.proginfo;

public class frontend extends ListActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		global = lcm.lcm;
		server = global.server;
		prefs = global.prefs;
		super.onCreate(savedInstanceState);

		episodeActivity = new Intent(getBaseContext(),
					     episode.class);
	}

	@Override
	public void onStart() {
		Log.v(TAG, "onStart()");

		super.onStart();

		update_list();
	}

	@Override
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed()");
		if (current != null) {
			current = null;
			update_list();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, "onOptionsItemSelected(): " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.about:
			view_about();
			return true;
		case R.id.settings:
			global.view_settings(false);
			return false;
		case R.id.statistics:
			global.view_statistics();
			return false;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void update_list() {
		Log.v(TAG, "update_list()");

		proglist pl = server.get_progs();

		if (pl != progs) {
			Log.v(TAG, "update progs");
			progs = pl;
		}

		if (current == null) {
			setTitle("MythTV Shows");
			create_titles();
		} else {
			setTitle("MythTV - " + current);
			create_episodes();
		}
	}

	private List program_titles() {
		int i;
		long count = progs.get_count();
		List list = new ArrayList();

		Log.v(TAG, "mythList prog count " + count);

		for (i=0; i<count; i++) {
			proginfo prog = progs.get_prog(i);
			String title = prog.title();
			list.add(title);
		}

		return list;
	}

	private void create_titles() {
		Log.v(TAG, "create_titles()");

		List titles;

		if (progs == null) {
			titles = new ArrayList<String>();
		} else {
			titles = program_titles();
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
							R.layout.frontend,
							titleList));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
				current = (String)titleList.get((int)id);
				update_list();
			}
		});
	}

	private void create_episodes() {
		Log.v(TAG, "create_episodes()");

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
					if (subtitle.equals("")) {
						String start = prog.start_str();
						episodes.add(start);
					} else {
						episodes.add(subtitle);
					}
					recordings.add(prog);
				}
			}
		}

		setListAdapter(new ArrayAdapter<String>(this,
							R.layout.frontend,
							episodes));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
				Log.v(TAG, "id " + id);
				episode.prog = (proginfo)recordings.get((int)id);
				startActivity(episodeActivity);
			}
		});
	}

	private static final String TAG = "frontend";

	private SharedPreferences prefs;

	private lcm global;

	private backend server;
	private proglist progs;

	private String current;
	private List recordings;
	private List titleList;

	private Intent episodeActivity;
}
