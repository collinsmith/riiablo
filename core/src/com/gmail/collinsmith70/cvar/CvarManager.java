package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CvarManager implements CvarChangeListener {

private static final Map<Class<?>, StringSerializer<?>> DEFAULT_SERIALIZERS;
static {
    DEFAULT_SERIALIZERS = new HashMap<Class<?>, StringSerializer<?>>();
    DEFAULT_SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
}

private final Trie<String, Cvar<?>> CVARS;
private final Map<Class<?>, StringSerializer<?>> SERIALIZERS;

private boolean autosave;

public CvarManager() {
    this(true);
}

public CvarManager(boolean autosave) {
    this.autosave = autosave;

    this.CVARS = new PatriciaTrie<Cvar<?>>();
    this.SERIALIZERS = new ConcurrentHashMap<Class<?>, StringSerializer<?>>(DEFAULT_SERIALIZERS);
}

public void setAutosave(boolean b) {
    this.autosave = b;
    if (autosave) {
        saveAll();
    }
}

public boolean isAutosaving() {
    return autosave;
}

@Override
public Object beforeChanged(Cvar cvar, Object from, Object to) {
    return to;
}

@Override
public void afterChanged(Cvar cvar, Object from, Object to) {
    save(cvar);
    commit(cvar);
}

public <T> Cvar<T> create(String alias, String description, Class<T> type, T defaultValue) {
    return create(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
}

public <T> Cvar<T> create(String alias, String description, Class<T> type, T defaultValue, Validator<T> validator) {
    Cvar<T> cvar = new Cvar<T>(alias, description, type, defaultValue, validator);
    load(cvar);
    return add(cvar);
}

public <T> Cvar<T> add(Cvar<T> cvar) {
    if (isManaging(cvar)) {
        return cvar;
    } else if (containsAlias(cvar.getAlias())) {
        throw new DuplicateCvarException(cvar, String.format(
                "A cvar with the alias %s is already registered. Cvar aliases must be unique!",
                cvar.getAlias()));
    }

    CVARS.put(cvar.getAlias().toLowerCase(), cvar);
    cvar.addCvarChangeListener(this);
    return cvar;
}

public <T> boolean remove(Cvar<T> cvar) {
    if (!isManaging(cvar)) {
        return false;
    }

    return CVARS.remove(cvar.getAlias().toLowerCase()) == null;
}

public Cvar<?> get(String alias) {
    alias = alias.toLowerCase();
    return CVARS.get(alias);
}

public <T> Cvar<T> get(String alias, Class<T> type) {
    alias = alias.toLowerCase();
    Cvar<?> cvar = get(alias);
    if (!cvar.getType().isAssignableFrom(type)) {
        throw new IllegalArgumentException(String.format(
                "type should match cvar's type (%s)", cvar.getType().getName()));
    }

    return (Cvar<T>)cvar;
}

public SortedMap<String, Cvar<?>> search(String alias) {
    alias = alias.toLowerCase();
    return CVARS.prefixMap(alias);
}

public Collection<Cvar<?>> getCvars() {
    return CVARS.values();
}

public <T> void load(Cvar<T> cvar) {
    checkIfManaged(cvar);
}

public <T> void save(Cvar<T> cvar) {
    checkIfManaged(cvar);
    commit(cvar);
}

public void saveAll() {
    for (Cvar<?> cvar : CVARS.values()) {
        save(cvar);
    }
}

protected <T> void commit(Cvar<T> cvar) {
    checkIfManaged(cvar);
}

private <T> void checkIfManaged(Cvar<T> cvar) throws UnmanagedCvarException {
    if (isManaging(cvar)) {
        return;
    }

    throw new UnmanagedCvarException(cvar, String.format(
            "Cvar %s is not managed by this %s",
            cvar.getAlias(),
            getClass().getSimpleName()));
}

public <T> boolean isManaging(Cvar<T> cvar) {
    Cvar value = CVARS.get(cvar.getAlias().toLowerCase());
    return cvar.equals(value);
}

public boolean containsAlias(String alias) {
    return CVARS.containsKey(alias.toLowerCase());
}

public <T> StringSerializer<T> getSerializer(Class<T> type) {
    return (StringSerializer<T>)SERIALIZERS.get(type);
}

public <T> StringSerializer<T> getSerializer(Cvar<T> cvar) {
    return getSerializer(cvar.getType());
}

public <T> void putSerializer(Class<T> type, StringSerializer<T> serializer) {
    SERIALIZERS.put(type, serializer);
}

public static abstract class CvarException extends RuntimeException {

    public final Cvar CVAR;

    private CvarException() {
        this(null, null);
    }

    private CvarException(Cvar cvar) {
        this(cvar, null);
    }

    private CvarException(String message) {
        this(null, message);
    }

    private CvarException(Cvar cvar, String message) {
        super(message);
        this.CVAR = cvar;
    }

    private Cvar getCvar() {
        return CVAR;
    }

}

public static class DuplicateCvarException extends CvarException {

    private DuplicateCvarException() {
        this(null, null);
    }

    private DuplicateCvarException(Cvar cvar) {
        this(cvar, null);
    }

    private DuplicateCvarException(String message) {
        this(null, message);
    }

    private DuplicateCvarException(Cvar cvar, String message) {
        super(cvar, message);
    }

}

public static class UnmanagedCvarException extends CvarException {

    private UnmanagedCvarException() {
        this(null, null);
    }

    private UnmanagedCvarException(Cvar cvar) {
        this(cvar, null);
    }

    private UnmanagedCvarException(String message) {
        this(null, message);
    }

    private UnmanagedCvarException(Cvar cvar, String message) {
        super(cvar, message);
    }

}

}
