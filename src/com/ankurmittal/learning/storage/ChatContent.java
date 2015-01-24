package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ChatContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<ChatItem> ITEMS = new ArrayList<ChatItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, ChatItem> ITEM_MAP = new HashMap<String, ChatItem>();

	static {
		// Add 3 sample items.
//		addItem(new ChatItem("1", "Item 1"));
//		addItem(new ChatItem("2", "Item 2"));
//		addItem(new ChatItem("3", "Item 3"));
	}

	public static void addItem(ChatItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
	public static void deleteAllItems() {
		ITEMS.clear();
		ITEM_MAP.clear();
	}

}
