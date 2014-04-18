package com.bsb.hike.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.R;

public class HikeTip
{
	public static enum TipType
	{
		EMOTICON, LAST_SEEN, STATUS, MOOD, CHAT_BG_FTUE
	}

	public static void showTip(final Activity activity, final TipType tipType, final View parentView)
	{
		showTip(activity, tipType, parentView, null);
	}

	public static void showTip(final Activity activity, final TipType tipType, final View parentView, String name)
	{
		parentView.setVisibility(View.VISIBLE);

		View container = parentView.findViewById(R.id.tip_container);
		TextView tipText = (TextView) parentView.findViewById(R.id.tip_text);
		ImageButton closeTip = (ImageButton) parentView.findViewById(R.id.close);

		switch (tipType)
		{
		case EMOTICON:
			container.setBackgroundResource(R.drawable.bg_sticker_ftue);
			tipText.setText(R.string.sticker_ftue_body);
			break;
		case LAST_SEEN:
			container.setBackgroundResource(R.drawable.bg_tip_top_left);
			tipText.setText(R.string.last_seen_tip_friends);
			break;
		case MOOD:
			container.setBackgroundResource(R.drawable.bg_tip_top_left);
			tipText.setText(R.string.moods_tip);
			break;
		case STATUS:
			container.setBackgroundResource(R.drawable.bg_tip_top_left);
			tipText.setText(activity.getString(R.string.status_tip, name));
			break;
		case CHAT_BG_FTUE:
			container.setBackgroundResource(R.drawable.bg_tip_top_right);
			tipText.setText(R.string.chat_bg_ftue_tip);
			break;
		}
		if (closeTip != null)
		{
			closeTip.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					closeTip(tipType, parentView, activity.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0));
				}
			});
		}

		parentView.setTag(tipType);
	}

	public static void closeTip(TipType tipType, View parentView, SharedPreferences preferences)
	{
		parentView.setVisibility(View.GONE);

		Editor editor = preferences.edit();

		switch (tipType)
		{
		case EMOTICON:
			editor.putBoolean(HikeMessengerApp.SHOWN_EMOTICON_TIP, true);
			break;
		case LAST_SEEN:
			editor.putBoolean(HikeMessengerApp.SHOWN_LAST_SEEN_TIP, true);
			break;
		case MOOD:
			editor.putBoolean(HikeMessengerApp.SHOWN_MOODS_TIP, true);
			break;
		case STATUS:
			editor.putBoolean(HikeMessengerApp.SHOWN_STATUS_TIP, true);
			break;
		case CHAT_BG_FTUE:
			editor.putBoolean(HikeMessengerApp.SHOWN_CHAT_BG_TOOL_TIP, true);
			editor.putBoolean(HikeMessengerApp.SHOWN_NEW_CHAT_BG_TOOL_TIP, true);
			break;
		}

		editor.commit();
	}

}
