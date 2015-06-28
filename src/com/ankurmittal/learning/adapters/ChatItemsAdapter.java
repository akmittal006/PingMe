//package com.ankurmittal.learning.adapters;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Environment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.ankurmittal.learning.R;
//import com.ankurmittal.learning.storage.ChatItem;
//import com.ankurmittal.learning.util.CustomTarget;
//import com.ankurmittal.learning.util.ParseConstants;
//import com.parse.ParseUser;
//import com.squareup.picasso.Picasso;
//
//public class ChatItemsAdapter extends ArrayAdapter<ChatItem> {
//	
//	private static final String TAG = "Chat Item Adapter";
//
//	Context mContext;
//	String[] usernames;
//	ArrayList<ChatItem> mChatItems;
//	protected String userID;
//
//	public ChatItemsAdapter(Context context, ArrayList<ChatItem> chatItems) {
//
//		super(context, R.layout.fragment_friends, chatItems);
//		mContext = context;
//		mChatItems = chatItems;
//		usernames = new String[mChatItems.size()];
//		int i = 0;
//		//getting username array
//		for (ChatItem chatItem : mChatItems) {
//			//usernames[i] = chatItem.;
//			i++;
//		}
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder holder;
//
//		if (convertView == null) {
//			convertView = LayoutInflater.from(mContext).inflate(
//					R.layout.frnd_item, null);
//			holder = new ViewHolder();
//			holder.userImageView = (ImageView) convertView
//					.findViewById(R.id.userImageView);
//			holder.nameLabel = (TextView) convertView
//					.findViewById(R.id.usernameTextView);
//			convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
//
//		ChatItem chatItem = mChatItems.get(position);
//		String imgUrl = chatItem.getString(ParseConstants.KEY_PROFILE_IMAGE);
//		
//		
//		 if (chatItem.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null ) {
//			imgUrl = chatItem.getParseFile(ParseConstants.KEY_PROFILE_IMAGE).getUrl();
//			Log.d(TAG, imgUrl);
//		} else if (imgUrl == null){
//			Log.d(TAG, "null image url "+ position + chatItem.getUsername());
//			holder.nameLabel.setText(chatItem.getUsername());
//			holder.userImageView.setImageResource(R.drawable.avatar_empty);
//			return convertView;
//		}
//		
//
//		if (imgUrl.equals("null")) {
//			holder.userImageView.setImageResource(R.drawable.avatar_empty);
//			Log.i(TAG, imgUrl + " img set - " + position);
//		} else if(imgUrl != null) {
//			Log.i("url check", imgUrl);
//
//			Picasso.with(mContext).setIndicatorsEnabled(true);
//			CustomTarget target = new CustomTarget();
//			//Log.d("frnds adpater",user.getString("UserId"));
//			target.setTargetHash(chatItem.getString("UserId"));
//			if (isExternalStorageAvailable()) {
//
//				// 1. Get the external storage directory
//				String appName = "PingMe";
//				File mediaStorageDir = new File(
//						Environment
//								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//						appName);
//
//				String path = mediaStorageDir.getPath() + File.separator;
//
//				File file = new File(path + "/" + chatItem.getString("UserId") + ".jpg");
//				if (file.exists()) {
//					Log.i(TAG +" --image loaded from mobile", Uri.fromFile(file).toString());
//					Picasso.with(mContext).load(Uri.fromFile(file))
//							.placeholder(R.drawable.avatar_empty)
//							.resize(96, 96).centerInside()
//							.into(holder.userImageView);
//				} else {
//					Log.i(TAG +" --image loaded from net", Uri.fromFile(file).toString());
//					Picasso.with(mContext).load(imgUrl)
//							.placeholder(R.drawable.avatar_empty)
//							.resize(75, 75).centerInside()
//							.into(holder.userImageView);
//					Picasso.with(mContext).load(imgUrl).into(target);
//				}
//
//			}
//
//		}
//
//		holder.nameLabel.setText(chatItem.getUsername());
//
//		return convertView;
//	}
//
//
//	private static class ViewHolder {
//		ImageView userImageView;
//		TextView nameLabel;
//		// TextView frndLabel;
//	}
//
//	public void refill(List<ParseUser> friends) {
//		mChatItems.clear();
//		mChatItems.addAll(friends);
//		notifyDataSetChanged();
//	}
//
//	private boolean isExternalStorageAvailable() {
//		String state = Environment.getExternalStorageState();
//
//		if (state.equals(Environment.MEDIA_MOUNTED)) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//}
