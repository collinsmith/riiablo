package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

public class CvarManager {

private final Map<Class<?>, StringSerializer> SERIALIZERS;

public <T> StringSerializer<T> getSerializer(Class<T> type) {
    return (StringSerializer<T>)SERIALIZERS.get(type);
}

public <T> void setSerializer(Class<T> type, StringSerializer<T> serializer) {
    SERIALIZERS.put(type, serializer);
}

private final Trie<String, Cvar<?>> CVARS;

private CvarManagerListener cvarManagerListener;

public CvarManager(CvarManagerListener cvarManagerListener) {
    CVARS = new PatriciaTrie<Cvar<?>>();
    SERIALIZERS = new ConcurrentHashMap<Class<?>, StringSerializer>();
    configureDefaultSerializers();

    setCvarManagerListener(cvarManagerListener);
}

private void configureDefaultSerializers() {
    SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
}

public void setCvarManagerListener(CvarManagerListener cvarManagerListener) {
    this.cvarManagerListener = Preconditions.checkNotNull(cvarManagerListener);
}

public CvarManagerListener getCvarManagerListener() {
    return cvarManagerListener;
}

public SortedMap<String, Cvar<?>> search(String key) {
    key = key.toLowerCase();
    return CVARS.prefixMap(key);
}

public Cvar<?> get(String key) {
    key = key.toLowerCase();
    return CVARS.get(key);
}

public <T> Cvar<T> get(String key, Class<T> type) {
    if (type == null) {
        throw new NullPointerException(
                "type cannot be null, did you mean to use Cvar.get(String key) instead?");
    }

    return (Cvar<T>)Cvar.get(key);
}

public Collection<Cvar<?>> getCvars() {
    return CVARS.values();
}

public <T> Cvar<T> register(Cvar<T> cvar) throws IllegalArgumentException {
    String lowercaseAlias = cvar.getAlias().toLowerCase();
    if (CVARS.containsKey(lowercaseAlias)) {
        throw new IllegalArgumentException(String.format(
                "A Cvar with the alias %s is already registered. Cvar aliases must be unique!",
                cvar.getAlias()));
    }

    CVARS.put(lowercaseAlias, cvar);
    return cvar;
}

public void commit() {
    cvarManagerListener.onCommit();
}

public void save(Cvar cvar) {
    cvarManagerListener.onSave(cvar);
    commit();
}

public void load(Cvar cvar) {
    cvarManagerListener.onLoad(cvar);
}

}
