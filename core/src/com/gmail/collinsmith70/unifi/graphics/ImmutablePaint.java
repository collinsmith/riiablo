package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

import org.apache.commons.lang3.Validate;

public final class ImmutablePaint extends Paint {

    public static ImmutablePaint newImmutablePaint() {
        return new ImmutablePaint();
    }

    public static ImmutablePaint copyOf(@NonNull Paint src) {
        return new ImmutablePaint(src);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(@NonNull Paint src) {
        return new Builder(src);
    }

    private ImmutablePaint() {
        super();
    }

    private ImmutablePaint(@NonNull Paint src) {
        super(src);
    }

    @Deprecated
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setColor(@NonNull Color color) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setStyle(@NonNull Style style) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
        throw new UnsupportedOperationException();
    }

    public static final class Builder {

        @NonNull
        private static Paint tmp;

        private Builder() {
            if (tmp == null) {
                tmp = new Paint();
            } else {
                tmp.reset();
            }
        }

        private Builder(@NonNull Paint src) {
            Validate.isTrue(src != null, "source Paint cannot be null");
            tmp = new Paint(src);
        }

        @NonNull
        public Color getColor() {
            return tmp.getColor();
        }

        @NonNull
        public Builder setColor(@NonNull Color color) {
            tmp.setColor(color);
            return this;
        }

        @NonNull
        public Paint.Style getStyle() {
            return tmp.getStyle();
        }

        @NonNull
        public Builder setStyle(@NonNull Paint.Style style) {
            tmp.setStyle(style);
            return this;
        }

        @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
        public float getStrokeWidth() {
            return tmp.getStrokeWidth();
        }

        @NonNull
        public Builder setStrokeWidth(
                @FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
            tmp.setStrokeWidth(strokeWidth);
            return this;
        }

        public ImmutablePaint build() {
            return new ImmutablePaint(tmp);
        }

    }

}
