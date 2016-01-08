package com.gmail.collinsmith70.cvar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.validator.NullValueValidator;
import com.gmail.collinsmith70.util.Serializer;
import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
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

private static final Preferences PREFERENCES = Gdx.app.getPreferences(Cvar.class.getName());
private static final Trie<String, Cvar<?>> CVARS = new PatriciaTrie<Cvar<?>>();

/**
 * Commits all changes made to all registered {@linkplain Cvar}s. This is a manual operation
 * intended to be used in the case where {@link Cvar#isAutosaving()} is set to {@literal false},
 * which in turn implies that {@linkplain Cvar} values are not committed whenever they are changed.
 */
public static void commit() {
    Cvar.out.println("Committing CVARs...");
    Cvar.PREFERENCES.flush();
}

/**
 * Returns a {@linkplain SortedMap} of all registered {@linkplain Cvar}s found when searching a
 * specified {@linkplain String}. This operation is case-insensitive and will return a map sorted
 * in lexicographical order.
 *
 * @note in the case where no {@linkplain Cvar}s are found, an empty map is returned
 *
 * @param key {@linkplain String} to search for. This value is interpreted as case-insensitive and
*             partial keys are implied
 *
 * @return {@linklain SortedMap} containing all {@linkplain Cvar}s with the specified
 *         {@linkplain String} prefixing the alias
 */
public static SortedMap<String, Cvar<?>> search(String key) {
    key = key.toLowerCase();
    return CVARS.prefixMap(key);
}

/**
 * Searches for a {@linkplain Cvar} registered with the specified alias and returns it. If the type
 * of the {@linkplain Cvar} is known, then {@link #get(String, Class)} can be used instead.
 *
 * @param key {@linkplain String} to search for. This value is interpreted as case-insensitive and
 *            the exact alias should be specified (i.e., no partial keys).
 *
 * @return {@linkplain Cvar} with the specified alias, otherwise {@literal null} if no
 *         {@linklain Cvar} by that name has been registered.
 *
 * @see Cvar#get(String, Class)
 */
public static Cvar<?> get(String key) {
    key = key.toLowerCase();
    return CVARS.get(key);
}

/**
 * Specific implementation of {@link Cvar#get(String)} in which the expected type of the
 * {@linkplain Cvar} can be passed (if known). If the type is not known (or required), then
 * {@link Cvar#get(String)} should be used instead.
 *
 * @param key  {@linkplain String} to search for. This value is interpreted as case-insensitive and
 *             the exact alias should be specified (i.e., no partial keys).
 * @param type {@link Class} reference for the type which the value represented by the found
 *             {@linkplain Cvar} represents
 * @param <T>  type which the value represented by the found {@linkplain Cvar} represents
 *
 * @return {@linkplain Cvar} with the specified alias, otherwise {@literal null} if no
 *         {@linklain Cvar} by that name has been registered.
 *
 * @see Cvar#get(String)
 */
public static <T> Cvar<T> get(String key, Class<T> type) {
    if (type == null) {
        throw new NullPointerException(
                "type cannot be null, did you mean to use Cvar.get(String key) instead?");
    }

    return (Cvar<T>)Cvar.get(key);
}

/**
 * Returns a {@linkplain Collection} of all registered {@linkplain Cvar}s.
 *
 * @return {@linkplain Collection} of all registered {@linkplain Cvar}s
 */
public static Collection<Cvar<?>> getCvars() {
    return CVARS.values();
}

/**
 * Registers a specified {@linkplain Cvar} into the statically accessible {@linkplain Cvar}
 * collection. Registered {@linkplain Cvar}s can be looked up via {@link Cvar#get(String)}
 *
 * @param cvar {@linkplain Cvar} to register
 * @param <T>  type which the value represented by the specified {@linkplain Cvar} represents
 *
 * @return reference to the passed {@linkplain Cvar}
 *
 * @throws IllegalArgumentException if there is already a {@linkplain Cvar} registered with the
 *                                  same alias as the passed {@linkplain Cvar}
 */
public static <T> Cvar<T> register(Cvar<T> cvar) throws IllegalArgumentException {
    String lowercaseAlias = cvar.getAlias().toLowerCase();
    if (Cvar.CVARS.containsKey(lowercaseAlias)) {
        throw new IllegalArgumentException(String.format(
                "A Cvar with the alias %s is already registered. Cvar aliases must be unique!",
                cvar.getAlias()));
    }

    Cvar.CVARS.put(lowercaseAlias, cvar);
    return cvar;
}

