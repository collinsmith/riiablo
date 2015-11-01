package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum FloatCvarLoadListener implements CvarLoadListener<Float> {
INSTANCE;

@Override
public Float onCvarLoaded(String value) {
    return Float.parseFloat(value);
}

@Override
public String toString(Float obj) {
    return obj.toString();
}

}
