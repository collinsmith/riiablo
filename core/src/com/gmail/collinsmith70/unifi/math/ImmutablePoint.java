package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.NonNull;

public final class ImmutablePoint extends Point {

    public static ImmutablePoint newImmutablePoint() {
        return new ImmutablePoint();
    }

    public static ImmutablePoint newImmutablePoint(int x, int y) {
        return new ImmutablePoint(x, y);
    }

    public static ImmutablePoint copyOf(@NonNull Point src) {
        return new ImmutablePoint(src);
    }

    private ImmutablePoint() {
        super();
    }

    private ImmutablePoint(int x, int y) {
        super(x, y);
    }

    private ImmutablePoint(@NonNull Point src) {
        super();
    }

    @Deprecated
    @Override
    public void setX(int x) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setY(int y) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void set(@NonNull Point src) {
        throw new UnsupportedOperationException();
    }

}
