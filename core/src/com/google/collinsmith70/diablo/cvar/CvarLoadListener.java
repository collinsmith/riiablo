package com.google.collinsmith70.diablo.cvar;

public interface CvarLoadListener<T> {
T onCvarLoaded(String value);
}
