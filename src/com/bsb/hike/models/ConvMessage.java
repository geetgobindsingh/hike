package com.bsb.hike.models;

public class ConvMessage {
	public ConvMessage(String message, String msisdn, String contactId, int timeestamp, boolean isSent) {
		this.contactId = contactId;
		this.mMsisdn = msisdn;
		this.mMessage = message;
		this.mTimestamp = timeestamp;
		this.mIsSent = isSent;
	}

	public String getMessage() {
		return mMessage;
	}

	public boolean isSent() {
		return mIsSent;
	}

	public int getTimestamp() {
		return this.mTimestamp;
	}

	public State getState() {
		return mState;
	}

	public String getId() {
		return contactId;
	}

	public String getMsisdn() {
		return mMsisdn;
	}

	@Override
	public String toString() {
		return "Conversation [mMessage=" + mMessage + ", mMsisdn=" + mMsisdn
				+ ", contactId=" + contactId + ", mTimestamp=" + mTimestamp
				+ ", mIsSent=" + mIsSent + ", mState=" + mState + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contactId == null) ? 0 : contactId.hashCode());
		result = prime * result + (mIsSent ? 1231 : 1237);
		result = prime * result
				+ ((mMessage == null) ? 0 : mMessage.hashCode());
		result = prime * result + ((mMsisdn == null) ? 0 : mMsisdn.hashCode());
		result = prime * result + ((mState == null) ? 0 : mState.hashCode());
		result = prime * result + mTimestamp;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConvMessage other = (ConvMessage) obj;
		if (contactId == null) {
			if (other.contactId != null)
				return false;
		} else if (!contactId.equals(other.contactId))
			return false;
		if (mIsSent != other.mIsSent)
			return false;
		if (mMessage == null) {
			if (other.mMessage != null)
				return false;
		} else if (!mMessage.equals(other.mMessage))
			return false;
		if (mMsisdn == null) {
			if (other.mMsisdn != null)
				return false;
		} else if (!mMsisdn.equals(other.mMsisdn))
			return false;
		if (mState != other.mState)
			return false;
		if (mTimestamp != other.mTimestamp)
			return false;
		return true;
	}

	private String mMessage;
	private String mMsisdn;
	private String contactId;
	private int mTimestamp;
	private boolean mIsSent;
	public enum State {SENT, DELIVERED, RECEIVED };
	private State mState;

}
