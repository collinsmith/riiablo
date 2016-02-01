package com.gmail.collinsmith70.cvar;

public interface CvarChangeListener<T> {

void onChanged(Cvar<T> cvar, T from, T to);
void onLoad(Cvar<T> cvar, T to);

}
