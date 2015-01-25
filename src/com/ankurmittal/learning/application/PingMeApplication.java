package com.ankurmittal.learning.application;

import android.app.Application;

import com.ankurmittal.learning.ChatListActivity;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class PingMeApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		 
		Parse.initialize(this, "lbCQLdZ8rXghMiZGCuEpIxvj88Pt5HhXqJ4D4PU0", "Yr8YMze4dltTmGbDTwBJ01iByOiaHC7OBnGHIz2e");
//		 PushService.setDefaultPushCallback(this, ChatListActivity.class
//		    		);
		    ParseInstallation.getCurrentInstallation().saveInBackground();
	}

}
