package com.bsb.hike.utils;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;

public class HikeAnalyticsEvent
{
	/*
	 * We send this event every time user mark some chats as stealth
	 */
	public static void sendStealthMsisdns(Set<String> enabledMsisdn, Set<String> disabledMsisnd)
	{
		JSONObject object = new JSONObject();
		try
		{
			object.put(HikeConstants.TYPE, HikeConstants.MqttMessageTypes.STEALTH);

			JSONObject dataJson = new JSONObject();
			dataJson.put(HikeConstants.ENABLED_STEALTH, new JSONArray(enabledMsisdn));
			dataJson.put(HikeConstants.DISABLED_STEALTH, new JSONArray(disabledMsisnd));
			object.put(HikeConstants.DATA, dataJson);
			HikeMessengerApp.getPubSub().publish(HikePubSub.MQTT_PUBLISH, object);
		}
		catch (JSONException e)
		{
			Logger.e("HikeAnalyticsEvent", "Exception in sending analytics event", e);
		}

	}

}
