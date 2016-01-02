package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.serializer.ObjectSerializer;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cvar<T> {

private static final String TAG = Cvar.class.getSimpleName();
private static final Preferences PREFERENCES = Gdx.app.getPreferences(Cvar.class.getName());
private static final Trie<String, Cvar<?>> CVARS = new PatriciaTrie<Cvar<?>>();

private static final Map<Class<?>, Serializer<?, String>> SERIALIZERS;
static {
    SERIALIZERS = new ConcurrentHashMap<Class<?>, Serializer<?, String>>();
    SERIALIZERS.put(String.class, ObjectSerializer.INSTANCE);
}

public static <T> Serializer<T, String> getSerializer(Class<T> type) {
    return (Serializer<T, String>)SERIALIZERS.get(type);
}

public static <T> void setSerializer(Class<T> type, Serializer<T, String> serializer) {
    SERIALIZERS.put(type, serializer);
}

private final String ALIAS;
private final T DEFAULT_VALUE;
private final Class<T> TYPE;
private final Serializer<T, String> SERIALIZER;
private final BoundsChecker<T> BOUNDS_CHECKER;
private final Set<CvarChangeListener<T>> CHANGE_LISTENERS;

private T value;

public Cvar(String alias, Class<T> type, T defaultValue) {
    this(alias, type, defaultValue, getSerializer(type));
}

public Cvar(String alias, Class<T> type, T defaultValue, Serializer<T, String> serializer) {
    this(alias, type, defaultValue, serializer, null);
}

public Cvar(String alias, Class<T> type, T defaultValue, Serializer<T, String> serializer,
            BoundsChecker<T> boundsChecker) {
    if (alias == null) {
        throw new NullPointerException("Cvar aliases cannot be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Cvar aliases cannot be empty");
    }

    this.ALIAS = alias;
    this.DEFAULT_VALUE = Preconditions.checkNotNull(defaultValue,
            "Cvar default values cannot be null");
    this.TYPE = Preconditions.checkNotNull(type, "Cvar types cannot be null");
    this.SERIALIZER = serializer;
    this.BOUNDS_CHECKER = boundsChecker;

    this.CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();

    this.value = DEFAULT_VALUE;
    if (SERIALIZER == null) {
        // log warning that Cvar cannot be serialized and saved/loaded
        //setValue(defaultValue);
    } else {
        String serializedValue = PREFERENCES.getString(ALIAS);
        if (serializedValue == null) {
            setValue(defaultValue);
        } else {
            setValue(SERIALIZER.deserialize(serializedValue));
        }

        // log info that Cvar has been loaded
    }
}

public String getAlias() {
    return ALIAS;
}

public T getDefaultValue() {
    return DEFAULT_VALUE;
}

public Class<T> getType() {
    return TYPE;
}

public Serializer<T, String> getSerializer() {
    return SERIALIZER;
}

public BoundsChecker<T> getBoundsChecker() {
    return BOUNDS_CHECKER;
}

public T getValue() {
    return value;
}

public void setValue(T value) {

}

}
