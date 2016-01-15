package com.gmail.collinsmith70.cvar;

public interface CvarChangeListener<T> {

T beforeChanged(Cvar<T> cvar, T from, T to);
void afterChanged(Cvar<T> cvar, T from, T to);

}
