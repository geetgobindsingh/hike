package com.bsb.hike.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.R;
import com.bsb.hike.BitmapModule.BitmapUtils;
import com.bsb.hike.BitmapModule.HikeBitmapFactory;
import com.bsb.hike.db.HikeConversationsDatabase;
import com.bsb.hike.models.Sticker;
import com.bsb.hike.models.StickerCategory;
import com.bsb.hike.utils.Utils.ExternalStorageState;

public class StickerManager
{
	public static final String STICKERS_MOVED_EXTERNAL_TO_INTERNAL = "movedStickersExtToInt";
	
	public static final String RECENT_STICKER_SERIALIZATION_LOGIC_CORRECTED = "recentStickerSerializationCorrected";

	public static final String SHOWN_DEFAULT_STICKER_DOGGY_CATEGORY_POPUP = "shownDefaultStickerCategoryPopup";

	public static final String SHOWN_DEFAULT_STICKER_HUMANOID_CATEGORY_POPUP = "shownDefaultStickerHumanoidCategoryPopup";

	public static final String EXPRESSIONS_CATEGORY_INSERT_TO_DB = "defaultExpressionsCategoryInsertedToDB";

	public static final String HUMANOID_CATEGORY_INSERT_TO_DB = "secondCategoryInsertedToDB";

	public static final String REMOVED_CATGORY_IDS = "removedCategoryIds";

	public static final String SHOW_BOLLYWOOD_STICKERS = "showBollywoodStickers";

	public static final String RESET_REACHED_END_FOR_DEFAULT_STICKERS = "resetReachedEndForDefaultStickers";

	public static final String CORRECT_DEFAULT_STICKER_DIALOG_PREFERENCES = "correctDefaultStickerDialogPreferences";

	public static final String REMOVE_HUMANOID_STICKERS = "removeHumanoiStickers";

	public static final String SHOWN_STICKERS_TUTORIAL = "shownStickersTutorial";
	
	public static final String STICKERS_DOWNLOADED = "st_downloaded";

	public static final String STICKERS_FAILED = "st_failed";

	public static final String STICKER_DOWNLOAD_TYPE = "stDownloadType";

	public static final String STICKER_DATA_BUNDLE = "stickerDataBundle";
	
	public static final String STICKER_DOWNLOAD_FAILED_FILE_TOO_LARGE = "stickerDownloadFailedTooLarge";

	public static final String STICKER_CATEGORY = "stickerCategory";

	public static final String RECENT_STICKER_SENT = "recentStickerSent";

	public static final String RECENTS_UPDATED = "recentsUpdated";

	public static final String STICKER_ID = "stId";

	public static final String CATEGORY_ID = "catId";

	public static final String FWD_STICKER_ID = "fwdStickerId";

	public static final String FWD_CATEGORY_ID = "fwdCategoryId";

	public static final String STICKERS_UPDATED = "stickersUpdated";

	public static final String ADD_NO_MEDIA_FILE_FOR_STICKERS = "addNoMediaFileForStickers";

	public static final String DELETE_DEFAULT_DOWNLOADED_EXPRESSIONS_STICKER = "delDefaultDownloadedExpressionsStickers";
	
	public static final String HARCODED_STICKERS = "harcodedStickers";
	
	public static final String STICKER_IDS = "stickerIds";
	
	public static final String RESOURCE_IDS = "resourceIds";
	
	public static final String MOVED_HARDCODED_STICKERS_TO_SDCARD = "movedHardCodedStickersToSdcard";

	private static final String TAG = "StickerManager";

	public static int RECENT_STICKERS_COUNT = 30;
	
	public static final int SIZE_IMAGE = (int) (80 * Utils.densityMultiplier);
	
	public enum StickerCategoryId
	{
		recent
		{
			@Override
			public int resId()
			{
				return R.drawable.recents;
			}

			/*
			 * This is not required for recent category as we dont wanna show preview for recents. Returning random negative integer
			 */
			@Override
			public int previewResId()
			{
				return -10;
			}

			/* This is again not for recent category */
			@Override
			public String downloadPref()
			{
				return "rs";
			}
		},
		humanoid
		{
			@Override
			public int resId()
			{
				return R.drawable.humanoid;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_humanoid;
			}

			@Override
			public String downloadPref()
			{
				return "humanoidDownloadShown";
			}
		},
		expressions
		{
			@Override
			public int resId()
			{
				return R.drawable.expressions;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_expressions;
			}

			@Override
			public String downloadPref()
			{
				return "expDownloadShown";
			}
		},
		love
		{
			@Override
			public int resId()
			{
				return R.drawable.love;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_love;
			}

			@Override
			public String downloadPref()
			{
				return "loveDownloadShown";
			}
		},
		bollywood
		{
			@Override
			public int resId()
			{
				return R.drawable.bollywood;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_bollywood;
			}

			@Override
			public String downloadPref()
			{
				return "bollywoodDownloadShown";
			}
		},
		indian
		{
			@Override
			public int resId()
			{
				return R.drawable.indian;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_indian;
			}

			@Override
			public String downloadPref()
			{
				return "indianDownloadShown";
			}
		},
		doggy
		{
			@Override
			public int resId()
			{
				return R.drawable.doggy;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_doggy;
			}

			@Override
			public String downloadPref()
			{
				return "doggyDownloadShown";
			}
		},
		rageface
		{
			@Override
			public int resId()
			{
				return R.drawable.rageface;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_rageface;
			}

			@Override
			public String downloadPref()
			{
				return "rfDownloadShown";
			}
		},
		jelly
		{
			@Override
			public int resId()
			{
				return R.drawable.wicked_jellies;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_jelly;
			}

			@Override
			public String downloadPref()
			{
				return "JellyDownloadShown";
			}
		},
		sports
		{
			@Override
			public int resId()
			{
				return R.drawable.sports;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_sports;
			}

			@Override
			public String downloadPref()
			{
				return "sportsDownloadShown";
			}
		},

