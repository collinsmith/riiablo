package com.google.collinsmith70.diablo.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Key {

private static final String TAG = Key.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Key.class.getName());
private static final Trie<String, Key> KEYS = new PatriciaTrie<Key>();

public static void saveAll() {
    Gdx.app.log(TAG, "Saving Keys...");
    Key.PREFERENCES.flush();
}

public static Key get(String key) {
    return KEYS.get(key.toUpperCase());
}

public static Collection<Key> getAllKeys() {
    return KEYS.values();
}

private final String ALIAS;
private final Set<Integer> VALUES;

public Key(String alias, int... defaultValues) {
    this.ALIAS = alias.toUpperCase();
    if (getAlias() == null) {
        throw new IllegalArgumentException("Key aliases cannot be null");
    } else if (getAlias().isEmpty()) {
        throw new IllegalArgumentException("Key aliases cannot be empty");
    } else if (Key.KEYS.containsKey(getAlias())) {
        throw new IllegalArgumentException(String.format(
                "Key %s already exists. Key aliases must be unique!",
                getAlias()));
    }

    this.VALUES = new LinkedHashSet<Integer>();
    if (PREFERENCES.contains(getAlias())) {
        String keys = PREFERENCES.getString(getAlias(), "");
        for (int i = 0; i < keys.length(); i++) {
            addKey(keys.codePointAt(i));
        }
    } else {
        for (int keycode : defaultValues) {
            addKey(keycode);
        }
    }

    Gdx.app.log(TAG, String.format(
            "%s loaded as %s",
            getAlias(), getKeysString()));

    KEYS.put(getAlias(), this); // potentially unsafe (technically object is not constructed yet)
}

public String getAlias() {
    return ALIAS;
}

public ImmutableSet<Integer> getKeys() {
    return ImmutableSet.copyOf(VALUES);
}

public String getKeysString() {
    StringBuilder keysListBuilder = new StringBuilder();
    for (Iterator<Integer> it = VALUES.iterator(); it.hasNext();) {
        int keycode = it.next();
        keysListBuilder.append(Input.Keys.toString(keycode));
        if (it.hasNext()) {
            keysListBuilder.append("; ");
        }
    }

    return keysListBuilder.toString();
}

public static boolean isValidKeyCode(int keycode) {
    return 0 <= keycode && keycode <= 255;
}

public void addKey(int keycode) {
    if (keycode == Input.Keys.ANY_KEY) {
        throw new IllegalArgumentException("Cannot add Input.Keys.ANY_KEY to key list");
    } else if (!isValidKeyCode(keycode)) {
        throw new IllegalArgumentException(String.format(
                "Invalid keycode specified: '%d'. Valid keycodes are between 0 and 255 (inclusive)",
                keycode));
    }

    VALUES.add(keycode);
}

public boolean removeKey(int keycode) {
    return VALUES.remove(keycode);
}

public boolean containsKey(int keycode) {
    return VALUES.contains(keycode);
}

public boolean containsKey(char ch) {
    return containsKey(Input.Keys.valueOf(Character.toString(ch)));
}

@Override
public String toString() {
    return String.format("%s = %s", getAlias(), getKeysString());
}

}
