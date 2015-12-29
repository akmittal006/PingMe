package com.ankurmittal.learning;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ankurmittal.learning.application.PingMeApplication;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

	EditText mUsername;
	EditText mPassword;
	Button mLoginButton;
	Button mFacebookLoginButton;
	ScrollView mFormView;
	TextView mSignUpTextView;
	ProgressBar mProgressBar;
	boolean mAuthTask = true;

	public static final String TAG = LoginActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getSupportActionBar().hide();

		// handling signup text view
		mSignUpTextView = (TextView) findViewById(R.id.usernameTextView);
		mSignUpTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(LoginActivity.this,
						SignUpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				
			}
		});
		// handling main views
		mUsername = (EditText) findViewById(R.id.usernameEditText);
		mPassword = (EditText) findViewById(R.id.passwordEditText);
		mLoginButton = (Button) findViewById(R.id.loginButton);
		mFormView = (ScrollView) findViewById(R.id.formView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		TextView pingLabel = (TextView)findViewById(R.id.textView1);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/LOBSTERTWO-BOLD.OTF");
		pingLabel.setTypeface(type);

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				attemptLogin();
			}
		};

		// set up click listener on login button
		mLoginButton.setOnClickListener(listener);

		mPassword.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int id, KeyEvent arg2) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		//fb login
		mFacebookLoginButton = (Button)findViewById(R.id.facebookLoginButton);
//		mFacebookLoginButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				ParseFacebookUtils.logIn(Arrays.asList(Permissions.User.ABOUT_ME, Permissions.User.EMAIL, "public_profile"),LoginActivity.this, new LogInCallback() {
//					  @Override
//					  public void done(ParseUser user, ParseException err) {
//					    if (user == null) {
//					      Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
//					      
//					    } else if (user.isNew()) {
//					      Log.d("MyApp", "User signed up and logged in through Facebook!");
//					     
//					      
//					      Intent intent = new Intent(LoginActivity.this,
//									ChatListActivity.class);
//							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//					    } else {
//					      Log.d("MyApp", "User logged in through Facebook!");
//					      
//					      Intent intent = new Intent(LoginActivity.this,
//									ChatListActivity.class);
//							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//					    }
//					  }
//					});
//				
//			}
//		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Log.d("On activity result", "callled");
//	  ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	// ////////////////ATTEMPT LOGIN//////////////////////////
	public void attemptLogin() {
	
		// Reset errors.

		mUsername.setError(null);
		mPassword.setError(null);

		// Store values at the time of the login attempt.
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();

		View focusView = mUsername;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPassword.setError(getString(R.string.error_invalid_password));
			focusView = mPassword;
			mAuthTask = false;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(username)) {
			mUsername.setError(getString(R.string.error_field_required));
			focusView = mUsername;
			mAuthTask = false;
		} else if (!isUsernameValid(username)) {
			mUsername.setError(getString(R.string.error_invalid_username));
			focusView = mUsername;
			mAuthTask = false;
		}

		if (mAuthTask) {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			// showProgress(true);
			showProgressBar(true);

			ParseUser.logInInBackground(username, password,
					new LogInCallback() {
						public void done(ParseUser user, ParseException e) {
							showProgressBar(false);
							if (user != null) {
								// Hooray! The user is logged in.
								PingMeApplication.updateParseInstallation(user);
								
								Intent intent = new Intent(LoginActivity.this,
										ChatListActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							} else {
								// Signup failed. Look at the ParseException to
								// see what happened.
								AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
								builder.setTitle(R.string.error_title);
								builder.setMessage(e.getMessage());
								builder.setPositiveButton(android.R.string.ok, null);
								AlertDialog dialog = builder.create();
								dialog.show();
							}
						}
					});

		} else {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();

		}
	}

	// ////////////PROGRESS BAR SHOW ///////////////////////
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgressBar(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_longAnimTime);

			mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressBar.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	// ///////////////VALIDATION///////////////////
	protected boolean isPasswordValid(String password) {
		if (password.length() > 4) {
			return true;
		}
		return false;
	}

	protected boolean isUsernameValid(String username) {
		if (username.length() > 4) {
			return true;
		}
		return false;
	}

}
