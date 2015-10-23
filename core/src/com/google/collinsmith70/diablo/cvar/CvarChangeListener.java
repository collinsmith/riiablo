package com.google.collinsmith70.diablo.cvar;

public interface CvarChangeListener<T> {
void onCvarChanged(Cvar<T> cvar, T fromValue, T toValue);
}