		humanoid2
		{
			@Override
			public int resId()
			{
				return R.drawable.humanoid2;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_humanoid2;
			}

			@Override
			public String downloadPref()
			{
				return "humanoid2DownloadShown";
			}
		},
		avatars
		{
			@Override
			public int resId()
			{
				return R.drawable.avtars;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_avtars;
			}

			@Override
			public String downloadPref()
			{
				return "avtarsDownloadShown";
			}
		},
		smileyexpressions
		{
			@Override
			public int resId()
			{
				return R.drawable.smileyexpressions;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_smilyexpressions;
			}

			@Override
			public String downloadPref()
			{
				return "smileyexpressionDownloadShown";
			}
		},
		kitty
		{
			@Override
			public int resId()
			{
				return R.drawable.kitty;
			}

			@Override
			public int previewResId()
			{
				return R.drawable.preview_kitty;
			}

			@Override
			public String downloadPref()
			{
				return "kittyDownloadShown";
			}
		},
		unknown
		{
			@Override
			public int resId()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int previewResId()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String downloadPref()
			{
				// TODO Auto-generated method stub
				return null;
			}

		};

		public static StickerCategoryId getCategoryIdFromName(String value)
		{
			if (value == null)
				throw new IllegalArgumentException();
			for (StickerCategoryId v : values())
				if (value.equalsIgnoreCase(v.name()))
					return v;
			throw new IllegalArgumentException();
		}

		public abstract int resId();

		public abstract int previewResId();

		public abstract String downloadPref();
	};

	public FilenameFilter stickerFileFilter = new FilenameFilter()
	{
		@Override
		public boolean accept(File file, String fileName)
		{
			return !".nomedia".equalsIgnoreCase(fileName);
		}
	};

	public Map<String, StickerTaskBase> stickerTaskMap;

	private Set<Sticker> recentStickers;

	private List<StickerCategory> stickerCategories;

	private Context context;

	private static SharedPreferences preferenceManager;

	private static StickerManager instance;

	public static StickerManager getInstance()
	{
		if (instance == null)
		{
			synchronized (StickerManager.class)
			{
				if (instance == null)
					instance = new StickerManager();
			}
		}
		return instance;
	}

	private StickerManager()
	{
		stickerCategories = new ArrayList<StickerCategory>();
		if (stickerTaskMap == null)
		{
			stickerTaskMap = new HashMap<String, StickerTaskBase>();
		}
	}

