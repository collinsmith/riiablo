package com.google.collinsmith70.diablo.cvar.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum LongCvarLoadListener implements CvarLoadListener<Long> {
INSTANCE;

@Override
public Long onCvarLoaded(String value) {
    return Long.parseLong(value);
}

@Override
public String toString(Long obj) {
    return obj.toString();
}

}