/**************************************************************************************************/

private static final Map<Class<?>, Serializer<?, String>> SERIALIZERS;
static {
    SERIALIZERS = new ConcurrentHashMap<Class<?>, Serializer<?, String>>();
    SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
}

/**
 * Returns the {@linkplain StringSerializer} which will be used by default when (de)serializing
 * {@linkplain Cvar}s of the specified type.
 *
 * @param type {@link Class} reference for the type which the {@linkplain StringSerializer} accepts
 * @param <T>  type which the {@linkplain StringSerializer} accepts
 *
 * @return reference to the {@linkplain StringSerializer} used by the passed type, otherwise
 *         {@literal null} if no {@linkplain StringSerializer} has been declared yet.
 */
public static <T> StringSerializer<T> getSerializer(Class<T> type) {
    return (StringSerializer<T>)SERIALIZERS.get(type);
}

/**
 * Sets the default {@linkplain StringSerializer} for the passed type to the passed one.
 *
 * @param type       {@link Class} reference for the type which the {@linkplain StringSerializer}
 *                   accepts and should replace
 * @param serializer replacement {@linkplain StringSerializer}
 * @param <T>        type which the {@linkplain StringSerializer} accepts
 */
public static <T> void setSerializer(Class<T> type, Serializer<T, String> serializer) {
    SERIALIZERS.put(type, serializer);
}

/**************************************************************************************************/

private static PrintStream out = System.out;

/**
 * Returns the current output {@linkplain PrintStream} used by {@linkplain Cvar} log messages. By
 * default, {@linkplain Cvar} log messages will output to {@link System#out}.
 *
 * @return {@linkplain PrintStream} used by {@linkplain Cvar} log messages
 */
public static PrintStream getOut() {
    return Cvar.out;
}

/**
 * Sets the current output {@linkplain PrintStream} used by {@linkplain Cvar} log messages to the
 * specified one. By default, {@linkplain Cvar} log messages will output to {@link System#out}.
 *
 * @param out output {@linkplain PrintStream} to be used by {@linkplain Cvar} log messages
 */
public static void setOut(PrintStream out) {
    Cvar.out = Preconditions.checkNotNull(out);
}

/**************************************************************************************************/

private static boolean autosave = true;

/**
 * Returns whether or not {@linkplain Cvar}s will automatically commit their changes to the backed
 * preferences instance. {@linkplain Cvar}s which do not automatically save must be saved using
 * {@link Cvar#commit()}.
 *
 * @return {@literal true} if {@linkplain Cvar}s automatically commit their changes, otherwise
 *         {@literal false}
 *
 * @see Cvar#commit()
 */
public static boolean isAutosaving() {
    return Cvar.autosave;
}

/**
 * Controls whether or not {@linkplain Cvar}s will automatically commit their changes to the backed
 * preferences instance. {@linkplain Cvar}s which do not automatically save must be saved using
 * {@link Cvar#commit()}. If this value is changed to {@literal true}, then this implementation
 * will call {@link Cvar#commit()}, which will commit all changes made immediately.
 *
 * @param b {@literal true} if {@linkplain Cvar}s should automatically commit their changes,
 *          otherwise {@literal false}
 *
 * @see Cvar#commit()
 */
public static void setAutosave(boolean b) {
    Cvar.autosave = b;
    if (Cvar.autosave) {
        Cvar.commit();
    }
}

/**************************************************************************************************/

private static final Set<CvarChangeListener> CHANGE_LISTENERES
        = new CopyOnWriteArraySet<CvarChangeListener>();

/**
 * Adds the specified {@linkplain CvarChangeListener} to the statically accessible {@linkplain Set}
 * of {@linkplain CvarChangeListener} instances which will have
 * {@link CvarChangeListener#onCvarChanged(Cvar, Object, Object)} called whenever a change is made
 * to any {@linkplain Cvar}.
 *
 * @param l {@linkplain CvarChangeListener} to add
 */
public static void addGlobalCvarChangeListener(CvarChangeListener<?> l) {
    Cvar.CHANGE_LISTENERES.add(l);
}

/**
 * Returns whether or not the specified {@linkplain CvarChangeListener} is contained within the
 * statically accessible {@linkplain Set} of {@linkplain CvarChangeListener} instances.
 *
 * @param l {@linkplain CvarChangeListener} to check
 *
 * @return {@literal true} if it is, otherwise {@literal false}
 */
