package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.key.Key;
import com.gmail.collinsmith70.key.KeyManager;

import java.util.Arrays;

public class GdxKeyManager extends KeyManager<Integer> {

private static final String TAG = GdxKeyManager.class.getSimpleName();

private final Preferences PREFERENCES;

public GdxKeyManager() {
    PREFERENCES = Gdx.app.getPreferences(GdxKeyManager.class.getName());
}

@Override
public void commit(Key<Integer> key) {
    super.commit(key);
    PREFERENCES.flush();
}

@Override
public void save(Key<Integer> key) {
    super.save(key);
    Integer[] bindings = key.getBindings().toArray(new Integer[0]);
    String serializedValue = Arrays.toString(bindings);
    PREFERENCES.putString(key.getAlias(), serializedValue);
    Gdx.app.log(TAG, String.format(
            "%s [%s] saved as %s%n",
            key.getName(),
            key.getAlias(),
            serializedValue));
}

@Override
public void load(Key<Integer> key) {
    super.load(key);
    String serializedValue = PREFERENCES.getString(key.getAlias());
    if (serializedValue == null) {
        return;
    }

    if (!serializedValue.matches("\\[(\\d+,)*\\d+\\]")) {
        Gdx.app.log(TAG, String.format(
                "Error processing saved value for key %s [%s]: \"%s\"",
                key.getName(),
                key.getAlias(),
                serializedValue));
        return;
    }

    serializedValue = serializedValue.substring(1, serializedValue.length());
    for (String serializedBinding : serializedValue.split(",")) {
        key.addBinding(Integer.parseInt(serializedBinding));
    }

    Gdx.app.log(TAG, String.format(
            "%s [%s] loaded as %s%n",
            key.getName(),
            key.getAlias(),
            serializedValue));
}

@Override
public void onAdded(Integer binding, Key<Integer> key) {
    super.onAdded(binding, key);
    Gdx.app.log(TAG, String.format("added %s to %s%n",
            binding,
            key.getName()));
}

@Override
public void onRemoved(Integer binding, Key<Integer> key) {
    super.onRemoved(binding, key);
    Gdx.app.log(TAG, String.format("removed %s from %s%n",
            binding,
            key.getName()));
}

}
