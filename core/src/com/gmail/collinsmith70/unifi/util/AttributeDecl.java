package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

import java.util.Locale;

public final class AttributeDecl {

    @NonNull
    private final String name;

    @NonNull
    private final Class<?> valueType;

    public AttributeDecl(@NonNull String name, @NonNull Class<?> valueType) {
        Validate.isTrue(name != null, "name cannot be null");
        Validate.isTrue(valueType != null, "valueType cannot be null");
        this.name = name;
        this.valueType = valueType;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Class<?> getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "AttributeDecl: { name=\"%s\", type=\"%s\" }",
                name, valueType.getName());
    }

}
