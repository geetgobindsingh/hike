package com.bsb.hike.platform;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;
import com.bsb.hike.HikePubSub.Listener;
import com.bsb.hike.R;
import com.bsb.hike.adapters.MessagesAdapter;
import com.bsb.hike.models.ConvMessage;
import com.bsb.hike.platform.content.PlatformContent;
import com.bsb.hike.platform.content.PlatformContent.ErrorCode;
import com.bsb.hike.platform.content.PlatformContentListener;
import com.bsb.hike.platform.content.PlatformContentModel;
import com.bsb.hike.platform.content.PlatformWebClient;
import com.bsb.hike.utils.Logger;
import com.bsb.hike.utils.Utils;

/**
 * Created by shobhitmandloi on 14/01/15.
 */
public class WebViewCardRenderer extends BaseAdapter implements Listener
{

	static final String tag = "webviewcardRenderer";

	private static final int WEBVIEW_CARD = 0;

	private static final int FORWARD_WEBVIEW_CARD_RECEIVED = 1;

	private static final int FORWARD_WEBVIEW_CARD_SENT = 2;

	private static final int WEBVIEW_CARD_COUNT = 3;

	Context mContext;

	ArrayList<ConvMessage> convMessages;

	BaseAdapter adapter;

	private HashMap<Integer, String> cardAlarms;

	public WebViewCardRenderer(Context context, ArrayList<ConvMessage> convMessages)
	{
		this.mContext = context;
		this.convMessages = convMessages;
		cardAlarms = new HashMap<Integer, String>();
		HikeMessengerApp.getPubSub().addListener(HikePubSub.PLATFORM_CARD_ALARM, this);
	}

	public WebViewCardRenderer(Context context, ArrayList<ConvMessage> convMessages, BaseAdapter adapter)
	{
		this.mContext = context;
		this.adapter = adapter;
		this.convMessages = convMessages;
	}

	public static class WebViewHolder extends MessagesAdapter.DetailViewHolder
	{
		long id = 0;

		WebView myBrowser;

		PlatformJavaScriptBridge platformJavaScriptBridge;

		public View selectedStateOverlay;

		private void initializeHolderForForward(View view, boolean isReceived)
		{
			time = (TextView) view.findViewById(R.id.time);
			status = (ImageView) view.findViewById(R.id.status);
			timeStatus = (View) view.findViewById(R.id.time_status);
			messageContainer = (ViewGroup) view.findViewById(R.id.message_container);
			dayStub = (ViewStub) view.findViewById(R.id.day_stub);
			messageInfoStub = (ViewStub) view.findViewById(R.id.message_info_stub);

			if (isReceived)
			{
				senderDetails = view.findViewById(R.id.sender_details);
				senderName = (TextView) view.findViewById(R.id.sender_name);
				senderNameUnsaved = (TextView) view.findViewById(R.id.sender_unsaved_name);
				avatarImage = (ImageView) view.findViewById(R.id.avatar);
				avatarContainer = (ViewGroup) view.findViewById(R.id.avatar_container);
			}

		}

	}

	private WebViewHolder initializaHolder(WebViewHolder holder, View view, ConvMessage convMessage)
	{
		holder.myBrowser = (WebView) view.findViewById(R.id.webcontent);
		holder.platformJavaScriptBridge = new PlatformJavaScriptBridge(mContext, holder.myBrowser, convMessage, this);
		holder.selectedStateOverlay = view.findViewById(R.id.selected_state_overlay);
		webViewStates(holder);

		return holder;
	}

	private void webViewStates(WebViewHolder holder)
	{
		holder.myBrowser.setVerticalScrollBarEnabled(false);
		holder.myBrowser.setHorizontalScrollBarEnabled(false);
		holder.myBrowser.addJavascriptInterface(holder.platformJavaScriptBridge, HikePlatformConstants.PLATFORM_BRIDGE_NAME);
		holder.platformJavaScriptBridge.allowUniversalAccess();
		holder.platformJavaScriptBridge.allowDebugging();
		holder.myBrowser.getSettings().setJavaScriptEnabled(true);

	}

	@Override
	public int getItemViewType(int position)
	{
		if (convMessages.get(position).getMessageType() == HikeConstants.MESSAGE_TYPE.WEB_CONTENT)
		{
			return WEBVIEW_CARD;
		}
		else if (convMessages.get(position).isSent())
		{
			return FORWARD_WEBVIEW_CARD_SENT;
		}
		else
		{
			return FORWARD_WEBVIEW_CARD_RECEIVED;
		}

	}

	@Override
	public int getViewTypeCount()
	{
		return WEBVIEW_CARD_COUNT;
	}


