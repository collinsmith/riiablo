package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.cvar.validator.AcceptAllValueValidator;
import com.gmail.collinsmith70.util.Serializer;
import com.gmail.collinsmith70.util.StringSerializer;
import com.google.common.base.Preconditions;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Representation used for a <a href="https://en.wikipedia.org/wiki/CVAR">CVAR</a>. CVARs are
 * variables used for controlling parameters of a client application. In this implementation, a
 * CVAR represents a container for this variable which is accessable through {@link #getValue()}
 * and set through {@link #setValue(Object)}.
 *
 * By default, CVARs will be saved and loaded using their wrapper object representations
 * (if available), however if {@linkplain T} has no wrapper, then a custom {@link StringSerializer}
 * will need to be assigned to {@linkplain T} using {@link Cvar#setSerializer(Class, Serializer)}.
 * In the case where no {@linkplain StringSerializer} has been specified for {@linkplain T}, then
 * the CVAR cannot be saved or loaded and will be initialized with the default value returned by
 * {@link #getDefaultValue()}.
 *
 * @param <T> class which this {@linkplain Cvar} represents a value for
 */
public class Cvar<T> {



/**************************************************************************************************/

private final String ALIAS;
private final T DEFAULT_VALUE;
private final Class<T> TYPE;
private final StringSerializer<T> SERIALIZER;
private final ValueValidator<T> VALUE_VALIDATOR;
private final Set<CvarChangeListener<T>> CHANGE_LISTENERS;
private final Set<CvarManager> CVAR_MANAGERS;

private T value;

/**
 * Constructs a {@linkplain Cvar} with an alias, type and default value. The value of this
 * {@linkplain Cvar} will attempt to be loaded using the default {@link StringSerializer} for the
 * specified type accessible through {@link Cvar#getSerializer(Class)}.
 *
 * @note this constructor will construct a {@link Cvar} with {@link AcceptAllValueValidator#INSTANCE} set
 *       as its {@link ValueValidator}.
 *
 * @param alias        {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type         {@link Class} reference for the type of the value represented by the
 *                     {@linkplain Cvar}
 * @param defaultValue initial value of the {@linkplain Cvar} should it not be loaded
 */
public Cvar(String alias, Class<T> type, T defaultValue) {
    this(alias, type, defaultValue, getSerializer(type), AcceptAllValueValidator.INSTANCE);
}

/**
 * Constructs a {@linkplain Cvar} with an alias, type, default value and {@link ValueValidator}
 * which will be used to validate whether or not values attempted to be assigned to the
 * {@linkplain Cvar} are valid. The value of this {@linkplain Cvar} will attempt to be loaded using
 * the default {@link StringSerializer} for the specified type accessible through
 * {@link Cvar#getSerializer(Class)}.
 *
 * @note the default value will not be validated using the specified {@link ValueValidator} and will
 *       be assigned regardless. Loaded values will still be validated and set to the default value
 *       when invalid.
 *
 * @param alias          {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type           {@link Class} reference for the type of the value represented by the
 *                       {@linkplain Cvar}
 * @param defaultValue   initial value of the {@linkplain Cvar} should it not be loaded
 * @param valueValidator {@linkplain ValueValidator} used to validate values loaded by (or set to)
 *                       the {@linkplain Cvar}
 */
public Cvar(String alias, Class<T> type, T defaultValue, ValueValidator<T> valueValidator) {
    this(alias, type, defaultValue, getSerializer(type), valueValidator);
}

/**
 * Constructs a {@linkplain Cvar} with an alias, type, default value and custom
 * {@link StringSerializer}. The value of this {@linkplain Cvar} will attempt to be loaded using
 * the specified {@linkplain StringSerializer}.
 *
 * @note this constructor will construct a {@link Cvar} with {@link AcceptAllValueValidator#INSTANCE} set
 *       as its {@link ValueValidator}.
 *
 * @param alias        {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type         {@link Class} reference for the type of the value represented by the
 *                     {@linkplain Cvar}
 * @param defaultValue initial value of the {@linkplain Cvar} should it not be loaded
 * @param serializer   custom {@linkplain StringSerializer} to (de)serialize the {@linkplain Cvar}
 */
public Cvar(String alias, Class<T> type, T defaultValue, StringSerializer<T> serializer) {
    this(alias, type, defaultValue, serializer, AcceptAllValueValidator.INSTANCE);
}

/**
 * Constructs a {@linkplain Cvar} with a custom {@link StringSerializer} and {@link ValueValidator}.
 *
 * @note the default value will not be validated using the specified {@link ValueValidator} and will
 *       be assigned regardless. Loaded values will still be validated and set to the default value
 *       when invalid.
 *
 * @param alias          {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type           {@link Class} reference for the type of the value represented by the
 *                       {@linkplain Cvar}
 * @param defaultValue   initial value of the {@linkplain Cvar} should it not be loaded
 * @param serializer     custom {@linkplain StringSerializer} to (de)serialize the {@linkplain Cvar}
 * @param valueValidator {@linkplain ValueValidator} used to validate values loaded by (or set to)
 *                       the {@linkplain Cvar}
 */
public Cvar(String alias, Class<T> type, T defaultValue, StringSerializer<T> serializer,
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
    this.VALUE_VALIDATOR = Preconditions.checkNotNull(valueValidator,
            "Cvar valueValidator cannot be null");

    this.CHANGE_LISTENERS = new CopyOnWriteArraySet<CvarChangeListener<T>>();
    this.CVAR_MANAGERS = new CopyOnWriteArraySet<CvarManager>();

    reset();
    load();

    if (!VALUE_VALIDATOR.isValid(DEFAULT_VALUE)) {

    }
}

/**
 * Returns the {@link String} key used to represent the name of this {@linkplain Cvar}.
 *
 * @return {@link String} key used to represent the name of this {@linkplain Cvar}
 */
public String getAlias() {
    return ALIAS;
}

/**
 * Returns the initial (or fallback) value of this {@linkplain Cvar}.
 *
 * @return initial (or fallback) value of this {@linkplain Cvar}
 */
public T getDefaultValue() {
    return DEFAULT_VALUE;
}

/**
 * Returns the {@linkplain Class} instance which is the type of this {@linkplain Cvar}.
 *
 * @return {@linkplain Class} instance which is the type of this {@linkplain Cvar}
 */
public Class<T> getType() {
    return TYPE;
}

/**
 * Returns the {@linkplain StringSerializer} used by this {@linkplain Cvar}.
 *
 * @return {@linkplain StringSerializer} used by this {@linkplain Cvar}
 */
public StringSerializer<T> getSerializer() {
    return SERIALIZER;
}

/**
 * Returns the {@linkplain ValueValidator} used by this {@linkplain Cvar}.
 *
 * @return {@linkplain ValueValidator} used by this {@linkplain Cvar}
 */
public ValueValidator<T> getValueValidator() {
    return VALUE_VALIDATOR;
}

/**
 * Returns the value of the field represented by this {@link Cvar}.
 *
 * @return value of this {@link Cvar}.
 */
public T getValue() {
    return value;
}

/**
 * Sets the value of the field represented by this {@link Cvar} to the specified one.
 *
 * @note logs whether or not the specified value was rejected by the {@link ValueValidator} used by
 *       this {@linkplain Cvar}
 *
 * @param value value to set this {@link Cvar} to
 */
public void setValue(T value) {
    if (this.value.equals(value)) {
        return;
    }

    value = VALUE_VALIDATOR.getValidatedValue(value);

    T oldValue = this.value;
    this.value = value;
    if (Cvar.autosave && SERIALIZER != null) {
        save();
    }

    for (CvarChangeListener<T> l : CHANGE_LISTENERS) {
        l.onCvarChanged(this, oldValue, this.value);
    }

    for (CvarManager cvarManager : CVAR_MANAGERS) {
        cvarManager.onCvarChanged(this, oldValue, this.value);
    }
}

/**
 * Sets the value of the field represented by this {@link Cvar} to the deserialized representation
 * of the specified value.
 *
 * @param serializedValue serialized representation of the value to set this {@linkplain Cvar} to
 */
public void setValue(String serializedValue) {
    if (SERIALIZER == null) {
        throw new NullPointerException(String.format("Cvar %s does not have a serializer set",
                ALIAS));
    }

    setValue(SERIALIZER.deserialize(serializedValue));
}

/**
 * Returns the serialized representation of the value represented by this {@linkplain Cvar}.
 *
 * @return serialized representation of the value represented by this {@linkplain Cvar}
 */
public String getSerializedValue() throws NullSerializerPointerException {
    if (SERIALIZER == null) {
        throw new NullSerializerPointerException(String.format(
                "Cvar %s cannot be serialized because no serializer has been set%n",
                ALIAS));
    }

    return SERIALIZER.serialize(value);
}

/**
 * Saves and commits the serialized representation of the value of this {@linkplain Cvar} to the
 * backed preferences instance for persistence.
 */
public void save() throws NullSerializerPointerException {
    if (SERIALIZER == null) {
        throw new NullSerializerPointerException(String.format(
                "Cvar %s cannot be saved because no serializer has been set%n",
                ALIAS));
    }

    for (CvarManager cvarManager : CVAR_MANAGERS) {
        cvarManager.save(this);
    }
}

/**
 * Reloads the value of this {@linkplain Cvar} from the backed preferences instance.
 */
public void load() throws NullSerializerPointerException {
    if (SERIALIZER == null) {
        throw new NullSerializerPointerException(String.format(
                "Cvar %s cannot be loaded because no serializer has been set%n",
                ALIAS));
    }

    for (CvarManager cvarManager : CVAR_MANAGERS) {
        cvarManager.load(this);
        break;
    }
}

/**
 * Resets the value of this {@linkplain Cvar} to the initial value specified within the constructor
 * and accessible through {@link Cvar#getDefaultValue()}.
 *
 * @note no validation is performed on the value which is being reset to, however all
 *       {@link CvarChangeListener} instances will have
 *       {@link CvarChangeListener#onCvarChanged(Cvar, Object, Object)} called as expected
 */
public void reset() {
    T oldValue = this.value;
    this.value = DEFAULT_VALUE;
    for (CvarChangeListener<T> l : CHANGE_LISTENERS) {
        l.onCvarChanged(this, oldValue, this.value);
    }

    for (CvarChangeListener l : Cvar.CHANGE_LISTENERES) {
        l.onCvarChanged(this, oldValue, this.value);
    }
}

/**
 * {@inheritDoc}
 */
@Override
public String toString() {
    return String.format("%s:%s=%s", TYPE.getName(), ALIAS, getSerializedValue());
}

/**
 * Adds a {@linkplain CvarChangeListener} to the {@linkplain Set} of {@linkplain CvarChangeListener}
 * instance which will have {@link CvarChangeListener#onCvarChanged(Cvar, Object, Object)} called
 * whenever the value of this {@linkplain Cvar} is changed.
 *
 * @param l {@linkplain CvarChangeListener} to add
 */
public void addCvarChangeListener(CvarChangeListener<T> l) {
    CHANGE_LISTENERS.add(l);
    l.onCvarChanged(this, value, value);
}

/**
 * Returns whether or not the specified {@linkplain CvarChangeListener} is contained within the
 * {@linkplain Set} of {@linkplain CvarChangeListener} instances used by this {@linkplain Cvar}.
 *
 * @param l {@linkplain CvarChangeListener} to check
 *
 * @return {@literal true} if it is, otherwise {@literal false}
 */
public boolean containsCvarChangeListener(CvarChangeListener<T> l) {
    return CHANGE_LISTENERS.contains(l);
}

/**
 * Removes the specified {@linkplain CvarChangeListener} from the {@linkplain Set} of
 * {@linkplain CvarChangeListener} instances used by this {@linkplain Cvar}.
 *
 * @param l {@linkplain CvarChangeListener} to remove
 *
 * @return {@literal true} if it was contained within the {@linkplain Set} and was removed,
 *         otherwise {@literal false}
 */
public boolean removeCvarChangeListener(CvarChangeListener<T> l) {
    return CHANGE_LISTENERS.remove(l);
}

void addCvarManager(CvarManager l) {
    CVAR_MANAGERS.add(l);
}

boolean containsCvarManager(CvarManager l) {
    return CVAR_MANAGERS.contains(l);
}

boolean removeCvarManager(CvarManager l) {
    return CVAR_MANAGERS.remove(l);
}

}
