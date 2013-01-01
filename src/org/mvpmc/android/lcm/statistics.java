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

import android.app.ListActivity;

import android.os.Bundle;

import android.util.Log;

import android.widget.ArrayAdapter;

public class statistics extends ListActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		global = lcm.lcm;
		super.onCreate(savedInstanceState);
		String[] values = new String[] { "Connections",
						 "Events",
						 "Memory Usage",
						 "Uptime",
		};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
									android.R.layout.simple_list_item_1,
									values);
		setListAdapter(adapter);
	}

	@Override
	public void onStart() {
		Log.v(TAG, "onStart()");

		super.onStart();
	}

	private static final String TAG = "statistics";

	private lcm global;
}
