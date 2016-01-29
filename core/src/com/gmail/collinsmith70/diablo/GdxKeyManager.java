package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.key.Key;
import com.gmail.collinsmith70.key.KeyManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    Gdx.app.log(TAG, "committing changes");
}

@Override
public void save(Key<Integer> key) {
    super.save(key);
    Integer[] bindings = key.getBindings().toArray(new Integer[0]);
    String serializedValue = Arrays.toString(bindings);
    PREFERENCES.putString(key.getAlias(), serializedValue);

    String[] bindingNames = new String[bindings.length];
    for (int i = 0; i < bindingNames.length; i++) {
        bindingNames[i] = Input.Keys.toString(bindings[i]);
    }

    Gdx.app.log(TAG, String.format(
            "%s [%s] saved as %s",
            key.getName(),
            key.getAlias(),
            Arrays.toString(bindingNames)));
}

@Override
public Set<Integer> load(Key<Integer> key) {
    super.load(key);
    String serializedValue = PREFERENCES.getString(key.getAlias());
    if (serializedValue == null) {
        return Collections.EMPTY_SET;
    }

    if (!serializedValue.matches("\\[(\\d+,)*\\d+\\]")) {
        Gdx.app.log(TAG, String.format(
                "Error processing saved value for key %s [%s]: \"%s\"",
                key.getName(),
                key.getAlias(),
                serializedValue));
        return Collections.EMPTY_SET;
    }

    Set<Integer> bindings = new HashSet<Integer>();
    serializedValue = serializedValue.substring(1, serializedValue.length()-1);
    for (String serializedBinding : serializedValue.split(",")) {
        bindings.add(Integer.parseInt(serializedBinding));
    }

    String[] bindingNames = new String[bindings.size()];
    int i = 0;
    for (int binding : bindings) {
        bindingNames[i] = Input.Keys.toString(binding);
    }

    Gdx.app.log(TAG, String.format(
            "%s [%s] loaded as %s",
            key.getName(),
            key.getAlias(),
            Arrays.toString(bindingNames)));
    return bindings;
}

@Override
public void onAdded(Integer binding, Key<Integer> key) {
    super.onAdded(binding, key);
    Gdx.app.log(TAG, String.format("added [%s] to [%s]",
            Input.Keys.toString(binding),
            key.getAlias()));
}

@Override
public void onRemoved(Integer binding, Key<Integer> key) {
    super.onRemoved(binding, key);
    Gdx.app.log(TAG, String.format("removed [%s] from [%s]",
            Input.Keys.toString(binding),
            key.getAlias()));
}

}
