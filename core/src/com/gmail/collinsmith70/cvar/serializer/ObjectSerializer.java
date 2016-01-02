package com.gmail.collinsmith70.cvar.serializer;

import com.gmail.collinsmith70.cvar.Serializer;

/**
 * Serializer which (de)serializes an {@link Object} into its {@link String} representation.
 */
public enum ObjectSerializer implements Serializer<Object, String> {
/**
 * @see ObjectSerializer
 */
INSTANCE;

@Override
public String serialize(Object obj) {
    return obj.toString();
}

@Override
public Object deserialize(String obj) {
    return obj;
}

}