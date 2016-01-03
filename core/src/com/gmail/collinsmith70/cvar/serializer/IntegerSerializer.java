package com.gmail.collinsmith70.cvar.serializer;

import com.gmail.collinsmith70.util.Serializer;

/**
 * Serializer which (de)serializes a {@link Integer} object into its {@link String} representation.
 */
public enum IntegerSerializer implements Serializer<Integer, String> {
/**
 * @see IntegerSerializer
 */
INSTANCE;

@Override
public String serialize(Integer obj) {
    return obj.toString();
}

@Override
public Integer deserialize(String obj) {
    return Integer.parseInt(obj);
}

}
