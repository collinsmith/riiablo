package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.NonNull;

public final class ImmutableRectF extends RectF {

    public static ImmutableRectF newImmutableRect() {
        return new ImmutableRectF();
    }

    public static ImmutableRectF newImmutableRect(float left, float top,
                                                  float right, float bottom) {
        return new ImmutableRectF(left, top, right, bottom);
    }

    public static ImmutableRectF copyOf(@NonNull RectF src) {
        return new ImmutableRectF(src);
    }

    public static ImmutableRectF copyOf(@NonNull Rect src) {
        return new ImmutableRectF(src);
    }

    private ImmutableRectF() {
        super();
    }

    private ImmutableRectF(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    private ImmutableRectF(@NonNull RectF src) {
        super(src);
    }

    private ImmutableRectF(@NonNull Rect src) {
        super(src);
    }

    @Deprecated
    @Override
    public void setLeft(float left) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setTop(float top) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setRight(float right) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setBottom(float bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(float left, float top, float right, float bottom) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull RectF src) {
        throw new UnsupportedOperationException();
    }

}
