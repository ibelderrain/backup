package com.ufasta.mobile.ui.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;

import com.ufasta.mobile.UFastaApp;
import com.ufasta.mobile.ui.flow.LogoutReceiver;

public abstract class BaseNavigationActivity extends BaseActivity {

	private LogoutReceiver logoutListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logoutListener = new LogoutReceiver(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LogoutReceiver.ACTION_LOGOUT);
		LocalBroadcastManager.getInstance(this).registerReceiver(logoutListener, intentFilter);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(logoutListener);
		super.onDestroy();
	}

	public void onSaveInstanceState(Bundle outState) {
		UFastaApp.instance.saveCurrentState();
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {

		}
		return super.onOptionsItemSelected(item);
	}

}
