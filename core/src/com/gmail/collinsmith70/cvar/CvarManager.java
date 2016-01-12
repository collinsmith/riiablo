package com.gmail.collinsmith70.cvar;

import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class CvarManager {

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

private final Map<Class<?>, StringSerializer> SERIALIZERS;
private final Trie<String, Cvar<?>> CVARS;
private final Set<CvarChangeListener> CHANGE_LISTENERES;

private CvarManagerListener cvarManagerListener;

public CvarManager(CvarManagerListener cvarManagerListener) {
    this.CVARS = new PatriciaTrie<Cvar<?>>();
    this.SERIALIZERS = new ConcurrentHashMap<Class<?>, StringSerializer>();
    this.CHANGE_LISTENERES
            = new CopyOnWriteArraySet<CvarChangeListener>();
    configureDefaultSerializers();

    setCvarManagerListener(cvarManagerListener);
}

private void configureDefaultSerializers() {
    SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
}

public void setCvarManagerListener(CvarManagerListener cvarManagerListener) {
    this.cvarManagerListener = Preconditions.checkNotNull(cvarManagerListener);
}

public CvarManagerListener getCvarManagerListener() {
    return cvarManagerListener;
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
public <T> StringSerializer<T> getSerializer(Class<T> type) {
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
public <T> void setSerializer(Class<T> type, StringSerializer<T> serializer) {
    SERIALIZERS.put(type, serializer);
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
public SortedMap<String, Cvar<?>> search(String key) {
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
public Cvar<?> get(String key) {
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
public <T> Cvar<T> get(String key, Class<T> type) {
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
public Collection<Cvar<?>> getCvars() {
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
public <T> Cvar<T> register(Cvar<T> cvar) throws IllegalArgumentException {
    String lowercaseAlias = cvar.getAlias().toLowerCase();
    if (CVARS.containsKey(lowercaseAlias)) {
        throw new IllegalArgumentException(String.format(
                "A Cvar with the alias %s is already registered. Cvar aliases must be unique!",
                cvar.getAlias()));
    }

    CVARS.put(lowercaseAlias, cvar);
    cvar.addCvarManager(this);
    return cvar;
}

public <T> boolean unregister(Cvar<T> cvar) {
    String lowercaseAlias = cvar.getAlias().toLowerCase();
    if (CVARS.remove(lowercaseAlias) == null) {
        return false;
    }

    cvar.removeCvarManager(this);
    return true;
}

/**
 * Commits all changes made to all registered {@linkplain Cvar}s. This is a manual operation
 * intended to be used in the case where {@link Cvar#isAutosaving()} is set to {@literal false},
 * which in turn implies that {@linkplain Cvar} values are not committed whenever they are changed.
 */
public void commit() {
    cvarManagerListener.onCommit();
}

public void save(Cvar cvar) {
    cvarManagerListener.onSave(cvar);
    commit();
}

public void load(Cvar cvar) {
    cvarManagerListener.onLoad(cvar);
}

/**
 * Adds the specified {@linkplain CvarChangeListener} to the statically accessible {@linkplain Set}
 * of {@linkplain CvarChangeListener} instances which will have
 * {@link CvarChangeListener#onCvarChanged(Cvar, Object, Object)} called whenever a change is made
 * to any {@linkplain Cvar}.
 *
 * @param l {@linkplain CvarChangeListener} to add
 */
public void addCvarChangeListener(CvarChangeListener<?> l) {
    CHANGE_LISTENERES.add(l);
}

/**
 * Returns whether or not the specified {@linkplain CvarChangeListener} is contained within the
 * statically accessible {@linkplain Set} of {@linkplain CvarChangeListener} instances.
 *
 * @param l {@linkplain CvarChangeListener} to check
 *
 * @return {@literal true} if it is, otherwise {@literal false}
 */
public boolean containsCvarChangeListener(CvarChangeListener<?> l) {
    return CHANGE_LISTENERES.contains(l);
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
public boolean removeCvarChangeListener(CvarChangeListener<?> l) {
    return CHANGE_LISTENERES.remove(l);
}

}
