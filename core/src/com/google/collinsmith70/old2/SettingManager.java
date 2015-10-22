package com.google.collinsmith70.old2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;

import java.util.Locale;

public final class SettingManager {
    private static final String TAG = "Settings";

	private final Preferences PREFERENCES;

	public SettingManager(Preferences prefs) {
		this.PREFERENCES = prefs;
		reload();
	}

	public Preferences getPreferences() {
		return PREFERENCES;
	}

	public void reload() {
		loadLocale();
		loadController();
		loadVSync();
	}

	private Locale locale;

	private void loadLocale() {
		String tag = PREFERENCES.getString(Cvar.Client.Language, "");
		if (tag.isEmpty()) {
			locale = Locale.getDefault();
		} else {
			locale = Locale.forLanguageTag(tag);
		}

		Gdx.app.log(TAG, "Locale loaded as " + locale);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale l) {
		this.locale = l;
		Gdx.app.log(TAG, "Locale set to " + locale);
	}

	private Controller controller;

	public void loadController() {
		controller = findFirstController();
		if (controller != null) {
			Gdx.app.log(TAG, "Controller loaded as " + controller.getName());
			controller.addListener(new ControllerAdapter() {
				@Override
				public void connected(Controller controller) {
					SettingManager.this.controller = controller;
					Gdx.app.log(TAG, "Controller connected: " + SettingManager.this.controller.getName());
				}

				@Override
				public void disconnected(Controller controller) {
					SettingManager.this.controller = null;
					Gdx.app.log(TAG, "Controller disconnected: " + controller.getName());
				}
			});
		}
	}

	public boolean isUsingController() {
		return controller != null;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller c) {
		this.controller = c;
		Gdx.input.setCursorCatched(controller != null);
		Gdx.app.log(TAG, "Controller set to " + controller.getName());
	}

	private static Controller findFirstController() {
		for (Controller c : Controllers.getControllers()) {
			return c;
		}

		return null;
	}

	private boolean vsyncEnabled;

	private void loadVSync() {
		vsyncEnabled = PREFERENCES.getBoolean(Cvar.Client.Vis.VSyncEnabled, false);
		Gdx.app.log(TAG, "Vertical sync loaded as " + vsyncEnabled);
	}

	public void setVSyncEnabled(boolean b) {
		this.vsyncEnabled = b;
		Gdx.graphics.setVSync(vsyncEnabled);
	}

	public boolean isVSyncEnabled() {
		return vsyncEnabled;
	}
}
