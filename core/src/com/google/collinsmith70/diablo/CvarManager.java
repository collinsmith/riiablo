package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Preferences;

import java.util.Map;

public class CvarManager {
private final Preferences PREFERENCES;

public CvarManager(Preferences p) {
    this.PREFERENCES = p;
}

public Map<String, ?> get() {
    return PREFERENCES.get();
}

public <T> T getValue(Cvar<T> cvar) {
    Class<T> type = cvar.getType();
    String typeName = type.getTypeName();
    if (typeName.equals(Integer.class.getSimpleName())) {
        return (T)new Integer(PREFERENCES.getInteger(cvar.getKey()));
    } else if (typeName.equals(String.class.getSimpleName())) {
        return (T)PREFERENCES.getString(cvar.getKey());
    }

    return null;
}
}
