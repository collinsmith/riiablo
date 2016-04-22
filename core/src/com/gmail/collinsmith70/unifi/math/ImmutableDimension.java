package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public final class ImmutableDimension extends Dimension {

    public static ImmutableDimension newImmutableDimension() {
        return new ImmutableDimension();
    }

    public static ImmutableDimension newImmutableDimension(
            @IntRange(from = 0, to = Integer.MAX_VALUE) int width,
            @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        return new ImmutableDimension(width, height);
    }

    public static ImmutableDimension copyOf(@NonNull Dimension src) {
        return new ImmutableDimension(src);
    }

    private ImmutableDimension() {
        super();
    }

    private ImmutableDimension(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        super(width, height);
    }

    private ImmutableDimension(@NonNull Dimension src) {
        super(src);
    }

    @Deprecated
    @Override
    public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Dimension src) {
        throw new UnsupportedOperationException();
    }

}
