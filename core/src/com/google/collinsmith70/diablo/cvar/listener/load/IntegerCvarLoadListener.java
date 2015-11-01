package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum IntegerCvarLoadListener implements CvarLoadListener<Integer> {
INSTANCE;

@Override
public Integer onCvarLoaded(String value) {
    return Integer.parseInt(value);
}

@Override
public String toString(Integer obj) {
    return obj.toString();
}

}
