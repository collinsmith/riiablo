package com.google.collinsmith70.diablo.cvar.listener.load;

import com.google.collinsmith70.diablo.cvar.CvarLoadListener;

public enum DoubleCvarLoadListener implements CvarLoadListener<Double> {
INSTANCE;

@Override
public Double onCvarLoaded(String value) {
    return Double.parseDouble(value);
}

@Override
public String toString(Double obj) {
    return obj.toString();
}

}