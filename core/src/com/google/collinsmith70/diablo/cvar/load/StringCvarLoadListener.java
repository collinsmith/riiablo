package com.google.collinsmith70.diablo.cvar.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum StringCvarLoadListener implements CvarLoadListener<String> {
INSTANCE;

@Override
public String onCvarLoaded(String value) {
    return value;
}

@Override
public String toString(String obj) {
    return obj;
}

}
