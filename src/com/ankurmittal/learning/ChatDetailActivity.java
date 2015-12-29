package com.ankurmittal.learning;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankurmittal.learning.application.PingMeApplication.MyActivityLifecycleCallbacks;
import com.ankurmittal.learning.fragments.ChatDetailFragment;
import com.ankurmittal.learning.storage.FriendsDataSource;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Utils;

/**
 * An activity representing a single Chat detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link ChatListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ChatDetailFragment}.
 * 
 * 
 */

public class ChatDetailActivity extends AppCompatActivity {
	private TextMessageDataSource mMessageDataSource;
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_detail);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// CoordinatorLayout coordinatorLayout =
		// (CoordinatorLayout)findViewById(R.id.chatDetail_coordinatorLayout);
		toolbar = (Toolbar) findViewById(R.id.chatDetail_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			ImageView iconView = (ImageView) findViewById(R.id.chatDetail_toolbar_iconView);
			TextView titleView = (TextView) findViewById(R.id.chatDetail_toolbar_title);
			String id = getIntent().getStringExtra(
					ChatDetailFragment.ARG_ITEM_ID);
			FriendsDataSource frndsDataSource = new FriendsDataSource(this);
			frndsDataSource.open();
			String imgUrl = frndsDataSource.getImageUrlFromId(this, id);
			String title = frndsDataSource.getNameFromId(this, id);
			frndsDataSource.close();
			titleView.setText(title);
			if (imgUrl.equals("null")) {
				iconView.setImageResource(R.drawable.avatar_empty);
			} else if (imgUrl != null) {
				Utils.loadUserImageByUrl(this, iconView, imgUrl);
			}
			mMessageDataSource = new TextMessageDataSource(this);
			arguments.putString(ChatDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(ChatDetailFragment.ARG_ITEM_ID));
			ChatDetailFragment fragment = new ChatDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.chat_detail_container, fragment).commit();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		super.onPause();

		if (mMessageDataSource != null) {
			mMessageDataSource.open();
			mMessageDataSource.close();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this,
					new Intent(this, ChatListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
