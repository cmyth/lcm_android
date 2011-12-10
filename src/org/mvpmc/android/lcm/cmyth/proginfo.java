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

public class proginfo extends libcmyth
{
	public proginfo(long proglist, int which) {
		handle = getProginfo(proglist, which);
	}

	public String getTitle() {
		return getProginfoTitle(handle);
	}

	public String getSubtitle() {
		return getProginfoSubtitle(handle);
	}

	public String getHost() {
		return getProginfoHost(handle);
	}

	public int getPort() {
		return getProginfoPort(handle);
	}

	public long getLength() {
		return getProginfoLength(handle);
	}

	public String getFilename() {
		return getProginfoFilename(handle);
	}

	public file getFile(String server, int port,
			    long buflen, long tcp_rcvbuf) {
		long conn = connect(server, port, buflen, tcp_rcvbuf);
		file f = new file(handle, conn, buflen, tcp_rcvbuf);
		refRelease(conn);
		return f;
	}

	public void close() {
		refRelease(handle);
	}

	private long handle;
}
