package com.bsb.hike.modules.stickerdownloadmgr;

import static com.bsb.hike.modules.httpmgr.HttpRequests.StickerPreviewImageDownloadRequest;
import static com.bsb.hike.modules.httpmgr.exception.HttpException.REASON_CODE_OUT_OF_SPACE;

import java.io.File;

import org.json.JSONObject;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.modules.httpmgr.RequestToken;
import com.bsb.hike.modules.httpmgr.exception.HttpException;
import com.bsb.hike.modules.httpmgr.request.facade.RequestFacade;
import com.bsb.hike.modules.httpmgr.request.listener.IPreProcessListener;
import com.bsb.hike.modules.httpmgr.request.listener.IRequestListener;
import com.bsb.hike.modules.httpmgr.response.Response;
import com.bsb.hike.utils.Logger;
import com.bsb.hike.utils.StickerManager;
import com.bsb.hike.utils.Utils;

public class StickerPreviewImageDownloadTask extends BaseStickerDownloadTask
{
	private String TAG = "StickerPreviewImageDownloadTask";

	private String catId;

	String previewImagePath;

	protected StickerPreviewImageDownloadTask(String taskId, String categoryId)
	{
		super(taskId);
		this.catId = categoryId;
		
		if (!StickerManager.getInstance().isMinimumMemoryAvailable())
		{
			onFailure(new HttpException(REASON_CODE_OUT_OF_SPACE));
			return;
		}
		
		RequestToken requestToken = StickerPreviewImageDownloadRequest(categoryId, getPreProcessListener(), getRequestListener());
		requestToken.execute();
	}

	private IPreProcessListener getPreProcessListener()
	{
		return new IPreProcessListener()
		{

			@Override
			public void doInBackground(RequestFacade facade)
			{
				String dirPath = StickerManager.getInstance().getStickerDirectoryForCategoryId(catId);
				if (dirPath == null)
				{
					Logger.e(TAG, "Sticker download failed directory does not exist");
					onFailure(null);
					return;
				}

				previewImagePath = dirPath + StickerManager.OTHER_STICKER_ASSET_ROOT + "/" + StickerManager.PREVIEW_IMAGE + StickerManager.OTHER_ICON_TYPE;

				File otherDir = new File(dirPath + StickerManager.OTHER_STICKER_ASSET_ROOT);
				if (!otherDir.exists())
				{
					if (!otherDir.mkdirs())
					{
						Logger.e(TAG, "Sticker download failed directory not created");
						onFailure(null);
						return;
					}
				}
			}
		};
	}

	private IRequestListener getRequestListener()
	{
		return new IRequestListener()
		{

			@Override
			public void onRequestSuccess(Response result)
			{
				try
				{
					JSONObject response = (JSONObject) result.getBody().getContent();
					if (!Utils.isResponseValid(response))
					{
						Logger.e(TAG, "Sticker download failed null or invalid response");
						onFailure(null);
						return;
					}
					Logger.d(TAG, "Got response for download task " + response.toString());
					JSONObject data = response.getJSONObject(HikeConstants.DATA_2);

					if (null == data)
					{
						Logger.e(TAG, "Sticker download failed null data");
						onFailure(null);
						return;
					}

					String stickerData = data.getString(HikeConstants.PREVIEW_IMAGE);
					HikeMessengerApp.getLruCache().remove(StickerManager.getInstance().getCategoryOtherAssetLoaderKey(catId, StickerManager.PREVIEW_IMAGE_TYPE));
					Utils.saveBase64StringToFile(new File(previewImagePath), stickerData);
				}
				catch (Exception e)
				{
					onFailure(e);
				}
			}

			@Override
			public void onRequestProgressUpdate(float progress)
			{

			}

			@Override
			public void onRequestFailure(HttpException httpException)
			{
				onFailure(httpException);
			}
		};
	}

	@Override
	void onSuccess(Object result)
	{
		// TODO Auto-generated method stub
		super.onSuccess(result);
	}

	@Override
	void onFailure(Exception e)
	{
		// TODO Auto-generated method stub
		super.onFailure(e);
	}
}
