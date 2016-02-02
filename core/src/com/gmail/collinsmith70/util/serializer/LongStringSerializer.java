package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Long} object into its {@link String} representation.
 */
public enum LongStringSerializer implements StringSerializer<Long> {
/**
 * @see LongStringSerializer
 */
INSTANCE;

/**
 * {@inheritDoc}
 */
@Override
public String serialize(Long obj) {
    return obj.toString();
}

/**
 * {@inheritDoc}
 */
@Override
public Long deserialize(String obj) {
    return Long.parseLong(obj);
}

}
