package com.ankurmittal.learning.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.ProfilePicDialogActivity;
import com.ankurmittal.learning.R;
import com.ankurmittal.learning.emojicon.EmojiconTextView;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.Utils;

public class ChatItemsAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "Chat Item Adapter";
	Rect finalBounds;
	Point globalOffset;

	Context mContext;
	String[] usernames;
	ArrayList<ChatItem> mChatItems;
	ArrayList<TextMessage> mLastMessages;
	ArrayList<Boolean> isTyping;
	TextMessageDataSource textMessageDataSource;
	TextMessage lastMessage = new TextMessage();
	ViewHolder holder;
	String imgUrl;
	View expandedImageView;
	int counter = 0;
	protected String userID;
	int temp;

	public ChatItemsAdapter(Context context, ArrayList<ChatItem> chatItems) {

		super(context, R.layout.chat_item, chatItems);
		mContext = context;
		mChatItems = chatItems;
		mLastMessages = new ArrayList<TextMessage>();
		isTyping = new ArrayList<Boolean>();
		textMessageDataSource = TextMessageDataSource.getInstance(mContext);
		textMessageDataSource.open();
		// getting last messages
		refreshSubtitles();
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

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
			holder.chatSubtitle = (EmojiconTextView) convertView
					.findViewById(R.id.subtitleView);
			holder.newMsgNumView = (TextView) convertView
					.findViewById(R.id.newMessageNumView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ChatItem chatItem = mChatItems.get(position);

		// 1. Image View
		imgUrl = chatItem.getImgUrl();
		if (imgUrl.equals("null")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
//			Log.i(TAG, imgUrl + " img set - " + position);
		} else if (imgUrl != null) {
//			Log.i("url check", imgUrl);
			Utils.loadUserImageByUrl(mContext, holder.userImageView, imgUrl);
		}

		holder.userImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// We can get the fragment manager
				FragmentActivity activity = (FragmentActivity) getContext();
				Intent i = new Intent(activity, ProfilePicDialogActivity.class);
				i.putExtra("imgUrlFrmAdapter", mChatItems.get(position).imgUrl);
				View sharedView = holder.userImageView;
				String transitionName = "dialogTransition";

				ActivityOptions transitionActivityOptions = ActivityOptions
						.makeSceneTransitionAnimation(activity, sharedView,
								transitionName);
				activity.startActivity(i, transitionActivityOptions.toBundle());

			}
		});
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
		// 4. subtitle status

		if (isTyping.get(position)) {
			// add a holo green typing subtitle
			holder.chatSubtitle.setText("Typing...");
			holder.chatSubtitle.setTextColor(mContext.getResources().getColor(android.R.color.holo_green_light));
			holder.newMsgNumView.setVisibility(View.INVISIBLE);
		} else {
			// do other stuff
			if (!lastMessage.getSenderId().equals(chatItem.id)) {
				// it is a sent message
//				Log.i("subtitle check", lastMessage.getMessageStatus());
				if (lastMessage.getMessage().length() > 20) {
					holder.chatSubtitle
							.setText("You: "
									+ lastMessage.getMessage().substring(0, 20)
									+ "...");
				} else {
					holder.chatSubtitle.setTextColor(mContext.getResources()
							.getColor(android.R.color.black));
					holder.chatSubtitle.setText("You: "
							+ lastMessage.getMessage());
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
				holder.chatSubtitle.setTextColor(mContext.getResources()
						.getColor(android.R.color.black));
				if (lastMessage.getMessage().length() > 20) {
					holder.chatSubtitle.setText(lastMessage.getMessage()
							.substring(0, 20) + "...");
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
		}

		return convertView;
	}

	private static class ViewHolder {
		ImageView userImageView;
		ImageView chatSubtitleStatus;
		TextView nameLabel;
		EmojiconTextView chatSubtitle;
		TextView newMsgNumView;
	}

	public void refill(List<ChatItem> chatItems) {
		mChatItems.clear();
		mChatItems.addAll(chatItems);
		mLastMessages.clear();
		try {
			refreshSubtitles();
		} finally {
			notifyDataSetChanged();
		}

	}

	private void refreshSubtitles() {
		counter = 0;
		if (mChatItems != null && mChatItems.size() > 0) {
			for (ChatItem chatItem : mChatItems) {
				isTyping.add(false);
				mLastMessages.add(counter,
						textMessageDataSource.getLastMessageFrom(chatItem.id));
				counter++;
			}
		}
	}

	public void changeSubtitleToTyping(final int index) {
		
		isTyping.set(index, true);
		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					// update TextView here!
					isTyping.set(index, false);
					((AppCompatActivity) mContext)
							.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									notifyDataSetChanged();
								}
							});
				} catch (InterruptedException e) {
					Log.e("DEBUG THREAD INTERRUPTED", e.getMessage());
				}
			}
		};
		notifyDataSetChanged();
		t.start();
	}

}
