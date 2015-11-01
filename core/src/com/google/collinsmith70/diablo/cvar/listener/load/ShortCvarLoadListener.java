package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum ShortCvarLoadListener implements CvarLoadListener<Short> {
INSTANCE;

@Override
public Short onCvarLoaded(String value) {
    return Short.parseShort(value);
}

@Override
public String toString(Short obj) {
    return obj.toString();
}

}