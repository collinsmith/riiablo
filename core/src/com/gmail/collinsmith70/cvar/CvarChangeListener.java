package com.gmail.collinsmith70.cvar;

/**
 * Functional interface for {@link #onCvarChanged(Cvar, Object, Object)} which will be called
 * whenever a {@link Cvar} is changed.
 *
 * @param <T> class which the {@link Cvar} represents
 */
public interface CvarChangeListener<T> {

/**
 * Called when a {@link Cvar} object has its value changed.
 *
 * @note this method will only be called in the case where the value is changed to a different,
 *       discrete, value
 *
 * @param cvar {@linkplain Cvar} calling the event
 * @param from previous
 * @param to   current
 */
void onCvarChanged(Cvar<T> cvar, T from, T to);

}
