package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum BooleanCvarLoadListener implements CvarLoadListener<Boolean> {
INSTANCE;

@Override
public Boolean onCvarLoaded(String value) {
    if (value.equalsIgnoreCase("true")) {
        return Boolean.TRUE;
    } else if (value.equalsIgnoreCase("yes")) {
        return Boolean.TRUE;
    } else {
        try {
            int i = Integer.parseInt(value);
            return i > 0;
        } catch (NumberFormatException e) {
            return Boolean.FALSE;
        }
    }
}

@Override
public String toString(Boolean obj) {
    return obj.toString();
}

}
