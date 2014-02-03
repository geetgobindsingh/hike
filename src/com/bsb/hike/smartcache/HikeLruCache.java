package com.bsb.hike.smartcache;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;
import com.bsb.hike.db.HikeConversationsDatabase;
import com.bsb.hike.db.HikeUserDatabase;
import com.bsb.hike.smartImageLoader.IconLoader;
import com.bsb.hike.smartImageLoader.ImageWorker;
import com.bsb.hike.ui.utils.RecyclingBitmapDrawable;
import com.bsb.hike.utils.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.support.v4.util.LruCache;

public class HikeLruCache extends LruCache<String, RecyclingBitmapDrawable>
{
	// Default memory cache size in kilobytes
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;

	private static final int DEFAULT_COMPRESS_QUALITY = 70;

	// Constants to easily toggle various caches
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;

	private static HikeLruCache instance;

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams
	{
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;

		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;

		public int compressQuality = DEFAULT_COMPRESS_QUALITY;

		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;

		/**
		 * Create a set of image cache parameters that can be provided to {@link ImageCache#getInstance(ImageCacheParams)}
		 */
		public ImageCacheParams(int memCacheSize)
		{
			this.memCacheSize = memCacheSize;
		}

		public ImageCacheParams()
		{

		}

		/**
		 * Sets the memory cache size based on a percentage of the max available VM memory. Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
		 * memory. Throws {@link IllegalArgumentException} if percent is < 0.01 or > .8. memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed to
		 * construct a LruCache which takes an int in its constructor.
		 * 
		 * This value should be chosen carefully based on a number of factors Refer to the corresponding Android Training class for more discussion:
		 * http://developer.android.com/training/displaying-bitmaps/
		 * 
		 * @param percent
		 *            Percent of available app memory to use to size memory cache
		 */
		public void setMemCacheSizePercent(float percent)
		{
			if (percent < 0.01f || percent > 0.8f)
			{
				throw new IllegalArgumentException("setMemCacheSizePercent - percent must be " + "between 0.01 and 0.8 (inclusive)");
			}
			memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
		}
	}

	private final Set<SoftReference<RecyclingBitmapDrawable>> reusableBitmaps;

	public HikeLruCache(ImageCacheParams cacheParams)
	{
		super(cacheParams.memCacheSize);
		reusableBitmaps = Utils.canInBitmap() ? Collections.synchronizedSet(new HashSet<SoftReference<RecyclingBitmapDrawable>>()) : null;
	}

