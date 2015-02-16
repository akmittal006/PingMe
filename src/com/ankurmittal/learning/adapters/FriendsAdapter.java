package com.ankurmittal.learning.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.util.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class FriendsAdapter extends ArrayAdapter<ParseUser> {

	Context mContext;
	String[] usernames;
	ArrayList<ParseUser> mFriends;
	protected String hash;

	public FriendsAdapter(Context context, ArrayList<ParseUser> friends) {

		super(context, R.layout.fragment_friends, friends);
		mContext = context;
		mFriends = friends;
		usernames = new String[mFriends.size()];
		int i = 0;
		for (ParseUser user : mFriends) {
			usernames[i] = user.getUsername();
			i++;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.frnd_item, null);
			holder = new ViewHolder();
			holder.userImageView = (ImageView) convertView
					.findViewById(R.id.userImageView);
			holder.nameLabel = (TextView) convertView
					.findViewById(R.id.usernameTextView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ParseUser user = mFriends.get(position);
		String email = user.getEmail().toLowerCase();

		if (email.equals("")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
		} else {
			hash = MD5Util.md5Hex(email);
			String gravatarUrl = "http://www.gravatar.com/avatar/" + hash
					+ "?s=204&d=404";
			Picasso.with(mContext).setIndicatorsEnabled(true);
			if (isExternalStorageAvailable()) {

				// 1. Get the external storage directory
				String appName = "PingMe";
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);

				String path = mediaStorageDir.getPath() + File.separator;

				File file = new File(path + "/" + hash + ".jpg");
				if (file.exists()) {
					Log.i("IMPPPPPP", Uri.fromFile(file).toString());
					Picasso.with(mContext).load(Uri.fromFile(file))
							.placeholder(R.drawable.avatar_empty)
							.resize(75, 75).centerCrop()
							.into(holder.userImageView);
				} else {
					Picasso.with(mContext).load(gravatarUrl)
							.placeholder(R.drawable.avatar_empty)
							.resize(75, 75).centerCrop()
							.into(holder.userImageView);
					Picasso.with(mContext).load(gravatarUrl).into(target);
				}

			}

		}

		holder.nameLabel.setText(user.getUsername());

		return convertView;
	}

	private Target target = new Target() {
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
								Log.e("friends", "Failed to create directory.");
								// return null;
							}
						}
						// 3. Create a file name
						// 4. Create the file
						File mediaFile;

						String path = mediaStorageDir.getPath()
								+ File.separator;

						File file = new File(path + "/" + hash + ".jpg");
						try {
							file.createNewFile();
							FileOutputStream ostream = new FileOutputStream(
									file);
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
	};

	private static class ViewHolder {
		ImageView userImageView;
		TextView nameLabel;
		// TextView frndLabel;
	}

	public void refill(List<ParseUser> friends) {
		mFriends.clear();
		mFriends.addAll(friends);
		notifyDataSetChanged();
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
