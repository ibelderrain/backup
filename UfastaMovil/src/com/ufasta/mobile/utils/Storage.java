package com.ufasta.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Storage {

	private SharedPreferences prefs;
	private Context context;

	public Storage(Context context) {
		this.context = context;
	}

	public void open() {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void close() {

	}

	public void clear() {
		prefs.getAll().clear();
	}

}
