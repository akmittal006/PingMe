package com.ankurmittal.learning.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import com.ankurmittal.learning.util.CustomTarget;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.ParseUser;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class FriendsAdapter extends ArrayAdapter<ParseUser> {

	Context mContext;
	String[] usernames;
	ArrayList<ParseUser> mFriends;
	protected String userID;

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
		String imgUrl = user.getString(ParseConstants.KEY_PROFILE_IMAGE);
		if(imgUrl != null) {
			Log.d("friends adapter", imgUrl);
		} else if (user.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null ) {
			imgUrl = user.getParseFile(ParseConstants.KEY_PROFILE_IMAGE).getUrl();
			Log.d("friends adapter", imgUrl);
		} else {
			Log.d("friends adapter", "null image url "+ position + user.getUsername());
			holder.nameLabel.setText(user.getUsername());
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
			return convertView;
		}
		

		if (imgUrl.equals("null")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
			Log.i("frnds adapter", imgUrl + " img set - " + position);
		} else if(imgUrl != null) {
			//if the image is not null
			Log.i("url check", imgUrl);
			//hash = MD5Util.md5Hex(email);
//			String gravatarUrl = "http://www.gravatar.com/avatar/" + hash
//					+ "?s=204&d=404";
			String middlePath = imgUrl.substring(93, 116);
			Picasso.with(mContext).setIndicatorsEnabled(true);
			CustomTarget target = new CustomTarget(mContext);
			//Log.d("frnds adpater",user.getString("UserId"));
			target.setTargetHash(middlePath);
			if (isExternalStorageAvailable()) {

				// 1. Get the external storage directory
				String appName = "PingMe";
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);

				String path = mediaStorageDir.getPath() + File.separator;
				
				//Log.e("middle Path", middlePath);

				File file = new File(path + "/" + middlePath + ".jpg");
				if (file.exists()) {
					Log.i("image loaded from mobile", Uri.fromFile(file).toString());
					Picasso.with(mContext).load(Uri.fromFile(file))
							.placeholder(R.drawable.avatar_empty)
							.resize(88, 88).centerInside()
							.into(holder.userImageView);
				} else {
					Log.i("image loaded from net", Uri.fromFile(file).toString());
					Picasso.with(mContext).load(imgUrl)
							.placeholder(R.drawable.avatar_empty)
							.resize(88, 88).centerInside().memoryPolicy(MemoryPolicy.NO_CACHE)
							.into(holder.userImageView);
					Picasso.with(mContext).load(imgUrl).into(target);
				}

			}

		}

		holder.nameLabel.setText(user.getUsername());

		return convertView;
	}

//	private Target target = new Target() {
//		@Override
//		public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					if (isExternalStorageAvailable()) {
//
//						// 1. Get the external storage directory
//						String appName = "PingMe";
//						File mediaStorageDir = new File(
//								Environment
//										.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//								appName);
//
//						// 2. Create our subdirectory
//						if (!mediaStorageDir.exists()) {
//							if (!mediaStorageDir.mkdirs()) {
//								Log.e("friends", "Failed to create directory. " + mediaStorageDir.toString());
//								// return null;
//							}
//						}
//						// 3. Create a file name
//						// 4. Create the file
//						File mediaFile;
//
//						String path = mediaStorageDir.getPath()
//								+ File.separator;
//
//						File file = new File(path + "/" + hash + ".jpg");
//						try {
//							file.createNewFile();
//							FileOutputStream ostream = new FileOutputStream(
//									file);
//							bitmap.compress(CompressFormat.JPEG, 75, ostream);
//							ostream.close();
//							Log.i("check", file.getPath());
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//
//					}
//
//				}
//			}).start();
//		}
//
//		@Override
//		public void onBitmapFailed(Drawable errorDrawable) {
//		}
//
//		@Override
//		public void onPrepareLoad(Drawable placeHolderDrawable) {
//			if (placeHolderDrawable != null) {
//			}
//		}
//	};

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
