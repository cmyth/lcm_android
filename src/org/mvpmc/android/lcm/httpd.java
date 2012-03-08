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

import android.util.Log;
import android.media.MediaPlayer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.nio.ByteBuffer;

import org.mvpmc.cmyth.java.cmythConstants;
import org.mvpmc.cmyth.java.proginfo;
import org.mvpmc.cmyth.java.file;

public class httpd extends Thread
{
	public httpd(proginfo p) {
		prog = p;

		createSocket();
	}

	public void setProgram(proginfo p) {
		prog = p;
	}

	@Override
	public void run() {
		super.run();

		Log.v(TAG, "http port " + port);

		while (true) {
			ServerSocket ss = server;
			try {
				Log.v(TAG, "wait for connection");
				Socket s = ss.accept();
				Log.v(TAG, "got connection");
				responder resp = new responder(prog,
							       s);
				resp.start();
			} catch (IOException ioe) {
				Log.v(TAG, "io exception");
			}
		}
	}

	public int getPort() {
		return port;
	}

	private boolean createSocket() {
		Random randomGenerator = new Random();
		int attempts = 100;
		boolean done = false;

		while ((attempts-- > 0) && !done) {
			try {
//				port = 34144;
				port = 5001 + randomGenerator.nextInt(32768);
				server = new ServerSocket(port);
				done = true;
			} catch (IOException ioe) {}
		}

		if (done) {
			Log.v(TAG, "socket created at port " + port);
		}

		return done;
	}

	public class responder extends Thread
	{
		public responder(proginfo p, Socket s) {
			prog = p;
			socket = s;

			max = 128*1024;

			file = prog.open();

			length = prog.length();
		}

		@Override
		public void run() {
			super.run();

			try {
				start = 0;
				end = 0;
				url = null;

				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				String hdr = readHeader(is);
				parseHeader(hdr);
				if (url != null) {
					if (end > start) {
						respond_partial(os);
					} else {
						respond_full(os);
					}
				} else {
					Log.v(TAG, "skip response");
				}
				is.close();
				os.close();
				socket.close();
			} catch (IOException ioe) {
				Log.v(TAG, "io exception");
			}
		}

		private String readHeader(InputStream is) throws IOException {
			int len, offset = 0;
			int size = 512;
			byte buf[] = new byte[size];

			while (offset < size) {
				int i;

				len = is.read(buf, offset, size-offset);
				if (len < 0) {
					break;
				}
				Log.v(TAG, "bytes read: " + len);
				String s = new String(buf);
				String lines[] = s.split("\r\n");
				Log.v(TAG, "lines read: " + lines.length);
				
				for (i=0; i<lines.length; i++) {
					if (lines[i].length() == 0) {
						return s;
					}
				}

				offset += len;
			}

			return null;
		}

		private void parseHeader(String hdr) {
			String lines[] = hdr.split("\r\n");

			Log.v(TAG, "lines read: " + lines.length);

			Log.v(TAG, "Header: " + lines[0]);

			String first[] = lines[0].split("[ ]+");

			Log.v(TAG, "command: " + first[0]);
			Log.v(TAG, "url: " + first[1]);
			Log.v(TAG, "version: " + first[2]);

			if (first.equals("GET")) {
				return;
			}

			url = first[1];

			int i;
			for (i=1; i<lines.length; i++) {
				String line = lines[i].toLowerCase();
				Log.v(TAG, "Header: " + lines[i]);
				if (line.startsWith("range:")) {
					Log.v(TAG, "range found: " + line);
					String t1[] = lines[i].split("=");
					String t2[] = t1[1].split("-");
					start = Long.valueOf(t2[0]);
					if (t2.length == 1) {
						end = length;
					} else {
						end = Long.valueOf(t2[1]);
					}
				}
			}
		}

		private void respond_partial(OutputStream os) throws IOException {
			String hdr;
			byte buf[];

			Log.v(TAG, "send partial response");

			int size = ((int)(end - start)) + 1;

			hdr = String.format("HTTP/1.1 206 Partial Content\r\n" +
					    "Server: mvpmc\r\n" +
					    "Accept-Ranges: bytes\r\n" +
					    "Content-Length: %d\r\n" +
					    "Content-Range: bytes %d-%d/%d\r\n" +
					    "Connection: close\r\n" +
					    "\r\n",
					    size, start, end, length);

			buf = hdr.getBytes();

			os.write(buf);

			file.seek(start);

			long offset = start;

			while (offset < end) {
				ByteBuffer bb;
				int len;

				bb = ByteBuffer.allocateDirect(cmythConstants.DEFAULT_BUFLEN);
				len = file.read(bb);

				if (len > 0) {
					byte b[] = new byte[len];
					bb.get(b, 0, len);
					os.write(b);
					offset += len;
				}

				if (len == 0) {
					break;
				}
			}

			Log.v(TAG, "response complete");
		}

		private void respond_full(OutputStream os) throws IOException {
			String hdr;
			byte buf[];

			Log.v(TAG, "send full response");

			hdr = String.format("HTTP/1.1 200 OK\r\n" +
					    "Server: mvpmc\r\n" +
					    "Accept-Ranges: bytes\r\n" +
					    "Content-Length: %d\r\n" +
					    "Connection: close\r\n" +
					    "\r\n",
					    length);

			buf = hdr.getBytes();

			os.write(buf);

			file.seek(0);

			int offset = 0;

			while (offset < length) {
				ByteBuffer bb;
				int len;

				bb = ByteBuffer.allocateDirect(cmythConstants.DEFAULT_BUFLEN);
				len = file.read(bb);

				if (len > 0) {
					byte b[] = new byte[len];
					bb.get(b, 0, len);
					os.write(b);
					offset += len;
				}

				if (len == 0) {
					break;
				}
			}

			Log.v(TAG, "file transfer complete");
		}

		private String TAG = "responder";

		private proginfo prog;
		private file file;
		private long length;
		private Socket socket;

		private long start;
		private long end;
		private String url;

		private int max;
	}

	public void close() {
		if (prog != null) {
			prog.release();
		}
	}

	private String TAG = "httpd";
	private proginfo prog;

	private int port;
	private ServerSocket server;
}
