package com.ankurmittal.learning.adapters;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.util.Constants;
import com.parse.ParseUser;

public class TextMessageAdapter extends ArrayAdapter<TextMessage> {

	Context mContext;
	String[] usernames;
	ArrayList<TextMessage> mMessages;

	public TextMessageAdapter(Context context, ArrayList<TextMessage> messages) {
		super(context, R.id.chatListView, messages);
		mContext = context;
		mMessages = messages;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		TextMessage message = mMessages.get(position);
		// if (convertView == null) {

		if (message.getReceiverName().equals("pingMe")) {
			
			//neutral message
			
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_item_neutral, null);
			holder = new ViewHolder();
			holder.messageView = (TextView) convertView
					.findViewById(R.id.messageTextView);
			convertView.setTag(holder);
			holder.messageView.setText(message.getMessage());
			convertView.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					return true;
				}
			});
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});

		} else if (message.getSenderName().equals(
				ParseUser.getCurrentUser().getUsername())) {
			
			//sent message

			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_item_sent, null);
			holder = new ViewHolder();
			holder.messageView = (TextView) convertView
					.findViewById(R.id.messageTextView);
			holder.timeLabel = (TextView) convertView
					.findViewById(R.id.createdAtTextView);
			holder.sentView = (ImageView) convertView.findViewById(R.id.sentStatusView);
			convertView.setTag(holder);
			holder.messageView.setText(message.getMessage());
			if (message.mMessageStatus.equals(Constants.MESSAGE_STATUS_SENT)) {
				holder.messageView.setTypeface(null, Typeface.NORMAL);
				holder.sentView.setImageResource(R.drawable.ic_action_delivered);
			} else if (message.mMessageStatus.equals(Constants.MESSAGE_STATUS_DELIVERED)) {
				holder.messageView.setTypeface(null, Typeface.NORMAL);
				holder.sentView.setImageResource(R.drawable.ic_action_sent);
			} else if (message.mMessageStatus.equals(Constants.MESSAGE_STATUS_READ)) {
				holder.messageView.setTypeface(null, Typeface.NORMAL);
				holder.sentView.setImageResource(R.drawable.ic_action_read);
			} else {
				holder.messageView.setTypeface(null, Typeface.ITALIC);
				holder.sentView.setImageResource(R.drawable.ic_action_wait);
			}
			holder.timeLabel.setText(getTimeFromDate(message.getCreatedAt()));
		} else {
			
			//received message
			
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.chat_item_received, null);
			holder = new ViewHolder();
			holder.messageView = (TextView) convertView
					.findViewById(R.id.messageTextView);
			holder.timeLabel = (TextView) convertView
					.findViewById(R.id.createdAtTextView);
			convertView.setTag(holder);
			holder.messageView.setText(message.getMessage());
			if (message.mMessageStatus.equals(Constants.MESSAGE_STATUS_SENT)) {
				holder.messageView.setTypeface(null, Typeface.NORMAL);
			} else {
				holder.messageView.setTypeface(null, Typeface.ITALIC);
			}
			holder.timeLabel.setText(getTimeFromDate(message.getCreatedAt()));
		}
		// } else {
		// Log.d("adapter", message.getSenderName());
		// holder = (ViewHolder) convertView.getTag();
		// }
		



		return convertView;
	}

	private String getTimeFromDate(Date date) {
		String strDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		strDate = dateFormat.format(date);
		return strDate;
	}

	private static class ViewHolder {
		TextView messageView;
		TextView timeLabel;
		ImageView sentView;
	}

	public void refill(ArrayList<TextMessage> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}
}
