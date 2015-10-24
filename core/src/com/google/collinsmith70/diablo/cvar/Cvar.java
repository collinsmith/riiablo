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

public static Set<String> search(String key) {
    return CVARS.getKeysPrefixedWith(key);
}

public static Cvar<?> get(String key) {
    return CVARS.get(key);
}

private final String key;
private final Class<T> type;
private CvarLoadListener<T> loadListener;
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
    this.loadListener = l;
    this.value = loadListener.onCvarLoaded(PREFERENCES.getString(getKey(), defaultStringValue));
    Gdx.app.log(TAG, String.format("Value loaded as (%s) %s", getType().getName(), getValue()));
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
    Gdx.app.log(TAG, String.format("%s changed from %s to %s", getKey(), oldValue, getValue()));
    for (CvarChangeListener<T> l : changeListeners) {
        l.onCvarChanged(this, oldValue, getValue());
    }
}

public void setValue(String value) {
    if (loadListener != null) {
        setValue(loadListener.onCvarLoaded(PREFERENCES.getString(getKey(), value)));
        return;
    }

    if (getType().equals(String.class)) {
        setValue((T)value);
    } else if (getType().equals(Boolean.class)) {
        setValue((T)new Boolean(Boolean.parseBoolean(value)));
    } else if (getType().equals(Float.class)) {
        setValue((T)new Float(Float.parseFloat(value)));
    } else if (getType().equals(Double.class)) {
        setValue((T)new Double(Double.parseDouble(value)));
    } else if (getType().equals(Byte.class)) {
        setValue((T)new Byte(Byte.parseByte(value)));
    } else if (getType().equals(Integer.class)) {
        setValue((T)new Integer(Integer.parseInt(value)));
    } else if (getType().equals(Long.class)) {
        setValue((T)new Long(Long.parseLong(value)));
    } else {
        throw new IllegalStateException(String.format(
                "No CvarLoadListener configured for this Cvar " +
                "which can parse the given value \"%s\"", value));
    }
}

@Override
public String toString() {
    return String.format("%s:%s=%s", type.getName(), getKey(), getValue());
}
}
