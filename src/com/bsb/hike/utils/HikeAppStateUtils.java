package com.bsb.hike.utils;

import android.app.Activity;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikeMessengerApp.CurrentState;

public class HikeAppStateUtils
{

	private static final String TAG = "HikeAppState";

	public static void onCreate(Activity activity)
	{
		if (HikeMessengerApp.currentState == CurrentState.BACKGROUNDED || HikeMessengerApp.currentState == CurrentState.CLOSED)
		{
			Logger.d(TAG + activity.getClass().getSimpleName(), "App was opened. Sending packet");
			HikeMessengerApp.currentState = CurrentState.OPENED;
			Utils.appStateChanged(activity.getApplicationContext());
		}

	}

	public static void onResume(Activity activity)
	{
		com.facebook.Settings.publishInstallAsync(activity, HikeConstants.APP_FACEBOOK_ID);
		Logger.d(TAG + activity.getClass().getSimpleName(), "App was resumed");
	}

	public static void onStart(Activity activity)
	{
		if (HikeMessengerApp.currentState == CurrentState.BACKGROUNDED || HikeMessengerApp.currentState == CurrentState.CLOSED)
		{
			Logger.d(TAG + activity.getClass().getSimpleName(), "App was started. Sending packet");
			HikeMessengerApp.currentState = CurrentState.RESUMED;
			Utils.appStateChanged(activity.getApplicationContext());
		}
	}

	public static void onBackPressed()
	{
		HikeMessengerApp.currentState = CurrentState.BACK_PRESSED;
	}

	public static void onStop(Activity activity)
	{
		Logger.d(TAG + activity.getClass().getSimpleName(), "OnStop");
		if (HikeMessengerApp.currentState == CurrentState.NEW_ACTIVITY)
		{
			Logger.d(TAG, "App was going to another activity");
			HikeMessengerApp.currentState = CurrentState.RESUMED;
		}
		else if (HikeMessengerApp.currentState == CurrentState.BACK_PRESSED)
		{
			HikeMessengerApp.currentState = CurrentState.RESUMED;
		}
		else
		{
			if(Utils.isHoneycombOrHigher() && activity.isChangingConfigurations())
			{
				Logger.d(TAG, "App was going to another activity");
				HikeMessengerApp.currentState = CurrentState.RESUMED;
				return;
			}
			Logger.d(TAG + activity.getClass().getSimpleName(), "App was backgrounded. Sending packet");
			HikeMessengerApp.currentState = CurrentState.BACKGROUNDED;
			Utils.appStateChanged(activity.getApplicationContext(), true, true);
		}
	}

	public static void finish()
	{
		HikeMessengerApp.currentState = CurrentState.BACK_PRESSED;
	}

	public static void startActivityForResult(Activity activity)
	{
		HikeMessengerApp.currentState = CurrentState.NEW_ACTIVITY;
		Logger.d(TAG + activity.getClass().getSimpleName(), "startActivityForResult. Previous state: " + HikeMessengerApp.currentState);
	}

	public static void onActivityResult(Activity activity)
	{
		if (HikeMessengerApp.currentState == CurrentState.BACKGROUNDED)
		{
			Logger.d(TAG + activity.getClass().getSimpleName(), "App returning from activity with result. Sending packet");
			HikeMessengerApp.currentState = CurrentState.RESUMED;
			Utils.appStateChanged(activity.getApplicationContext());
		}
	}

}
