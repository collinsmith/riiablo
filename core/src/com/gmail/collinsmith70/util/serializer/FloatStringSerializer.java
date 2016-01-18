package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Float} object into its {@link String} representation.
 */
public enum FloatStringSerializer implements StringSerializer<Float> {
/**
 * @see FloatStringSerializer
 */
INSTANCE;

@Override
public String serialize(Float obj) {
    return obj.toString();
}

@Override
public Float deserialize(String obj) {
    return Float.parseFloat(obj);
}

}
