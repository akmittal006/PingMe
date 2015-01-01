package com.ankurmittal.learning.application;

import android.app.Application;

import com.parse.Parse;

public class PingMeApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		 
		Parse.initialize(this, "lbCQLdZ8rXghMiZGCuEpIxvj88Pt5HhXqJ4D4PU0", "Yr8YMze4dltTmGbDTwBJ01iByOiaHC7OBnGHIz2e");
		
	}

}