public static boolean containsGlobalCvarChangeListener(CvarChangeListener<?> l) {
    return Cvar.CHANGE_LISTENERES.contains(l);
}

/**
 * Removes the specified {@linkplain CvarChangeListener} from the statically accessible
 * {@linkplain Set} of {@linkplain CvarChangeListener} instances and returns whether or not the
 * operation was carried out successfully (i.e., it was contained within the {@linkplain Set} and
 * was removed).
 *
 * @param l {@linkplain CvarChangeListener} to remove
 *
 * @return {@literal true} if it was contained within the {@linkplain Set} and was removed,
 *         otherwise {@literal false}
 */
public static boolean removeGlobalCvarChangeListener(CvarChangeListener<?> l) {
    return Cvar.CHANGE_LISTENERES.remove(l);
}

/**************************************************************************************************/

private final String ALIAS;
private final T DEFAULT_VALUE;
private final Class<T> TYPE;
private final StringSerializer<T> SERIALIZER;
private final ValueValidator<T> VALUE_VALIDATOR;
private final Set<CvarChangeListener<T>> CHANGE_LISTENERS;

private T value;

/**
 * Constructs a {@linkplain Cvar} with an alias, type and default value. The value of this
 * {@linkplain Cvar} will attempt to be loaded using the default {@link StringSerializer} for the
 * specified type accessible through {@link Cvar#getSerializer(Class)}.
 *
 * @note this constructor will construct a {@link Cvar} with {@link NullValueValidator#INSTANCE} set
 *       as its {@link ValueValidator}.
 *
 * @param alias        {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type         {@link Class} reference for the type of the value represented by the
 *                     {@linkplain Cvar}
 * @param defaultValue initial value of the {@linkplain Cvar} should it not be loaded
 */
public Cvar(String alias, Class<T> type, T defaultValue) {
    this(alias, type, defaultValue, getSerializer(type), NullValueValidator.INSTANCE);
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
 * @note this constructor will construct a {@link Cvar} with {@link NullValueValidator#INSTANCE} set
 *       as its {@link ValueValidator}.
 *
 * @param alias        {@linkplain String} used to represent this {@linkplain Cvar}
 * @param type         {@link Class} reference for the type of the value represented by the
 *                     {@linkplain Cvar}
 * @param defaultValue initial value of the {@linkplain Cvar} should it not be loaded
 * @param serializer   custom {@linkplain StringSerializer} to (de)serialize the {@linkplain Cvar}
 */
public Cvar(String alias, Class<T> type, T defaultValue, StringSerializer<T> serializer) {
    this(alias, type, defaultValue, serializer, NullValueValidator.INSTANCE);
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

    reset();
    load();

    if (!VALUE_VALIDATOR.isValid(DEFAULT_VALUE)) {
        Cvar.out.printf("warning: the specified default value for %s is not considered valid " +
                "by the specified %s%n",
                ALIAS,
                valueValidator.getClass().getName());
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
public void setValue(T value) throws IllegalArgumentException {
    if (this.value.equals(value)) {
        return;
    }

    if (!VALUE_VALIDATOR.isValid(value)) {
        Cvar.out.printf("failed to change %s from %s to %s%n",
                ALIAS,
                this.value,
                value);
        return;
    } else {
        Cvar.out.printf("changing %s from %s to %s%n",
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

    for (CvarChangeListener l : Cvar.CHANGE_LISTENERES) {
        l.onCvarChanged(this, oldValue, this.value);
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
public String getSerializedValue() {
    return SERIALIZER.serialize(value);
}

/**
 * Saves and commits the serialized representation of the value of this {@linkplain Cvar} to the
 * backed preferences instance for persistence.
 */
public void save() {
    Cvar.PREFERENCES.putString(ALIAS, SERIALIZER.serialize(value));
    Cvar.commit();
}

/**
 * Reloads the value of this {@linkplain Cvar} from the backed preferences instance.
 */
public void load() {
    if (SERIALIZER == null) {
        Cvar.out.printf("Cvar %s cannot be saved or loaded because no serializer has been set%n",
                ALIAS);
        return;
    }

    String serializedValue = Cvar.PREFERENCES.getString(ALIAS);
    if (serializedValue == null) {
        serializedValue = SERIALIZER.serialize(DEFAULT_VALUE);
    } else {
        setValue(SERIALIZER.deserialize(serializedValue));
    }

    Cvar.out.printf("%s loaded as %s [%s]%n", ALIAS, serializedValue, TYPE.getName());
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

}
