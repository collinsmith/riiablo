package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Point {

    public static final Point ZERO = ImmutablePoint.newImmutablePoint();

    private int x;
    private int y;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        _set(x, y);
    }

    public Point(@NonNull Point src) {
        _set(src);
    }

    public int getX() {
        return x;
    }

    private void _setX(int x) {
        this.x = x;
    }

    public void setX(int x) {
        _setX(x);
    }

    public int getY() {
        return y;
    }

    private void _setY(int y) {
        this.y = y;
    }

    public void setY(int y) {
        _setY(y);
    }

    private void _set(int x, int y) {
        _setX(x);
        _setY(y);
    }

    public void set(int x, int y) {
        _set(x, y);
    }

    private void _set(@NonNull Point src) {
        Validate.isTrue(src != null, "source Point cannot be null");
        _setX(src.getX());
        _setY(src.getY());
    }

    public void set(@NonNull Point src) {
        _set(src);
    }

    public final boolean equals(int x, int y) {
        return getX() == x && getY() == y;
    }

    @Override
    @CallSuper
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof Point)) {
            return false;
        }

        Point other = (Point) obj;
        return equals(other.getX(), other.getY());
    }

    @Override
    @CallSuper
    public int hashCode() {
        int result = 17;
        result = 31 * result + getX();
        result = 31 * result + getY();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: { x=%d, y=%d }", getClass().getSimpleName(), getX(), getY());
    }

}
