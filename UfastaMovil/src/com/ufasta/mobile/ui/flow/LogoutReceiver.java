package com.ufasta.mobile.ui.flow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogoutReceiver extends BroadcastReceiver {

	public static final String ACTION_LOGOUT = "com.ufasta.mobile.ACTION_LOGOUT";

	private Activity activity;

	public LogoutReceiver(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		activity.finish();
		activity = null;
	}

}
