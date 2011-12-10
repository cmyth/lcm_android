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

package org.mvpmc.android.lcm.cmyth;

import java.util.List;
import java.util.ArrayList;

import android.util.Log;

public class proglist extends libcmyth
{
	public proglist(long conn) {
		handle = getAllRecorded(conn);
	}

	public long getCount() {
		return proglistGetCount(handle);
	}

	public List getProgTitles() {
		int i;
		long count = proglistGetCount(handle);
		List list = new ArrayList();

		for (i=0; i<count; i++) {
			proginfo prog = new proginfo(handle, i);
			String title = prog.getTitle();
			Log.v(TAG, title);
			list.add(title);
			prog.close();
		}

		return list;
	}

	public proginfo getProginfo(long which) {
		return new proginfo(handle, (int)which);
	}

	public void close() {
		refRelease(handle);
	}

	private long handle;

	private static final String TAG = "proglist";
}
