package com.gmail.collinsmith70.key;

import com.gmail.collinsmith70.util.AddRemoveListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Key<T> {

private final String NAME;
private final String ALIAS;
private final Set<T> BINDINGS;

private final Set<AddRemoveListener<Key<T>, T>> BIND_LISTENERS;
private final Set<KeyStateListener<T>> STATE_LISTENERS;

private boolean isPressed;

public Key(String name, String alias, T... bindings) {
    if (name == null) {
        throw new NullPointerException("Key names cannot be null");
    } else if (name.isEmpty()) {
        throw new IllegalArgumentException("Key names cannot be empty");
    }

    if (alias == null) {
        throw new NullPointerException("Key aliases cannot be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Key aliases cannot be empty");
    }

    this.BIND_LISTENERS = new CopyOnWriteArraySet<AddRemoveListener<Key<T>, T>>();
    this.STATE_LISTENERS = new CopyOnWriteArraySet<KeyStateListener<T>>();

    this.NAME = name;
    this.ALIAS = alias;
    this.BINDINGS = new CopyOnWriteArraySet<T>();
    for (T binding : bindings) {
        addBinding(binding);
    }

    this.isPressed = false;
}

public String getName() { return NAME; }
public String getAlias() { return ALIAS; }
public Set<T> getBindings() { return ImmutableSet.copyOf(BINDINGS); }
public boolean isPressed() { return isPressed; }
public void setPressed(boolean b) { setPressed(b, null); }

public void setPressed(boolean b, T binding) {
    this.isPressed = b;
    if (isPressed) {
        for (KeyStateListener<T> keyStateListener : STATE_LISTENERS) {
            keyStateListener.onPressed(this, binding);
        }
    } else {
        for (KeyStateListener<T> keyStateListener : STATE_LISTENERS) {
            keyStateListener.onDepressed(this, binding);
        }
    }
}

public Key<T> addBinding(T binding) {
    if (binding == null) {
        throw new NullPointerException("Key binds cannot not be null");
    }

    BINDINGS.add(binding);
    for (AddRemoveListener<Key<T>, T> bindListener : BIND_LISTENERS) {
        bindListener.onAdded(binding, this);
    }

    return this;
}

void load(Set<T> bindings) {
    BINDINGS.clear();
    for (T binding : bindings) {
        BINDINGS.add(Preconditions.checkNotNull(binding));
    }
}

public boolean containsBinding(T binding) {
    return BINDINGS.contains(binding);
}

public boolean removeBinding(T binding) {
    boolean removed = BINDINGS.remove(binding);
    if (removed) {
        for (AddRemoveListener<Key<T>, T> bindListener : BIND_LISTENERS) {
            bindListener.onRemoved(binding, this);
        }
    }

    return removed;
}

public void addBindingListener(AddRemoveListener<Key<T>, T> l) {
    BIND_LISTENERS.add(l);
    for (T binding : BINDINGS) {
        l.onLoad(binding, this);
    }
}

public boolean containsBindingListener(AddRemoveListener<Key<T>, T> l) {
    return BIND_LISTENERS.contains(l);
}

public boolean removeBindingListener(AddRemoveListener<Key<T>, T> l) {
    return BIND_LISTENERS.remove(l);
}

public void addStateListener(KeyStateListener<T> l) {
    STATE_LISTENERS.add(l);
}

public boolean containsStateListener(KeyStateListener<T> l) {
    return STATE_LISTENERS.contains(l);
}

public boolean removeStateListener(KeyStateListener<T> l) {
    return STATE_LISTENERS.remove(l);
}

}
