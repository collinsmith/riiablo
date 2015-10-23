package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Cvar<T> {
private static final String TAG = Cvar.class.getSimpleName();
private static final Preferences CVARS = Gdx.app.getPreferences(Cvar.class.getName());

private final String key;
private final Class<T> type;
private final Set<CvarChangeListener<T>> changeListeners;

private T value;

public Cvar(String key, Class<T> type, T defaultValue) {
    this.key = Objects.toString(key, "");
    this.type = Objects.requireNonNull(type);
    this.value = defaultValue;

    this.changeListeners = new ConcurrentSkipListSet<CvarChangeListener<T>>();
}

public Cvar(String key, Class<T> type, T defaultValue, CvarLoadListener<T> l) {
    this(key, type, defaultValue, defaultValue.toString(), l);
}

public Cvar(String key, Class<T> type, T defaultValue, String defaultStringValue, CvarLoadListener<T> l) {
    this(key, type, defaultValue);
    this.value = l.onCvarLoaded(CVARS.getString(getKey(), defaultStringValue));
    Gdx.app.debug(TAG, String.format("Value loaded as (%s) %s", type.getName(), value));
}

public void addCvarChangeListener(CvarChangeListener<T> l) {
    changeListeners.add(l);
}

public boolean removeCvarChangeListener(CvarChangeListener<T> l) {
    return changeListeners.remove(l);
}

public String getKey() {
    return key;
}

public Class<T> getType() {
    return type;
}

public T getValue() {
    return value;
}

public void setValue(T value) {
    T oldValue = this.value;
    this.value = value;
    Gdx.app.debug(TAG, String.format("Value changed from %s to %s", oldValue, this.value));
    for (CvarChangeListener<T> l : changeListeners) {
        l.onCvarChanged(this, oldValue, this.value);
    }
}

@Override
public String toString() {
    return String.format("%s:%s=%s", type.getName(), getKey(), getValue());
}
}
