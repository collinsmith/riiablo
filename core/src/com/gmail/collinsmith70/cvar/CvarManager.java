package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.ByteStringSerializer;
import com.gmail.collinsmith70.util.serializer.CharacterStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.FloatStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.LongStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;
import com.gmail.collinsmith70.util.serializer.ShortStringSerializer;
import com.google.common.base.Strings;
import com.sun.istack.internal.NotNull;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstraction which manages {@link Cvar} instances (specifically querying, saving and loading) and
 * contains serializers for their values. This class is intended to be subclassed with specific
 * implementations regarding how {@linkplain Cvar} instances should be saved and loaded so that
 * their values can persist after run time.
 */
public abstract class CvarManager implements CvarChangeListener {

/**
 * Data structure which contains a default mapping of {@linkplain Class}es to
 * {@linkplain StringSerializer} instances which will be used to (de)serialize {@link Cvar}
 * instances of that {@linkplain Class}.
 */
@NotNull
private static final Map<Class<?>, StringSerializer<?>> DEFAULT_SERIALIZERS
        = new HashMap<Class<?>, StringSerializer<?>>();
static {
    DEFAULT_SERIALIZERS.put(Character.class, CharacterStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Byte.class, ByteStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Short.class, ShortStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Long.class, LongStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Float.class, FloatStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
}

/**
 * {@linkplain Trie} containing a mapping of {@link Cvar} instances, accessible using their
 * aliases.
 */
@NotNull
private final Trie<String, Cvar<?>> CVARS;

/**
 * {@linkplain Map} which stores a mapping of {@linkplain Class}es to {@linkplain StringSerializer}
 * instances which will be used to (de)serialize {@link Cvar} instances of that {@linkplain Class}.
 */
@NotNull
private final Map<Class<?>, StringSerializer<?>> SERIALIZERS;

/**
 * Determines whether or not {@link Cvar} instances will automatically save and commit their
 * changes whenever they are modified.
 */
private boolean isAutosaving;

/**
 * Constructs a new CvarManager instance with auto-saving enabled.
 *
 * @see #setAutosave(boolean)
 */
public CvarManager() {
    this(true);
}

/**
 * Constructs a new CvarManager instance.
 *
 * @param autosave {@code true} to enable auto-saving, otherwise {@code false}
 */
public CvarManager(boolean autosave) {
    this.isAutosaving = autosave;

    this.CVARS = new PatriciaTrie<Cvar<?>>();
    this.SERIALIZERS = new ConcurrentHashMap<Class<?>, StringSerializer<?>>(DEFAULT_SERIALIZERS);
}

/**
 * Controls whether or not auto-saving of {@link Cvar} instances managed by this
 * {@linkplain CvarManager} will have their values saved and committed whenever they are modified.
 * If auto-saving is disabled, then {@linkplain Cvar} instances can be saved manually by calling
 * either {@link #save(Cvar)} or {@link #saveAll()}
 *
 * @param autosave {@code true} to enable auto-saving, otherwise {@code false}
 */
public void setAutosave(boolean autosave) {
    if (isAutosaving == autosave) {
        return;
    }

    this.isAutosaving = autosave;
    if (isAutosaving) {
        saveAll();
    }
}

/**
 * @return {@code true} if auto-saving on {@link Cvar} change is enabled,
 *         otherwise {@code false}
 */
public boolean isAutosaving() {
    return isAutosaving;
}

/**
 * {@inheritDoc}
 */
@Override
public void onChanged(Cvar cvar, Object from, Object to) {
    save(cvar);
}

/**
 * {@inheritDoc}
 */
@Override
public void onLoad(Cvar cvar, Object to) {
    //...
}

/**
 * Creates a new {@link Cvar} instance and adds it to this {@linkplain CvarManager} with its
 * {@link Validator} set to {@link Validator#ACCEPT_NON_NULL}.
 *
 * @param alias        key to be used when representing the name of the CVAR
 * @param description  brief description of the CVAR and the kinds of values it expects
 * @param type         reference to the class type of the value the CVAR represents
 * @param defaultValue value to be assigned to the CVAR by default and which no validation is
 *                     performed
 *
 * @return the created {@link Cvar} instance
 *
 * @throws DuplicateCvarException if there is already a {@link Cvar} instance added to this
 *                                {@linkplain CvarManager} with the given {@code alias}
 */
public <T> Cvar<T> create(String alias, String description, Class<T> type, T defaultValue) {
    return create(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
}

/**
 * Creates a new {@link Cvar} instance and adds it to this {@linkplain CvarManager}.
 *
 * @param alias        key to be used when representing the name of the CVAR
 * @param description  brief description of the CVAR and the kinds of values it expects
 * @param type         reference to the class type of the value the CVAR represents
 * @param defaultValue value to be assigned to the CVAR by default and which no validation is
 *                     performed
 * @param validator    {@link Validator} to be used when checking value changes of this CVAR
 *
 * @return the created {@link Cvar} instance
 *
 * @throws DuplicateCvarException if there is already a {@link Cvar} instance added to this
 *                                {@linkplain CvarManager} with the given {@code alias}
 */
public <T> Cvar<T> create(String alias, String description, Class<T> type, T defaultValue, Validator<?> validator) {
    Cvar<T> cvar = new Cvar<T>(alias, description, type, defaultValue, validator);
    add(cvar);
    return cvar;
}

/**
 * Adds the given {@link Cvar} instance to this {@linkplain CvarManager} supporting method chaining.
 *
 * @param cvar {@linkplain Cvar} instance to add
 *
 * @return reference to this {@linkplain CvarManager}
 *
 * @throws DuplicateCvarException if there is already a {@link Cvar} instance added to this
 *                                {@linkplain CvarManager} with the given {@code alias}
 */
public <T> CvarManager add(Cvar<T> cvar) {
    if (isManaging(cvar)) {
        return this;
    } else if (containsAlias(cvar.getAlias())) {
        throw new DuplicateCvarException(cvar, String.format(
                "A cvar with the alias %s is already registered. Cvar aliases must be unique!",
                cvar.getAlias()));
    }

    CVARS.put(cvar.getAlias().toLowerCase(), cvar);
    cvar.setValue(load(cvar));
    cvar.addCvarChangeListener(this);
    return this;
}

/**
 * @param cvar {@linkplain Cvar} instance to remove
 *
 * @return {@literal true} if the {@link Cvar} was removed and is now no longer managed by this
 *         {@linkplain CvarManager}, otherwise {@literal false} if the {@linkplain Cvar} is
 *         not managed by this {@linkplain CvarManager} and could not be removed
 */
public boolean remove(Cvar<?> cvar) {
    if (!isManaging(cvar)) {
        return false;
    }

    return CVARS.remove(cvar.getAlias().toLowerCase()) == null;
}

/**
 * Searches case-insensitively for a {@link Cvar} managed by this {@linkplain CvarManager} using its
 * alias.
 *
 * @note This method requires a perfect character-for-character match with the passed alias and
 *       the {@linkplain Cvar}'s alias.
 *
 * @param alias alias to search for (interpreted case-insensitively)
 *
 * @return reference to the {@linkplain Cvar} if it is found, otherwise {@code null}
 *
 * @see Cvar#getAlias()
 */
public <T> Cvar<T> get(String alias) {
    alias = alias.toLowerCase();
    return (Cvar<T>)CVARS.get(alias);
}

/**
 * Queries and returns a {@link SortedMap} of {@link Cvar} instances with the given
 * {@linkplain String} as part of their name. This operation is performed case-insensitively.
 *
 * @param alias alias to search for (interpreted case-insensitively)
 *
 * @return {@link SortedMap} containing the results of the query sorted lexicographically according
 *         to the {@linkplain Cvar#getAlias() alias}
 */
public SortedMap<String, Cvar<?>> search(String alias) {
    alias = alias.toLowerCase();
    return CVARS.prefixMap(alias);
}

/**
 * @return {@link Collection} of all {@link Cvar} instances managed by this {@linkplain CvarManager}
 */
public Collection<Cvar<?>> getCvars() {
    return CVARS.values();
}

/**
 * Loads the specified {@link Cvar}.
 *
 * @param cvar {@link Cvar} to attempt to {@linkplain StringSerializer#deserialize(Object)
 *             deserialize} and load
 *
 * @return value which was stored
 */
public abstract <T> T load(Cvar<T> cvar);

/**
 * Saves the specified {@link Cvar}
 *
 * @param cvar {@link Cvar} to attempt to {@linkplain StringSerializer#serialize(Object) serialize}
 *             and save
 */
public abstract <T> void save(Cvar<T> cvar);

/**
 * Macro version of {@link #save(Cvar)} which attempts to save all {@link Cvar} instances managed
 * by this {@linkplain CvarManager}
 */
public void saveAll() {
    for (Cvar<?> cvar : CVARS.values()) {
        save(cvar);
    }
}

/**
 * @param cvar {@link Cvar} to check
 *
 * @return {@code true} if the specified {@link Cvar} is being managed by this
 *         {@linkplain CvarManager}, otherwise {@code false}
 */
public <T> boolean isManaging(Cvar<T> cvar) {
    Cvar value = CVARS.get(cvar.getAlias().toLowerCase());
    return cvar.equals(value);
}

/**
 * @param alias {@linkplain String} to check (case-insensitively)
 *
 * @return {@code true} if this {@linkplain CvarManager} is managing a {@link Cvar} with the given
 *         {@linkplain Cvar#getAlias() alias}, otherwise {@code false}
 */
public boolean containsAlias(String alias) {
    return CVARS.containsKey(alias.toLowerCase());
}

/**
 * @param type {@linkplain Class} reference to search for
 * @return {@link StringSerializer} used by this {@linkplain CvarManager} to (de)serialize
 *         {@link Cvar} instances with the given type, otherwise {@code null} if no
 *         {@link StringSerializer} has been set for that {@code type} yet
 */
public <T> StringSerializer<T> getSerializer(Class<T> type) {
    return (StringSerializer<T>)SERIALIZERS.get(type);
}

/**
 * @param cvar {@link Cvar} to retrieve the {@link StringSerializer} for
 * @return the {@link StringSerializer} which will be used for that {@link Cvar}, otherwise
 *         {@code null} if none has been set yet
 */
public <T> StringSerializer<T> getSerializer(Cvar<T> cvar) {
    return getSerializer(cvar.getType());
}

/**
 * @param type       {@linkplain Class type} reference to assign the {@link StringSerializer} to
 * @param serializer {@link StringSerializer} to assign
 */
public <T> void putSerializer(Class<T> type, StringSerializer<T> serializer) {
    SERIALIZERS.put(type, serializer);
}

/**
 * Abstraction to represent a {@link RuntimeException} for {@link CvarManager} events.
 */
public static abstract class CvarManagerException extends RuntimeException {

    /**
     * Reference to the {@link Cvar} causing the exception
     */
    public final Cvar CVAR;

    /**
     * Constructs a new {@linkplain CvarManagerException} with a {@code null} {@link Cvar}
     * and message
     */
    public CvarManagerException() {
        this(null, null);
    }

    /**
     * Constructs a new {@linkplain CvarManagerException} with a {@code null} message
     *
     * @param cvar {@link Cvar} causing the exception
     */
    public CvarManagerException(Cvar cvar) {
        this(cvar, null);
    }

    /**
     * Constructs a new {@linkplain CvarManagerException} with a {@code null} {@link Cvar}
     *
     * @param message {@linkplain String} describing the cause
     */
    public CvarManagerException(String message) {
        this(null, message);
    }

    /**
     * Constructs a new {@linkplain CvarManagerException} instance
     *
     * @param cvar
     * @param message
     */
    public CvarManagerException(Cvar cvar, String message) {
        super(Strings.nullToEmpty(message));
        this.CVAR = cvar;
    }

    /**
     * @return {@link Cvar} causing the exception
     */
    public Cvar getCvar() {
        return CVAR;
    }

}

/**
 * A {@link CvarManagerException} to be thrown when duplicate {@link Cvar}s are detected, typically
 * thrown when a {@link Cvar} is attempted to be added to a {@link CvarManager} who already has
 * a {@link Cvar} at that {@linkplain Cvar#getAlias() alias}.
 */
public static class DuplicateCvarException extends CvarManagerException {

    /**
     * Constructs a new {@linkplain DuplicateCvarException} with a {@code null} {@link Cvar}
     * and message
     */
    public DuplicateCvarException() {
        this(null, null);
    }

    /**
     * Constructs a new {@linkplain DuplicateCvarException} with a {@code null} message
     *
     * @param cvar {@link Cvar} causing the exception
     */
    public DuplicateCvarException(Cvar cvar) {
        this(cvar, null);
    }

    /**
     * Constructs a new {@linkplain DuplicateCvarException} with a {@code null} {@link Cvar}
     *
     * @param message {@linkplain String} describing the cause
     */
    public DuplicateCvarException(String message) {
        this(null, message);
    }

    /**
     * Constructs a new {@linkplain DuplicateCvarException} instance
     *
     * @param cvar
     * @param message
     */
    public DuplicateCvarException(Cvar cvar, String message) {
        super(cvar, message);
    }

}

public static class UnmanagedCvarException extends CvarManagerException {

    /**
     * Constructs a new {@linkplain UnmanagedCvarException} with a {@code null} {@link Cvar}
     * and message
     */
    public UnmanagedCvarException() {
        this(null, null);
    }

    /**
     * Constructs a new {@linkplain UnmanagedCvarException} with a {@code null} message
     *
     * @param cvar {@link Cvar} causing the exception
     */
    public UnmanagedCvarException(Cvar cvar) {
        this(cvar, null);
    }

    /**
     * Constructs a new {@linkplain UnmanagedCvarException} with a {@code null} {@link Cvar}
     *
     * @param message {@linkplain String} describing the cause
     */
    public UnmanagedCvarException(String message) {
        this(null, message);
    }

    /**
     * Constructs a new {@linkplain UnmanagedCvarException} instance
     *
     * @param cvar
     * @param message
     */
    public UnmanagedCvarException(Cvar cvar, String message) {
        super(cvar, message);
    }

}

}
