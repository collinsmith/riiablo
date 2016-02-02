package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Byte} object into its {@link String} representation.
 */
public enum ByteStringSerializer implements StringSerializer<Byte> {
/**
 * @see ByteStringSerializer
 */
INSTANCE;

/**
 * {@inheritDoc}
 */
@Override
public String serialize(Byte obj) {
    return obj.toString();
}

/**
 * {@inheritDoc}
 */
@Override
public Byte deserialize(String obj) {
    return Byte.parseByte(obj);
}

}
