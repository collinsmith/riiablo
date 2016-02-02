package com.gmail.collinsmith70.cvar;

import com.sun.istack.internal.NotNull;

/**
 * Abstraction enumerating the methods required to listen and receive callbacks from certain
 * {@link Cvar} state transitions.
 *
 * @param <T> type of the value which the {@linkplain Cvar} represents
 */
public interface CvarChangeListener<T> {

/**
 * Called when a {@linkplain Cvar}'s value is changed.
 *
 * @note This is not guaranteed to be called when a CVAR is loaded or when this listener is
 *       registered, but is guaranteed to be called for all subsequent changes.
 *
 * @param cvar {@linkplain Cvar} whose state is transitioning
 * @param from previous value of the {@linkplain Cvar}
 * @param to   value of the {@linkplain Cvar}
 */
void onChanged(@NotNull Cvar<T> cvar, T from, @NotNull T to);

/**
 * Called when a {@linkplain Cvar} is loaded.
 *
 * @param cvar {@linkplain Cvar} whose state is transitioning
 * @param to   value of the {@linkplain Cvar}
 */
void onLoad(@NotNull Cvar<T> cvar, @NotNull T to);

}