	public static HikeLruCache getInstance(ImageCacheParams cacheParams)
	{
		if (instance == null)
		{
			synchronized (HikeLruCache.class)
			{
				if (instance == null)
					instance = new HikeLruCache(cacheParams);
			}
		}
		return instance;
	}

	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat) onward this returns the allocated memory size of the bitmap which can be larger
	 * than the actual bitmap data byte count (in the case it was re-used).
	 * 
	 * @param value
	 * @return size in bytes
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static int getBitmapSize(BitmapDrawable value)
	{
		Bitmap bitmap = value.getBitmap();

		// From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
		// larger than bitmap byte count.
		if (Utils.hasKitKat())
		{
			return bitmap.getAllocationByteCount();
		}

		if (Utils.hasHoneycombMR1())
		{
			return bitmap.getByteCount();
		}

		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Measure item size in kilobytes rather than units which is more practical for a bitmap cache
	 */
	@Override
	protected int sizeOf(String key, RecyclingBitmapDrawable value)
	{
		final int bitmapSize = getBitmapSize(value) / 1024;
		return bitmapSize == 0 ? 1 : bitmapSize;
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, RecyclingBitmapDrawable oldValue, RecyclingBitmapDrawable newValue)
	{
		// Notify the wrapper that it's no longer being cached
		oldValue.setIsCached(false);

		if (reusableBitmaps != null && oldValue.isBitmapValid() && oldValue.isBitmapMutable())
		{
			synchronized (reusableBitmaps)
			{
				reusableBitmaps.add(new SoftReference<RecyclingBitmapDrawable>(oldValue));
			}
		}
	}

	public RecyclingBitmapDrawable putInCache(String data, RecyclingBitmapDrawable value)
	{
		if (null != value)
		{
			value.setIsCached(true);
			return put(data, value);
		}
		return null;
	}

	/**
	 * @param options
	 *            - BitmapFactory.Options with out* options populated
	 * @return Bitmap that case be used for inBitmap
	 */
	public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options)
	{
		Bitmap bitmap = null;

		if (reusableBitmaps != null && !reusableBitmaps.isEmpty())
		{
			synchronized (reusableBitmaps)
			{
				final Iterator<SoftReference<RecyclingBitmapDrawable>> iterator = reusableBitmaps.iterator();
				RecyclingBitmapDrawable item;

				while (iterator.hasNext())
				{
					item = iterator.next().get();

					if (null != item && item.isBitmapMutable())
					{
						// Check to see it the item can be used for inBitmap
						if (canUseForInBitmap(item, options))
						{
							bitmap = item.getBitmap();

							// Remove from reusable set so it can't be used again
							iterator.remove();
							break;
						}
					}
					else
					{
						// Remove from the set if the reference has been cleared.
						iterator.remove();
					}
				}
			}
		}

		return bitmap;
	}

	/**
	 * @param candidate
	 *            - Bitmap to check
	 * @param targetOptions
	 *            - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use with <code>targetOptions</code>
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	private static boolean canUseForInBitmap(RecyclingBitmapDrawable candidate, BitmapFactory.Options targetOptions)
	{

		if (!Utils.hasKitKat())
		{
			// On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
			return candidate.getBitmap().getWidth() == targetOptions.outWidth && candidate.getBitmap().getHeight() == targetOptions.outHeight && targetOptions.inSampleSize == 1;
		}

		// From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
		// is smaller than the reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height * getBytesPerPixel(candidate.getBitmap().getConfig());
		return byteCount <= candidate.getBitmap().getAllocationByteCount();
	}

	/**
	 * Return the byte usage per pixel of a bitmap based on its configuration.
	 * 
	 * @param config
	 *            The bitmap configuration.
	 * @return The byte usage per pixel.
	 */
	private static int getBytesPerPixel(Config config)
	{
		if (config == Config.ARGB_8888)
		{
			return 4;
		}
		else if (config == Config.RGB_565)
		{
			return 2;
		}
		else if (config == Config.ARGB_4444)
		{
			return 2;
		}
		else if (config == Config.ALPHA_8)
		{
			return 1;
		}
		return 1;
	}

	protected Resources mResources;

	/**
	 * 
	 * @param key
	 * @return This method is synchronous method and can call db if image is not found in cache
	 */
	public BitmapDrawable getIconFromCache(String key)
	{
		return getIconFromCache(key,false);
	}

	public BitmapDrawable getIconFromCache(String key,boolean rounded)
	{
		String cacheKey = rounded ? key + IconLoader.ROUND_SUFFIX : key;
		RecyclingBitmapDrawable b = get(cacheKey);
		if (b == null)
		{
			BitmapDrawable bd = (BitmapDrawable) HikeUserDatabase.getInstance().getIcon(key, rounded);
			RecyclingBitmapDrawable rbd = new RecyclingBitmapDrawable(mResources, bd.getBitmap());
			putInCache(cacheKey, rbd);
			return rbd;
		}
		else
			return b;
	}
	
	public BitmapDrawable getFileIconFromCache(String key)
	{
		RecyclingBitmapDrawable b = get(key);
		if (b == null)
		{
			BitmapDrawable bd = (BitmapDrawable) HikeConversationsDatabase.getInstance().getFileThumbnail(key);
			if(bd == null)
				return null;
			RecyclingBitmapDrawable rbd = new RecyclingBitmapDrawable(mResources, bd.getBitmap());
			putInCache(key, rbd);
			return rbd;
		}
		else
			return b;
	}
	
	public void deleteIconForMSISDN(String msisdn)
	{
		HikeUserDatabase.getInstance().removeIcon(msisdn);
		clearIconForMSISDN(msisdn);
	}

	public void clearIconForMSISDN(String msisdn)
	{
		remove(msisdn);
		remove(msisdn);
		remove(msisdn + IconLoader.ROUND_SUFFIX);
		HikeMessengerApp.getPubSub().publish(HikePubSub.ICON_CHANGED, msisdn);
	}

	public void clearIconCache()
	{
		evictAll();
	}

	public Drawable getSticker(Context ctx, String path)
	{
		RecyclingBitmapDrawable rbd = new RecyclingBitmapDrawable(ctx.getResources(),BitmapFactory.decodeFile(path));
		putInCache(path, rbd);
		return rbd;
	}
}
