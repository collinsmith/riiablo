package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.util.Validator;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Collections;

/**
 * A <a href="https://en.wikipedia.org/wiki/Factory_method_pattern">factory</a> class for creating
 * and managing arbitrary {@link Cvar} instances.
 */
public class CvarManager implements Cvar.StateListener {

  /**
   * {@link Trie} mapping {@link Cvar} {@linkplain Cvar#getAlias() aliases} to themselves. This
   * mapping is necessary to maintain which {@link Cvar} instances are managed by this
   * {@link CvarManager}, and implies that no two managed {@link Cvar} instances can have the same
   * {@linkplain Cvar#getAlias() alias}. In this implementation, {@link Cvar}
   * {@linkplain Cvar#getAlias() aliases} are to be stored {@linkplain String#toLowerCase()
   * case-insensitively}.
   */
  @NonNull
  private final Trie<String, Cvar> CVARS;

  /**
   * Constructs a new {@link CvarManager} instance.
   */
  public CvarManager() {
    this.CVARS = new PatriciaTrie<Cvar>();
  }

  /**
   * @return {@link Collection} of all {@link Cvar} instances {@linkplain #isManaging managed} by this
   * {@link CvarManager}
   */
  @NonNull
  public Collection<Cvar> getCvars() {
    return CVARS.values();
  }

  /**
   * Constructs and {@linkplain #add adds} a {@link Cvar} with the specified arguments to this
   * {@link CvarManager}.
   *
   * @param <T>          {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *                     {@link Cvar} represents
   * @param alias        {@link String} representation of the {@linkplain Cvar#getAlias name}
   * @param description  Brief {@linkplain Cvar#getDescription description} explaining the function
   *                     and values it expects
   * @param type         {@link Class} instance for the {@linkplain T type} of the {@linkplain
   *                     Cvar#getValue variable}
   * @param defaultValue {@linkplain Cvar#getDefaultValue Default value} which will be assigned to the
   *                     {@link Cvar} now and whenever it is {@linkplain Cvar#reset reset}.
   * @return reference to the created {@link Cvar}
   * @see SimpleCvar#SimpleCvar
   * @see #add
   * @see #remove
   */
  @NonNull
  public <T> Cvar<T> create(@Nullable final String alias, @Nullable final String description,
                            @NonNull final Class<T> type, @Nullable final T defaultValue) {
    Cvar<T> cvar = new SimpleCvar<T>(alias, description, type, defaultValue);
    add(cvar);
    return cvar;
  }

  /**
   * Constructs and {@linkplain #add adds} a {@link ValidatableCvar} with the specified arguments to
   * this {@link CvarManager}.
   *
   * @param <T>          {@linkplain Class type} of the {@linkplain ValidatableCvar#getValue variable}
   *                     which the {@link ValidatableCvar} represents
   * @param alias        {@link String} representation of the {@linkplain ValidatableCvar#getAlias
   *                     name}
   * @param description  Brief {@linkplain ValidatableCvar#getDescription description} explaining the
   *                     function and values it expects
   * @param type         {@link Class} instance for the {@linkplain T type} of the {@linkplain
   *                     ValidatableCvar#getValue variable}
   * @param defaultValue {@linkplain ValidatableCvar#getDefaultValue Default value} which will be
   *                     assigned to the {@link Cvar} now and whenever it is
   *                     {@linkplain ValidatableCvar#reset reset}. This value will not be validated
   *                     when {@linkplain ValidatableCvar#reset resetting} the {@link Cvar}
   * @param validator    {@link Validator} to use when performing {@linkplain ValidatableCvar#isValid
   *                     validations} on {@linkplain ValidatableCvar#setValue assignments}
   * @return reference to the created {@link ValidatableCvar}
   * @see ValidatableCvar#ValidatableCvar(String, String, Class, Object, Validator)
   * @see #add
   * @see #remove
   */
  @NonNull
  public <T> ValidatableCvar<T> create(@Nullable final String alias,
                                       @Nullable final String description,
                                       @NonNull final Class<T> type,
                                       @Nullable final T defaultValue,
                                       @NonNull final Validator validator) {
    ValidatableCvar<T> cvar
            = new ValidatableCvar<T>(alias, description, type, defaultValue, validator);
    add(cvar);
    return cvar;
  }

  /**
   * Adds the specified {@link Cvar} to this {@link CvarManager}.
   * <p>
   * Note: Duplicate {@linkplain Cvar#getAlias aliases} are not allowed.
   * </p>
   *
   * @param <T>  {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@link Cvar} represents
   * @param cvar {@link Cvar} instance to add
   * @throws IllegalArgumentException if there is already a {@link Cvar} instance being
   *                                  {@linkplain #isManaging managed} by this {@link CvarManager}
   * @see #create(String, String, Class, Object)
   * @see #create(String, String, Class, Object, Validator)
   * @see #remove(Cvar)
   */
  public <T> void add(@NonNull final Cvar<T> cvar) {
    if (isManaging(cvar)) {
      return;
    } else if (containsAlias(cvar.getAlias())) {
      throw new IllegalArgumentException(String.format(
              "cvar with the alias %s is already being managed by this CvarManager",
              cvar.getAlias()));
    }

    CVARS.put(getKey(cvar), cvar);
    cvar.addStateListener(this);
  }

