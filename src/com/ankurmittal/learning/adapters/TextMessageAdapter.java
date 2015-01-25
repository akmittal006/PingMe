package com.ankurmittal.learning.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.storage.TextMessage;
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
		//if (convertView == null) {
			Log.d("adapter", message.getSenderName());
			if(message.getSenderName().equals(ParseUser.getCurrentUser().getUsername())) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.chat_item_sent, null);
				holder = new ViewHolder();
				holder.messageView = (TextView) convertView
						.findViewById(R.id.messageTextView);
				holder.timeLabel = (TextView)convertView.findViewById(R.id.createdAtTextView);
				convertView.setTag(holder);
				holder.messageView.setText(message.getMessage());
			} else{
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.chat_item_received, null);
				holder = new ViewHolder();
				holder.messageView = (TextView) convertView
						.findViewById(R.id.messageTextView);
				holder.timeLabel = (TextView)convertView.findViewById(R.id.createdAtTextView);
				convertView.setTag(holder);
				holder.messageView.setText(message.getMessage());
			}
//		} else {
//			Log.d("adapter", message.getSenderName());
//			holder = (ViewHolder) convertView.getTag();
//		}
		holder.messageView.setText(message.getMessage());
		holder.timeLabel.setText(message.getCreatedAtString());
		return convertView;
	}

	private static class ViewHolder {
		TextView messageView;
		 TextView timeLabel;
	}
	

	public void refill(ArrayList<TextMessage> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}
}
