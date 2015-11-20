package com.google.collinsmith70.diablo.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

public class Key {

private static final String TAG = Key.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Key.class.getName());
private static final int[] KEYS = new int[256];

public static void saveAll() {
    Gdx.app.log(TAG, "Saving Keys...");
    Key.PREFERENCES.flush();
}

public static int get(int keycode) {
    if (keycode == Input.Keys.ANY_KEY) {
        return Input.Keys.ANY_KEY;
    }

    return KEYS[keycode];
}

public int[] getKeys() {
    return KEYS;
}



}
