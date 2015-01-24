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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class UserAdapter extends ArrayAdapter<ParseUser> {
	
	protected Context mContext;
	protected List<ParseUser> mUsers;
	protected ArrayList<Integer> mIsFrndReqSentAndFriends;
	protected ArrayList<String> mFrndsUsernames;
	

	public UserAdapter(Context context,  List<ParseUser> users, ArrayList<Integer> isFrndReqSentAndFriends) {
		super(context, R.layout.user_item, users);
		mContext = context;
		mUsers = users;
		mIsFrndReqSentAndFriends = isFrndReqSentAndFriends;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
			holder = new ViewHolder();
			holder.userImageView = (ImageView)convertView.findViewById(R.id.userImageView);
			holder.nameLabel = (TextView)convertView.findViewById(R.id.usernameTextView);
			holder.frndLabel = (TextView)convertView.findViewById(R.id.frndStatus);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		ParseUser user = mUsers.get(position);
		String email = user.getEmail().toLowerCase();
		
		if (email.equals("")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
		}
		else {
			String hash = MD5Util.md5Hex(email);
			String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + 
					"?s=204&d=404";
			Picasso.with(mContext)
				.load(gravatarUrl)
				.placeholder(R.drawable.avatar_empty)
				.into(holder.userImageView);
		}
		
		holder.nameLabel.setText(user.getUsername());
		
		if(mIsFrndReqSentAndFriends.get(position) == 10){
			holder.frndLabel.setText(R.string.friend_request_sent_label);
		}
		else if (mIsFrndReqSentAndFriends.get(position) == 01) {
			holder.frndLabel.setText(R.string.friends_label);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		ImageView userImageView;
		TextView nameLabel;
		TextView frndLabel;
	}
	
	public void refill(List<ParseUser> users, ArrayList<Integer> isFrndReqSentAndFriends) {
		mUsers.clear();
		mUsers.addAll(users);
		mIsFrndReqSentAndFriends.clear();
		mIsFrndReqSentAndFriends.addAll(isFrndReqSentAndFriends);
		notifyDataSetChanged();
	}
}


	
	


