package com.gmail.collinsmith70.unifi;

import com.google.common.primitives.UnsignedInteger;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

public final class R {

    private R() {
    }

    public static final class styleable {

        private styleable() {
        }

        public static abstract class Style {

            private Style() {
            }

            public static final class AttributeDecl {

                public final String name;
                public final Class<?> valueType;

                private AttributeDecl(@NonNull String name, @NonNull Class<?> valueType) {
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

            }

        }

        public static final class ColorDrawable extends Style {

            public static final AttributeDecl color
                    = new AttributeDecl("color", UnsignedInteger.class);

            private static AttributeDecl[] values = { color };

        }

    }

}
