package com.ankurmittal.learning.util;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;



public class CustomTarget implements Target {
	
	public String middlePath;
	public Context context;
	
	public CustomTarget( Context context) {
		this.context = context;
	}
	
	public String getTargetHash() {
		return middlePath;
	}

	public void setTargetHash(String userID) {
		this.middlePath = userID;
	}
	
	

	@Override
	public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
		Log.e("custom target","on bitmap loaded");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (Utils.isExternalStorageAvailable()) {
					
					Log.e("target thread ", "deleting and updating file");
					// 1. Get the external storage directory
					String path = Utils.getAppPath();

					File file = new File(path + "/" + middlePath + ".jpg");
//					if (file.exists()){
//						Log.e("custom target thread","deleting file");
//						file.delete(); //DELETE existing file
//				        file = new File(path + "/" + userID + ".jpg");
//				    }
					try {
						file.createNewFile();
						Log.e("custom target thread","overwriting file..");
						FileOutputStream ostream = new FileOutputStream(file);
						bitmap.compress(CompressFormat.JPEG, 75, ostream);
						ostream.close();
						Log.i("check", file.getPath());
						Intent intent = new Intent("Custom target refresh");
						context.sendBroadcast(intent);
						
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					Log.e("custom target","error not avail");
				}

			}

		}).start();
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		Log.e("custom target","bitmap failed");
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		Log.e("custom target","on prepare load");
		if (placeHolderDrawable != null) {
		}
	}
	

	
	
	public void deleteFile () {
		Log.e("custom target","deleting file...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (Utils.isExternalStorageAvailable()) {
					
					Log.e("target ", "thread running");
					// 1. Get the external storage directory
					String appName = "PingMe";
					File mediaStorageDir = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							appName);

					// 2. Create our subdirectory
					if (!mediaStorageDir.exists()) {
						if (!mediaStorageDir.mkdirs()) {
							Log.e("custom target", "Failed to create directory. "
									+ mediaStorageDir.toString());
							// //return null;
						}
					}
					// 3. Create a file name
					// 4. Create the file
					//File mediaFile;

					String path = mediaStorageDir.getPath() + File.separator;
					//String middlePath = .substring(93, 116);

					File file = new File(path + "/" + middlePath + ".jpg");
					if (file.exists()){
						Log.e("custom target","deleting file inside");
						file.delete(); //DELETE existing file
				        file = new File(path + "/" + middlePath + ".jpg");
				    }
					

				} else {
					Log.e("custom target","error not avail");
				}

			}
		}).start();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	

}
