package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

import java.util.Locale;

public final class SettingManager {
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
		String tag = PREFERENCES.getString("cl.glob.language", "");
		if (tag.isEmpty()) {
			locale = Locale.getDefault();
		} else {
			locale = Locale.forLanguageTag(tag);
		}

		Gdx.app.log("setting", "Locale loaded as " + locale);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale l) {
		this.locale = l;
		Gdx.app.log("setting", "Locale set to " + locale);
	}

	private Controller controller;

	public void loadController() {
		controller = findFirstController();
		if (controller != null) {
			Gdx.app.log("setting", "Controller loaded as " + controller.getName());
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
		Gdx.app.log("setting", "Controller set to " + controller.getName());
	}

	private static Controller findFirstController() {
		for (Controller c : Controllers.getControllers()) {
			return c;
		}

		return null;
	}

	private boolean vsyncEnabled;

	private void loadVSync() {
		vsyncEnabled = PREFERENCES.getBoolean("cl.vis.vsync", false);
		Gdx.app.log("setting", "Vertical sync loaded as " + vsyncEnabled);
	}

	public void setVSyncEnabled(boolean b) {
		this.vsyncEnabled = b;
		Gdx.graphics.setVSync(vsyncEnabled);
	}

	public boolean isVSyncEnabled() {
		return vsyncEnabled;
	}
}
