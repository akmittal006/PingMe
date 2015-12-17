package com.ankurmittal.learning;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.util.FileHelper;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends Activity {

	protected static final int GALLERY_INTENT_CALLED = 50;
	protected static final int GALLERY_KITKAT_INTENT_CALLED = 51;
	private ImageView profileView;
	private ImageButton editProfileImageBtn;
	private TextView usernameView;
	//private Button saveProfileBtn;
	private ParseUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		// initialize components
		profileView = (ImageView) findViewById(R.id.profileImageView);
		editProfileImageBtn = (ImageButton) findViewById(R.id.editProfileImageButton);
		usernameView = (TextView) findViewById(R.id.usernameView);
		//saveProfileBtn = (Button) findViewById(R.id.saveProfileButton);

		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			Log.d("ProfileACtivity", currentUser.getUsername());
			usernameView.setText(currentUser.getUsername());
			if (currentUser.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null) {
				Uri uri = Uri.parse(currentUser.getParseFile(
						ParseConstants.KEY_PROFILE_IMAGE).getUrl());
				Log.d("profile activity", uri.toString());
				Picasso.with(this)
						.load(currentUser.getParseFile(
								ParseConstants.KEY_PROFILE_IMAGE).getUrl())
						.placeholder(R.drawable.profile_empty).resize(756, 550)
						.centerCrop().into(profileView);
			}

		}

		// start image intent on click on image
		editProfileImageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// open pic chooser
				if (Build.VERSION.SDK_INT < 19) {
					Log.d("profile activity", "started");
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(
							Intent.createChooser(intent, "Select Picture"),
							GALLERY_INTENT_CALLED);
				} else {
					Log.d("profile activity", "started2");
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
				}

			}

		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("profile activity", "rece2" + resultCode + Activity.RESULT_OK);
		if (resultCode != Activity.RESULT_OK)
			return;
		if (null == data)
			return;
		Uri originalUri = null;
		if (requestCode == GALLERY_INTENT_CALLED) {
			originalUri = data.getData();
				
			 String[] projection = { MediaStore.Images.Media.DATA };
			
			 Cursor cursor = getContentResolver().query(originalUri, projection, null,
			 null, null);
			 cursor.moveToFirst();
			
			 Log.d("profile activity", DatabaseUtils.dumpCursorToString(cursor));
			
			 int columnIndex = cursor.getColumnIndex(projection[0]);
			 String picturePath = cursor.getString(columnIndex); // returns null
			 cursor.close();
			 Log.d("profile activity", picturePath);
			 File file = new File(picturePath);
			 
			 Log.e("path", picturePath); // use selectedImagePath
				Picasso.with(this).load(Uri.fromFile(file))
						.placeholder(R.drawable.profile_empty).resize(756, 550)
						.centerCrop().into(profileView);

				byte[] fileBytes = FileHelper.getByteArrayFromFile(this,
						picturePath);
				Log.d("profile activity", "starting to upload");
				ParseFile profileImage = new ParseFile(currentUser.getObjectId()
						+ ".png", fileBytes);
				Log.d("profile activity", "parse file created...");
				currentUser.put(ParseConstants.KEY_PROFILE_IMAGE, profileImage);
				Log.d("profile activity", "current user updated ...");
				currentUser.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						// TODO Auto-generated method stub
						if (e == null) {
							Log.d("profile activity", "yayy uploaded");
							Toast.makeText(ProfileActivity.this,
									"Profile photo updated", Toast.LENGTH_SHORT)
									.show();
							triggerFriendsUpdate();
						} else {
							Log.d("profile activity", "aww not uploaded");
							Toast.makeText(ProfileActivity.this,
									"Unable to update profile photo",
									Toast.LENGTH_SHORT).show();
						}

					}
				});
			 
			 
			 
		} else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
			originalUri = data.getData();
			final int takeFlags = data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			// Check for the freshest data.
			getContentResolver().takePersistableUriPermission(originalUri,
					takeFlags);

			/*
			 * now extract ID from Uri path using getLastPathSegment() and then
			 * split with ":" then call get Uri to for Internal storage or
			 * External storage for media I have used getUri()
			 */

			Log.d("profile activity", "received2");
			Log.d("profile activity", " " + originalUri);

			String id = originalUri.getLastPathSegment().split(":")[1];
			final String[] imageColumns = { MediaStore.Images.Media.DATA };
			final String imageOrderBy = null;

			Uri uri = getUri();
			String selectedImagePath = "path";

			@SuppressWarnings("deprecation")
			Cursor imageCursor = managedQuery(uri, imageColumns,
					MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);

			if (imageCursor.moveToFirst()) {
				selectedImagePath = imageCursor.getString(imageCursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
			}
			Log.e("path", selectedImagePath); // use selectedImagePath
			Picasso.with(this).load(Uri.fromFile(new File(selectedImagePath)))
					.placeholder(R.drawable.profile_empty).resize(756, 550)
					.centerCrop().into(profileView);

			byte[] fileBytes = FileHelper.getByteArrayFromFile(this,
					selectedImagePath);
			Log.d("profile activity", "starting to upload");
			ParseFile profileImage = new ParseFile(currentUser.getObjectId()
					+ ".png", fileBytes);
			Log.d("profile activity", "parse file created...");
			currentUser.put(ParseConstants.KEY_PROFILE_IMAGE, profileImage);
			Log.d("profile activity", "current user updated ...");
			currentUser.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					// TODO Auto-generated method stub
					if (e == null) {
						Log.d("profile activity", "yayy uploaded");
						Toast.makeText(ProfileActivity.this,
								"Profile photo updated", Toast.LENGTH_SHORT)
								.show();
						triggerFriendsUpdate();
					} else {
						Log.d("profile activity", "aww not uploaded");
						Toast.makeText(ProfileActivity.this,
								"Unable to update profile photo",
								Toast.LENGTH_SHORT).show();
					}

				}
			});
		}

		// By using this method get the Uri of Internal/External Storage for
		// Media

	}

	private Uri getUri() {
		String state = Environment.getExternalStorageState();
		if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
			return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	//
	// if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
	// && data != null && data.getData() != null) {
	//

	//
	// // Bitmap bitmap = MediaStore.Images.Media.getBitmap(
	// // getContentResolver(), uri);
	// // // Log.d(TAG, String.valueOf(bitmap));
	// //
	// // ImageView imageView = (ImageView)
	// // findViewById(R.id.profileImageView);
	// // imageView.setImageBitmap(bitmap);
	//
	// }
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
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
	
	private void triggerFriendsUpdate() {
		Log.e("profile activity", "Triggered frnd update");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id", ParseUser.getCurrentUser().getObjectId());
		ParseCloud.callFunctionInBackground("triggerUpdateInFriends", params, new FunctionCallback<String>() {

			@Override
			public void done(String arg0, ParseException e) {
				// TODO Auto-generated method stub
				if(e == null) {
					Log.e("profile activity", arg0);
				} else {
					Log.e("profile activity", e.getMessage());
				}
				
			}
		});
	}
	
}
