package com.ankurmittal.learning.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


@SuppressWarnings("unused")
public class QuickAction {

	private static final String PARAM_STATUS_BAR_HEIGHT = "status_bar_height";
	private static final String PARAM_DIMEN = "dimen";
	private static final String PARAM_ANDROID = "android";

	private static final int X_INDEX = 0;
	private static final int Y_INDEX = 1;

	private Context context;
	private int screenWidth;
	private int screenHeight;

	private PopupWindow popupWindow;
	private WindowManager windowManager;
	private RelativeLayout topRootLayout;
	private RelativeLayout bottomRootLayout;

	public QuickAction(Context context, int animationStyle,
			RelativeLayout topRootLayout, RelativeLayout bottomRootLayout) {
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		this.context = context;
		this.topRootLayout = topRootLayout;
		this.bottomRootLayout = bottomRootLayout;

		initScreen();
		initPopupWindow(animationStyle);
	}

	@SuppressWarnings("deprecation")
	private void initScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Display display = windowManager.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		} else {
			screenWidth = windowManager.getDefaultDisplay().getWidth();
			screenHeight = windowManager.getDefaultDisplay().getHeight();
		}
	}

	private void initPopupWindow(int animationStyle) {
		popupWindow = new PopupWindow(context);
		popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setAnimationStyle(animationStyle);
		popupWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					popupWindow.dismiss();
					return true;
				} else
					return false;
			}
		});
	}

	public void setMaxHeightResource(int heightResource) {
		int maxHeight = context.getResources().getDimensionPixelSize(
				heightResource);
		popupWindow.setHeight(maxHeight);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void dismiss() {
		popupWindow.dismiss();
	}

	public void show(View anchor) {
		try {
			int[] location = new int[2];
			anchor.getLocationOnScreen(location);

			Rect anchorRect = new Rect(location[X_INDEX], location[Y_INDEX],
					location[X_INDEX] + anchor.getWidth(), location[Y_INDEX]
							+ anchor.getHeight());

			RelativeLayout rootLayout;
			boolean onTop = false;
			if (location[Y_INDEX] > screenHeight / 2)
				rootLayout = bottomRootLayout;
			else {
				rootLayout = topRootLayout;
				onTop = true;
			}

			if (rootLayout.getLayoutParams() == null)
				rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
			rootLayout.measure(View.MeasureSpec.UNSPECIFIED,
					View.MeasureSpec.UNSPECIFIED);

			int rootHeight = rootLayout.getMeasuredHeight();
			int rootWidth = rootLayout.getMeasuredWidth();

			int x = calculateHorizontalPosition(anchor, anchorRect, rootWidth,
					screenWidth);
			int y = calculateVerticalPosition(anchorRect, rootHeight, onTop);

			popupWindow.setContentView(rootLayout);
			popupWindow.dismiss();
			popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int calculateHorizontalPosition(View anchor, Rect anchorRect,
			int rootWidth, int screenWidth) {
		int x;

		if ((anchorRect.left + rootWidth) > screenWidth) {
			x = anchorRect.left - (rootWidth - anchor.getWidth());
			if (x < 0)
				x = 0;
		} else {
			if (anchor.getWidth() > rootWidth)
				x = anchorRect.centerX() - (rootWidth / 2);
			else
				x = anchorRect.left;
		}

		return x;
	}

	@SuppressWarnings("ConstantConditions")
	private int calculateVerticalPosition(Rect anchorRect, int rootHeight,
			boolean onTop) {
		int y;

		if (onTop)
			y = anchorRect.top;
		else
			y = anchorRect.bottom - rootHeight;

		return y;
	}

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = context.getResources().getIdentifier(
				PARAM_STATUS_BAR_HEIGHT, PARAM_DIMEN, PARAM_ANDROID);
		if (resourceId > 0)
			result = context.getResources().getDimensionPixelSize(resourceId);

		return result;
	}

}