package com.bsb.hike.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.NUXConstants;
import com.bsb.hike.R;
import com.bsb.hike.BitmapModule.BitmapUtils;
import com.bsb.hike.BitmapModule.HikeBitmapFactory;
import com.bsb.hike.models.ContactInfo;
import com.bsb.hike.models.ContactInfo.FavoriteType;
import com.bsb.hike.models.NuxSelectFriends;
import com.bsb.hike.models.StatusMessage;
import com.bsb.hike.modules.contactmgr.ContactManager;
import com.bsb.hike.smartImageLoader.IconLoader;
import com.bsb.hike.tasks.FetchFriendsTask;
import com.bsb.hike.utils.EmoticonConstants;
import com.bsb.hike.utils.Logger;
import com.bsb.hike.utils.NUXManager;
import com.bsb.hike.utils.Utils;
import com.bsb.hike.utils.Utils.WhichScreen;
import com.bsb.hike.view.PinnedSectionListView.PinnedSectionListAdapter;

public class ComposeChatAdapter extends FriendsAdapter implements PinnedSectionListAdapter
{

	private Map<String, ContactInfo> selectedPeople;

	private Map<String, ContactInfo> existingParticipants;

	private boolean showCheckbox, showExtraAtFirst;

	private int mIconImageSize;

	private IconLoader iconloader;

	private boolean fetchGroups;

	private boolean fetchRecents;
	
	private boolean fetchRecentlyJoined;

	private String existingGroupId;

	private String sendingMsisdn;

	private int statusForEmptyContactInfo;

	private List<ContactInfo> newContactsList;

	private boolean isCreatingOrEditingGroup;

	private boolean lastSeenPref;
	
	private boolean showDefaultEmptyList;
	
	private boolean nuxStateActive = false;

	public ComposeChatAdapter(Context context, ListView listView, boolean fetchGroups, boolean fetchRecents, boolean fetchRecentlyJoined, String existingGroupId, String sendingMsisdn, FriendsListFetchedCallback friendsListFetchedCallback, boolean showSMSContacts)
	{
		super(context, listView, friendsListFetchedCallback, ContactInfo.lastSeenTimeComparatorWithoutFav);
		selectedPeople = new LinkedHashMap<String, ContactInfo>();
		existingParticipants = new HashMap<String, ContactInfo>();
		mIconImageSize = context.getResources().getDimensionPixelSize(R.dimen.icon_picture_size);
		iconloader = new IconLoader(context, mIconImageSize);
		iconloader.setDefaultAvatarIfNoCustomIcon(true);
		iconloader.setImageFadeIn(false);

		this.existingGroupId = existingGroupId;
		this.sendingMsisdn = sendingMsisdn;
		this.fetchGroups = fetchGroups;
		this.fetchRecents = fetchRecents;
		this.fetchRecentlyJoined = fetchRecentlyJoined;
		
		groupsList = new ArrayList<ContactInfo>(0);
		groupsStealthList = new ArrayList<ContactInfo>(0);
		filteredGroupsList = new ArrayList<ContactInfo>(0);
		

		recentContactsList = new ArrayList<ContactInfo>(0);
		recentStealthContactsList = new ArrayList<ContactInfo>(0);
		filteredRecentsList = new ArrayList<ContactInfo>(0);

		this.lastSeenPref = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(HikeConstants.LAST_SEEN_PREF, true);
		/*
		 * We should show sms contacts section in new compose
		 */
		this.showSMSContacts = showSMSContacts;
	}

	public void setIsCreatingOrEditingGroup(boolean b)
	{
		isCreatingOrEditingGroup = b;
	}
	
	public void setNuxStateActive(boolean nuxStateActive) {
		this.nuxStateActive = nuxStateActive;
	}

