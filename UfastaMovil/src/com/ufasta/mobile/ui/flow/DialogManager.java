package com.ufasta.mobile.ui.flow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.Gravity;
import android.widget.TextView;

import com.ufasta.mobile.R;

public class DialogManager {

	public void showMessage(final Activity origin, final String msj) {
		showMessage(origin, msj, (OnClickListener) null);
	}

	public void showMessage(final Activity origin, final String title, final String msj) {
		showMessage(origin, title, msj, null, null);
	}

	public void showMessage(final Activity origin, final String msj, final OnClickListener listener) {
		showMessage(origin, origin.getResources().getString(R.string.app_name), msj, listener, null);
	}

	public void showMessage(final Activity origin, final String title, final String msg, final String positiveButtonText) {
		showMessage(origin, title, msg, null, positiveButtonText);
	}

	public void showMessage(final Activity origin, final String title, final String msg, final OnClickListener listener) {
		showMessage(origin, title, msg, listener, null);
	}

	private void showMessage(final Activity origin, final String title, final String msj,
			final OnClickListener positiveListener, final String positiveButtonText) {
		origin.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = getBuilder(origin);
				if (title != null) {
					builder.setTitle(title);
				} else {
					builder.setTitle(origin.getResources().getString(R.string.app_name));
				}
				builder.setMessage(msj);

				if (positiveButtonText != null) {
					builder.setPositiveButton(positiveButtonText, positiveListener);
				} else {
					builder.setPositiveButton(origin.getResources().getString(R.string.ok), positiveListener);
				}

				builder.show();
			}
		});
	}

	public void showDialog(final Activity origin, final String title, final String msj, final String positive,
			final DialogInterface.OnClickListener positiveListener, final String negative,
			final DialogInterface.OnClickListener negativeListener) {
		origin.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = getBuilder(origin);
				builder.setTitle(title);
				builder.setMessage(msj);
				builder.setCancelable(false);
				builder.setPositiveButton(positive, positiveListener);
				builder.setNegativeButton(negative, negativeListener);
				AlertDialog dialog = builder.show();
				TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
				if (messageText != null) {
					messageText.setGravity(Gravity.CENTER);
				}
			}
		});
	}

	public void showOptionDialog(final Activity origin, final String title, final String[] items,
			final OnClickListener listener, final OnCancelListener onCancelListener) {
		origin.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = getBuilder(origin);
				if (title != null) {
					builder.setTitle(title);
				} else {
					builder.setTitle(origin.getResources().getString(R.string.app_name));
				}
				builder.setItems(items, listener);
				builder.setOnCancelListener(onCancelListener);
				builder.show();
			}
		});
	}

	public void showOptionDialog(final Activity origin, final String[] items, final OnClickListener listener) {
		showOptionDialog(origin, null, items, listener, null);
	}

	public void showOptionDialog(final Activity origin, String title, String[] items, OnClickListener listener) {
		showOptionDialog(origin, title, items, listener, null);
	}

	private AlertDialog.Builder getBuilder(Activity origin) {
		return new AlertDialog.Builder(origin);
	}

}