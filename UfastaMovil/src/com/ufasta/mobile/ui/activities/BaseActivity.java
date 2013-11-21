package com.ufasta.mobile.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

import com.ufasta.mobile.R;
import com.ufasta.mobile.UFastaApp;
import com.ufasta.mobile.core.action.ActionController.IAction;
import com.ufasta.mobile.core.action.IActionListener;
import com.ufasta.mobile.core.request.Params;

public abstract class BaseActivity extends ActionBarActivity implements IActionListener {

	private ProgressDialog progress = null;
	private static boolean wasProgressShowing = false;

	private OnKeyListener searchScaper = new OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {
				return true;
			}
			return false;
		}
	};

	@SuppressLint("InlinedApi")
	public void showProgress(final String msg) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if ((progress == null) || (!progress.isShowing())) {
					wasProgressShowing = true;
					if (Build.VERSION.SDK_INT >= 11) {
						progress = new ProgressDialog(BaseActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
						progress.setMessage(msg);
						progress.setIndeterminate(true);
						progress.setCancelable(false);
						progress.setOnKeyListener(searchScaper);
						progress.show();

					} else {
						progress = ProgressDialog.show(BaseActivity.this, "", msg, true, false);
						progress.setOnKeyListener(searchScaper);
						progress.setCancelable(false);
					}
				}
			}
		});
	}

	public void showProgress() {
		showProgress(this.getResources().getString(R.string.progressmsg));
	}

	public void hideProgress() {
		hideProgress(true);
	}

	public void hideProgress(final boolean kill) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (kill) {
					wasProgressShowing = false;
				}
				if ((progress != null) && (progress.isShowing())) {
					progress.dismiss();
				}
				progress = null;
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (wasProgressShowing) {
			showProgress();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		hideProgress(false);
		UFastaApp.actionController.unregister(this);
	}

	@Override
	public void onDone(IAction action, Params bundle) {
		hideProgress();
	}

	@Override
	public void onProgress(IAction action, Params bundle) {
		showProgress();
	}

	@Override
	public void onError(IAction action, Params bundle) {
		hideProgress();
	}

	@Override
	public void onException(IAction action, Params bundle) {
		hideProgress();
	}
}
