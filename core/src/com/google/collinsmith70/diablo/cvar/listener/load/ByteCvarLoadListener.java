package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum ByteCvarLoadListener implements CvarLoadListener<Byte> {
INSTANCE;

@Override
public Byte onCvarLoaded(String value) {
    return Byte.parseByte(value);
}

@Override
public String toString(Byte obj) {
    return obj.toString();
}

}
