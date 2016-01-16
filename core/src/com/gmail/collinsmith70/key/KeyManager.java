package com.gmail.collinsmith70.key;

import com.gmail.collinsmith70.util.AddRemoveListener;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyManager<T> {

private final Map<T, Key<T>> KEYS;

public KeyManager() {
    this.KEYS = new ConcurrentHashMap<T, Key<T>>();
}

public Key<T> add(Key<T> key) {
    if (isManaging(key)) {
        return key;
    }

    key.addBindingListener(new AddRemoveListener<Key<T>, T>() {
        @Override
        public void onAdded(T binding, Key<T> key) {
            KEYS.put(binding, key);
        }

        @Override
        public void onRemoved(T binding, Key<T> key) {
            KEYS.remove(binding);
        }
    });

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

public <T> boolean isManaging(Key<T> key) {
    return KEYS.containsValue(key);
}

}
