package com.ankurmittal.learning.adapters;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.util.MD5Util;
import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class FrndReqAdapter extends ArrayAdapter<ParseUser> {

	protected Context mContext;
	protected List<ParseUser> mUsers;
	protected List<ParseObject> mReqs;
	protected ParseUser currentUser;
	protected ProgressBar mProgressBar;
	protected ParseUser user;

	public FrndReqAdapter(Context context, List<ParseUser> users,
			List<ParseObject> reqs, ProgressBar progBar) {
		super(context, R.layout.frnd_req_item, users);
		mContext = context;
		mUsers = users;
		mReqs = reqs;
		mProgressBar = progBar;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.frnd_req_item, null);
			holder = new ViewHolder();
			holder.userImageView = (ImageView) convertView
					.findViewById(R.id.frndReqSenderImageView);
			holder.nameLabel = (TextView) convertView
					.findViewById(R.id.frndReqSenderTextView);
			holder.mAcceptButton = (Button) convertView
					.findViewById(R.id.accept_frnd_req_btn);
			holder.mCancelButton = (Button) convertView
					.findViewById(R.id.cancel_frnd_req_btn);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		user = mUsers.get(position);
		currentUser = ParseUser.getCurrentUser();
		String email = user.getEmail().toLowerCase();

		if (email.equals("")) {
			holder.userImageView.setImageResource(R.drawable.avatar_empty);
		} else {
			String hash = MD5Util.md5Hex(email);
			String gravatarUrl = "http://www.gravatar.com/avatar/" + hash
					+ "?s=204&d=404";
			Picasso.with(mContext).load(gravatarUrl)
					.placeholder(R.drawable.avatar_empty)
					.into(holder.userImageView);
		}

		holder.nameLabel.setText(user.getUsername());

		// ///////if request is accepted/////////////
		holder.mAcceptButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("clicked", "accepted " + position);
				//ParseObject frndReq = mReqs.get(position);
				
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("FrndReqSentBy", user.getObjectId());
				params.put("reqObjId", mReqs.get(position).getObjectId());
				ParseCloud.callFunctionInBackground("frndReqAccepted", params,
						new FunctionCallback<String>() {

							@Override
							public void done(final String message, ParseException e) {
								// TODO Auto-generated method stub
								if(e==null) {
									
									mReqs.get(position).deleteInBackground(new DeleteCallback() {
										
										@Override
										public void done(ParseException e) {
											// TODO Auto-generated method stub
											if(e == null) {
												mUsers.remove(position);
												mReqs.remove(position);
												notifyDataSetChanged();
												Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
											} else {
												Toast.makeText(mContext, "frnd req " + e.getMessage(),  Toast.LENGTH_LONG).show();
											}
										}
									});
								} else {
									e.printStackTrace();
									Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
								}
							}
						});

			}
		});

		// ///////if cancel button is pressed/////////////
		holder.mCancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// remove from frontend
				mUsers.remove(position);
				Log.d("frndReqRemoved", "frndAdded" + position);
				notifyDataSetChanged();

				// remove at backend
				mProgressBar.setVisibility(View.VISIBLE);
				ParseObject req = mReqs.get(position);
				req.deleteInBackground(new DeleteCallback() {

					@Override
					public void done(ParseException arg0) {
						mProgressBar.setVisibility(View.INVISIBLE);
						mReqs.remove(position);
						Log.d("checking", "" + mUsers.size());
					}

				});

			}

		});

		return convertView;
	}

	private static class ViewHolder {
		ImageView userImageView;
		TextView nameLabel;
		Button mAcceptButton;
		Button mCancelButton;
		// TextView frndLabel;
	}

	public void refill(List<ParseUser> users) {
		mUsers.clear();
		mUsers.addAll(users);
		notifyDataSetChanged();
	}

}
