package com.ankurmittal.learning.application;

import android.app.Application;

import com.ankurmittal.learning.util.ParseConstants;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class PingMeApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		
		// Enable Crash Reporting
		ParseCrashReporting.enable(this);

		Parse.initialize(this, "lbCQLdZ8rXghMiZGCuEpIxvj88Pt5HhXqJ4D4PU0", "Yr8YMze4dltTmGbDTwBJ01iByOiaHC7OBnGHIz2e");

		    ParseInstallation.getCurrentInstallation().saveInBackground();
	}
	public static void updateParseInstallation(ParseUser user) {
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
		installation.saveInBackground();
	}
}
