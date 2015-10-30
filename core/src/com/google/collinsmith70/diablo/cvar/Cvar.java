package com.google.collinsmith70.diablo.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.util.TernaryTrie;
import com.gmail.collinsmith70.util.Trie;

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

private static final Set<CvarChangeListener<Object>> GLOBAL_CHANGE_LISTENERES
        = new CopyOnWriteArraySet<CvarChangeListener<Object>>();

public static void addGlobalCvarChangeListener(CvarChangeListener<Object> l) {
    GLOBAL_CHANGE_LISTENERES.add(l);
}

public static boolean containsGlobalCvarChangeListener(CvarChangeListener<Object> l) {
    return GLOBAL_CHANGE_LISTENERES.contains(l);
}

public static boolean removeGlobalCvarChangeListener(CvarChangeListener<Object> l) {
    return GLOBAL_CHANGE_LISTENERES.remove(l);
}

private final String KEY;
private final Class<T> TYPE;
private final CvarLoadListener<T> LOAD_LISTENER;
private final Set<CvarChangeListener<T>> CHANGE_LISTENERS;

private T value;

public Cvar(String key, Class<T> type, T defaultValue) {
    this(key, type, defaultValue, defaultValue.toString(), null);
}

public Cvar(String key, Class<T> type, T defaultValue, CvarLoadListener<T> l) {
    this(key, type, defaultValue, defaultValue.toString(), l);
}

public Cvar(String key, Class<T> type, T defaultValue, String defaultStringValue, CvarLoadListener<T> l) {
    this.KEY = Objects.toString(key, "");
    this.TYPE = type != null ? type : (Class<T>)defaultValue.getClass();

    this.CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();
    CVARS.put(key, this); // potentially unsafe (technically object is not constructed yet)

    this.LOAD_LISTENER = l;
    this.value = LOAD_LISTENER == null
            ? defaultValue
            : LOAD_LISTENER.onCvarLoaded(PREFERENCES.getString(getKey(), defaultStringValue));

    Gdx.app.log(TAG, String.format("%s loaded as %s [%s]",
            key,
            getValue().toString().toUpperCase(),
            getType().getName()));
}

public void addCvarChangeListener(CvarChangeListener<T> l) {
    CHANGE_LISTENERS.add(l);
    l.onCvarChanged(this, null, this.value);
}

public boolean containsCvarChangeListener(CvarChangeListener<T> l) {
    return CHANGE_LISTENERS.contains(l);
}

public boolean removeCvarChangeListener(CvarChangeListener<T> l) {
    return CHANGE_LISTENERS.remove(l);
}

public String getKey() {
    return KEY;
}

public Class<T> getType() {
    return TYPE;
}

public T getValue() {
    return value;
}

public void setValue(T value) {
    T oldValue = this.value;
    this.value = value;
    Gdx.app.log(TAG, String.format("%s changed from %s to %s", getKey(), oldValue, getValue()));
    for (CvarChangeListener<T> l : CHANGE_LISTENERS) {
        l.onCvarChanged(this, oldValue, getValue());
    }

    for (CvarChangeListener<Object> l : GLOBAL_CHANGE_LISTENERES) {
        l.onCvarChanged((Cvar<Object>)this, oldValue, getValue());
    }
}

public void setValue(String value) {
    if (LOAD_LISTENER != null) {
        setValue(LOAD_LISTENER.onCvarLoaded(PREFERENCES.getString(getKey(), value)));
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
    return String.format("%s:%s=%s", TYPE.getName(), getKey(), getValue());
}
}
