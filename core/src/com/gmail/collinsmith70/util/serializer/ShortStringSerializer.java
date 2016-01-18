package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Short} object into its {@link String} representation.
 */
public enum ShortStringSerializer implements StringSerializer<Short> {
/**
 * @see ShortStringSerializer
 */
INSTANCE;

@Override
public String serialize(Short obj) {
    return obj.toString();
}

@Override
public Short deserialize(String obj) {
    return Short.parseShort(obj);
}

}
