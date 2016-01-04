package com.gmail.collinsmith70.cvar.serializer;

import com.gmail.collinsmith70.util.Serializer;

public enum DoubleSerializer implements Serializer<Double, String> {
INSTANCE;

@Override
public String serialize(Double obj) {
    return obj.toString();
}

@Override
public Double deserialize(String obj) {
    return Double.parseDouble(obj);
}

}
