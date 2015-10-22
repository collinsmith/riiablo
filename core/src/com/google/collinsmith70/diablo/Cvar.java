package com.google.collinsmith70.diablo;

public class Cvar<T> {
private final String key;
private final Class<T> type;

public Cvar(String key, Class<T> type) {
    this.key = key;
    this.type = type;
}

public String getKey() {
    return key;
}

public Class<T> getType() {
    return type;
}

@Override
public String toString() {
    return getKey();
}
}
