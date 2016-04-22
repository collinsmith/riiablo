package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.math.Rect;

public final class ImmutableBounds extends Bounds {

    public static ImmutableBounds newImmutableBounds() {
        return new ImmutableBounds();
    }

    public static ImmutableBounds newImmutableBounds(int left, int top, int right, int bottom) {
        return new ImmutableBounds(left, top, right, bottom);
    }

    public static ImmutableBounds copyOf(@NonNull Rect src) {
        return new ImmutableBounds(src);
    }

    public static ImmutableBounds copyOf(@NonNull Bounds src) {
        return new ImmutableBounds(src);
    }

    private ImmutableBounds() {
        super();
    }

    private ImmutableBounds(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);
    }

    private ImmutableBounds(@NonNull Rect src) {
        super(src);
    }

    private ImmutableBounds(@NonNull Bounds src) {
        super(src);
    }

    @Deprecated
    @Override
    public void setLeft(int left) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setTop(int top) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setRight(int right) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setBottom(int bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(int left, int top, int right, int bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Rect src) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Bounds src) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    protected final void onChange() {
        throw new AssertionError();
    }

}
