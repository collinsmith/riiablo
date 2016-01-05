package com.gmail.collinsmith70.util.serializer;

import com.gmail.collinsmith70.util.StringSerializer;

/**
 * Serializer which (de)serializes a {@link Boolean} object into its {@link String} representation.
 */
public enum BooleanStringSerializer implements StringSerializer<Boolean> {
/**
 * @see BooleanStringSerializer
 */
INSTANCE;

@Override
public String serialize(Boolean obj) {
    return obj.toString();
}

@Override
public Boolean deserialize(String obj) {
    if (obj.equalsIgnoreCase("true")) {
        return Boolean.TRUE;
    } else if (obj.equalsIgnoreCase("yes")) {
        return Boolean.TRUE;
    } else {
        try {
            int i = Integer.parseInt(obj);
            return i > 0;
        } catch (NumberFormatException e) {
            return Boolean.FALSE;
        }
    }
}

}
