package com.bsb.hike.utils;

import android.content.Context;
import android.os.Handler;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;

public class StealthResetTimer
{
	private static final int RESET_TOGGLE_TIME_MS = 10 * 1000;

	private static StealthResetTimer stealthResetTimer;

	private Handler handler;

	private Context context;

	private StealthResetTimer(Context context)
	{
		this.handler = new Handler();
		this.context = context;
	}

	public static StealthResetTimer getInstance(Context context)
	{
		if (stealthResetTimer == null)
		{
			stealthResetTimer = new StealthResetTimer(context.getApplicationContext());
		}
		return stealthResetTimer;
	}

	public void resetStealthToggle()
	{
		clearScheduledStealthToggleTimer();
		handler.postDelayed(toggleReset, RESET_TOGGLE_TIME_MS);
	}

	public void clearScheduledStealthToggleTimer()
	{
		handler.removeCallbacks(toggleReset);
	}

	private Runnable toggleReset = new Runnable()
	{

		@Override
		public void run()
		{
			HikeSharedPreferenceUtil.getInstance(context).saveData(HikeMessengerApp.STEALTH_MODE, HikeConstants.STEALTH_OFF);
			HikeMessengerApp.getPubSub().publish(HikePubSub.STEALTH_MODE_TOGGLED, true);
		}
	};
}
