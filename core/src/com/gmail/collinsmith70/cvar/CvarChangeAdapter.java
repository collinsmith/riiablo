package com.gmail.collinsmith70.cvar;

public class CvarChangeAdapter<T> implements CvarChangeListener<T> {

@Override
public void onChanged(Cvar<T> cvar, T from, T to) {
    //...
}

@Override
public void onLoad(Cvar<T> cvar, T to) {
    onChanged(cvar, null, to);
}

}
