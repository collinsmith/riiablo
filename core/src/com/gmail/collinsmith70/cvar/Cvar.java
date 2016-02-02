package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.RangeValidator;
import com.gmail.collinsmith70.util.Validator;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.istack.internal.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A <a href="https://en.wikipedia.org/wiki/CVAR">CVAR</a> is a variable used for configuring some
 * part of a game client. Typically, a CVAR is a representation for a value of some arbitrary type,
 * {@link T}, which has a default value and has changes to that value checked via its
 * {@link Validator}. Additionally, CVARs support sending out callbacks when their values change via
 * {@link CvarChangeListener} instances.
 *
 * @param <T> type of the value which this CVAR represents
 */
public class Cvar<T> {

/**
 * {@linkplain String} representation for the alias, or key, of this CVAR
 */
@NotNull
private final String ALIAS;

/**
 * Brief description of this CVAR which should tell what it does and the kinds of values it accepts
 */
@NotNull
private final String DESCRIPTION;

/**
 * Default value of this CVAR set when this CVAR is constructed and which can be reset to at any
 * time via {@link #reset()} which will bypass the {@link Validator} as this default value is
 * asserted to be valid.
 */
@NotNull
private final T DEFAULT_VALUE;

/**
 * Reference to the {@linkplain Class} for value which this CVAR represents.
 */
@NotNull
private final Class<T> TYPE;

/**
 * {@linkplain Validator} to use when checking whether or not objects passed into
 * {@link #setValue(Object)} are valid.
 */
@NotNull
private final Validator<?> VALIDATOR;

/**
 * {@linkplain Set} of {@link CvarChangeListener} instances which should be called back upon
 * certain events in this CVAR's lifetime.
 */
@NotNull
private final Set<CvarChangeListener<T>> CVAR_CHANGE_LISTENERS;

/**
 * Raw value represented by this CVAR.
 */
@NotNull
private T value;

/**
 * Determines whether or not this CVAR has had a value loaded to it. CVARs are marked as loaded
 * as soon as their value changes the first time. Calling {@link #setValue(Object)} makes loading
 * impossible as a value has already been set.
 */
private boolean isLoaded;

/**
 * Constructs a new {@linkplain Cvar} instance with its {@link Validator} set to
 * {@link Validator#ACCEPT_NON_NULL}.
 *
 * @param alias        key to be used when representing the name of this CVAR
 * @param description  brief description of this CVAR and the kinds of values it expects
 * @param type         reference to the class type of the value this CVAR represents
 * @param defaultValue value to be assigned to this CVAR by default and which no validation is
 *                     performed
 */
public Cvar(String alias, String description, Class<T> type, T defaultValue) {
    this(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
}

/**
 * Constructs a new {@linkplain Cvar} instance.
 *
 * @param alias        key to be used when representing the name of this CVAR
 * @param description  brief description of this CVAR and the kinds of values it expects
 * @param type         reference to the class type of the value this CVAR represents
 * @param defaultValue value to be assigned to this CVAR by default and which no validation is
 *                     performed
 * @param validator    {@link Validator} to be used when checking value changes of this CVAR
 */
public Cvar(String alias, String description, Class<T> type, T defaultValue, Validator<?> validator) {
    if (alias == null) {
        throw new NullPointerException("Cvar aliases cannot be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Cvar aliases cannot be empty");
    }

    this.ALIAS = alias;
    this.DESCRIPTION = Strings.nullToEmpty(description);
    this.DEFAULT_VALUE = Preconditions.checkNotNull(defaultValue,
            "Cvar default values cannot be null");
    this.TYPE = Preconditions.checkNotNull(type, "Cvar types cannot be null");
    this.VALIDATOR = validator == null ? Validator.ACCEPT_NON_NULL : validator;

    this.CVAR_CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();

    reset();
    this.isLoaded = false;
}

/**
 * @return key which represents the name of this CVAR
 */
@NotNull
public String getAlias() {
    return ALIAS;
}

/**
 * @return brief description of this CVAR and the kinds of values it expects
 */
@NotNull
public String getDescription() {
    return DESCRIPTION;
}

/**
 * @return value which is assigned to this CVAR by default and which no validation is performed
 */
@NotNull
public T getDefaultValue() {
    return DEFAULT_VALUE;
}

/**
 * @return reference to the class type of the value this CVAR represents
 */
@NotNull
public Class<T> getType() {
    return TYPE;
}

/**
 * @return value represented by this CVAR
 */
@NotNull
public T getValue() {
    return value;
}

/**
 * @return {@code true} if this CVAR has had its value loaded or set at least once,
 *         otherwise {@code false}
 */
public boolean isLoaded() {
    return isLoaded;
}

/**
 * @return {@code true} if the {@link Validator} used by this CVAR is a subclass of
 *         {@link RangeValidator}, otherwise {@code false}
 */
public boolean isRangeValidator() {
    return VALIDATOR instanceof RangeValidator;
}

/**
 * @param value reference to attempt to assign this CVAR to, so long as the {@link Validator} used
 *              accepts it
 *
 * @return {@code true} if the value changed as a result of this action,
 *         otherwise {@code false}
 */
public boolean setValue(T value) {
    if (this.value.equals(value)) {
        return true;
    }

    VALIDATOR.validate(value);
    changeValue(value);
    return true;
}

/**
 * @param o object to check
 * @return {@code true} if the passed object is considered a valid value for this CVAR,
 *         otherwise {@code false}
 */
public boolean isValid(Object o) {
    return VALIDATOR.isValid(o);
}

/**
 * Resets this CVAR to its default value
 *
 * @see #getDefaultValue()
 */
public void reset() {
    changeValue(DEFAULT_VALUE);
}

/**
 * Changes the value of this CVAR to the one specified and notifies all registered listeners of
 * the change.
 *
 * @param value value to change this CVAR to
 */
private void changeValue(T value) {
    assert value != null;
    T oldValue = this.value;
    this.value = value;

    if (!isLoaded) {
        for (CvarChangeListener<T> cvarChangeListener : CVAR_CHANGE_LISTENERS) {
            cvarChangeListener.onLoad(this, this.value);
        }

        this.isLoaded = true;
    } else {
        for (CvarChangeListener<T> cvarChangeListener : CVAR_CHANGE_LISTENERS) {
            cvarChangeListener.onChanged(this, oldValue, this.value);
        }
    }
}

/**
 * Registers a {@linkplain CvarChangeListener} instance so that it will receive callbacks from
 * state change events regarding this CVAR.
 *
 * @param l {@linkplain CvarChangeListener} instance to register
 */
public void addCvarChangeListener(CvarChangeListener<T> l) {
    CVAR_CHANGE_LISTENERS.add(l);
    l.onLoad(this, value);
}

/**
 * @param l {@linkplain CvarChangeListener} to check
 * @return {@code true} if the passed {@linkplain CvarChangeListener} will receive callbacks
 *         regarding changes made to the state of this CVAR, otherwise {@code false}
 */
public boolean containsCvarChangeListener(CvarChangeListener<T> l) {
    return CVAR_CHANGE_LISTENERS.contains(l);
}

/**
 * @param l {@linkplain CvarChangeListener} to remove
 * @return {@code true} if the passed {@linkplain CvarChangeListener} was removed, otherwise
 *         {@code false} if it was not receiving callbacks
 */
public boolean removeCvarChangeListener(CvarChangeListener<T> l) {
    return CVAR_CHANGE_LISTENERS.remove(l);
}

/**
 * @return {@linkplain String} representation of the value of this CVAR
 */
@Override
public String toString() {
    return value.toString();
}

}
