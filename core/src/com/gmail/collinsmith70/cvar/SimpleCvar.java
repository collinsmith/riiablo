package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Basic implementation of a {@link Cvar}.
 *
 * @param <T> {@linkplain Class type} of the {@linkplain #getValue variable} which this SimpleCvar
 *            represents
 */
public class SimpleCvar<T> implements Cvar<T> {

/**
 * {@link String} representation for the <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">
 * key</a> of this {@link Cvar}, or {@code null} if no alias has been set
 *
 * @see <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">
 *      Wikipedia article on key-value pairs</a>
 * @see #getAlias()
 */
@Nullable
private final String ALIAS;

/**
 * Default value of this {@link Cvar}, which is assigned upon instantiation and when this {@link
 * Cvar} is {@linkplain #reset reset}.
 *
 * @see #reset()
 * @see #getDefaultValue()
 */
@Nullable
private final T DEFAULT_VALUE;

/**
 * Brief {@linkplain String description} explaining the function of this {@link Cvar} and the values
 * it expects.
 *
 * @see #getDescription()
 */
@NonNull
private final String DESCRIPTION;

/**
 * {@link Class} instance for the {@linkplain T type} of the {@linkplain #getValue variable} which
 * this {@link Cvar} represents.
 *
 * @see #getType()
 */
@NonNull
private final Class<T> TYPE;

/**
 * {@link Set} of {@link Cvar.StateListener} which will receive callbacks during state transitions
 * of this {@link Cvar}.
 *
 * @see #addStateListener(StateListener)
 * @see #containsStateListener(StateListener)
 * @see #removeStateListener(StateListener)
 */
@NonNull
private final Set<Cvar.StateListener<T>> STATE_LISTENERS;

/**
 * Value of the {@linkplain #getValue variable} represented by this {@link Cvar}.
 *
 * @see #getValue()
 * @see #setValue(Object)
 */
@Nullable
private T value;

/**
 * {@code true} if this {@link Cvar} has had its first {@linkplain #setValue assignment},
 * otherwise {@code false}.
 *
 * @see #isLoaded()
 */
private boolean isLoaded;

/**
 * Constructs a new {@link SimpleCvar} instance.
 * <p>
 * Note: Passing a {@code null} reference for {@code description} will set the {@linkplain
 *       #getDescription description} to {@code ""}.
 *
 * @param alias        {@link String} representation of the {@linkplain #getAlias name}
 * @param description  Brief {@linkplain #getDescription description} explaining the function and
 *                     values it expects
 * @param type         {@link Class} instance for the {@linkplain T type} of the {@linkplain
 *                     #getValue variable}
 * @param defaultValue {@linkplain #getDefaultValue Default value} which will be assigned to the
 *                     {@link Cvar} now and whenever it is {@linkplain #reset reset}.
 */
public SimpleCvar(@Nullable final String alias, @Nullable final String description,
                  @NonNull final Class<T> type, @Nullable final T defaultValue) {
    Preconditions.checkArgument(type != null, "type is not allowed to be null");

    this.ALIAS = alias;
    this.DESCRIPTION = Strings.nullToEmpty(description);
    this.DEFAULT_VALUE = defaultValue;
    this.TYPE = type;
    this.STATE_LISTENERS = new CopyOnWriteArraySet<Cvar.StateListener<T>>();

    this.value = DEFAULT_VALUE;
    this.isLoaded = false;
}

@Nullable
@Override
public String getAlias() {
    return ALIAS;
}

@Nullable
@Override
public T getDefaultValue() {
    return DEFAULT_VALUE;
}

@NonNull
@Override
public String getDescription() {
    return DESCRIPTION;
}

@NonNull
@Override
public Class<T> getType() {
    return TYPE;
}

@Override
@Nullable
public T getValue() {
    return value;
}

@Override
public boolean isEmpty() {
    return getValue() == null;
}

@Override
public boolean isLoaded() {
    return isLoaded;
}

@Override
public void reset() {
    setValue(DEFAULT_VALUE);
}

@Override
public void setValue(@Nullable final T value) {
    if (getValue() == null && value == null) {
        return;
    } else if (getValue() != null && getValue().equals(value)) {
        return;
    } else if (value != null && value.equals(getValue())) {
        return;
    }

    final T oldValue = this.value;
    this.value = value;

    if (isLoaded()) {
        for (Cvar.StateListener<T> stateListener : STATE_LISTENERS) {
            stateListener.onChanged(this, oldValue, getValue());
        }
    } else {
        for (Cvar.StateListener<T> stateListener : STATE_LISTENERS) {
            stateListener.onLoaded(this, getValue());
        }

        this.isLoaded = true;
    }
}

/**
 * {@inheritDoc}
 * <p>
 * Note: Calls {@link Cvar.StateListener#onLoaded} for the passed {@link Cvar.StateListener} so that
 *       it can perform any necessary setup.
 * </p>
 */
@Override
public void addStateListener(@NonNull final StateListener<T> l) {
    if (l == null) {
        throw new IllegalArgumentException("state listener is not allowed to be null");
    }

    STATE_LISTENERS.add(l);
    l.onLoaded(this, getValue());
}

@Override
public boolean containsStateListener(@Nullable final StateListener<T> l) {
    return l != null && STATE_LISTENERS.contains(l);

}

@Override
public boolean removeStateListener(@Nullable final StateListener<T> l) {
    return l != null && STATE_LISTENERS.remove(l);

}

/**
 * @return {@link String} representation of the {@linkplain #getValue value} of this {@link Cvar},
 *         or {@code "null"} if it is {@code null}.
 */
@Override
@NonNull
public String toString() {
    if (getValue() == null) {
        return "null";
    }

    return getValue().toString();
}

}
