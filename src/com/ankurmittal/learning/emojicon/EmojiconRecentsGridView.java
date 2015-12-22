package com.ankurmittal.learning.emojicon;

import android.content.Context;
import android.widget.GridView;

import com.ankurmittal.learning.R;
import com.ankurmittal.learning.emojicon.emoji.Emojicon;

public class EmojiconRecentsGridView extends EmojiconGridView implements
		EmojiconRecents {
	EmojiAdapter mAdapter;

	public EmojiconRecentsGridView(Context context, Emojicon[] emojicons,
			EmojiconRecents recents, EmojiconsPopup emojiconsPopup) {
		super(context, emojicons, recents, emojiconsPopup);
		EmojiconRecentsManager recents1 = EmojiconRecentsManager
				.getInstance(rootView.getContext());
		mAdapter = new EmojiAdapter(rootView.getContext(), recents1);
		mAdapter.setEmojiClickListener(new OnEmojiconClickedListener() {

			@Override
			public void onEmojiconClicked(Emojicon emojicon) {
				if (mEmojiconPopup.onEmojiconClickedListener != null) {
					mEmojiconPopup.onEmojiconClickedListener
							.onEmojiconClicked(emojicon);
				}
			}
		});
		GridView gridView = (GridView) rootView
				.findViewById(R.id.Emoji_GridView);
		gridView.setAdapter(mAdapter);
	}

	@Override
	public void addRecentEmoji(Context context, Emojicon emojicon) {
		EmojiconRecentsManager recents = EmojiconRecentsManager
				.getInstance(context);
		recents.push(emojicon);

		// notify dataset changed
		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}

}
