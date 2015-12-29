package com.ankurmittal.learning.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.storage.NavListItem;
import com.ankurmittal.learning.util.TypefaceSpan;

public class NavListAdapter extends ArrayAdapter<NavListItem> {

	Context mContext;
	ArrayList<NavListItem> mNavOptions;
	// ArrayList<ParseUser> mFriends;
	protected String userID;

	public NavListAdapter(Context context, ArrayList<NavListItem> options) {

		super(context, R.id.navDrawerListView, options);
		mContext = context;
		mNavOptions = options;
		// mNavOptions = new String[mFriends.size()];
		// int i = 0;
		// for (ParseUser user : mFriends) {
		// mNavOptions[i] = user.getUsername();
		// i++;
		// }
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.nav_drawer_item, null);
			holder = new ViewHolder();
			holder.optionIconView = (ImageView) convertView
					.findViewById(R.id.navIconView);
			holder.optionView = (TextView) convertView
					.findViewById(R.id.navOptionView);
			holder.optionNumView = (TextView) convertView
					.findViewById(R.id.navOptionNumView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		NavListItem option = mNavOptions.get(position);

		if (option.getOption().equals("Messages")) {
			if (option.isSelected()) {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_msgs_selected));
			} else {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_msgs));
			}
			Log.i("NAv check", "setting SIMPLE icon");
			
		} else if (option.getOption().equals("Friends")) {
			if (option.isSelected()) {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_friends_selected));
			} else {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_friends));
			}
			
		} else if (option.getOption().equals("Profile")) {
			if (option.isSelected()) {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_profile_selected));
			} else {
				holder.optionIconView.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.nav_profile));
			}
			
		}

		SpannableString s = new SpannableString(option.getOption());
		s.setSpan(new TypefaceSpan(mContext, "LOBSTERTWO-REGULAR.OTF"), 0,
				s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		holder.optionView.setText(s);
		if (option.isSelected()) {
			holder.optionView.setTextColor(mContext.getResources().getColor(
					R.color.ping_pink));
		} else {
			holder.optionView.setTextColor(mContext.getResources().getColor(
					android.R.color.black));
		}
		holder.optionNumView.setVisibility(View.INVISIBLE);

		return convertView;
	}

	private static class ViewHolder {
		ImageView optionIconView;
		TextView optionView;
		TextView optionNumView;
		// TextView frndLabel;
	}

	public void refill(ArrayList<NavListItem> options) {
		Log.e("nav check in adapter","refill calld " + options.size());
		mNavOptions.clear();
		mNavOptions.addAll(options);
		notifyDataSetChanged();
	}

}
