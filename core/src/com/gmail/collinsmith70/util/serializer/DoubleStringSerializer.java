package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Double} object into its {@link String} representation.
 */
public enum DoubleStringSerializer implements StringSerializer<Double> {
/**
 * @see DoubleStringSerializer
 */
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
