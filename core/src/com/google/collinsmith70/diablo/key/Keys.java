package com.google.collinsmith70.diablo.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

public class Keys {

private static final String TAG = Keys.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Keys.class.getName());

public static void saveAll() {
    Gdx.app.log(TAG, "Saving Keys...");
    Keys.PREFERENCES.flush();
}

public static int get(int keycode) {
    if (keycode == Input.Keys.ANY_KEY) {
        return Input.Keys.ANY_KEY;
    }

    return KEYS[keycode];
}

private static final int[] KEYS;
static {
    KEYS = new int[256];
    for (int keycode = 0; keycode < KEYS.length; keycode++) {
        KEYS[keycode] = PREFERENCES.getInteger(Integer.toHexString(keycode), keycode);
    }
}

}
