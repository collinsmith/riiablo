package com.gmail.collinsmith70.key;

import com.gmail.collinsmith70.util.AddRemoveListener;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class KeyManager<T> implements AddRemoveListener<Key<T>, T> {

private final Map<T, Key<T>> KEYS;

private boolean autosave;

public KeyManager() {
    this(true);
}

public KeyManager(boolean autosave) {
    this.autosave = true;
    this.KEYS = new ConcurrentHashMap<T, Key<T>>();
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
public void onAdded(T binding, Key<T> key) {
    KEYS.put(binding, key);
}

@Override
public void onRemoved(T binding, Key<T> key) {
    KEYS.remove(binding);
}

public Key<T> add(Key<T> key) {
    if (isManaging(key)) {
        return key;
    }

    key.addBindingListener(this);
    return key;
}

public boolean remove(Key<T> key) {
    if (!isManaging(key)) {
        return false;
    }

    boolean removed = false;
    for (T binding : key.getBindings()) {
        if (!removed) {
            removed = KEYS.remove(binding) != null;
            continue;
        }

        KEYS.remove(binding);
    }

    return removed;
}

public Key<T> get(T binding) {
    return KEYS.get(binding);
}

public Collection<Key<T>> getKeys() {
    return KEYS.values();
}

public boolean isManaging(Key<?> key) {
    return KEYS.containsValue(key);
}


public void load(Key<T> key) {
    checkIfManaged(key);
}

public void save(Key<T> key) {
    checkIfManaged(key);
    commit(key);
}

public void saveAll() {
    for (Key<T> key : KEYS.values()) {
        save(key);
    }
}

protected void commit(Key<T> key) {
    checkIfManaged(key);
}

private void checkIfManaged(Key<T> key) throws UnmanagedKeyException {
    if (isManaging(key)) {
        return;
    }

    throw new UnmanagedKeyException(key, String.format(
            "Key %s is not managed by this %s",
            key.getAlias(),
            getClass().getSimpleName()));
}

public boolean containsBinding(T binding) {
    return KEYS.containsKey(binding);
}

public abstract class KeyException extends RuntimeException {

    public final Key<T> KEY;

    private KeyException() {
        this(null, null);
    }

    private KeyException(Key<T> key) {
        this(key, null);
    }

    private KeyException(String message) {
        this(null, message);
    }

    private KeyException(Key<T> key, String message) {
        super(message);
        this.KEY = key;
    }

    private Key<T> getKey() {
        return KEY;
    }

}

public class DuplicateKeyException extends KeyException {

    private DuplicateKeyException() {
        this(null, null);
    }

    private DuplicateKeyException(Key<T> key) {
        this(key, null);
    }

    private DuplicateKeyException(String message) {
        this(null, message);
    }

    private DuplicateKeyException(Key<T> key, String message) {
        super(key, message);
    }

}

public class UnmanagedKeyException extends KeyException {

    private UnmanagedKeyException() {
        this(null, null);
    }

    private UnmanagedKeyException(Key<T> key) {
        this(key, null);
    }

    private UnmanagedKeyException(String message) {
        this(null, message);
    }

    private UnmanagedKeyException(Key<T> key, String message) {
        super(key, message);
    }

}

}
