package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes an {@link Object} into its {@link String} representation.
 */
public enum ObjectStringSerializer implements StringSerializer<Object> {
/**
 * @see ObjectStringSerializer
 */
INSTANCE;

/**
 * {@inheritDoc}
 */
@Override
public String serialize(Object obj) {
    return obj.toString();
}

/**
 * {@inheritDoc}
 */
@Override
public Object deserialize(String obj) {
    return obj;
}

}