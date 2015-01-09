package com.ankurmittal.learning.storage;

import java.util.ArrayList;

import com.parse.ParseUser;

public class TextMessageSource {

	public ArrayList<TextMessage> messages;

	public TextMessageSource(ParseUser currentUser) {
		messages = new ArrayList<TextMessage>();
		TextMessage message = new TextMessage();
		message.setMessage("hi how r u?");
		message.setSender(currentUser.getUsername());
		messages.add(message);
		TextMessage message1 = new TextMessage();
		message1.setMessage("m Great");
		message1.setReceiver(currentUser.getUsername());
		messages.add(message1);
		TextMessage message2 = new TextMessage();
		message2.setMessage("this is working fine abso fine \n m happy");
		message2.setSender(currentUser.getUsername());
		messages.add(message2);
		TextMessage message3 = new TextMessage();
		message3.setMessage("this is working fine abso fine \n m happy tooooooooo");
		message3.setReceiver(currentUser.getUsername());
		messages.add(message3);
	}
	public ArrayList<TextMessage> getMessages() {
		return messages;
	}
}
