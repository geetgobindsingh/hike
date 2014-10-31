package com.bsb.hike.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.filetransfer.FileTransferManager;
import com.bsb.hike.utils.CBCEncryption;
import com.bsb.hike.utils.Logger;

public class DBBackupRestore
{
	private static DBBackupRestore _instance = null;

	private static final String HIKE_PACKAGE_NAME = "com.bsb.hike";

	public static final String DATABASE_EXT = ".db";

	private static final String[] dbNames = { DBConstants.CONVERSATIONS_DATABASE_NAME };

	private static final String[] resetTableNames = { DBConstants.STICKER_SHOP_TABLE, DBConstants.STICKER_CATEGORIES_TABLE };
	
	private String backupToken;
	
	private Context mContext;

	private DBBackupRestore(Context context)
	{
		this.mContext = context;
		SharedPreferences settings = context.getSharedPreferences(HikeMessengerApp.ACCOUNT_SETTINGS, 0);
		backupToken = settings.getString(HikeMessengerApp.BACKUP_TOKEN_SETTING, null);
	}

	public static DBBackupRestore getInstance(Context context)
	{
		if (_instance == null)
		{
			synchronized (FileTransferManager.class)
			{
				if (_instance == null)
					_instance = new DBBackupRestore(context.getApplicationContext());
			}
		}
		return _instance;
	}

	public boolean backupDB()
	{
		Long time = System.currentTimeMillis();
		try
		{
			for (String fileName : dbNames)
			{
				File dbCopy = exportDatabse(fileName);
				if (dbCopy == null || !dbCopy.exists())
					return false;

				File backup = getDBBackupFile(dbCopy.getName());
				CBCEncryption.encryptFile(dbCopy, backup, backupToken);
				dbCopy.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		time = System.currentTimeMillis() - time;
		Logger.d(getClass().getSimpleName(), "Backup complete!! in " + time / 1000 + "." + time % 1000 + "s");
		return true;
	}

	public File exportDatabse(String databaseName)
	{
		Long time = System.currentTimeMillis();
		File dbCopy;
		try
		{
			File currentDB = getCurrentDBFile(databaseName);
			dbCopy = getDBCopyFile(currentDB.getName());
			if (currentDB.exists())
			{
				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(dbCopy).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		time = System.currentTimeMillis() - time;
		Logger.d(getClass().getSimpleName(), "DB Export complete!! in " + time / 1000 + "." + time % 1000 + "s");
		return dbCopy;
	}

	public boolean restoreDB()
	{
		Long time = System.currentTimeMillis();
		try
		{
			for (String fileName : dbNames)
			{
				File currentDB = getCurrentDBFile(fileName);
				File DBCopy = getDBCopyFile(currentDB.getName());
				File backup = getDBBackupFile(DBCopy.getName());
				CBCEncryption.decryptFile(backup, DBCopy, backupToken);
				importDatabase(DBCopy);
				postRestoreSetup();
				DBCopy.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		time = System.currentTimeMillis() - time;
		Logger.d(getClass().getSimpleName(), "Restore complete!! in " + time / 1000 + "." + time % 1000 + "s");
		return true;
	}

	private void importDatabase(File dbCopy)
	{
		Long time = System.currentTimeMillis();
		File currentDB = getCurrentDBFile(dbCopy.getName());
		if (dbCopy.exists())
		{
			try
			{
				FileChannel src = new FileInputStream(dbCopy).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Logger.d(getClass().getSimpleName(), "copy fail");
			}
			time = System.currentTimeMillis() - time;
			Logger.d(getClass().getSimpleName(), "DB import complete!! in " + time / 1000 + "." + time % 1000 + "s");
		}
	}

	private void postRestoreSetup()
	{
		SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int oldVersion = appPrefs.getInt(HikeConstants.PREVIOUS_CONV_DB_VERSION, DBConstants.CONVERSATIONS_DATABASE_VERSION);
		int newVersion = DBConstants.CONVERSATIONS_DATABASE_VERSION;
		if (newVersion > oldVersion && oldVersion > 0)
		{
			// 1. Upgrade db
			HikeConversationsDatabase.getInstance().upgrade(oldVersion, newVersion);
			// 2. Reset tables.
			for ( String table : resetTableNames)
			{
				HikeConversationsDatabase.getInstance().clearTable(table);
			}
		}
		Editor editor = appPrefs.edit();
		editor.putInt(HikeConstants.PREVIOUS_CONV_DB_VERSION, 0);
		editor.commit();
	}

	public boolean isBackupAvailable()
	{
		for (String fileName : dbNames)
		{
			File currentDB = getCurrentDBFile(fileName);
			File DBCopy = getDBCopyFile(currentDB.getName());
			File backup = getDBBackupFile(DBCopy.getName());
			if (!backup.exists())
				return false;
		}
		return true;
	}

	public long getLastBackupTime()
	{
		for (String fileName : dbNames)
		{
			File currentDB = getCurrentDBFile(fileName);
			File DBCopy = getDBCopyFile(currentDB.getName());
			File backup = getDBBackupFile(DBCopy.getName());
			if (backup.exists())
			{
				return backup.lastModified();
			}
		}
		return -1;
	}

	private static File getCurrentDBFile(String dbName)
	{
		File data = Environment.getDataDirectory();
		String currentDBPath = "//data//" + HIKE_PACKAGE_NAME + "//databases//" + dbName + "";
		File currentDB = new File(data, currentDBPath);
		return currentDB;
	}

	private static File getDBBackupFile(String name)
	{
		new File(HikeConstants.HIKE_BACKUP_DIRECTORY_ROOT).mkdirs();
		return new File(HikeConstants.HIKE_BACKUP_DIRECTORY_ROOT, name + ".backup");
	}

	private static File getDBCopyFile(String name)
	{
		new File(HikeConstants.HIKE_BACKUP_DIRECTORY_ROOT).mkdirs();
		return new File(HikeConstants.HIKE_BACKUP_DIRECTORY_ROOT, name);
	}

}
