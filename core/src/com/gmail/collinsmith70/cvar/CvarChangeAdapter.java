package com.gmail.collinsmith70.cvar;

import com.sun.istack.internal.NotNull;

/**
 * Implementation of a {@link CvarChangeListener} which treats load events as change event from
 * {@literal null} to the current value.
 *
 * @param <T> type of the value which the {@linkplain Cvar} represents
 */
public class CvarChangeAdapter<T> implements CvarChangeListener<T> {

/**
 * Called when a {@linkplain Cvar}'s value is changed.
 *
 * @param cvar {@linkplain Cvar} whose state is transitioning
 * @param from previous value of the {@linkplain Cvar}
 * @param to   value of the {@linkplain Cvar}
 */
@Override
public void onChanged(@NotNull Cvar<T> cvar, T from, @NotNull T to) {}

/**
 * Called when a {@linkplain Cvar} is loaded and propagates that callback to
 * {@link #onChanged(Cvar, Object, Object)} instead, passing {@literal null} as the {@code from}
 * argument.
 *
 * @param cvar {@linkplain Cvar} whose state is transitioning
 * @param to   value of the {@linkplain Cvar}
 */
@Override
public void onLoad(@NotNull Cvar<T> cvar, @NotNull T to) {
    onChanged(cvar, null, to);
}

}