	@Override
	public void executeFetchTask()
	{
		setLoadingView();
		FetchFriendsTask fetchFriendsTask;
		if(nuxStateActive){
			boolean fetchHikeContacts = true;
			boolean fetchSMSContacts = true;
			boolean fetchRecommendedContacts;
			boolean fetchHideListContacts;
			
			
			NuxSelectFriends nuxPojo = NUXManager.getInstance().getNuxSelectFriendsPojo();
			fetchHideListContacts = (nuxPojo.getHideList() != null && !nuxPojo.getHideList().isEmpty());
			fetchRecommendedContacts = (nuxPojo.getRecoList() != null && !nuxPojo.getRecoList().isEmpty());
			
			int contactsShown = nuxPojo.getContactSectionType();
			switch(NUXConstants.ContactSectionTypeEnum.getEnum(contactsShown)){
				case none : 
					fetchHikeContacts = false;
					fetchSMSContacts = false;
					break;
				case nonhike:
					fetchHikeContacts = false;
					break;
				case hike : 
					fetchSMSContacts = false;
					break;
				case both :
				case all :
				default:
						
			}
			
			fetchFriendsTask = new FetchFriendsTask(this, context, friendsList, hikeContactsList, smsContactsList, recentContactsList,recentlyJoinedHikeContactsList, friendsStealthList, hikeStealthContactsList,
					smsStealthContactsList, recentStealthContactsList, filteredFriendsList, filteredHikeContactsList, filteredSmsContactsList, groupsList, groupsStealthList, nuxRecommendedList, nuxFilteredRecoList, filteredGroupsList, filteredRecentsList,filteredRecentlyJoinedHikeContactsList,
					existingParticipants, sendingMsisdn, false, existingGroupId, isCreatingOrEditingGroup, fetchSMSContacts, false, false , false, showDefaultEmptyList, fetchHikeContacts, false, fetchRecommendedContacts, fetchHideListContacts);
			
		} else {
			fetchFriendsTask = new FetchFriendsTask(this, context, friendsList, hikeContactsList, smsContactsList, recentContactsList,recentlyJoinedHikeContactsList, friendsStealthList, hikeStealthContactsList,
					smsStealthContactsList, recentStealthContactsList, filteredFriendsList, filteredHikeContactsList, filteredSmsContactsList, groupsList, groupsStealthList, null, null, filteredGroupsList, filteredRecentsList,filteredRecentlyJoinedHikeContactsList,
					existingParticipants, sendingMsisdn, fetchGroups, existingGroupId, isCreatingOrEditingGroup, showSMSContacts, false, fetchRecents , fetchRecentlyJoined, showDefaultEmptyList, true, true, false , false );
		}
		Utils.executeAsyncTask(fetchFriendsTask);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewType viewType = ViewType.values()[getItemViewType(position)];

		ContactInfo contactInfo = null;
		ViewHolder holder = null;

		if (convertView == null)
		{
			convertView = inflateView(viewType, parent);

		}

		contactInfo = getItem(position);
		// either section or other we do have
		if (viewType == ViewType.SECTION)
		{
			TextView tv = (TextView) convertView.findViewById(R.id.name);
			tv.setText(contactInfo.getName());

			TextView count = (TextView) convertView.findViewById(R.id.count);
			count.setText(contactInfo.getMsisdn());
			// set section heading
			if (contactInfo.getPhoneNum() != null && contactInfo.getPhoneNum().equals(FRIEND_PHONE_NUM))
			{
				tv.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_favorites_star), null, null, null);
				tv.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.favorites_star_icon_drawable_padding));
			}
			else
			{
				tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}

		}
		else if (viewType == ViewType.EXTRA)
		{
			TextView tv = (TextView) convertView.findViewById(R.id.contact);
			tv.setText(R.string.compose_chat_heading);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
			String msisdn = contactInfo.getMsisdn();
			holder.msisdn = msisdn;

			holder.status.setText(contactInfo.getMsisdn());

			String name = contactInfo.getName();
			if(TextUtils.isEmpty(name))
			{
				holder.name.setText(msisdn);
			}
			else
			{
				Integer startIndex = contactSpanStartIndexes.get(msisdn);
				if(startIndex!=null)
				{
					holder.name.setText(getSpanText(name, startIndex), TextView.BufferType.SPANNABLE);
				}
				else
				{
					holder.name.setText(name);
				}
			}

			if (viewType == ViewType.NEW_CONTACT)
			{
				holder.status.setTextColor(context.getResources().getColor(R.color.list_item_subtext));
				holder.status.setText(statusForEmptyContactInfo);
				holder.statusMood.setVisibility(View.GONE);
				holder.onlineIndicator.setVisibility(View.GONE);
			}
			else if (contactInfo.getFavoriteType() == FavoriteType.FRIEND || contactInfo.getFavoriteType() == FavoriteType.REQUEST_RECEIVED)
			{
				holder.status.setText("is friend");
				StatusMessage lastStatusMessage = getLastStatusMessagesMap().get(contactInfo.getMsisdn());
				if (lastStatusMessage != null)
				{
					holder.status.setTextColor(context.getResources().getColor(R.color.list_item_subtext));
					switch (lastStatusMessage.getStatusMessageType())
					{
					case TEXT:
						holder.status.setText(lastStatusMessage.getText());
						if (lastStatusMessage.hasMood())
						{
							holder.statusMood.setVisibility(View.VISIBLE);
							holder.statusMood.setImageResource(EmoticonConstants.moodMapping.get(lastStatusMessage.getMoodId()));
						}
						else
						{
							holder.statusMood.setVisibility(View.GONE);
						}
						break;

					case PROFILE_PIC:
						holder.status.setText(R.string.changed_profile);
						holder.statusMood.setVisibility(View.GONE);
						break;

					default:
						break;
					}
				}
				else
				{
					holder.status.setTextColor(context.getResources().getColor(R.color.list_item_subtext));
					holder.status.setText(contactInfo.getMsisdn());
					holder.statusMood.setVisibility(View.GONE);
				}
				if (lastSeenPref && contactInfo.getOffline() == 0 && !showCheckbox)
				{
					holder.onlineIndicator.setVisibility(View.VISIBLE);
					holder.onlineIndicator.setImageResource(R.drawable.ic_online_green_dot);
				}
				else
				{
					holder.onlineIndicator.setVisibility(View.GONE);
				}
			}
			else
			{
				holder.status.setTextColor(context.getResources().getColor(R.color.list_item_subtext));
				holder.status.setText(Utils.isGroupConversation(contactInfo.getMsisdn()) ? contactInfo.getPhoneNum():contactInfo.getMsisdn());
				holder.statusMood.setVisibility(View.GONE);
				holder.onlineIndicator.setVisibility(View.GONE);
				if (viewType != ViewType.FRIEND && viewType != ViewType.FRIEND_REQUEST)
				{
					if (!contactInfo.isOnhike() && !showCheckbox)
					{
						long inviteTime = contactInfo.getInviteTime();
						if (inviteTime == 0)
						{
							holder.inviteIcon.setVisibility(View.VISIBLE);
							holder.inviteText.setVisibility(View.GONE);
							holder.divider.setVisibility(View.VISIBLE);
							holder.inviteIcon.setTag(contactInfo);
							holder.inviteIcon.setOnClickListener(new OnClickListener()
							{

								public void onClick(View v)
								{
									ContactInfo contactInfo = (ContactInfo) v.getTag();
									Utils.sendInviteUtil(contactInfo, context, HikeConstants.SINGLE_INVITE_SMS_ALERT_CHECKED, context.getString(R.string.native_header),
											context.getString(R.string.native_info), WhichScreen.SMS_SECTION);
									notifyDataSetChanged();
								}
							});
						}
						else
						{

							holder.inviteIcon.setVisibility(View.GONE);
							holder.inviteText.setVisibility(View.VISIBLE);
							holder.divider.setVisibility(View.GONE);
						}
					}
				}
			}

			/*
			 * We don't have an avatar for new contacts. So set a hard coded one
			 */
			if (viewType == ViewType.NEW_CONTACT)
			{
				holder.userImage.setImageDrawable(HikeMessengerApp.getLruCache().getDefaultAvatar(1));
			}
			else
			{
				updateViewsRelatedToAvatar(convertView, contactInfo);
			}

			if (showCheckbox)
			{
				holder.checkbox.setVisibility(View.VISIBLE);
				if (selectedPeople.containsKey(contactInfo.getMsisdn()))
				{

					holder.checkbox.setChecked(true);
				}
				else
				{
					holder.checkbox.setChecked(false);
				}
			}
			else
			{
				holder.checkbox.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	private void updateViewsRelatedToAvatar(View parentView, ContactInfo contactInfo)
	{
		ViewHolder holder = (ViewHolder) parentView.getTag();

		/*
		 * If the viewholder's msisdn is different from the converstion's msisdn, it means that the viewholder is currently being used for a different conversation. We don't need
		 * to do anything here then.
		 */
		if (!contactInfo.getMsisdn().equals(holder.msisdn))
		{
			return;
		}

		holder.userImage.setScaleType(ScaleType.FIT_CENTER);
		String id = contactInfo.isGroupConversationContact() ? contactInfo.getId() : contactInfo.getMsisdn();
		iconloader.loadImage(id, holder.userImage, isListFlinging, false, true);
	}

	private View inflateView(ViewType viewType, ViewGroup parent)
	{
		View convertView = null;
		ViewHolder holder = null;
		switch (viewType)
		{
		case SECTION:
			convertView = LayoutInflater.from(context).inflate(R.layout.friends_group_view, null);
			break;
		case EXTRA:
			convertView = LayoutInflater.from(context).inflate(R.layout.compose_chat_header, null);
			break;
		case FRIEND:
		case FRIEND_REQUEST:
			convertView = LayoutInflater.from(context).inflate(R.layout.friends_child_view, null);
			holder = new ViewHolder();
			holder.userImage = (ImageView) convertView.findViewById(R.id.avatar);
			holder.name = (TextView) convertView.findViewById(R.id.contact);
			holder.status = (TextView) convertView.findViewById(R.id.last_seen);
			holder.statusMood = (ImageView) convertView.findViewById(R.id.status_mood);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.onlineIndicator = (ImageView) convertView.findViewById(R.id.online_indicator);
			convertView.setTag(holder);
			break;
		default:
			convertView = LayoutInflater.from(context).inflate(R.layout.hike_list_item, parent, false);
			holder = new ViewHolder();
			holder.userImage = (ImageView) convertView.findViewById(R.id.contact_image);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.status = (TextView) convertView.findViewById(R.id.number);
			holder.statusMood = (ImageView) convertView.findViewById(R.id.status_mood);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.onlineIndicator = (ImageView) convertView.findViewById(R.id.online_indicator);
			holder.inviteText = (TextView) convertView.findViewById(R.id.invite_Text);
			holder.inviteIcon = (ImageView) convertView.findViewById(R.id.invite_icon);
			holder.divider = (View) convertView.findViewById(R.id.invite_divider);
			convertView.setTag(holder);
			break;
		}
		return convertView;
	}

	private static class ViewHolder
	{
		ImageView userImage;

		TextView name;

		TextView status;

		CheckBox checkbox;

		ImageView statusMood;

		ImageView onlineIndicator;

		String msisdn;

		View divider;

		TextView inviteText;

		ImageView inviteIcon;
	}

	@Override
	protected List<List<ContactInfo>> makeOriginalList()
	{
		if(showDefaultEmptyList)
		{
			List<List<ContactInfo>> resultList = new ArrayList<List<ContactInfo>>();
			return resultList;
		}
		else
		{
			return super.makeOriginalList();
		}
	}

	@Override
	public void makeCompleteList(boolean filtered)
	{
		makeCompleteList(filtered, false);
	}

	@Override
	public void makeCompleteList(boolean filtered, boolean firstFetch)
	{
		if (firstFetch)
		{
			friendsListFetchedCallback.listFetched();
		}

		boolean shouldContinue = makeSetupForCompleteList(filtered);

		if (!shouldContinue)
		{
			return;
		}
		
		if(nuxRecommendedList != null && !nuxRecommendedList.isEmpty())
		{
			String recoSectionHeader = NUXManager.getInstance().getNuxSelectFriendsPojo().getRecoSectionTitle();
			ContactInfo recommendedSection = new ContactInfo(SECTION_ID, Integer.toString(nuxFilteredRecoList.size()), recoSectionHeader, RECOMMENDED);
			if(nuxFilteredRecoList.size() > 0){
				completeList.add(recommendedSection);
				completeList.addAll(nuxFilteredRecoList);
			}
		}

		if(fetchRecentlyJoined && !recentlyJoinedHikeContactsList.isEmpty())
		{
			ContactInfo recentsSection = new ContactInfo(SECTION_ID, Integer.toString(filteredRecentlyJoinedHikeContactsList.size()), context.getString(R.string.recently_joined_hike), RECENTLY_JOINED);
			if (filteredRecentlyJoinedHikeContactsList.size() > 0)
			{
				completeList.add(recentsSection);
				completeList.addAll(filteredRecentlyJoinedHikeContactsList);
			}
		}
		
		// hack for header, as we are using pinnedSectionListView
		if(fetchRecents && !recentContactsList.isEmpty())
		{
			ContactInfo recentsSection = new ContactInfo(SECTION_ID, Integer.toString(filteredRecentsList.size()), context.getString(R.string.recent_chats), RECENT_PHONE_NUM);
			if (filteredRecentsList.size() > 0)
			{
				completeList.add(recentsSection);
				completeList.addAll(filteredRecentsList);
			}
		}

		if (fetchGroups && !groupsList.isEmpty())
		{
			ContactInfo groupSection = new ContactInfo(SECTION_ID, Integer.toString(filteredGroupsList.size()), context.getString(R.string.group_chats_upper_case), GROUP_MSISDN);
			if (filteredGroupsList.size() > 0)
			{
				completeList.add(groupSection);
				completeList.addAll(filteredGroupsList);
			}
		}
		ContactInfo friendsSection = null;
		if (!filteredFriendsList.isEmpty())
		{
			friendsSection = new ContactInfo(SECTION_ID, Integer.toString(filteredFriendsList.size()), context.getString(R.string.favorites_upper_case), FRIEND_PHONE_NUM);
		}
		updateFriendsList(friendsSection, false, false);
		if (isHikeContactsPresent())
		{
			ContactInfo hikeContactsSection = new ContactInfo(SECTION_ID, Integer.toString(filteredHikeContactsList.size()), context.getString(R.string.hike_contacts),
					CONTACT_PHONE_NUM);
			updateHikeContactList(hikeContactsSection);
		}
		if (showSMSContacts)
		{
			ContactInfo smsContactsSection = new ContactInfo(SECTION_ID, Integer.toString(filteredSmsContactsList.size()), context.getString(R.string.sms_contacts),
					CONTACT_PHONE_NUM);
			updateSMSContacts(smsContactsSection);
		}
		if (newContactsList != null)
		{
			completeList.addAll(newContactsList);
		}
		if (completeList.size() != 0 && showExtraAtFirst)
		{
			// items are > 0
			ContactInfo header = new ContactInfo(EXTRA_ID, null, null, null);
			completeList.add(0, header);
		}

		notifyDataSetChanged();
		setEmptyView();
		friendsListFetchedCallback.completeListFetched();
		
		
		
	}

	public void addContact(ContactInfo contactInfo)
	{
		selectedPeople.put(contactInfo.getMsisdn(), contactInfo);
		notifyDataSetChanged();
	}

	public void removeContact(ContactInfo contactInfo)
	{
		selectedPeople.remove(contactInfo.getMsisdn());
		notifyDataSetChanged();
	}

	public void clearAllSelection(boolean showCheckbox)
	{
		selectedPeople.clear();
		this.showCheckbox = showCheckbox;
		notifyDataSetChanged();
	}

	public void showCheckBoxAgainstItems(boolean showCheckbox)
	{
		this.showCheckbox = showCheckbox;

	}

	public ArrayList<ContactInfo> getAllSelectedContacts()
	{
		return new ArrayList<ContactInfo>(selectedPeople.values());
	}

	public List<String> getAllSelectedContactsMsisdns()
	{
		List<String> people = new ArrayList<String>(selectedPeople.keySet());
		return people;
	}
	
	/**
	 * It includes contact which are currently selected and existing to group (if applicable)
	 * 
	 * @return
	 */
	public int getSelectedContactCount()
	{
		return selectedPeople.size() + existingParticipants.size();
	}

	public int getCurrentSelection()
	{
		return selectedPeople.size();
	}

	public int getExistingSelection()
	{
		return existingParticipants.size();
	}

	public void setShowExtraAtFirst(boolean showExtraAtFirst)
	{
		this.showExtraAtFirst = showExtraAtFirst;
	}

	@Override
	protected void makeFilteredList(CharSequence constraint, List<List<ContactInfo>> resultList)
	{
		// TODO Auto-generated method stub

		super.makeFilteredList(constraint, resultList);
		// to add new section and number for user typed number
		String text = constraint.toString();
		if (isIntegers(text))
		{
			newContactsList = new ArrayList<ContactInfo>();
			ContactInfo section = new ContactInfo(SECTION_ID, null, context.getString(R.string.compose_chat_other_contacts), null);
			String normalisedMsisdn = getNormalisedMsisdn(text);
			ContactInfo info = new ContactInfo(normalisedMsisdn, normalisedMsisdn, normalisedMsisdn, text);
			newContactsList.add(section);
			newContactsList.add(info);
		}
		else
		{
			newContactsList = null;
		}
	}

	private boolean isIntegers(String input)
	{
		return input.matches("\\+?\\d+");
	}

	private String getNormalisedMsisdn(String textEntered)
	{
		return Utils.normalizeNumber(textEntered,
				context.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0).getString(HikeMessengerApp.COUNTRY_CODE, HikeConstants.INDIA_COUNTRY_CODE));
	}

	public void setStatusForEmptyContactInfo(int statusStringId)
	{
		this.statusForEmptyContactInfo = statusStringId;
	}

	public boolean isContactAdded(ContactInfo info)
	{
		return selectedPeople.containsKey(info.getMsisdn());
	}

	public boolean isContactPresentInExistingParticipants(ContactInfo info)
	{
		return existingParticipants.containsKey(info.getMsisdn());
	}

	@Override
	public int getItemViewType(int position)
	{
		ContactInfo info = getItem(position);
		if(Utils.isGroupConversation(info.getMsisdn()))
		{
			return super.getItemViewType(position);
		}
		else if (info.isUnknownContact() && info.getFavoriteType() == null)
		{
			return ViewType.NEW_CONTACT.ordinal();
		}
		return super.getItemViewType(position);
	}

	private boolean isListFlinging;

	public void setIsListFlinging(boolean b)
	{
		boolean notify = b != isListFlinging;

		isListFlinging = b;

		if (notify && !isListFlinging)
		{
			/*
			 * We don't want to call notifyDataSetChanged here since that causes the UI to freeze for a bit. Instead we pick out the views and update the avatars there.
			 */
			int count = listView.getChildCount();
			for (int i = 0; i < count; i++)
			{
				View view = listView.getChildAt(i);
				int indexOfData = listView.getFirstVisiblePosition() + i;

				ViewType viewType = ViewType.values()[getItemViewType(indexOfData)];
				ContactInfo contactInfo = getItem(indexOfData);

				/*
				 * Since sms contacts and dividers cannot have custom avatars, we simply skip these cases.
				 */
				if (viewType == ViewType.SECTION || viewType == ViewType.EXTRA || !contactInfo.isOnhike())
				{
					continue;
				}

				updateViewsRelatedToAvatar(view, getItem(indexOfData));
			}
		}
	}
	
	public IconLoader getIconLoader()
	{
		return iconloader;
	}
	
	public void selectAllContacts(boolean select)
	{
		if(select)
		{
			selectAllFromList(friendsList,recentContactsList,recentlyJoinedHikeContactsList,hikeContactsList,groupsList);
			
		}
		else
		{
			selectedPeople.clear();
		}
		notifyDataSetChanged();
	}
	
	public void preSelectContacts(HashSet<String> ... preSelectedMsisdnSets){
		int total = preSelectedMsisdnSets.length;
		for(int i=0;i<total;i++){
			HashSet<String> preSelectedSet = preSelectedMsisdnSets[i];
			if(preSelectedSet != null){
				for(String msisdn : preSelectedSet){
					if(msisdn != null && ContactManager.getInstance().getContact(msisdn) != null){
						selectedPeople.put(msisdn, ContactManager.getInstance().getContact(msisdn));
					}
				}
			}
		}
	}
	
	private void selectAllFromList(List<ContactInfo> ...lists){
		int total = lists.length;
		for(int i=0;i<total;i++){
			List<ContactInfo> list = lists[i];
			if(list!=null){
				for(ContactInfo contactInfo: list){
					if(contactInfo.isOnhike()){
						selectedPeople.put(contactInfo.getMsisdn(), contactInfo);
					}
				}
			}
		}
	}
	
	public void selectAllFromList(ArrayList<String> msisdns)
	{
		if (msisdns == null || msisdns.isEmpty())
		{
			return;
		}
		
		for (String msisdn : msisdns)
		{
			ContactInfo contactInfo = ContactManager.getInstance().getContact(msisdn, true, false);
			selectedPeople.put(msisdn, contactInfo);
		}
	}
	
}
