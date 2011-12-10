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

import java.nio.ByteBuffer;

public class libcmyth extends Object
{
	// Memory management
	protected native void refRelease(long ptr);
	protected native void refHold(long ptr);
	public static native void refDebug();

	// Create connections
	protected native long connect(String server, int port,
				      long buflen, long tcp_rcvbuf);
	protected native long connectEvent(String server, int port,
					   long buflen, long tcp_rcvbuf);
	protected native long connectFile(long prog, long control,
					  long buflen, long tcp_rcvbuf);

	// Connection methods
	protected native int getProtocolVersion(long conn);
	protected native long getAllRecorded(long conn);

	// Program list methods
	protected native long proglistGetCount(long proglist);
	protected native long getProginfo(long proglist, int which);

	// Program info methods
	protected native String getProginfoTitle(long proginfo);
	protected native String getProginfoSubtitle(long proginfo);
	protected native String getProginfoHost(long proginfo);
	protected native int getProginfoPort(long proginfo);
	protected native long getProginfoLength(long proginfo);
	protected native String getProginfoFilename(long proginfo);

	// File methods
	protected native long fileSeek(long file, long offset);
	protected native int fileRequestBlock(long file, long size);
	protected native int fileGetBlock(long file, ByteBuffer buf, long size);

	static {
		System.loadLibrary("cmyth");
	}
}
