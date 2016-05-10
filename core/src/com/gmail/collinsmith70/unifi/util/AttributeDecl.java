package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

import java.util.Locale;

public final class AttributeDecl<T> {

    @NonNull
    private final String name;

    @NonNull
    private final Class<T> valueType;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private final int index;

    public AttributeDecl(@IntRange(from = 0, to = Integer.MAX_VALUE) int index,
                         @NonNull String name,
                         @NonNull Class<T> valueType) {
        Validate.isTrue(index >= 0, "index cannot be less than 0");
        Validate.isTrue(name != null, "name cannot be null");
        Validate.isTrue(valueType != null, "valueType cannot be null");
        this.index = index;
        this.name = name;
        this.valueType = valueType;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getIndex() {
        return index;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Class<T> getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "AttributeDecl: { name=\"%s\", type=\"%s\" }",
                name, valueType.getName());
    }

}
