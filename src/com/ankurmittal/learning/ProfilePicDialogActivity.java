package com.ankurmittal.learning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ProfilePicDialogActivity extends Activity {
	
	private String url;
	ImageView profilePicView;
	
	public ProfilePicDialogActivity(String url) {
		this.url = url;
	}
	
	public ProfilePicDialogActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_profile_pic_dialog);
		
		profilePicView = (ImageView)findViewById(R.id.profileDialogview);
		Intent intent = getIntent();
		
		//getActionBar().hide();
		Log.e("remove it",intent.getStringExtra("imgUrlFrmAdapter"));
		Picasso.with(this).load(intent.getStringExtra("imgUrlFrmAdapter"))
		.placeholder(R.drawable.avatar_empty).fit().into(profilePicView);
	}
	
	

	@Override
	public View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		
		
		return super.onCreateView(parent, name, context, attrs);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_pic_dialog, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
