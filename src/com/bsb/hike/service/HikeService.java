package com.bsb.hike.service;

import java.net.URISyntaxException;
import java.util.ArrayList;

import com.bsb.hike.HikeMessengerApp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class HikeService extends Service
{

	static final int MSG_APP_CONNECTED = 1;
	static final int MSG_APP_DISCONNECTED = 2;
	static final int MSG_APP_TOKEN_CREATED = 3;

	protected Messenger mApp;
	protected ArrayList<String> pendingMessages;

	class IncomingHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_APP_CONNECTED:
				mApp = msg.replyTo;
				/* TODO what if the app crashes while we're sending the message? */
				for (String m: pendingMessages)
				{
					sendToApp(m);
				}
				break;
			case MSG_APP_DISCONNECTED:
				mApp = null;
				break;
			case MSG_APP_TOKEN_CREATED:
				mPushManager = PushManager.createInstance(HikeService.this);
			}
		}
	}

	public void storeMessage(String message)
	{
		pendingMessages.add(message);
	}

	public boolean sendToApp(String message)
	{
		if (mApp == null)
		{
			return false;
		}

		try
		{
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			bundle.putString("msg", message);
			msg.setData(bundle);
			mApp.send(msg);
		} catch(RemoteException e)
		{
			//client is dead :(
			mApp = null;
			Log.e("HikeService", "Can't send message to the application");
			return false;
		}
		return true;
	}

	private PushManager mPushManager;
	private Messenger mMessenger;

	@Override
	public void onCreate()
	{
		super.onCreate();
		mMessenger = new Messenger(new IncomingHandler());
		pendingMessages = new ArrayList<String>();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d("HikeService", "Start Command Called");
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mMessenger.getBinder();
	}

}
