package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.RangeValidator;
import com.gmail.collinsmith70.util.Validator;
import com.google.common.base.Preconditions;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cvar<T> {

private final String ALIAS;
private final String DESCRIPTION;
private final T DEFAULT_VALUE;
private final Class<T> TYPE;
private final Validator<?> VALIDATOR;
private final Set<CvarChangeListener<T>> CVAR_CHANGE_LISTENERS;

private T value;

public Cvar(String alias, String description, Class<T> type, T defaultValue) {
    this(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
}

public Cvar(String alias, String description, Class<T> type, T defaultValue, Validator<?> validator) {
    if (alias == null) {
        throw new NullPointerException("Cvar aliases cannot be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Cvar aliases cannot be empty");
    }

    this.ALIAS = alias;
    this.DESCRIPTION = description;
    this.DEFAULT_VALUE = Preconditions.checkNotNull(defaultValue,
            "Cvar default values cannot be null");
    this.TYPE = Preconditions.checkNotNull(type, "Cvar types cannot be null");
    this.VALIDATOR = validator == null ? Validator.ACCEPT_NON_NULL : validator;

    this.CVAR_CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();

    reset();
}

public String getAlias() { return ALIAS; }
public String getDescription() { return DESCRIPTION; }
public T getDefaultValue() { return DEFAULT_VALUE; }
public Class<T> getType() { return TYPE; }
public Validator<?> getValidator() { return VALIDATOR; }
public T getValue() { return value; }

public boolean isRangeValidator() {
    return VALIDATOR instanceof RangeValidator;
}

public boolean setValue(T value) {
    if (this.value.equals(value)) {
        return true;
    }

    if (!VALIDATOR.isValid(value)) {
        return false;
    }

    for (CvarChangeListener<T> cvarChangeListener : CVAR_CHANGE_LISTENERS) {
        value = cvarChangeListener.beforeChanged(this, this.value, value);
    }

    if (this.value.equals(value)) {
        return true;
    }

    changeValue(value);
    return true;
}

public void reset() {
    changeValue(DEFAULT_VALUE);
}

private void changeValue(T value) {
    T oldValue = this.value;
    this.value = value;

    for (CvarChangeListener<T> cvarChangeListener : CVAR_CHANGE_LISTENERS) {
        cvarChangeListener.afterChanged(this, oldValue, this.value);
    }
}

void load(T value) {
    this.value = value;
    for (CvarChangeListener<T> cvarChangeListener : CVAR_CHANGE_LISTENERS) {
        cvarChangeListener.onLoad(this, this.value);
    }
}

public void addCvarChangeListener(CvarChangeListener<T> l) {
    CVAR_CHANGE_LISTENERS.add(l);
    l.onLoad(this, value);
}

public boolean containsCvarChangeListener(CvarChangeListener<T> l) {
    return CVAR_CHANGE_LISTENERS.contains(l);
}

public boolean removeCvarChangeListener(CvarChangeListener<T> l) {
    return CVAR_CHANGE_LISTENERS.remove(l);
}

@Override
public String toString() {
    return String.format("%s:%s=%s", TYPE.getName(), ALIAS, value);
}

}
