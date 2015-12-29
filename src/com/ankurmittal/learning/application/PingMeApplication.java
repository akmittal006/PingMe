package com.ankurmittal.learning.application;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.ankurmittal.learning.ChatDetailActivity;
import com.ankurmittal.learning.ChatListActivity;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class PingMeApplication extends Application {

	public static Socket mSocket;
	

	@Override
	public void onCreate() {
		super.onCreate();
		
		Parse.enableLocalDatastore(this);

		// Enable Crash Reporting
		ParseCrashReporting.enable(this);

		Parse.initialize(this, "lbCQLdZ8rXghMiZGCuEpIxvj88Pt5HhXqJ4D4PU0",
				"Yr8YMze4dltTmGbDTwBJ01iByOiaHC7OBnGHIz2e");

		ParseInstallation.getCurrentInstallation().saveInBackground();
			Log.e("APP DEBUG", "USER NOT NULL");
			try {
				mSocket = IO.socket("http://pingme-lbcqld-2172.herokuapp.com/");
			} catch (URISyntaxException e) {
			}
			

		
		// Enable Local Datastore.
	}
	
	

	public static void updateParseInstallation(ParseUser user) {
		ParseInstallation installation = ParseInstallation
				.getCurrentInstallation();
		installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
		installation.saveInBackground();
	}

	public static class MyActivityLifecycleCallbacks implements
			ActivityLifecycleCallbacks {

		@Override
		public void onActivityCreated(Activity activity,
				Bundle savedInstanceState) {
			Log.i(activity.getClass().getSimpleName(), "onCreate(Bundle)");
		}

		@Override
		public void onActivityStarted(Activity activity) {
			Log.i(activity.getClass().getSimpleName(), "onStart()");
		}

		@Override
		public void onActivityResumed(Activity activity) {
			Log.i(activity.getClass().getSimpleName(), "onResume()");
			String activityName = activity.getClass().getSimpleName();
			if(activityName.equals(ChatListActivity.class.getSimpleName()) || activityName.equals(ChatDetailActivity.class.getSimpleName())) {
				if(ParseUser.getCurrentUser() != null) {
					Log.e("DEBUG", "Socket connected");
					mSocket.connect();
					mSocket.emit(Constants.EVENT_NEW_USER, ParseUser.getCurrentUser().getObjectId());
				}
				
			}
		}

		@Override
		public void onActivityPaused(Activity activity) {
			Log.i(activity.getClass().getSimpleName(), "onPause()");
			String activityName = activity.getClass().getSimpleName();
			if(activityName.equals(ChatListActivity.class.getSimpleName()) || activityName.equals(ChatDetailActivity.class.getSimpleName())) {
				if(ParseUser.getCurrentUser() != null) {
					Log.e("DEBUG", "Socket DISconnected");
					mSocket.disconnect();
				}
				
			}
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity,
				Bundle outState) {
			Log.i(activity.getClass().getSimpleName(),
					"onSaveInstanceState(Bundle)");
		}

		@Override
		public void onActivityStopped(Activity activity) {
			Log.i(activity.getClass().getSimpleName(), "onStop()");
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			Log.i(activity.getClass().getSimpleName(), "onDestroy()");
		}
	}

}
