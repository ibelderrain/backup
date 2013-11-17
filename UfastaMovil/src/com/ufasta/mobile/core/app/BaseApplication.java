package com.ufasta.mobile.core.app;

import java.lang.reflect.Field;

import android.app.Application;

import com.ufasta.mobile.core.logger.Logger;
import com.ufasta.mobile.core.persistence.global.ControllerField;
import com.ufasta.mobile.core.persistence.global.GlobalStatePersister;

public class BaseApplication extends Application {

	private GlobalStatePersister persister;

	public void onCreate() {
		super.onCreate();
		persister = new GlobalStatePersister(getApplicationContext());
	}

	public void clearPersistedState() {
		persister.clear();
	}

	public void saveCurrentState() {

		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(ControllerField.class)) {

				int id = f.getAnnotation(ControllerField.class).value();

				try {
					Object o = f.get(this);
					if (o != null) {
						String state = null;// = JsonParser.toJson(o);

						try {
							// state =
							// Base64.encodeToString(state.getBytes("UTF-8"),
							// Base64.DEFAULT);
							System.out.println(state);

							// // **
							// if (state != null && state.length() > 0) {
							// String decoded = new String(Base64.decode(state,
							// Base64.DEFAULT), "UTF-8");
							// System.out.println(decoded);
							// }

							// **

							persister.saveController(id, state);
						} catch (Exception e) {
							state = null;
							e.printStackTrace();
						}

						Logger.info("CONTROLLER SAVED");

					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@SuppressWarnings("unused")
	protected void restoreSavedState() {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(ControllerField.class)) {

				int id = f.getAnnotation(ControllerField.class).value();

				try {

					String json = persister.restoreController(id);
					// try {
					// if (json != null && json.length() > 0) {
					// json = new String(Base64.decode(json, Base64.DEFAULT),
					// "UTF-8");
					// }
					// } catch (UnsupportedEncodingException e) {
					// json = null;
					// e.printStackTrace();
					// }

					System.out.println("RESTORED: " + json);
					if (json != null && json.length() > 0) {
						Object o = null; // JsonParser.fromJson(f.getType(),
											// json);
						if (o != null) {
							f.set(this, o);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
