package com.ankurmittal.learning.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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

public class FriendsAdapter extends ArrayAdapter<ParseUser> {

	Context mContext;
	String[] usernames;
	ArrayList<ParseUser> mFriends;

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
			String hash = MD5Util.md5Hex(email);
			String gravatarUrl = "http://www.gravatar.com/avatar/" + hash
					+ "?s=204&d=404";
			Picasso.with(mContext).setIndicatorsEnabled(true);
			 Picasso.with(mContext).load(gravatarUrl).placeholder(R.drawable.avatar_empty).resize(75, 75)
					.centerCrop().into(holder.userImageView);
		}

		holder.nameLabel.setText(user.getUsername());

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
