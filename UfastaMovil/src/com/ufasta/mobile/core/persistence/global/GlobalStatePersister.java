package com.ufasta.mobile.core.persistence.global;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GlobalStatePersister extends SQLiteOpenHelper {

	private static final String GLOBAL_STATE_TABLE = "global_state";
	private static final String CONTROLLER_ID = "key_id";
	private static final String CONTROLLER_STATE = "global_state";

	public GlobalStatePersister(Context context) {
		super(context, context.getApplicationContext().getPackageName(), null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + GLOBAL_STATE_TABLE + "(" + CONTROLLER_ID
				+ " INTEGER PRIMARY KEY," + CONTROLLER_STATE + " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + GLOBAL_STATE_TABLE);
		onCreate(db);
	}

	public void saveController(int id, String state) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(CONTROLLER_ID, id);
		cv.put(CONTROLLER_STATE, state);

		db.insertWithOnConflict(GLOBAL_STATE_TABLE, "NULL", cv, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public String restoreController(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(GLOBAL_STATE_TABLE, new String[] { CONTROLLER_ID, CONTROLLER_STATE }, CONTROLLER_ID
				+ "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			return cursor.getString(1);
		}
		return null;
	}

	public void clear() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(GLOBAL_STATE_TABLE, null, null);
	}

}
