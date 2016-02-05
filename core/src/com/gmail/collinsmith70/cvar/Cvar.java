package com.gmail.collinsmith70.cvar;

import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

/**
 * <a href="https://en.wikipedia.org/wiki/CVAR">CVAR</a>s are representations for variables which
 * are used for configuring some part of a client, specifically in game applications. Additionally,
 * CVARs support {@linkplain StateListener callbacks} when certain state transitions occur.
 *
 * @param <T> {@linkplain Class type} of the {@linkplain #getValue variable} which this CVAR
 *            represents
 *
 * @see <a href="https://en.wikipedia.org/wiki/CVAR">Wikipedia article on CVARs</a>
 */
public interface Cvar<T> {

/**
 * @return {@link String} representation for the <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">
 *         key</a> of this {@link Cvar}, or {@code null} if no alias has been set
 *
 * @see <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">
 *      Wikipedia article on key-value pairs</a>
 */
@Nullable
String getAlias();

/**
 * @return default value of this {@link Cvar}, which is assigned upon instantiation and when this
 *         {@link Cvar} is {@linkplain #reset reset}, or {@code null} if there is no default value
 *
 * @see #reset()
 */
@Nullable
T getDefaultValue();

/**
 * @return brief {@linkplain String description} explaining the function of this {@link Cvar} and
 *         the values it expects, or <a href="https://en.wikipedia.org/wiki/Empty_string">
 *         empty string</a> if no description was given
 */
@NonNull
String getDescription();

/**
 * @return {@link Class} instance for the {@linkplain T type} of the {@linkplain #getValue variable}
 *         which this {@link Cvar} represents
 */
@NonNull
Class<T> getType();

/**
 * @return value of the variable represented by this {@link Cvar}, or {@code null} if no value has
 *         been set
 */
@Nullable
T getValue();

/**
 * @return {@code true} if the {@linkplain #getValue value} of this {@link Cvar} is {@code null},
 *         otherwise {@code false}
 */
boolean isEmpty();

/**
 * @return {@code true} if this {@link Cvar} has had its first {@linkplain #setValue assignment},
 *         otherwise {@code false}
 */
boolean isLoaded();

/**
 * Resets this {@link Cvar} to its {@linkplain #getDefaultValue default value}.
 * <p>
 * Note: Calling this method does not set the state of this {@link Cvar} as {@linkplain #isLoaded
 *       loaded}
 * </p>
 *
 * @see #getDefaultValue()
 */
void reset();

/**
 * Note: Setting the value of this {@link Cvar} to {@code null} will not change the state of this
 *       {@link Cvar} to {@linkplain #isLoaded unloaded}
 * <p>
 * Note: Calling this method will invoke either {@link Cvar.StateListener#onChanged} or
 *       {@link Cvar.StateListener#onLoaded} depending on whether or not this {@link Cvar} has
 *       been {@linkplain #isLoaded loaded}
 * </p>
 *
 * @param value value to change the variable represented by this {@link Cvar} to, or {@code null} to
 *              mark this {@link Cvar} as having no value
 */
void setValue(@Nullable final T value);

/**
 * @param l {@link Cvar.StateListener} to add
 *
 * @see #containsStateListener(StateListener)
 * @see #removeStateListener(StateListener)
 */
void addStateListener(@NonNull final StateListener<T> l);

/**
 * Note: If the passed reference is {@code null}, {@code false} will be returned.
 *
 * @param l {@link Cvar.StateListener} to check
 *
 * @return {@code true} if the passed {@link Cvar.StateListener} is receiving callbacks for state
 *         changes of this {@link Cvar}, otherwise {@code false}
 *
 * @see #addStateListener(StateListener)
 * @see #removeStateListener(StateListener)
 */
boolean containsStateListener(@Nullable final StateListener<T> l);

/**
 * Note: If the passed reference is {@code null}, {@code false} will be returned.
 *
 * @param l {@link Cvar.StateListener} to remove
 *
 * @return {@code true} if the passed {@link Cvar.StateListener} was removed by this operation,
 *         otherwise {@code false} if it is not receiving callbacks from this {@link Cvar}
 *
 * @see #addStateListener(StateListener)
 * @see #containsStateListener(StateListener)
 */
boolean removeStateListener(@Nullable final StateListener<T> l);

/**
 * Interface for representing the various <a href="https://en.wikipedia.org/wiki/Callback_(computer_programming)">
 * callbacks</a> {@link Cvar} instances will give during state transitions.
 *
 * @param <T> {@linkplain Class type} of the {@linkplain #getValue variable} which the CVAR
 *            represents
 *
 * @see <a href="https://en.wikipedia.org/wiki/Callback_(computer_programming)">
 *      Wikipedia article on callbacks</a>
 */
interface StateListener<T> {

    /**
     * Called synchronously when the value of a {@link Cvar} changes.
     * <p>
     * Note: This callback may not be called when a {@link Cvar} is {@linkplain #isLoaded loaded},
     *       as {@link #onLoaded(Cvar, Object)} is designed specifically for that purpose and this
     *       callback may not apply in all cases.
     * </p>
     *
     * @param cvar {@link Cvar} where the event occurred
     * @param from previous value of the {@link Cvar}
     * @param to   current value of the {@link Cvar}
     */
    void onChanged(@NonNull final Cvar<T> cvar, @Nullable final T from, @Nullable final T to);

    /**
     * Called synchronously when a {@link Cvar} is {@linkplain #isLoaded loaded}.
     *
     * @param cvar {@link Cvar} where the event occurred
     * @param to   current value of the {@link Cvar}
     */
    void onLoaded(@NonNull final Cvar<T> cvar, @Nullable final T to);

}

}