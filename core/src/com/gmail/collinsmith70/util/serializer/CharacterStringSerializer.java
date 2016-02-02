package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Character} object into its {@link String} representation.
 */
public enum CharacterStringSerializer implements StringSerializer<Character> {
/**
 * @see CharacterStringSerializer
 */
INSTANCE;

/**
 * {@inheritDoc}
 */
@Override
public String serialize(Character obj) {
    return obj.toString();
}

/**
 * {@inheritDoc}
 */
@Override
public Character deserialize(String obj) {
    if (obj.length() != 1) {
        throw new IllegalArgumentException("Character serializations should have a length of 1");
    }

    return obj.charAt(0);
}

}
