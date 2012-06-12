package com.bsb.hike.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.NetworkManager;
import com.bsb.hike.db.HikeConversationsDatabase;
import com.bsb.hike.utils.Utils;

public class Conversation implements Comparable<Conversation>
{

	public static class ConversationComparator implements Comparator<Conversation>
	{
		/*
		 * This comparator reverses the order of the normal comparable
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Conversation lhs, Conversation rhs)
		{
			if (rhs == null)
			{
				return 1;
			}

			return rhs.compareTo(lhs);
		}
	}

	public String getMsisdn()
	{
		return msisdn;
	}

	public long getConvId()
	{
		return convId;
	}

	@Override
	public String toString()
	{
		return "Conversation [msisdn=" + msisdn + ", convId=" + convId + ", contactId=" + contactId + ", messages=" + messages.size() + ", contactName=" + contactName
				+ ", onhike=" + onhike + "]";
	}

	public String getContactId()
	{
		return contactId;
	}

	private String msisdn;

	private long convId;

	private String contactId;

	private List<ConvMessage> messages;

	private String contactName;

	private boolean onhike;

	private List<ContactInfo> groupParticipants;

	private String groupOwner;

	private boolean isGroupAlive;

	public void setOnhike(boolean onhike)
	{
		this.onhike = onhike;
	}

	public String getContactName()
	{
		return contactName;
	}

	public String getGroupOwner()
	{
		return groupOwner;
	}

	public boolean getIsGroupAlive()
	{
		return isGroupAlive;
	}
	/*
	 * Returns a friendly name for this conversation (name if non-empty, otherwise msisdn)
	 */
	public String getLabel()
	{
		if (isGroupConversation())
		{
			return !TextUtils.isEmpty(contactName) ? contactName : Utils.defaultGroupName(getGroupParticipants());
		}
		return TextUtils.isEmpty(contactName) ? msisdn : contactName;
	}

	public Conversation(String msisdn, long convId, String contactId, String contactName, boolean onhike)
	{
		this(msisdn, convId, contactId, contactName, onhike, null, false);
	}

	public Conversation(String msisdn, long convId, String contactId, String contactName, boolean onhike, String groupOwner, boolean isGroupAlive)
	{
		this.msisdn = msisdn;
		this.convId = convId;
		this.contactId = contactId;
		this.contactName = contactName;
		this.onhike = onhike;
		this.messages = new ArrayList<ConvMessage>();
		this.groupParticipants = new ArrayList<ContactInfo>();
		this.groupOwner = groupOwner;
		this.isGroupAlive = isGroupAlive;
	}

	public Conversation(JSONObject jsonObject, Context context) throws JSONException
	{
		this.msisdn = jsonObject.getString(HikeConstants.TO);
		this.groupOwner = jsonObject.getString(HikeConstants.FROM);

		this.groupParticipants = new ArrayList<ContactInfo>();
		JSONArray array = jsonObject.getJSONArray(HikeConstants.DATA);
		for (int i = 0; i < array.length(); i++) 
		{
			JSONObject nameMsisdn = array.getJSONObject(i);
			String contactNum = nameMsisdn.getString(HikeConstants.MSISDN);
			String contactName = nameMsisdn.getString(HikeConstants.NAME);
			ContactInfo contactInfo = new ContactInfo(contactNum, contactNum, contactName, contactNum);
			Log.d(getClass().getSimpleName(), "Parsing JSON and adding contact to conversation: " + contactNum);
			this.groupParticipants.add(contactInfo);
		}

		if (this.isGroupConversation())
		{
			HikeConversationsDatabase db = new HikeConversationsDatabase(context);
			this.contactName = db.getGroupName(this.msisdn);
			db.close();
		}

	}

	public boolean isOnhike()
	{
		return onhike;
	}

	/* TODO this should be addAll to conform w/ normal java semantics */
	public void setMessages(List<ConvMessage> messages)
	{
		this.messages = messages;
	}

	public void addMessage(ConvMessage message)
	{
		this.messages.add(message);
	}

	public List<ContactInfo> getGroupParticipants() 
	{
		return groupParticipants;
	}

	public void setGroupParticipants(List<ContactInfo> groupParticipants) 
	{
		this.groupParticipants = groupParticipants;
	}

	public void addGroupParticipant(ContactInfo contactInfo)
	{
		this.groupParticipants.add(contactInfo);
	}

	@Override
	public int compareTo(Conversation rhs)
	{
		if (this.equals(rhs))
		{
			return 0;
		}

		long ts = messages.isEmpty() ? 0 : messages.get(messages.size()-1).getTimestamp();
		if (rhs == null)
		{
			return 1;
		}

		long rhsTs = rhs.messages.isEmpty() ? 0 : rhs.messages.get(rhs.messages.size()-1).getTimestamp();
		
		if (rhsTs != ts)
		{
			return (ts < rhsTs) ? -1 : 1;
		}

		int ret = msisdn.compareTo(rhs.msisdn);
		if (ret != 0)
		{
			return ret;
		}

		if (convId != rhs.convId)
		{
			return (convId < rhs.convId) ? -1 : 1;
		}

		String cId = (contactId != null) ? contactId : "";
		return cId.compareTo(rhs.contactId);
	}

	public List<ConvMessage> getMessages()
	{
		return messages;
	}

	public boolean isGroupConversation()
	{
		return Utils.isGroupConversation(this.msisdn);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
		result = prime * result + ((contactName == null) ? 0 : contactName.hashCode());
		result = prime * result + (int) (convId ^ (convId >>> 32));
		result = prime * result + ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + ((msisdn == null) ? 0 : msisdn.hashCode());
		result = prime * result + (onhike ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conversation other = (Conversation) obj;
		if (contactId == null)
		{
			if (other.contactId != null)
				return false;
		}
		else if (!contactId.equals(other.contactId))
			return false;
		if (contactName == null)
		{
			if (other.contactName != null)
				return false;
		}
		else if (!contactName.equals(other.contactName))
			return false;
		if (convId != other.convId)
			return false;
		if (messages == null)
		{
			if (other.messages != null)
				return false;
		}
		else if (!messages.equals(other.messages))
			return false;
		if (msisdn == null)
		{
			if (other.msisdn != null)
				return false;
		}
		else if (!msisdn.equals(other.msisdn))
			return false;
		if (onhike != other.onhike)
			return false;
		return true;
	}

	public JSONObject serialize(String type)
	{
		JSONObject object = new JSONObject();
		try
		{
			object.put(HikeConstants.TYPE, type);
			object.put(HikeConstants.TO, msisdn);
			if(type.equals(NetworkManager.GROUP_CHAT_JOIN))
			{
				JSONArray array = new JSONArray();
				for(ContactInfo participant : groupParticipants)
				{
					JSONObject nameMsisdn = new JSONObject();
					nameMsisdn.put(HikeConstants.NAME, participant.getName());
					nameMsisdn.put(HikeConstants.MSISDN, participant.getMsisdn());
					array.put(nameMsisdn);
				}
				object.put(HikeConstants.DATA, array);
			}
		}
		catch (JSONException e)
		{
			Log.e("ConvMessage", "invalid json message", e);
		}
		return object;
	}

	public void setContactName(String contactName)
	{
		this.contactName = contactName;
	}
}
