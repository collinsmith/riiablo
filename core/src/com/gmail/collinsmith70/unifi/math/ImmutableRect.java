package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.NonNull;

public final class ImmutableRect extends Rect {

    public static ImmutableRect newImmutableRect() {
        return new ImmutableRect();
    }

    public static ImmutableRect newImmutableRect(int left, int top, int right, int bottom) {
        return new ImmutableRect(left, top, right, bottom);
    }

    public static ImmutableRect copyOf(@NonNull Rect src) {
        return new ImmutableRect(src);
    }

    private ImmutableRect() {
        super();
    }

    private ImmutableRect(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);
    }

    private ImmutableRect(@NonNull Rect src) {
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

}