	public void init(Context ctx)
	{
		context = ctx;
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);

	}
	
	public void doInitialSetup(Context applicationContext)
	{
		// move stickers from external to internal if not done
		init(applicationContext);
		SharedPreferences settings = applicationContext.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0);
		if(!settings.getBoolean(StickerManager.RECENT_STICKER_SERIALIZATION_LOGIC_CORRECTED, false)){
			updateRecentStickerFile(settings);
		}
		
		SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		/*
		 * If we had earlier removed bollywood stickers we need to display them again.
		 */
		if (settings.contains(StickerManager.SHOW_BOLLYWOOD_STICKERS))
		{
			setupBollywoodCategoryVisibility(settings);
		}

		setupStickerCategoryList(settings);
		loadRecentStickers();

		/*
		 * This preference has been used here because of a bug where we were inserting this key in the settings preference
		 */
		if (!preferenceManager.contains(StickerManager.REMOVE_HUMANOID_STICKERS))
		{
			removeHumanoidSticker();
		}
		
		if (!preferenceManager.getBoolean(StickerManager.EXPRESSIONS_CATEGORY_INSERT_TO_DB, false))
		{
			insertExpressionsCategory();
		}

		if (!preferenceManager.getBoolean(StickerManager.HUMANOID_CATEGORY_INSERT_TO_DB, false))
		{
			insertHumanoidCategory();
		}

		if (!settings.getBoolean(StickerManager.RESET_REACHED_END_FOR_DEFAULT_STICKERS, false))
		{
			resetReachedEndForDefaultStickers();
		}

		if (!settings.getBoolean(StickerManager.ADD_NO_MEDIA_FILE_FOR_STICKERS, false))
		{
			addNoMediaFilesToStickerDirectories();
		}
		/*
		 * Adding these preferences since they are used in the load more stickers logic.
		 */
		if (!settings.getBoolean(StickerManager.CORRECT_DEFAULT_STICKER_DIALOG_PREFERENCES, false))
		{
			setDialoguePref();
		}

		/*
		 * this code path will be for users upgrading to the build where we make expressions a default loaded category
		 */
		if (!settings.getBoolean(StickerManager.DELETE_DEFAULT_DOWNLOADED_EXPRESSIONS_STICKER, false))
		{
			settings.edit().putBoolean(StickerManager.DELETE_DEFAULT_DOWNLOADED_EXPRESSIONS_STICKER, true).commit();

			if (checkIfStickerCategoryExists(StickerCategoryId.doggy.name()))
			{
				HikeConversationsDatabase.getInstance().stickerUpdateAvailable(StickerCategoryId.doggy.name());
				StickerManager.getInstance().setStickerUpdateAvailable(StickerCategoryId.doggy.name(), true);
			}
			else
			{
				HikeConversationsDatabase.getInstance().removeStickerCategory(StickerCategoryId.doggy.name());
			}
		}
	}

	public void loadRecentStickers()
	{
		recentStickers = getSortedListForCategory(StickerCategoryId.recent, getInternalStickerDirectoryForCategoryId(context, StickerCategoryId.recent.name()));
		if(recentStickers.isEmpty())
		{
			addDefaultRecentSticker();
		}
	}

	public List<StickerCategory> getStickerCategoryList()
	{
		return stickerCategories;
	}

	public void setupStickerCategoryList(SharedPreferences preferences)
	{
		/*
		 * TODO : This will throw an exception in case of remove category as, this function will be called from mqtt thread and stickerCategories will be called from UI thread
		 * also.
		 */
		stickerCategories = new ArrayList<StickerCategory>();
		EnumMap<StickerCategoryId, StickerCategory> stickerDataMap = HikeConversationsDatabase.getInstance().stickerDataForCategories();
		for (StickerCategoryId s : StickerCategoryId.values())
		{
			if (s.equals(StickerCategoryId.recent))
			{
				stickerCategories.add(new StickerCategory(StickerCategoryId.recent));
				continue;
			}
			else if (StickerCategoryId.unknown.equals(s))
			{
				/*
				 * We don't want to add the unknown category to this list.
				 */
				continue;
			}
			StickerCategory cat = stickerDataMap.get(s);
			if (cat != null)
				stickerCategories.add(cat);
			else
				stickerCategories.add(new StickerCategory(s, false, false));
		}
		String removedIds = preferences.getString(REMOVED_CATGORY_IDS, "[]");

		try
		{
			JSONArray removedIdArray = new JSONArray(removedIds);
			for (int i = 0; i < removedIdArray.length(); i++)
			{
				String removedCategoryId = removedIdArray.getString(i);
				removeCategoryFromList(removedCategoryId);
			}
		}
		catch (JSONException e)
		{
			Logger.w("HikeMessengerApp", "Invalid JSON", e);
		}
	}

	private void removeCategoryFromList(String removedCategoryId)
	{
		Iterator<StickerCategory> it = stickerCategories.iterator();
		while (it.hasNext())
		{
			StickerCategory cat = it.next();
			if (cat.categoryId.name().equals(removedCategoryId))
			{
				removeCategoryFromRecents(cat);
				it.remove();
			}
		}
	}

	private void removeCategoryFromRecents(StickerCategory category)
	{
		String categoryDirPath = getStickerDirectoryForCategoryId(context, category.categoryId.name());
		if (categoryDirPath != null)
		{
			File smallCatDir = new File(categoryDirPath + HikeConstants.SMALL_STICKER_ROOT);
			File bigCatDir = new File(categoryDirPath);
			if (smallCatDir.exists())
			{
				String[] stickerIds = smallCatDir.list();
				for (String stickerId : stickerIds)
				{
					recentStickers.remove(new Sticker(category, stickerId));
				}
			}
			Utils.deleteFile(bigCatDir);
		}
	}

	public void insertExpressionsCategory()
	{
		HikeConversationsDatabase.getInstance().insertExpressionsStickerCategory();
		Editor editor = preferenceManager.edit();
		editor.putBoolean(EXPRESSIONS_CATEGORY_INSERT_TO_DB, true);
		editor.commit();
	}

	public void insertHumanoidCategory()
	{
		HikeConversationsDatabase.getInstance().insertHumanoidStickerCategory();
		Editor editor = preferenceManager.edit();
		editor.putBoolean(HUMANOID_CATEGORY_INSERT_TO_DB, true);
		editor.commit();
	}

	public void resetReachedEndForDefaultStickers()
	{
		HikeConversationsDatabase.getInstance().updateReachedEndForCategory(StickerCategoryId.expressions.name(), false);
		HikeConversationsDatabase.getInstance().updateReachedEndForCategory(StickerCategoryId.humanoid.name(), false);
		Editor editor = context.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0).edit();
		editor.putBoolean(RESET_REACHED_END_FOR_DEFAULT_STICKERS, true);
		editor.commit();
	}

	public void setDialoguePref()
	{
		SharedPreferences settings = context.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0);
		Editor editor = settings.edit();
		editor.putBoolean(StickerCategoryId.humanoid.downloadPref(), settings.getBoolean(SHOWN_DEFAULT_STICKER_HUMANOID_CATEGORY_POPUP, false));
		editor.putBoolean(StickerCategoryId.doggy.downloadPref(), settings.getBoolean(SHOWN_DEFAULT_STICKER_DOGGY_CATEGORY_POPUP, false));
		editor.putBoolean(StickerManager.CORRECT_DEFAULT_STICKER_DIALOG_PREFERENCES, true);
		editor.commit();
	}

	public void removeHumanoidSticker()
	{
		String categoryDirPath = getStickerDirectoryForCategoryId(context, StickerCategoryId.humanoid.name());
		if (categoryDirPath != null)
		{
			File categoryDir = new File(categoryDirPath);
			Utils.deleteFile(categoryDir);
			Editor editor = preferenceManager.edit();
			editor.putBoolean(REMOVE_HUMANOID_STICKERS, true);
			editor.commit();
		}
	}
	
	public void addNoMediaFilesToStickerDirectories()
	{
		File dir = context.getExternalFilesDir(null);
		if (dir == null)
		{
			return;
		}
		String rootPath = dir.getPath() + HikeConstants.STICKERS_ROOT;
		File root = new File(rootPath);
		if (!root.exists())
		{
			return;
		}
		addNoMedia(root);

		Editor editor = preferenceManager.edit();
		editor.putBoolean(ADD_NO_MEDIA_FILE_FOR_STICKERS, true);
		editor.commit();
	}

	private void addNoMedia(File directory)
	{
		try
		{
			String path = directory.getPath();
			if (path.endsWith(HikeConstants.LARGE_STICKER_ROOT) || path.endsWith(HikeConstants.SMALL_STICKER_ROOT))
			{
				Utils.makeNoMediaFile(directory);
			}
			else if (directory.isDirectory())
			{
				for (File file : directory.listFiles())
				{
					addNoMedia(file);
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	public static void setStickersForIndianUsers(boolean isIndianUser, SharedPreferences prefs)
	{
		HikeMessengerApp.isIndianUser = isIndianUser;
		if (!prefs.contains(StickerManager.SHOW_BOLLYWOOD_STICKERS))
		{
			setupBollywoodCategoryVisibility(prefs);
		}
	}

	public static void setupBollywoodCategoryVisibility(SharedPreferences prefs)
	{
		/*
		 * We now show the bollywood category for all users.
		 */
		Editor editor = prefs.edit();
		editor.remove(SHOW_BOLLYWOOD_STICKERS);
		editor.remove(REMOVED_CATGORY_IDS);
		editor.commit();
	}

	public Set<Sticker> getRecentStickerList()
	{
		return recentStickers;
	}

	public void addRecentSticker(Sticker st)
	{
		boolean isRemoved = recentStickers.remove(st);
		if (isRemoved) // this means list size is less than 30
			recentStickers.add(st);
		else if (recentStickers.size() == RECENT_STICKERS_COUNT) // if size is already RECENT_STICKERS_COUNT remove first element and then add
		{
			synchronized (recentStickers)
			{
				Sticker firstSt = recentStickers.iterator().next();
				if (firstSt != null)
					recentStickers.remove(firstSt);
				recentStickers.add(st);
			}
		}
		else
		{
			recentStickers.add(st);
		}
	}
	
	public void addDefaultRecentSticker()
	{
	    String[] recentSticker = { "002_lol.png","003_teasing.png","112_watchadoing.png", "113_whereareyou.png","092_yo.png","069_hi.png" };
	    String[] recentCat = {  "expressions","humanoid", "expressions", "expressions","expressions", "humanoid" };
		
		int count = recentSticker.length;
		for (int i = 0; i < count; i++)
		{
			synchronized (recentStickers)
			{
				recentStickers.add(new Sticker(recentCat[i], recentSticker[i]));
			}	
		}
				
	}

	public void removeStickerFromRecents(Sticker st)
	{
		boolean rem = recentStickers.remove(st);

		Logger.d(TAG, "Sticker removed from recents : " + rem);

		// remove the sticker from cache too, recycling stuff is handled by the cache itself
		HikeMessengerApp.getLruCache().remove(st.getSmallStickerPath(context));
	}

	public void setStickerUpdateAvailable(String categoryId, boolean updateAvailable)
	{
		for (StickerCategory sc : stickerCategories)
		{
			if (sc.categoryId.name().equals(categoryId))
				sc.updateAvailable = updateAvailable;
		}
	}

	public StickerCategory getCategoryForIndex(int index)
	{
		if (index == -1 || index >= stickerCategories.size())
		{
			throw new IllegalArgumentException();
		}
		return stickerCategories.get(index);
	}

	private String getExternalStickerDirectoryForCategoryId(Context context, String catId)
	{
		File dir = context.getExternalFilesDir(null);
		if (dir == null)
		{
			return null;
		}
		return dir.getPath() + HikeConstants.STICKERS_ROOT + "/" + catId;
	}

	public String getInternalStickerDirectoryForCategoryId(Context context, String catId)
	{
		return context.getFilesDir().getPath() + HikeConstants.STICKERS_ROOT + "/" + catId;
	}

	/**
	 * Returns the directory for a sticker category.
	 * 
	 * @param context
	 * @param catId
	 * @return
	 */
	public String getStickerDirectoryForCategoryId(Context context, String catId)
	{
		/*
		 * We give a higher priority to external storage. If we find an exisiting directory in the external storage, we will return its path. Otherwise if there is an exisiting
		 * directory in internal storage, we return its path.
		 * 
		 * If the directory is not available in both cases, we return the external storage's path if external storage is available. Else we return the internal storage's path.
		 */
		boolean externalAvailable = false;
		ExternalStorageState st = Utils.getExternalStorageState();
		Logger.d(TAG, "External Storage state : " + st.name());
		if (st == ExternalStorageState.WRITEABLE)
		{
			externalAvailable = true;
			String stickerDirPath = getExternalStickerDirectoryForCategoryId(context, catId);
			Logger.d(TAG, "Sticker dir path : " + stickerDirPath);
			if (stickerDirPath == null)
			{
				return null;
			}

			File stickerDir = new File(stickerDirPath);

			if (stickerDir.exists())
			{
				Logger.d(TAG, "Sticker Dir exists .... so returning");
				return stickerDir.getPath();
			}
		}
		File stickerDir = new File(getInternalStickerDirectoryForCategoryId(context, catId));
		Logger.d(TAG, "Checking Internal Storage dir : " + stickerDir.getAbsolutePath());
		if (stickerDir.exists())
		{
			Logger.d(TAG, "Internal Storage dir exist so returning it.");
			return stickerDir.getPath();
		}
		if (externalAvailable)
		{
			Logger.d(TAG, "Returning external storage dir.");
			return getExternalStickerDirectoryForCategoryId(context, catId);
		}
		Logger.d(TAG, "Returning internal storage dir.");
		return getInternalStickerDirectoryForCategoryId(context, catId);
	}

	public boolean checkIfStickerCategoryExists(String categoryId)
	{
		String path = getStickerDirectoryForCategoryId(context, categoryId);
		if (path == null)
			return false;

		File categoryDir = new File(path + HikeConstants.SMALL_STICKER_ROOT);
		if (categoryDir.exists())
		{
			String[] stickerIds = categoryDir.list(stickerFileFilter);
			if (stickerIds.length > 0)
				return true;
			else
				return false;
		}
		return false;
	}

	public boolean isStickerDownloading(String key)
	{
		if (key != null)
			return stickerTaskMap.containsKey(key);
		return false;
	}

	public StickerTaskBase getTask(String key)
	{
		if (key == null)
			return null;
		return stickerTaskMap.get(key);
	}

	public void insertTask(String categoryId, StickerTaskBase downloadStickerTask)
	{
		stickerTaskMap.put(categoryId, downloadStickerTask);
	}

	public void removeTask(String key)
	{
		stickerTaskMap.remove(key);
	}

	public StickerCategory getCategoryForName(String categoryName)
	{
		for (int i = 0; i < stickerCategories.size(); i++)
		{
			if (stickerCategories.get(i).categoryId.name().equals(categoryName))
				return stickerCategories.get(i);
		}
		return new StickerCategory(StickerCategoryId.unknown);
	}

	/***
	 * 
	 * @param catId
	 * @return
	 * 
	 *         This function can return null if file doesnot exist.
	 */
	public Set<Sticker> getSortedListForCategory(StickerCategoryId catId, String dirPath)
	{
		Set<Sticker> list = null;
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try
		{
			long t1 = System.currentTimeMillis();
			Logger.d(TAG, "Calling function get sorted list for category : " + catId.name());
			File dir = new File(dirPath);
			if (!dir.exists())
			{
				dir.mkdirs();
				return Collections.synchronizedSet(new LinkedHashSet<Sticker>(RECENT_STICKERS_COUNT));
			}
			File catFile = new File(dirPath, catId.name() + ".bin");
			if (!catFile.exists())
				return Collections.synchronizedSet(new LinkedHashSet<Sticker>(RECENT_STICKERS_COUNT));
			fileIn = new FileInputStream(catFile);
			in = new ObjectInputStream(fileIn);
			int size = in.readInt();
			list = Collections.synchronizedSet(new LinkedHashSet<Sticker>(size));
			for (int i = 0; i < size; i++)
			{
				try
				{
					Sticker s = new Sticker();
					s.deSerializeObj(in);
					list.add(s);
				}
				catch (Exception e)
				{
					Logger.e(TAG, "Exception while deserializing sticker", e);
				}
			}
			long t2 = System.currentTimeMillis();
			Logger.d(TAG, "Time in ms to get sticker list of category : " + catId + " from file :" + (t2 - t1));
		}
		catch (Exception e)
		{
			Logger.e(TAG, "Exception while reading category file.", e);
			list = Collections.synchronizedSet(new LinkedHashSet<Sticker>(RECENT_STICKERS_COUNT));
		}
		finally
		{
			if (in != null)
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			if (fileIn != null)
				try
				{
					fileIn.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
		}
		return list;
	}

	public void saveSortedListForCategory(StickerCategoryId catId, Set<Sticker> list)
	{
		try
		{
			if (list.size() == 0)
				return;

			long t1 = System.currentTimeMillis();
			String extDir = getInternalStickerDirectoryForCategoryId(context, catId.name());
			File dir = new File(extDir);
			if (!dir.exists() && !dir.mkdirs())
			{
				return;
			}
			File catFile = new File(extDir, catId.name() + ".bin");
			FileOutputStream fileOut = new FileOutputStream(catFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeInt(list.size());
			synchronized (recentStickers)
			{
				Iterator<Sticker> it = list.iterator();
				Sticker st = null;
				while (it.hasNext())
				{
					try
					{
						st = it.next();
						st.serializeObj(out);
					}
					catch (Exception e)
					{
						Logger.e(TAG, "Exception while serializing a sticker : " + st.getStickerId(), e);
					}
				}
			}
			out.flush();
			out.close();
			fileOut.close();
			long t2 = System.currentTimeMillis();
			Logger.d(TAG, "Time in ms to save sticker list of category : " + catId + " to file :" + (t2 - t1));
		}
		catch (Exception e)
		{
			Logger.e(TAG, "Exception while saving category file.", e);
		}
	}

	public void deleteStickers()
	{
		/*
		 * First delete all stickers, if any, in the internal memory
		 */
		String dirPath = context.getFilesDir().getPath() + HikeConstants.STICKERS_ROOT;
		File dir = new File(dirPath);
		if (dir.exists())
		{
			Utils.deleteFile(dir);
		}

		/*
		 * Next is the external memory. We first check if its available or not.
		 */
		if (Utils.getExternalStorageState() != ExternalStorageState.WRITEABLE)
		{
			return;
		}
		String extDirPath = context.getExternalFilesDir(null).getPath() + HikeConstants.STICKERS_ROOT;
		File extDir = new File(extDirPath);
		if (extDir.exists())
		{
			Utils.deleteFile(extDir);
		}

		/* Delete recent stickers */
		String recentsDir = getStickerDirectoryForCategoryId(context, StickerCategoryId.recent.name());
		File rDir = new File(recentsDir);
		if (rDir.exists())
			Utils.deleteFile(rDir);
	}

	public void removeStickersFromRecents(String categoryName, String[] stickerIds)
	{
		for (String stickerId : stickerIds)
		{
			recentStickers.remove(new Sticker(categoryName, stickerId));
		}
	}

	public void setContext(Context context)
	{
		this.context = context;
	}

	public void moveRecentStickerFileToInternal(Context context)
	{
		try
		{
			this.context = context;
			Logger.i("stickermanager", "moving recent file from external to internal");
			String recent = StickerCategoryId.recent.name();
			Utils.copyFile(getExternalStickerDirectoryForCategoryId(context, recent) + "/" + recent + ".bin", getInternalStickerDirectoryForCategoryId(context, recent) + "/"
					+ recent + ".bin", null);
			Logger.i("stickermanager", "moving finished recent file from external to internal");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void deleteDuplicateStickers(String parentDir, String[] bundledFileNames)
	{

		HashSet<String> originalNames = new HashSet<String>(bundledFileNames.length);
		for (String name : bundledFileNames)
		{
			originalNames.add(name);
		}

		deleteDuplicateFiles(originalNames, parentDir + File.separator + HikeConstants.SMALL_STICKER_ROOT);
		deleteDuplicateFiles(originalNames, parentDir + File.separator + HikeConstants.LARGE_STICKER_ROOT);

	}

	public void deleteDuplicateFiles(HashSet<String> originalNames, String fileDir)
	{
		File dir = new File(fileDir);
		String[] fileNames = null;
		if (dir.exists() && dir.isDirectory())
		{
			fileNames = dir.list();
		}
		else
		{
			return;
		}
		for (String fileName : fileNames)
		{
			if (originalNames.contains(fileName))
			{
				File file = new File(fileDir, fileName);
				if (file.exists())
				{
					file.delete();
				}
			}
		}
	}
	
	private String getStickerRootDirectory(Context context) {
		boolean externalAvailable = false;
		ExternalStorageState st = Utils.getExternalStorageState();
		if (st == ExternalStorageState.WRITEABLE) {
			externalAvailable = true;
			String stickerDirPath = getExternalStickerRootDirectory(context);
			if (stickerDirPath == null) {
				return null;
			}

			File stickerDir = new File(stickerDirPath);

			if (stickerDir.exists()) {
				return stickerDir.getPath();
			}
		}
		File stickerDir = new File(getInternalStickerRootDirectory(context));
		if (stickerDir.exists()) {
			return stickerDir.getPath();
		}
		if (externalAvailable) {
			return getExternalStickerRootDirectory(context);
		}
		return getInternalStickerRootDirectory(context);
	}

	private String getExternalStickerRootDirectory(Context context) {
		File dir = context.getExternalFilesDir(null);
		if (dir == null) {
			return null;
		}
		return dir.getPath() + HikeConstants.STICKERS_ROOT;
	}

	private String getInternalStickerRootDirectory(Context context) {
		return context.getFilesDir().getPath() + HikeConstants.STICKERS_ROOT;
	}

	public Map<String, StickerCategoryId> getStickerToCategoryMapping(
			Context context) {
		String stickerRootDirectoryString = getStickerRootDirectory(context);

		/*
		 * Return null if the the path is null or empty
		 */
		if (TextUtils.isEmpty(stickerRootDirectoryString)) {
			return null;
		}

		File stickerRootDirectory = new File(stickerRootDirectoryString);

		/*
		 * Return null if the directory is null or does not exist
		 */
		if (stickerRootDirectory == null || !stickerRootDirectory.exists()) {
			return null;
		}

		Map<String, StickerCategoryId> stickerToCategoryMap = new HashMap<String, StickerManager.StickerCategoryId>();

		for (File stickerCategoryDirectory : stickerRootDirectory.listFiles()) {
			/*
			 * If this is not a directory we have no need for this file.
			 */
			if (!stickerCategoryDirectory.isDirectory()) {
				continue;
			}

			File stickerCategorySmallDirectory = new File(
					stickerCategoryDirectory.getAbsolutePath()
							+ HikeConstants.SMALL_STICKER_ROOT);

			/*
			 * We also don't want to do anything if the category does not have a
			 * small folder.
			 */
			if (stickerCategorySmallDirectory == null
					|| !stickerCategorySmallDirectory.exists()) {
				continue;
			}
			StickerCategoryId categoryId = null;
			try{
			categoryId = StickerCategoryId
					.valueOf(stickerCategoryDirectory.getName());
			}catch(IllegalArgumentException ie){
				continue;
			}

			for (File stickerFile : stickerCategorySmallDirectory.listFiles()) {
				stickerToCategoryMap.put(stickerFile.getName(), categoryId);
			}
		}

		return stickerToCategoryMap;
	}
	
	/**
	 * solves recent sticker proguard issue , we serialize stickers , but proguard is changing file name sometime and recent sticker deserialize fails , 
	 * and we loose recent sticker file
	 * 
	 * fix is : we read file , make recent sticker file as per new name and proguard has been changed so it will not obfuscate file name of Sticker
	 */
	public final void updateRecentStickerFile(SharedPreferences settings){
		Logger.i("recent", "Recent Sticker Save Mechanism started");
		// save to preference as we want to try correction logic only once
		Editor edit = settings.edit();
		edit.putBoolean(StickerManager.RECENT_STICKER_SERIALIZATION_LOGIC_CORRECTED, true);
		edit.commit();
		Map<String, StickerCategoryId> stickerCategoryMapping = getStickerToCategoryMapping(context);
		// we do not want to try more than once, any failure , lets ignore this process there after
		if(stickerCategoryMapping ==null){
			return;
		}
		BufferedReader bufferedReader = null;
		try{
			String filePath = getInternalStickerDirectoryForCategoryId(context, StickerCategoryId.recent.name());
			File dir = new File(filePath);
			if(!dir.exists()){
				return;
			}
			File file = new File(dir,StickerCategoryId.recent.name() + ".bin");
			if(file.exists()){
				bufferedReader = new BufferedReader(new FileReader(file));
				String line = "";
				StringBuilder str = new StringBuilder();
				while((line = bufferedReader.readLine())!=null){
					str.append(line);
				}
				recentStickers = Collections.synchronizedSet(new LinkedHashSet<Sticker>());
				
				Pattern p = Pattern.compile("(\\d{3}_.*?\\.png.*?)");
				Matcher m = p.matcher(str);
				
				while(m.find()){
					String stickerId = m.group();
					Logger.i("recent", "Sticker id found is "+stickerId);
					Sticker st = new Sticker();
					StickerCategory category = new StickerCategory();
					category.categoryId = stickerCategoryMapping.get(stickerId);
					if(category.categoryId==null){
						continue;
					}
					category.updateAvailable =false;
					category.setReachedEnd(true);
					st.setStickerData(-1, stickerId, category);
					recentStickers.add(st);
				}
				
				
				saveSortedListForCategory(StickerCategoryId.recent, recentStickers);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Logger.i("recent", "Recent Sticker Save Mechanism finished");
			if(bufferedReader!=null){
				try{
				bufferedReader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public File saveLargeStickers(File stickerDir, String stickerId, String stickerData) throws IOException
	{
		File f = new File(stickerDir, stickerId);
		Utils.saveBase64StringToFile(f, stickerData);
		return f;
	}

	/*
	 * TODO this logic is temporary we yet need to change it
	 */
	public File saveLargeStickers(File largeStickerDir, String stickerId, Bitmap largeStickerBitmap) throws IOException
	{
		Bitmap stickerImage = HikeBitmapFactory.createScaledBitmap(largeStickerBitmap, SIZE_IMAGE, SIZE_IMAGE, Bitmap.Config.ARGB_8888, true, true, false);
		
		if (stickerImage != null)
		{
			File largeImage = new File(largeStickerDir, stickerId);
			BitmapUtils.saveBitmapToFile(largeImage, stickerImage);
			stickerImage.recycle();
			return largeImage;
		}
		return null;
	}
	
	public void saveSmallStickers(File smallStickerDir, String stickerId, File f) throws IOException
	{
		Bitmap small = HikeBitmapFactory.scaleDownBitmap(f.getAbsolutePath(), SIZE_IMAGE, SIZE_IMAGE, true, false);

		if (small != null)
		{
			File smallImage = new File(smallStickerDir, stickerId);
			BitmapUtils.saveBitmapToFile(smallImage, small);
			small.recycle();
		}
	}

	public static boolean moveHardcodedStickersToSdcard(Context context)
	{
		if(Utils.getExternalStorageState() != ExternalStorageState.WRITEABLE)
		{
			return false;
		}
		
		try
		{
			JSONObject jsonObj = new JSONObject(Utils.loadJSONFromAsset(context, "stickers_data"));
			JSONArray harcodedStickers = jsonObj.optJSONArray(HARCODED_STICKERS);
			for (int i=0; i<harcodedStickers.length(); i++)
			{
				JSONObject obj = harcodedStickers.optJSONObject(i);
				String categoryId = obj.getString(CATEGORY_ID);
				
				String directoryPath = StickerManager.getInstance().getStickerDirectoryForCategoryId(context, categoryId);
				if (directoryPath == null)
				{
					return false;
				}

				Resources mResources = context.getResources();
				File largeStickerDir = new File(directoryPath + HikeConstants.LARGE_STICKER_ROOT);
				File smallStickerDir = new File(directoryPath + HikeConstants.SMALL_STICKER_ROOT);

				if (!smallStickerDir.exists())
				{
					smallStickerDir.mkdirs();
				}
				if (!largeStickerDir.exists())
				{
					largeStickerDir.mkdirs();
				}
				
				Utils.makeNoMediaFile(largeStickerDir);
				Utils.makeNoMediaFile(smallStickerDir);
				
				JSONArray stickerIds = obj.getJSONArray(STICKER_IDS);
				JSONArray resourceIds = obj.getJSONArray(RESOURCE_IDS);
				for (int j=0; j<stickerIds.length(); j++)
				{
					String stickerId = stickerIds.optString(j);
					String resName = resourceIds.optString(j);
					int resourceId = mResources.getIdentifier(resName, "drawable", 
							   context.getPackageName());
					Bitmap stickerBitmap = HikeBitmapFactory.decodeBitmapFromResource(mResources, resourceId, Bitmap.Config.ARGB_8888);
					File f = StickerManager.getInstance().saveLargeStickers(largeStickerDir, stickerId, stickerBitmap);
					if(f != null)
					{
						StickerManager.getInstance().saveSmallStickers(smallStickerDir, stickerId, f);
					}
					else
					{
						return false;
					}
				}	
			}
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
