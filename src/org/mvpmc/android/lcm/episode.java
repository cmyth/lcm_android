//
//  Copyright (C) 2013, Jon Gettler <gettler@mvpmc.org>
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

import org.mvpmc.cmyth.java.cmythConstants;
import org.mvpmc.cmyth.java.file;
import org.mvpmc.cmyth.java.filetype_t;
import org.mvpmc.cmyth.java.proginfo;

public class episode extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate()");
		global = lcm.lcm;
		server = global.server;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.episode);

		title = (TextView) findViewById(R.id.episode_title);
		subtitle = (TextView) findViewById(R.id.episode_subtitle);
		channel = (TextView) findViewById(R.id.episode_channel);
		start = (TextView) findViewById(R.id.episode_start);
		duration = (TextView) findViewById(R.id.episode_duration);
		size = (TextView) findViewById(R.id.episode_size);
		description = (TextView) findViewById(R.id.episode_description);

		play = (Button) findViewById(R.id.episode_play);
		download = (Button) findViewById(R.id.episode_download);
		delete = (Button) findViewById(R.id.episode_delete);

		play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					play_recording();
				}
			});

		download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					download_recording();
				}
			});

		delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					delete_recording();
				}
			});

		image = (ImageView) findViewById(R.id.episode_image);

		cache = getBaseContext().getCacheDir();
	}

	@Override
	public void onStart() {
		Log.v(TAG, "onStart()");

		super.onStart();

		int hours, minutes, seconds;

		seconds = prog.seconds();
		hours = seconds / (60 * 60);
		seconds = seconds - (hours * (60 * 60));
		minutes = seconds / 60;
		seconds = seconds - (minutes * 60);

		long mb = prog.length() / (1024 * 1024);

		title.setText(prog.title());
		subtitle.setText(prog.subtitle());
		channel.setText(prog.channel_name());
		start.setText(prog.start_str());
		description.setText(prog.description());

		if (mb >= 1000) {
			size.setText(String.format("%.2f", mb/1000.0) + " GB");
		} else {
			size.setText("" + mb + " MB");
		}

		String m, h;

		if (minutes == 0) {
			m = "";
		} else if (minutes == 1) {
			m = "" + minutes + " minute";
		} else {
			m = "" + minutes + " minutes";
		}

		if (hours == 0) {
			h = "";
		} else if (hours == 1) {
			h = "" + hours + " hour ";
		} else {
			h = "" + hours + " hours ";
		}

		duration.setText(h + m);

		File thumbnail = new File(cache, "thumbnail");

		thumbnail.delete();

		try {
			load_thumbnail(thumbnail);

			Uri uri = Uri.fromFile(thumbnail);
			image.setImageURI(uri);
		} catch (Exception e) {
			Log.v(TAG, "thumbnail load failed");
		}
	}

	private void play_recording() {
		Log.v(TAG, "play_recording");

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

	private void download_recording() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Download not supported yet!");
                alert.setCancelable(true);
		alert.setNegativeButton(R.string.close,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

		alert.show();
	}

	private void delete_recording() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Delete not supported yet!");
                alert.setCancelable(true);
		alert.setNegativeButton(R.string.close,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

		alert.show();
	}

	private void load_thumbnail(File thumbnail) throws IOException {
		file file;
		int size, len;
		ByteBuffer bb;
		FileOutputStream out;

		Log.v(TAG, "load_thumbnail()");

		out = new FileOutputStream(thumbnail);

		file = prog.open(filetype_t.FILETYPE_THUMBNAIL);
		file.seek(0);
		size = 0;
		while (true) {
			bb = ByteBuffer.allocateDirect(cmythConstants.DEFAULT_BUFLEN);
			len = file.read(bb);
			if (len > 0) {
				byte b[] = new byte[len];
				bb.get(b, 0, len);
				out.write(b, size, len);
				size += len;
			} else {
				break;
			}
		}

		Log.v(TAG, "loaded " + size + " bytes");

		out.close();
	}

	private static final String TAG = "episode";

	public static proginfo prog;

	private lcm global;
	private backend server;

	private TextView title;
	private TextView subtitle;
	private TextView channel;
	private TextView start;
	private TextView duration;
	private TextView size;
	private TextView description;

	private Button play;
	private Button download;
	private Button delete;

	private ImageView image;

	private File cache;

	private httpd daemon;
}