	@Override
	public int getCount()
	{
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		return convMessages.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return convMessages.get(position).getMsgID();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		Logger.i(tag, "get view with called with position " + position);
		int type = getItemViewType(position);
		View view = convertView;
		final ConvMessage convMessage = (ConvMessage) getItem(position);
		if (view == null)
		{
			Logger.i(tag, "view inflated");
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			WebViewHolder viewHolder = new WebViewHolder();
			switch (type){
			case WEBVIEW_CARD:
				view = inflater.inflate(R.layout.html_item, parent, false);
				initializaHolder(viewHolder, view, convMessage);
				break;

			case FORWARD_WEBVIEW_CARD_SENT :
				view = inflater.inflate(R.layout.forward_html_item_sent, parent, false);
				initializaHolder(viewHolder, view, convMessage);
				viewHolder.initializeHolderForForward(view, false);
				break;

			case FORWARD_WEBVIEW_CARD_RECEIVED:
				view = inflater.inflate(R.layout.forward_html_item_received, parent, false);
				initializaHolder(viewHolder, view, convMessage);
				viewHolder.initializeHolderForForward(view, true);
				break;
			}

			view.setTag(viewHolder);
			Logger.d(tag, "inflated");
			int height = convMessage.platformWebMessageMetadata.getCardHeight();
			Logger.i(tag, "minimum height given in card is ="+height);
			if (height != 0)
			{
				int minHeight  = (int) (height * Utils.densityMultiplier);
				LayoutParams lp = viewHolder.myBrowser.getLayoutParams();
				lp.height = minHeight;
				viewHolder.myBrowser.setLayoutParams(lp);
			}
		}
		else
		{
			Logger.i(tag, "view reused");
		}

		final WebViewHolder viewHolder = (WebViewHolder) view.getTag();

		

		final WebView web = viewHolder.myBrowser;
		web.setTag(view);
		
		
		if (viewHolder.id != getItemId(position))
		{
			Logger.i(tag, "either tag is null or reused ");
			PlatformContent.getContent(convMessage.platformWebMessageMetadata.JSONtoString(), new PlatformContentListener<PlatformContentModel>()
			{

				@Override
				public void onFailure(ErrorCode reason)
				{
					// TODO Auto-generated method stub

				}

				public void onComplete(PlatformContentModel content)
				{
					viewHolder.id = getItemId(position);
					fillContent(web, content, convMessage);
				}
			});
		}
		else
		{
			Logger.i(tag, "either tag is not null ");
			viewHolder.myBrowser.loadUrl("javascript:alarmPlayed(" + "'" + cardAlarms.get(convMessage.getMsgID()) + "')");
			cardAlarms.remove(convMessage.getMsgID());
		}

		return view;

	}

	private void fillContent(WebView web, PlatformContentModel content, ConvMessage convMessage)
	{
		web.setWebViewClient(new CustomWebViewClient(convMessage));
		web.loadDataWithBaseURL("", content.getFormedData(), "text/html", "UTF-8", "");
	}

	private class CustomWebViewClient extends PlatformWebClient
	{

		ConvMessage convMessage;

		public CustomWebViewClient(ConvMessage convMessage)
		{
			this.convMessage = convMessage;
		}
		
		
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			super.onPageStarted(view, url, favicon);
			View main = (View) view.getTag();
//			main.findViewById(R.id.loader).setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
//			LayoutParams lp = view.getLayoutParams();
//			lp.height = LayoutParams.WRAP_CONTENT;
//			view.setLayoutParams(lp);
			Log.d(tag, "Height of webView after loading is " + String.valueOf(view.getMeasuredHeight()) + "px");
			view.loadUrl("javascript:setData(" + "'" + convMessage.getMsgID() + "','" + convMessage.getMsisdn() + "','"
					+ convMessage.platformWebMessageMetadata.getHelperData().toString() + "')");
			String alarmData = convMessage.platformWebMessageMetadata.getAlarmData();
			Logger.d(tag, "alarm data to html is " + alarmData);
			if (!TextUtils.isEmpty(alarmData))
			{
				view.loadUrl("javascript:alarmPlayed(" + "'" + alarmData + "')");
				cardAlarms.remove(convMessage.getMsgID());
			}
			super.onPageFinished(view, url);
			View main = (View) view.getTag();
			// main.findViewById(R.id.loader).setVisibility(View.GONE);
		}
	}

	public void onDestroy()
	{
		HikeMessengerApp.getPubSub().removeListener(HikePubSub.PLATFORM_CARD_ALARM, this);
	}

	@Override
	public void onEventReceived(String type, Object object)
	{
		if (HikePubSub.PLATFORM_CARD_ALARM.equals(type))
		{
			if (object instanceof Message)
			{
				Message m = (Message) object;
				cardAlarms.put(m.arg1, (String) m.obj);
			}
			else
			{
				Logger.e(tag, "Expected Message in PubSub but received " + object.getClass());
			}
		}
	}

}
