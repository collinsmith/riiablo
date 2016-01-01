package com.gmail.collinsmith70.cvar.serializer;

import com.gmail.collinsmith70.cvar.Serializer;

/**
 * Serializer which (de)serializes a {@link Boolean} object into its {@link String} representation.
 */
public enum BooleanSerializer implements Serializer<Boolean, String> {
/**
 * @see BooleanSerializer
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
