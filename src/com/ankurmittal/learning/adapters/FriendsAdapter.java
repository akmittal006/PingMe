package com.ankurmittal.learning.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.ankurmittal.learning.util.Utils;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

public class FriendsAdapter extends ArrayAdapter<ParseUser> {

	Context mContext;
	String[] usernames;
	ArrayList<ParseUser> mFriends;
	ArrayList<Target> mTargets;
	ArrayList<ImageView> mViews;
	ArrayList<String> mPaths;
	protected String userID;
	String middlePath;

	public FriendsAdapter(Context context, ArrayList<ParseUser> friends) {

		super(context, R.layout.fragment_friends, friends);
		mContext = context;
		mFriends = friends;
		mTargets = new ArrayList<Target>();
		mViews = new ArrayList<ImageView>();
		mPaths = new ArrayList<String>();
		usernames = new String[mFriends.size()];
		int i = 0;
		for (ParseUser user : mFriends) {
			usernames[i] = user.getUsername();
			i++;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

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

		final ParseUser user = mFriends.get(position);
		String imgUrl = user.getString(ParseConstants.KEY_PROFILE_IMAGE);

		if (imgUrl == null || imgUrl.equals("null")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
		} else if (user.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null) {
			imgUrl = user.getParseFile(ParseConstants.KEY_PROFILE_IMAGE)
					.getUrl();
			Log.e("DEBUG",position + " --> img url = " + imgUrl);
			//Load and save image
		} else {
			Log.d("friends adapter",
					"null image url " + position + user.getUsername());
			// if the image is not null
						Log.i("url check", imgUrl);
						middlePath = imgUrl.substring(93, 116);
						Picasso.with(mContext).setIndicatorsEnabled(true);
						if (Utils.isExternalStorageAvailable()) {

							File file = new File(Utils.getAppPath() + "/" + imgUrl.substring(93, 116) + ".png");
							if (file.exists()) {
								Log.e("image loaded from mobile", Uri.fromFile(file)
										.toString());
								Picasso.with(mContext).invalidate(file);
								Picasso.with(mContext).load(Uri.fromFile(file))
										.placeholder(R.drawable.avatar_empty)
										.resize(88, 88).centerInside()
										.into(holder.userImageView);
							} else {
								 Target picTarget = new Target() {
									
									@Override
									public void onPrepareLoad(Drawable arg0) {
									}
									
									@Override
									public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
										int index = mTargets.indexOf(this);
										Utils.saveAndLoadProfilePic(bitmap, mPaths.get(index), mViews.get(index));
									}
									@Override
									public void onBitmapFailed(Drawable arg0) {
									}
								};
								
								mTargets.add(picTarget);
								mViews.add(holder.userImageView);
								mPaths.add(middlePath);
								holder.userImageView.setTag(mTargets.get(mTargets.size()-1));
								Log.e("image loaded from net", Uri.fromFile(file)
										.toString());
								Picasso.with(mContext).load(imgUrl)
										.placeholder(R.drawable.avatar_empty).into(mTargets.get(mTargets.size()-1));
							}

						}
			holder.nameLabel.setText(user.getUsername());
			convertView.setTag(holder);
			return convertView;
		}

		holder.nameLabel.setText(user.getUsername());
		convertView.setTag(holder);
		return convertView;
	}

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
}
