package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.checker.NullValueValidator;
import com.gmail.collinsmith70.cvar.serializer.ObjectSerializer;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.PrintStream;
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

private static boolean autosave = true;

public static boolean isAutosaving() {
    return autosave;
}

public static void setAutosave(boolean b) {
    Cvar.autosave = b;
}

private final String ALIAS;
private final T DEFAULT_VALUE;
private final Class<T> TYPE;
private final Serializer<T, String> SERIALIZER;
private final ValueValidator<T> VALUE_VALIDATOR;
private final Set<CvarChangeListener<T>> CHANGE_LISTENERS;

private PrintStream out;

private T value;

public Cvar(String alias, Class<T> type, T defaultValue) {
    this(alias, type, defaultValue, getSerializer(type));
}

public Cvar(String alias, Class<T> type, T defaultValue, Serializer<T, String> serializer) {
    this(alias, type, defaultValue, serializer, null);
}

public Cvar(String alias, Class<T> type, T defaultValue, Serializer<T, String> serializer,
            ValueValidator<T> valueValidator) {
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
    this.VALUE_VALIDATOR = valueValidator == null ? NullValueValidator.INSTANCE : valueValidator;

    this.CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();

    this.out = System.out;
    this.value = DEFAULT_VALUE;
    if (SERIALIZER == null) {
        out.printf("Cvar '%s' cannot be saved or loaded because no serializer has been set%n",
                ALIAS);
    } else {
        String serializedValue = PREFERENCES.getString(ALIAS);
        if (serializedValue == null) {
            serializedValue = SERIALIZER.serialize(defaultValue);
        } else {
            setValue(SERIALIZER.deserialize(serializedValue));
        }

        out.printf("%s loaded as %s [%s]%n",
                ALIAS,
                serializedValue,
                TYPE.getName());
    }
}

public PrintStream getOut() {
    return out;
}

public void setOut(PrintStream out) {
    this.out = out;
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

public ValueValidator<T> getBoundsChecker() {
    return VALUE_VALIDATOR;
}

public T getValue() {
    return value;
}

public void setValue(T value) {
    if (this.value.equals(value)) {
        return;
    }

    if (!VALUE_VALIDATOR.isValid(value)) {
        out.printf("failed to change %s from %s to %s%n",
                ALIAS,
                this.value,
                value);
        return;
    } else {
        out.printf("changing %s from %s to %s%n",
                ALIAS,
                this.value,
                value);
    }

    T oldValue = this.value;
    this.value = value;
    if (Cvar.autosave && SERIALIZER != null) {
        save();
    }

    for (CvarChangeListener<T> l : CHANGE_LISTENERS) {
        l.onCvarChanged(this, oldValue, this.value);
    }
}

public String getSerializedValue() {
    return SERIALIZER.serialize(value);
}

public void save() {
    Cvar.PREFERENCES.putString(ALIAS, SERIALIZER.serialize(value));
}

public void load() {
    throw new UnsupportedOperationException("Not supported yet!");
}

}