  /**
   * Removes the specified {@link Cvar} from this {@link CvarManager}.
   *
   * @param cvar {@link Cvar} instance to remove
   * @return {@code true} if the {@link Cvar} was removed by this operation, otherwise {@code false}
   * if the {@link Cvar} was not or if the passed {@link Cvar} reference was {@code null}
   * @see #create(String, String, Class, Object)
   * @see #create(String, String, Class, Object, Validator)
   * @see #add
   */
  public boolean remove(@Nullable final Cvar cvar) {
    return isManaging(cvar) && CVARS.remove(getKey(cvar)) == null;
  }

  /**
   * Searches for all {@link Cvar} the instances {@linkplain #isManaging managed} by this
   * {@link CvarManager} with the specified {@link String} in part of their
   * {@linkplain Cvar#getAlias() alias} (i.e., partial matches are valid and expected)
   * <p>
   * Note: This operation is performed case-insensitively.
   * </p>
   * <p>
   * Note: If a {@code null} {@linkplain Cvar#getAlias() alias} is passed, then an empty
   * {@link Collection} will be returned.
   * </p>
   * <p>
   * Note: If the exact {@linkplain Cvar#getAlias() alias} is known and only one result is expected,
   * use {@link #get} instead
   * </p>
   *
   * @param alias partial or exact {@link Cvar} {@linkplain Cvar#getAlias() alias} to search for
   * @return {@link Collection} of all {@link Cvar} instances with the specified {@link String} in
   * part of its {@linkplain Cvar#getAlias() alias}
   * @see #get
   */
  @NonNull
  public Collection<Cvar> search(@Nullable final String alias) {
    if (alias == null) {
      return Collections.EMPTY_LIST;
    }

    return CVARS.prefixMap(getCaseInsensitiveKey(alias)).values();
  }

  /**
   * Searches for a {@link Cvar} instance {@linkplain #isManaging managed} by this {@link CvarManager}
   * with the specified {@linkplain Cvar#getAlias() alias}.
   * <p>
   * Note: This operation is performed case-insensitively, however there must be an exact character
   * match with some {@link Cvar} for it to be returned by this operation. If only a partial
   * {@linkplain Cvar#getAlias() alias} is known, then {@link #search} may be a better option.
   * </p>
   *
   * @param <T>   {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *              {@link Cvar} represents
   * @param alias exact {@link Cvar} {@linkplain Cvar#getAlias() alias} to search for
   * @return {@link Cvar} instance with the specified {@linkplain Cvar#getAlias() alias}, otherwise
   * {@code null} if no {@link Cvar} with that {@linkplain Cvar#getAlias() alias} is being
   * managed by this {@link CvarManager} or if the passed {@linkplain Cvar#getAlias() alias}
   * is {@code null}
   * @see #search
   */
  @Nullable
  public <T> Cvar<T> get(@Nullable final String alias) {
    if (alias == null) {
      return null;
    }

    return (Cvar<T>) CVARS.get(getCaseInsensitiveKey(alias));
  }

  /**
   * @param cvar {@link Cvar} to check
   * @return {@code true} if the specified {@link Cvar} is being managed by this {@link CvarManager},
   * otherwise {@code false} if it is not or if the passed {@link Cvar} reference was
   * {@code null}
   */
  public boolean isManaging(@Nullable final Cvar cvar) {
    if (cvar == null) {
      return false;
    }

    Cvar queriedCvar = CVARS.get(getKey(cvar));
    return cvar.equals(queriedCvar);
  }

  /**
   * <p>
   * Note: This operation is performed case-insensitively, however there must be an exact character
   * match with some {@link Cvar} for {@code true} to be returned by this operation.
   * </p>
   *
   * @param alias {@link String} representation of the {@linkplain Cvar#getAlias name} of the
   *              {@link Cvar} to check
   * @return {@code true} if this {@link CvarManager} contains a mapping to the specified
   * {@linkplain Cvar#getAlias alias}, otherwise {@code false} if it does not or if the passed
   * {@linkplain Cvar#getAlias alias} is {@code null}
   * @see #get
   */
  public boolean containsAlias(@Nullable final String alias) {
    return alias != null && CVARS.containsKey(getCaseInsensitiveKey(alias));
  }

  /**
   * @param cvar {@link Cvar} to return a key for
   * @return {@link String} representation for the key of the {@link Cvar}
   */
  @NonNull
  private static String getKey(@NonNull final Cvar cvar) {
    return getCaseInsensitiveKey(cvar.getAlias());
  }

  /**
   * @param alias {@linkplain Cvar#getAlias alias} to transform into a mappable key
   * @return case-insensitive transformation of the {@linkplain Cvar#getAlias alias}
   */
  @NonNull
  private static String getCaseInsensitiveKey(@NonNull final String alias) {
    return alias.toLowerCase();
  }

  @Override
  public void onChanged(@NonNull final Cvar cvar, @Nullable final Object from,
                        @Nullable final Object to) {
  }

  @Override
  public void onLoaded(@NonNull final Cvar cvar, @Nullable final Object to) {
  }

}
