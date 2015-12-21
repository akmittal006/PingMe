package com.ankurmittal.learning.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.squareup.picasso.Picasso;

public class ChatItemsAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "Chat Item Adapter";

	Context mContext;
	String[] usernames;
	ArrayList<ChatItem> mChatItems;
	ArrayList<TextMessage> mLastMessages;
	TextMessageDataSource textMessageDataSource;
	TextMessage lastMessage = new TextMessage();
	ViewHolder holder;
	int counter = 0;
	protected String userID;
	int temp;

	public ChatItemsAdapter(Context context, ArrayList<ChatItem> chatItems) {

		super(context, R.layout.chat_item, chatItems);
		mContext = context;
		mChatItems = chatItems;

		mLastMessages = new ArrayList<TextMessage>();
		textMessageDataSource = new TextMessageDataSource(mContext);
		textMessageDataSource.open();

		// getting last messages
		refreshSubtitles();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		temp = position;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_item, null);
			holder = new ViewHolder();

			// add views to layout
			holder.userImageView = (ImageView) convertView
					.findViewById(R.id.userImageView);
			holder.chatSubtitleStatus = (ImageView) convertView
					.findViewById(R.id.sentStatusView);
			holder.nameLabel = (TextView) convertView
					.findViewById(R.id.usernameTextView);
			holder.chatSubtitle = (TextView) convertView
					.findViewById(R.id.subtitleView);
			holder.newMsgNumView = (TextView) convertView
					.findViewById(R.id.newMessageNumView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ChatItem chatItem = mChatItems.get(position);

		// 1. Image View
		String imgUrl = chatItem.getImgUrl();
		Log.d("chat item adapter", "img url- " + imgUrl);

		if (imgUrl.equals("null")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
			Log.i(TAG, imgUrl + " img set - " + position);
		} else if (imgUrl != null) {
			Log.i("url check", imgUrl);

			Picasso.with(mContext).setIndicatorsEnabled(true);
			Picasso.with(mContext).load(imgUrl)
					.placeholder(R.drawable.avatar_empty).resize(88, 88)
					.centerInside().into(holder.userImageView);

		}

		// 2. Username label
		holder.nameLabel.setText(chatItem.getContent());

		// 3. Subtitle label
		lastMessage = mLastMessages.get(position);
		if (lastMessage == null) {
			lastMessage = new TextMessage();
			lastMessage.setMessage("No MSgs!");
			lastMessage.setSenderId(chatItem.id);
			lastMessage.setMessageStatus(Constants.MESSAGE_STATUS_DELIVERED);
		}
		// Log.e("DEBUG",chatItem.getContent() + " " +
		// lastMessage.getMessage());
		// 4. subtitle status
		if (!lastMessage.getSenderId().equals(chatItem.id)) {
			// it is a sent message

			Log.i("subtitle check", lastMessage.getMessageStatus());
			if (lastMessage.getMessage().length() > 20) {
				holder.chatSubtitle.setText("You: "
						+ lastMessage.getMessage().substring(0, 20) + "...");
			} else {
				holder.chatSubtitle.setText("You: " + lastMessage.getMessage());
			}

			holder.newMsgNumView.setVisibility(View.INVISIBLE);
			String status = lastMessage.getMessageStatus();
			if (status.equals(Constants.MESSAGE_STATUS_DELIVERED)) {
				holder.chatSubtitleStatus
						.setImageResource(R.drawable.ic_action_send_now);
			} else if (status.equals(Constants.MESSAGE_STATUS_READ)) {
				holder.chatSubtitleStatus
						.setImageResource(R.drawable.ic_action_read);
			} else if (status.equals(Constants.MESSAGE_STATUS_PENDING)) {
				holder.chatSubtitleStatus
						.setImageResource(R.drawable.ic_action_time);
			} else {
				holder.chatSubtitleStatus.setVisibility(View.INVISIBLE);
			}

		} else {
			// received message

			if (lastMessage.getMessage().length() > 20) {
				holder.chatSubtitle.setText(lastMessage.getMessage().substring(
						0, 20)
						+ "...");
			} else {
				holder.chatSubtitle.setText(lastMessage.getMessage());
			}
			if (!lastMessage.getMessageStatus().equals(
					Constants.MESSAGE_STATUS_READ)) {
				holder.chatSubtitle.setTextColor(mContext.getResources()
						.getColor(android.R.color.holo_green_dark));
				holder.newMsgNumView.setText(chatItem.getNotReadMessages()
						.size() + "");
				holder.newMsgNumView.setVisibility(View.VISIBLE);
			} else {
				holder.newMsgNumView.setVisibility(View.INVISIBLE);
			}

			holder.chatSubtitleStatus.setVisibility(View.INVISIBLE);

		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView userImageView;
		ImageView chatSubtitleStatus;
		TextView nameLabel;
		TextView chatSubtitle;
		TextView newMsgNumView;
	}

	public void refill(List<ChatItem> chatItems) {
		mChatItems.clear();
		mChatItems.addAll(chatItems);
		mLastMessages.clear();
		// refreshing subtitles
		refreshSubtitles();
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

	private void refreshSubtitles() {
		counter = 0;
		if (mChatItems != null && mChatItems.size() > 0) {
			for (ChatItem chatItem : mChatItems) {

				// usernames[i] = chatItem.;
				mLastMessages.add(counter,
						textMessageDataSource.getLastMessageFrom(chatItem.id));
				counter++;
			}

		}
		if (counter == mChatItems.size()) {
			textMessageDataSource.close();
		}
	}
}
