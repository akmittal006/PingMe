package com.ankurmittal.learning.util;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;



public class CustomTarget implements Target {
	
	public String userID;
	
	public String getTargetHash() {
		return userID;
	}

	public void setTargetHash(String userID) {
		this.userID = userID;
	}

	@Override
	public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (isExternalStorageAvailable()) {

					// 1. Get the external storage directory
					String appName = "PingMe";
					File mediaStorageDir = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							appName);

					// 2. Create our subdirectory
					if (!mediaStorageDir.exists()) {
						if (!mediaStorageDir.mkdirs()) {
							Log.e("friends", "Failed to create directory. "
									+ mediaStorageDir.toString());
							// return null;
						}
					}
					// 3. Create a file name
					// 4. Create the file
					File mediaFile;

					String path = mediaStorageDir.getPath() + File.separator;

					File file = new File(path + "/" + userID + ".jpg");
					try {
						file.createNewFile();
						FileOutputStream ostream = new FileOutputStream(file);
						bitmap.compress(CompressFormat.JPEG, 75, ostream);
						ostream.close();
						Log.i("check", file.getPath());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		}).start();
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		if (placeHolderDrawable != null) {
		}
	}

	private boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();

		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

}
