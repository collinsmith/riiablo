package com.gmail.collinsmith70.cvar;

public class CvarChangeAdapter<T> implements CvarChangeListener<T> {

@Override
public T beforeChanged(Cvar<T> cvar, T from, T to) {
    return to;
}

@Override
public void afterChanged(Cvar<T> cvar, T from, T to) {
    //...
}

@Override
public void onLoad(Cvar<T> cvar, T to) {
    afterChanged(cvar, null, to);
}

}
