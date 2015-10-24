package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.google.collinsmith70.util.TernaryTrie;
import com.google.collinsmith70.util.Trie;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cvar<T> {
private static final String TAG = Cvar.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Cvar.class.getName());
private static final Trie<Cvar<?>> CVARS = new TernaryTrie<Cvar<?>>();

public static Iterable<String> lookup(String key) {
    return CVARS.prefixMatch(key);
}

private final String key;
private final Class<T> type;
private final Set<CvarChangeListener<T>> changeListeners;

private T value;

public Cvar(String key, Class<T> type, T defaultValue) {
    this.key = Objects.toString(key, "");
    this.type = type != null ? type : (Class<T>)defaultValue.getClass();
    this.value = defaultValue;

    this.changeListeners = new CopyOnWriteArraySet<CvarChangeListener<T>>();
    CVARS.put(key, this); // potentially unsafe (technically object is not constructed yet)
}

public Cvar(String key, Class<T> type, T defaultValue, CvarLoadListener<T> l) {
    this(key, type, defaultValue, defaultValue.toString(), l);
}

public Cvar(String key, Class<T> type, T defaultValue, String defaultStringValue, CvarLoadListener<T> l) {
    this(key, type, defaultValue);
    this.value = l.onCvarLoaded(PREFERENCES.getString(getKey(), defaultStringValue));
    Gdx.app.log(TAG, String.format("Value loaded as (%s) %s", getType().getName(), getValue()));
    CVARS.put(key, this); // potentially unsafe (technically object is not constructed yet)
}

public void addCvarChangeListener(CvarChangeListener<T> l) {
    changeListeners.add(l);
    l.onCvarChanged(this, null, this.value);
}

public boolean containsCvarChangeListener(CvarChangeListener<T> l) {
    return changeListeners.contains(l);
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
    Gdx.app.log(TAG, String.format("Value changed from %s to %s", oldValue, this.value));
    for (CvarChangeListener<T> l : changeListeners) {
        l.onCvarChanged(this, oldValue, this.value);
    }
}

@Override
public String toString() {
    return String.format("%s:%s=%s", type.getName(), getKey(), getValue());
}
}
