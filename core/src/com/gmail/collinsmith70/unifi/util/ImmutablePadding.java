package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.math.Rect;

public final class ImmutablePadding extends Padding {

    public static ImmutablePadding newImmutablePadding() {
        return new ImmutablePadding();
    }

    public static ImmutablePadding newImmutablePadding(
            @IntRange(from = 0, to = Integer.MAX_VALUE) int left,
            @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
            @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
            @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        return new ImmutablePadding(left, top, right, bottom);
    }

    public static ImmutablePadding copyOf(@NonNull Rect src) {
        return new ImmutablePadding(src);
    }

    public static ImmutablePadding copyOf(@NonNull Bounds src) {
        return new ImmutablePadding(src);
    }

    private ImmutablePadding() {
        super();
    }

    private ImmutablePadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                             @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                             @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                             @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        super(left, top, right, bottom);
    }

    private ImmutablePadding(@NonNull Rect src) {
        super(src);
    }

    private ImmutablePadding(@NonNull Bounds src) {
        super(src);
    }

    @Deprecated
    @Override
    public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Rect src) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Padding src) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    protected final void onChange() {
        throw new AssertionError();
    }

}
