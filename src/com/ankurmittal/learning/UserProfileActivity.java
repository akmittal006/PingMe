package com.ankurmittal.learning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.ankurmittal.learning.picCropper.CropImage;
import com.ankurmittal.learning.util.FileHelper;
import com.ankurmittal.learning.util.ParseConstants;
import com.ankurmittal.learning.util.Utils;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

public class UserProfileActivity extends AppCompatActivity {

	protected static final int GALLERY_INTENT_CALLED = 50;
	protected static final int GALLERY_KITKAT_INTENT_CALLED = 51;
	private static final int REQUEST_CODE_CROP_IMAGE = 56;

	Toolbar mToolBar;
	ParseUser currentUser;
	ImageView mProfilePicView;
	FloatingActionButton fabEdit;
	CoordinatorLayout coordinatorLayout;
	boolean success = false;

	File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		currentUser = ParseUser.getCurrentUser();
		
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.profile_coordinatorLayout);
		fabEdit = (FloatingActionButton) findViewById(R.id.main_fabEdit);
		mProfilePicView = (ImageView) findViewById(R.id.main_backdrop);
		mToolBar = (Toolbar) findViewById(R.id.main_toolbar);
		mToolBar.setTitle(currentUser.getString(ParseConstants.KEY_NAME));
		setSupportActionBar(mToolBar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		//set image view to saved pic 
		//on edit pic 
		////upload pic
		////save it locally
		////update image View
		Picasso.with(this).setIndicatorsEnabled(true);
		File downloadedPic = new File(Utils.getAppPath() + "/" + currentUser.getObjectId() + ".jpg");
		if(downloadedPic.exists()) {
			Picasso.with(this).invalidate(downloadedPic);
			Picasso.with(this)
			.load(downloadedPic).placeholder(R.drawable.avatar_empty).fit()
			.into(mProfilePicView);
		} else if(currentUser.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null){
			Picasso.with(this)
			.load(currentUser.getParseFile(ParseConstants.KEY_PROFILE_IMAGE).getUrl()).placeholder(R.drawable.avatar_empty).fit()
			.into(mProfilePicView);
		}
		

		fabEdit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.user_profile, menu);
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

			Cursor cursor = getContentResolver().query(originalUri, projection,
					null, null, null);
			cursor.moveToFirst();

			Log.d("profile activity", DatabaseUtils.dumpCursorToString(cursor));

			int columnIndex = cursor.getColumnIndex(projection[0]);
			String picturePath = cursor.getString(columnIndex); // returns null
			cursor.close();
			Log.d("profile activity", picturePath);

			Intent intent = new Intent(this, CropImage.class);

			// tell CropImage activity to look for image to crop
			intent.putExtra(CropImage.IMAGE_PATH, picturePath);

			// allow CropImage activity to rescale image
			intent.putExtra(CropImage.SCALE, true);

			// if the aspect ratio is fixed to ratio 3/2
			intent.putExtra(CropImage.ASPECT_X, 1);
			intent.putExtra(CropImage.ASPECT_Y, 1);

			// start activity CropImage with certain request code and listen
			// for result
			startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);

			Log.e("path", picturePath); // use selectedImagePath

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

			Intent intent = new Intent(this, CropImage.class);

			// tell CropImage activity to look for image to crop
			intent.putExtra(CropImage.IMAGE_PATH, selectedImagePath);

			// allow CropImage activity to rescale image
			intent.putExtra(CropImage.SCALE, true);

			// if the aspect ratio is fixed to ratio 3/2
			intent.putExtra(CropImage.ASPECT_X, 1);
			intent.putExtra(CropImage.ASPECT_Y, 1);

			// start activity CropImage with certain request code and listen
			// for result
			startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);

		} else if (requestCode == REQUEST_CODE_CROP_IMAGE) {
			String path = data.getStringExtra(CropImage.IMAGE_PATH);

			// if nothing received
			if (path == null) {
				return;
			}
			file = new File(path);

			byte[] fileBytes = FileHelper.getByteArrayFromFile(this, path);
			ParseFile profileImage = new ParseFile(currentUser.getObjectId()
					+ ".png", fileBytes);
			profileImage.saveInBackground(new ProgressCallback() {

				@Override
				public void done(Integer percent) {
					if (percent == 100) {
						UserProfileActivity.this.uploaded();
					}
				}

			});

			currentUser.put(ParseConstants.KEY_PROFILE_IMAGE, profileImage);
		}

	}

	private Uri getUri() {
		String state = Environment.getExternalStorageState();
		if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
			return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}

	private void uploaded() {
		
		currentUser.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					try {
						Target profilePicTarget = new Target() {
							
							@Override
							public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
								Utils.saveAndLoadProfilePic(bitmap,currentUser.getObjectId(),mProfilePicView);
								success = true;
							}
							@Override
							public void onBitmapFailed(Drawable arg0) {
							}
							@Override
							public void onPrepareLoad(Drawable arg0) {
							}
						};
						mProfilePicView.setTag(profilePicTarget);
						
						Picasso.with(UserProfileActivity.this).load(file).placeholder(R.drawable.profile_empty)
						.into(profilePicTarget);
					} finally {
						Snackbar snackbar = Snackbar
						        .make(coordinatorLayout, "Profile photo updated.", Snackbar.LENGTH_LONG);
						snackbar.show();
					}
					triggerFriendsUpdate();
					
				} else {
					Snackbar snackbar = Snackbar
					        .make(coordinatorLayout, "Sorry! error in uploading photo.", Snackbar.LENGTH_LONG);
					snackbar.show();
				}

			}
		});
	}

	private void triggerFriendsUpdate() {
		Log.e("profile activity", "Triggered frnd update");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("id", ParseUser.getCurrentUser().getObjectId());
		ParseCloud.callFunctionInBackground("triggerUpdateInFriends", params,
				new FunctionCallback<String>() {

					@Override
					public void done(String arg0, ParseException e) {
						if (e == null) {
							Log.e("profile activity", arg0);
						} else {
							Log.e("profile activity", e.getMessage());
						}

					}
				});
	}

}
