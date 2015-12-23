package com.ankurmittal.learning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.ankurmittal.learning.application.PingMeApplication.MyActivityLifecycleCallbacks;
import com.ankurmittal.learning.storage.TextMessageDataSource;

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



public class ChatDetailActivity extends Activity {
	private TextMessageDataSource mMessageDataSource;
	
	private final MyActivityLifecycleCallbacks mCallbacks = new MyActivityLifecycleCallbacks();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_detail);
		
		getApplication().registerActivityLifecycleCallbacks(mCallbacks);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(ChatDetailFragment.ARG_ITEM_ID));
			ChatDetailFragment fragment = new ChatDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.chat_detail_container, fragment).commit();
		}
		mMessageDataSource = new TextMessageDataSource(this);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		getApplication().unregisterActivityLifecycleCallbacks(mCallbacks);
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
		
		if(mMessageDataSource != null) {
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
