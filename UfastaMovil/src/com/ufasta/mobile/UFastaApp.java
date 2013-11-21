package com.ufasta.mobile;

import com.ufasta.mobile.core.action.ActionController;
import com.ufasta.mobile.core.app.BaseApplication;
import com.ufasta.mobile.core.request.CacheRequest;
import com.ufasta.mobile.core.request.RequestExecutor;
import com.ufasta.mobile.ui.flow.DialogManager;
import com.ufasta.mobile.ui.flow.ScreenManager;
import com.ufasta.mobile.utils.Storage;

public class UFastaApp extends BaseApplication {

	public static UFastaApp instance;

	private static final CacheRequest cacheRequest = new CacheRequest();

	public static final ScreenManager screenManager = new ScreenManager();
	public static final DialogManager dialogManager = new DialogManager();
	public static final ActionController actionController = new ActionController();

	public static RequestExecutor requestExecutor;
	public static Storage storage;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		storage = new Storage(this);
		storage.open();
		requestExecutor = new RequestExecutor(actionController, cacheRequest);
	}
}
