package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Dimension {

    public static final Dimension ZERO = ImmutableDimension.newImmutableDimension();

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int width;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int height;

    public Dimension() {
        this(0, 0);
    }

    public Dimension(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                     @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        _set(width, height);
    }

    public Dimension(@NonNull Dimension src) {
        _set(src);
    }

    protected void onChange() {
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getWidth() {
        return width;
    }

    private void _setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
        Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
        this.width = width;
    }

    public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
        if (getWidth() != width) {
            _setWidth(width);
            onChange();
        }
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getHeight() {
        return height;
    }

    private void _setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
        this.height = height;
    }

    public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        if (getHeight() != height) {
            _setHeight(height);
            onChange();
        }
    }

    private void _set(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        if (!equals(width, height)) {
            _setWidth(width);
            _setHeight(height);
            onChange();
        }
    }

    public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        _set(width, height);
    }

    private void _set(@NonNull Dimension src) {
        Validate.isTrue(src != null, "source Dimension cannot be null");
        if (!equals(src.getWidth(), src.getHeight())) {
            _setWidth(src.getWidth());
            _setHeight(src.getHeight());
            onChange();
        }
    }

    public void set(@NonNull Dimension src) {
        _set(src);
    }

    public final boolean equals(int width, int height) {
        return getWidth() == width && getHeight() == height;
    }

    @Override
    @CallSuper
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof Dimension)) {
            return false;
        }

        Dimension other = (Dimension) obj;
        return equals(other.getWidth(), other.getHeight());
    }

    @Override
    @CallSuper
    public int hashCode() {
        int result = 17;
        result = 31 * result + getWidth();
        result = 31 * result + getHeight();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: { width=%d, height=%d }",
                getClass().getSimpleName(), getWidth(), getHeight());
    }

}
