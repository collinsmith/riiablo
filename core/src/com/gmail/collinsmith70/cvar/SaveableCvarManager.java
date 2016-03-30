package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.util.StringSerializer;
import com.gmail.collinsmith70.util.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.util.serializer.ByteStringSerializer;
import com.gmail.collinsmith70.util.serializer.CharacterStringSerializer;
import com.gmail.collinsmith70.util.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.util.serializer.FloatStringSerializer;
import com.gmail.collinsmith70.util.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.util.serializer.LongStringSerializer;
import com.gmail.collinsmith70.util.serializer.ObjectStringSerializer;
import com.gmail.collinsmith70.util.serializer.ShortStringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of a {@link CvarManager} which adds a {@linkplain #save save} and
 * {@linkplain #load load} feature to {@link Cvar} instances so that their values can persist across
 * multiple run times.
 */
public abstract class SaveableCvarManager extends CvarManager {

  /**
   * {@linkplain Map Mapping} of {@link Class} instances to the default {@link StringSerializer}
   * for which they can be ({@linkplain StringSerializer#deserialize de})
   * {@linkplain StringSerializer#serialize serialized} with. This static copy is specifically used
   * when {@link CvarManager} instances are constructed so that they have a default mapping of
   * {@link StringSerializer} instances.
   *
   * @see #SERIALIZERS
   */
  @NonNull
  private static final Map<Class, StringSerializer> DEFAULT_SERIALIZERS
          = new HashMap<Class, StringSerializer>();

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
   * {@linkplain Map Mapping} of {@link Class} instances to the default {@link StringSerializer}
   * for which they can be ({@linkplain StringSerializer#deserialize de})
   * {@linkplain StringSerializer#serialize serialized} with.
   */
  @NonNull
  private final Map<Class, StringSerializer> SERIALIZERS;

  /**
   * {@code true} represents that this {@link CvarManager} will automatically save and commit
   * {@linkplain Cvar#setValue changes} made to a {@link Cvar} to the underlying data structure
   * (typically some implementation of a {@link java.util.prefs.Preferences} instance.
   *
   * @see #save(Cvar)
   * @see #isAutosaving()
   * @see #setAutosaving(boolean)
   */
  private boolean autosaving;

  /**
   * Constructs a new {@link CvarManager} instance with {@linkplain #isAutosaving() autosaving}
   * enabled.
   */
  public SaveableCvarManager() {
    this(true);
  }

  /**
   * Constructs a new {@link CvarManager} instance.
   *
   * @param autosave {@code true} to enable {@linkplain #isAutosaving() autosaving},
   *                 otherwise {@code false}. Autosaving will cause {@link Cvar} changes to be
   *                 {@linkplain #save saved} as soon as they occur.
   */
  public SaveableCvarManager(final boolean autosave) {
    super();
    this.autosaving = autosave;
    this.SERIALIZERS = new ConcurrentHashMap<Class, StringSerializer>(DEFAULT_SERIALIZERS);
  }

  /**
   * @return {@code true} if this {@link CvarManager} will automatically {@linkplain #save save}
   * {@linkplain Cvar#setValue changes} made to a {@link Cvar} to the underlying data
   * structure (typically some implementation of a {@link java.util.prefs.Preferences}
   * instance
   * @see #setAutosaving
   */
  public boolean isAutosaving() {
    return autosaving;
  }

  /**
   * <p>
   * Note: If {@linkplain #isAutosaving autosaving} transitions from disabled to enabled, then
   * {@link #saveAll()} will automatically be called to {@linkplain #save save} all current
   * {@linkplain Cvar#getValue values}.
   * </p>
   *
   * @param autosave {@code true} to enable {@linkplain #isAutosaving autosaving} of {@link Cvar}
   *                 instances {@linkplain #isManaging managed} by this {@link SaveableCvarManager}
   *                 whenever they are {@linkplain Cvar#setValue changed}, otherwise {@code false}
   * @see #isAutosaving
   */
  public void setAutosaving(final boolean autosave) {
    if (isAutosaving() == autosave) {
      return;
    }

    this.autosaving = autosave;
    if (isAutosaving()) {
      saveAll();
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note: This operation will also attempt to {@linkplain #load load} any persistent serialized data
   * </p>
   */
  @Override
  public <T> void add(@NonNull final Cvar<T> cvar) {
    super.add(cvar);
    load(cvar);
  }

  /**
   * <p>
   * Note: It is the responsibility of implementing classes to determine how the value should be
   * loaded, as well as actually {@linkplain Cvar#setValue setting that value} on the
   * {@link Cvar} itself.
   * </p>
   *
   * @param <T>  {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@link Cvar} represents
   * @param cvar {@link Cvar} instance to load
   * @return {@linkplain StringSerializer#deserialize deserialized} {@linkplain Cvar#getValue value}
   * which was loaded from a persistent backend
   * @see Cvar#setValue
   */
  @Nullable
  public abstract <T> T load(@NonNull final Cvar<T> cvar);

  /**
   * {@inheritDoc}
   * {@linkplain #save Saves} the specified {@link Cvar} if {@linkplain #isAutosaving autosaving} is
   * enabled.
   *
   * @see #isAutosaving
   * @see #setAutosaving
   */
  @Override
  public void onChanged(@NonNull final Cvar cvar, @Nullable final Object from,
                        @Nullable final Object to) {
    if (isAutosaving()) {
      save(cvar);
    }
  }

  /**
   * <p>
   * Note: It is the responsibility of implementing classes to determine how the value should be
   * saved.
   * </p>
   *
   * @param <T>  {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@link Cvar} represents
   * @param cvar {@link Cvar} instance to save
   */
  public abstract <T> void save(@NonNull final Cvar<T> cvar);

  /**
   * Aggregate operation which {@linkplain #save saves} all {@link Cvar} instances
   * {@linkplain #isManaging managed} by this {@link SaveableCvarManager}.
   *
   * @see #save
   */
  public void saveAll() {
    for (Cvar cvar : getCvars()) {
      save(cvar);
    }
  }

  /**
   * @param <T>  type which the {@link StringSerializer} is designed to handle
   * @param type {@link Class} which the {@link StringSerializer} is designed to handle
   * @return {@link StringSerializer} used for the specified {@link Class} type, or {@code null} if
   * no {@link StringSerializer} exists yet for that type
   * @see #getSerializer(Cvar)
   */
  @Nullable
  public <T> StringSerializer<T> getSerializer(@NonNull final Class<T> type) {
    if (type == null) {
      throw new IllegalArgumentException("type is not allowed to be null");
    }

    return (StringSerializer<T>) SERIALIZERS.get(type);
  }

  /**
   * @param <T>  {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@link Cvar} represents
   * @param cvar {@link Cvar} instance to check
   * @return {@link StringSerializer} instance which will be used when {@linkplain #save saving} and
   * {@linkplain #load loading} the specified {@link Cvar}, or {@code null} if no
   * {@link StringSerializer} has been set yet
   * @see #getSerializer(Class)
   */
  @Nullable
  public <T> StringSerializer<T> getSerializer(@NonNull final Cvar<T> cvar) {
    return getSerializer(cvar.getType());
  }

  /**
   * @param <T>        type which the {@link StringSerializer} is designed to handle
   * @param type       {@link Class} which the {@link StringSerializer} is designed to handle, and
   *                   which {@link Cvar} instances must {@linkplain Cvar#getType match} in order for
   *                   the specified {@link StringSerializer} to be used when
   *                   {@linkplain #save saving} and {@linkplain #load loading}
   * @param serializer {@link StringSerializer} to associate with the specified {@link Class}, or
   *                   {@code null} to {@linkplain #removeSerializer unassociate} that {@link Class}
   *                   with any {@link StringSerializer}
   */
  public <T> void putSerializer(@NonNull final Class<T> type,
                                @Nullable final StringSerializer<T> serializer) {
    if (serializer == null) {
      removeSerializer(type);
      return;
    }

    SERIALIZERS.put(type, serializer);
  }

  /**
   * <p>
   * Note: Passing a {@code null} reference will do nothing to the underlying {@link StringSerializer}
   * associations.
   * </p>
   *
   * @param type {@link Class} to unassociate with any {@link StringSerializer}
   */
  public void removeSerializer(@Nullable final Class type) {
    if (type == null) {
      return;
    }

    SERIALIZERS.remove(type);
  }

  /**
   * @param cvar {@link Cvar} instance whose {@linkplain Cvar#getType() type} to check if there is
   *             a registered {@link StringSerializer} for
   * @return {@code true} if there is a {@link StringSerializer} for the specified {@link Cvar}
   * instances {@linkplain Cvar#getType() type}, otherwise {@code false}
   */
  public boolean containsSerializer(@NonNull Cvar cvar) {
    return containsSerializer(cvar.getType());
  }

  /**
   * @param type {@linkplain Class type} to check if there is a registered {@link StringSerializer}
   *             for
   * @return {@code true} if there is a {@link StringSerializer} for the specified
   * {@linkplain Class type}, otherwise {@code false}
   */
  public boolean containsSerializer(@NonNull Class type) {
    if (type == null) {
      throw new IllegalArgumentException("type is not allowed to be null");
    }

    return SERIALIZERS.containsKey(type);
  }

}
