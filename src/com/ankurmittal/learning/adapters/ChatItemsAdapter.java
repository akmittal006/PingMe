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
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.util.CustomTarget;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class ChatItemsAdapter extends ArrayAdapter<ChatItem> {
	
	private static final String TAG = "Chat Item Adapter";

	Context mContext;
	String[] usernames;
	ArrayList<ChatItem> mChatItems;
	protected String userID;

	public ChatItemsAdapter(Context context, ArrayList<ChatItem> chatItems) {

		super(context, R.layout.chat_item, chatItems);
		mContext = context;
		mChatItems = chatItems;
		usernames = new String[mChatItems.size()];
		int i = 0;
		//getting username array
		for (ChatItem chatItem : mChatItems) {
			//usernames[i] = chatItem.;
			i++;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_item, null);
			holder = new ViewHolder();
			
			//add views to layout
			holder.userImageView = (ImageView) convertView
					.findViewById(R.id.userImageView);
			holder.nameLabel = (TextView) convertView
					.findViewById(R.id.usernameTextView);
			holder.chatSubtitle = (TextView) convertView.findViewById(R.id.subtitleView);
			holder.newMsgNumView = (TextView) convertView.findViewById(R.id.newMessageNumView);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ChatItem chatItem = mChatItems.get(position);
		
		//1. Image View
		String imgUrl = chatItem.getImgUrl();
		Log.d("chat item adapter", "img url- " + imgUrl);
		

		if (imgUrl.equals("null")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
			Log.i(TAG, imgUrl + " img set - " + position);
		} else if(imgUrl != null) {
			Log.i("url check", imgUrl);

			Picasso.with(mContext).setIndicatorsEnabled(true);
			Picasso.with(mContext).load(imgUrl)
			.placeholder(R.drawable.avatar_empty)
			.resize(88, 88).centerInside()
			.into(holder.userImageView);

		}
		
		// 2. Username label
		holder.nameLabel.setText(chatItem.getContent());
		
		//3. Subtitle label
		if(chatItem.getLastMessage() != null) {
			holder.chatSubtitle.setText(chatItem.getLastMessage().getMessage().toString());
		}
		
		
		//4. new msg num view
		holder.newMsgNumView.setVisibility(View.INVISIBLE);

		return convertView;
	}


	private static class ViewHolder {
		ImageView userImageView;
		TextView nameLabel;
	    TextView chatSubtitle;
	    TextView newMsgNumView;
	}

	public void refill(List<ChatItem> chatItems) {
		mChatItems.clear();
		mChatItems.addAll(chatItems);
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
