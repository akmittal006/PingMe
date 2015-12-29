package com.ankurmittal.learning.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.storage.TextMessage;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class Utils {

	public static TextMessage createTextMessage(ParseObject pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE));
		Log.d("detail frag ",
				" "
						+ pTextMessage.getString(ParseConstants.KEY_MESSAGE)
						+ ": "
						+ pTextMessage
								.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME)
						+ ", " + pTextMessage.getString("isSent"));
		if (pTextMessage.getObjectId() != null) {
			textMessage.setMessageId(pTextMessage.getObjectId());
		}

		textMessage.setReceiverId(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
		textMessage.setReceiverName(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
		textMessage.setSenderId(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getObjectId());
		textMessage.setSenderName(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getUsername());
		if (pTextMessage.getCreatedAt() != null) {
			textMessage.setCreatedAt(getDateTime(pTextMessage.getCreatedAt()));
		} else {
			textMessage.setCreatedAt(new Date());
		}
		if (textMessage.getSenderId() == ParseUser.getCurrentUser()
				.getObjectId()) {
			textMessage.setType(Constants.TYPE_SENT);
		} else {
			textMessage.setType(Constants.TYPE_RECEIVED);
		}

		textMessage.setMessageStatus(pTextMessage.getString("isSent"));

		return textMessage;
	}

	public static String getDateTime(java.util.Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		// Date date = new Date();
		return dateFormat.format(date);
	}

	public static JSONObject createJSONObject(ParseObject message) {
		JSONObject jsonMessage = new JSONObject();
		try {
			jsonMessage.put(
					"alert",
					message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME)
							+ ": "
							+ message.getString(ParseConstants.KEY_MESSAGE));
			jsonMessage.put("priority","high");
			jsonMessage.put(ParseConstants.KEY_MESSAGE,
					message.getString(ParseConstants.KEY_MESSAGE));
			jsonMessage.put(ParseConstants.KEY_MESSAGE_ID,
					message.getObjectId());
			jsonMessage.put(ParseConstants.KEY_SENDER_NAME, message
					.getParseUser(ParseConstants.KEY_MESSAGE_SENDER)
					.getUsername());
			jsonMessage.put(ParseConstants.KEY_SENDER_ID,
					message.getParseUser(ParseConstants.KEY_MESSAGE_SENDER)
							.getObjectId());
			jsonMessage.put(ParseConstants.KEY_MESSAGE_RECEIVER_ID,
					message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
			jsonMessage
					.put(ParseConstants.KEY_MESSAGE_RECEIVER_NAME,
							message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
			jsonMessage.put("isSent", message.getString("isSent"));
			jsonMessage.put(ParseConstants.KEY_CREATED_AT,
					Utils.getDateTime(message.getCreatedAt()));
			jsonMessage.put("type", "message");

			Log.d("Json message", jsonMessage.toString());
			return jsonMessage;
		} catch (Exception e) {
			Log.e("JSON ERROR", "error creating message");
		}
		return null;
	}

	public static TextMessage createNeutralMessage(Date currDate,
			TextMessage pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(getDateString(currDate));
		Log.d("list frag ", " " + pTextMessage.getMessage());
		textMessage.setMessageId(currDate.toString());
		textMessage.setReceiverId(pTextMessage.getReceiverId());
		textMessage.setReceiverName("pingMe9872719390");
		textMessage.setSenderId(pTextMessage.getSenderId());
		textMessage.setSenderName(pTextMessage.getSenderName());
		textMessage.setCreatedAt(currDate);
		textMessage.setType(Constants.TYPE_NEUTRAL);
		return textMessage;
	}

	public static String getDateString(Date date) {
		String strDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		strDate = dateFormat.format(date);
		return strDate;
	}
	
	public static TextMessage createTextMessageFromJsonData(JSONObject message) {
		TextMessage textMessage = new TextMessage();
		try {
			textMessage.setCreatedAt(message
					.getString(ParseConstants.KEY_CREATED_AT));
			textMessage.setMessage(message
					.getString(ParseConstants.KEY_MESSAGE));
			textMessage.setMessageId(message
					.getString(ParseConstants.KEY_MESSAGE_ID));
			textMessage.setReceiverId(message
					.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
			textMessage.setReceiverName(message
					.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
			textMessage.setSenderId(message
					.getString(ParseConstants.KEY_SENDER_ID));
			textMessage.setSenderName(message
					.getString(ParseConstants.KEY_SENDER_NAME));
			textMessage.setMessageStatus(Constants.MESSAGE_STATUS_DELIVERED);
			return textMessage;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("json error", "error creating message");
		}
		return null;
	}
	
	public static boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();

		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	

	public static String getAppPath() {
		String appName = "PingMe";
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				appName);

		// 2. Create our subdirectory
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e("friends", "Failed to create directory. "
						+ mediaStorageDir.toString());
				// //return null;
			}
		}
		// 3. Create a file name
		// 4. Create the file
		//File mediaFile;

		String path = mediaStorageDir.getPath() + File.separator;
		return path;
	}
	

	public static void saveAndLoadProfilePic(Bitmap bitmap, String fileName, ImageView mProfilePicView) {
		Log.e("DEBUG", "saving and loading pics for - file --" + fileName);
		try {
			if (Utils.isExternalStorageAvailable()) {
				File file = new File(Utils.getAppPath() + "/" + fileName + ".png");
				try {
					if(file.createNewFile()) {
						Log.e("DEBUG", "new file created");
						compressBitmapToFile(bitmap, file);
					} else {
						//delete file
						if(file.delete()) {
							Log.e("DEBUG", "new file deleted");
							if(file.createNewFile()) {
								Log.e("DEBUG", "new file created after deleting + file-" + file.getCanonicalPath().toString());
								compressBitmapToFile(bitmap, file);
							} else {
								Log.e("DEBUG", "unable to create new file after deleting");
							}
							
						} else {
							Log.e("DEBUG", "unable to delete pre existing file");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("DEBUG", "External Storage not avail");
			}
		} finally {
			mProfilePicView.setImageBitmap(bitmap);
			
		}
	}

	public static void compressBitmapToFile(Bitmap bitmap, File file)
			throws FileNotFoundException, IOException {
		FileOutputStream ostream = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 75, ostream);
		ostream.close();
	}
	
	public static void loadUserImageByUrl(Context mContext,ImageView iconView, String imgUrl) {
		Picasso.with(mContext).setIndicatorsEnabled(true);
		
		if (Utils.isExternalStorageAvailable()) {

			File file = new File(Utils.getAppPath() + "/"
					+ imgUrl.substring(93, 116) + ".png");
			if (file.exists()) {
				Log.e("image loaded from mobile", Uri.fromFile(file)
						.toString());
				//Picasso.with(mContext).invalidate(file);
				Picasso.with(mContext).load(Uri.fromFile(file))
						.placeholder(R.drawable.avatar_empty)
						.resize(88, 88).centerInside().into(iconView);
			} 
		}
	}

}
