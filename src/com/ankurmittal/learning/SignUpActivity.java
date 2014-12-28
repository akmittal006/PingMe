package com.ankurmittal.learning;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {

	EditText mUsername;
	EditText mEmail;
	EditText mPassword;
	TextView mLoginTextView;
	Button mSignUpButton;
	ScrollView mFormView;
	ProgressBar mProgressBar;

	boolean mAuthTask = true;

	public static final String TAG = SignUpActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		getActionBar().hide();
		
		// handling signup text view
				mLoginTextView = (TextView) findViewById(R.id.usernameTextView);
				mLoginTextView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(SignUpActivity.this,
								LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});

		// handling main views
		mUsername = (EditText) findViewById(R.id.usernameEditText);
		mEmail = (EditText) findViewById(R.id.emailEditText);
		mPassword = (EditText) findViewById(R.id.passwordEditText);
		mSignUpButton = (Button) findViewById(R.id.loginButton);
		mFormView = (ScrollView) findViewById(R.id.formView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		mUsername.requestFocus();

		// set up click listener on login button
		mSignUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptLogin();
			}
		});

		mPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int id, KeyEvent event) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}

				return false;
			}
		});

	}

	// /////////////LOGIN ATTEMPT///////////////////////
	public void attemptLogin() {
		// if (mAuthTask) {
		// return;
		// }

		// Reset errors.
		mEmail.setError(null);
		mPassword.setError(null);
		mUsername.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmail.getText().toString();
		String password = mPassword.getText().toString();
		String username = mUsername.getText().toString();

		View focusView = mPassword;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPassword.setError(getString(R.string.error_invalid_password));
			focusView = mPassword;
			mAuthTask = false;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmail.setError(getString(R.string.error_field_required));
			focusView = mEmail;
			mAuthTask = false;
		} else if (!isEmailValid(email)) {
			mEmail.setError(getString(R.string.error_invalid_email));
			focusView = mEmail;
			mAuthTask = false;
		}

		// Check for valid username
		if (TextUtils.isEmpty(username)) {
			mUsername.setError(getString(R.string.error_field_required));
			focusView = mUsername;
			mAuthTask = false;
		} else if (!isUsernameValid(username)) {
			mUsername.setError(getString(R.string.error_invalid_username));
			focusView = mUsername;
			mAuthTask = false;
		}

		Log.d(TAG, "" + mAuthTask);

		if (mAuthTask) {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			// showProgress(true);

			showProgressBar(true);

			ParseUser user = new ParseUser();
			user.setUsername(username);
			user.setPassword(password);
			user.setEmail(email);

			// other fields can be set just like with ParseObject
			// user.put("phone", "650-253-0000");

			user.signUpInBackground(new SignUpCallback() {
				public void done(ParseException e) {
					showProgressBar(false);
					if (e == null) {
						// Hooray! Let them use the app now.
						Log.d(TAG, "Sign upped");
						Intent intent = new Intent(SignUpActivity.this,
								LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					} else {
						// Sign up didn't succeed. Look at the ParseException
						// to figure out what went wrong
						Log.d(TAG, "Parse exception :" + e);
						AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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
					android.R.integer.config_shortAnimTime);

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

	// ////////////////VALIDATION//////////////
	protected boolean isUsernameValid(String username) {
		if (username.length() > 4) {
			return true;
		}
		return false;
	}

	protected boolean isPasswordValid(String password) {
		if (password.length() > 4) {
			return true;
		}
		return false;
	}

	protected boolean isEmailValid(String email) {
		if (email.contains("@")) {
			return true;
		}
		return false;
	}
}
