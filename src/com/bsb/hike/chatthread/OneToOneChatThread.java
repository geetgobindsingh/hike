package com.bsb.hike.chatthread;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bsb.hike.R;
import com.bsb.hike.utils.Logger;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */

public class OneToOneChatThread extends ChatThread {
	private static final String TAG = "oneonechatthread";

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public OneToOneChatThread(ChatThreadActivity activity) {
		super(activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Logger.i(TAG, "on create options menu " + menu.hashCode());
		chatThreadActionBar.onCreateOptionsMenu(menu,
				R.menu.one_one_chat_thread_menu, getOverFlowItems(), this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Logger.i(TAG, "on prepare options menu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.i(TAG, "menu item click" + item.getItemId());
		switch (item.getItemId()) {
		case R.id.chat_bg:
			showThemePicker();
			return true;
		}
		return chatThreadActionBar.onOptionsItemSelected(item) ? true : super
				.onOptionsItemSelected(item);
	}

	private List<OverFlowMenuItem> getOverFlowItems() {

		List<OverFlowMenuItem> list = new ArrayList<OverFlowMenuItem>();
		list.add(new OverFlowMenuItem(getString(R.string.view_profile), 0, 0,
				R.string.view_profile));
		list.add(new OverFlowMenuItem(getString(R.string.call), 0, 0,
				R.string.call));
		for (OverFlowMenuItem item : super.getOverFlowMenuItems()) {
			list.add(item);
		}
		list.add(new OverFlowMenuItem(
				isUserBlocked() ? getString(R.string.unblock_title)
						: getString(R.string.block_title), 0, 0,
				R.string.block_title));
		return list;
	}

	@Override
	public void setContentView() {
		activity.setContentView(R.layout.chatthread);
	}

	private boolean isUserBlocked() {
		return false;
	}

}
