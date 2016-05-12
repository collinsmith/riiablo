package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Rect {

    public static final Rect ZERO = ImmutableRect.newImmutableRect();

    private int left;
    private int top;
    private int right;
    private int bottom;

    public Rect() {
        this(0, 0, 0, 0);
    }

    public Rect(int left, int top, int right, int bottom) {
        _set(left, top, right, bottom);
    }

    public Rect(@NonNull Rect src) {
        _set(src);
    }

    protected void onChange() {
    }

    public int getLeft() {
        return left;
    }

    private void _setLeft(int left) {
        this.left = left;
    }

    public void setLeft(int left) {
        if (getLeft() != left) {
            _setLeft(left);
            onChange();
        }
    }

    public int getTop() {
        return top;
    }

    private void _setTop(int top) {
        this.top = top;
    }

    public void setTop(int top) {
        if (getTop() != top) {
            _setTop(top);
            onChange();
        }
    }

    public int getRight() {
        return right;
    }

    private void _setRight(int right) {
        this.right = right;
    }

    public void setRight(int right) {
        if (getRight() != right) {
            _setRight(right);
            onChange();
        }
    }

    public int getBottom() {
        return bottom;
    }

    private void _setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void setBottom(int bottom) {
        if (getBottom() != bottom) {
            _setBottom(bottom);
            onChange();
        }
    }

    private void _set(int left, int top, int right, int bottom) {
        _setLeft(left);
        _setTop(top);
        _setRight(right);
        _setBottom(bottom);
    }

    public void set(int left, int top, int right, int bottom) {
        if (!equals(left, top, right, bottom)) {
            _set(left, top, right, bottom);
            onChange();
        }
    }

    private void _set(@NonNull Rect src) {
        Validate.isTrue(src != null, "source Rect cannot be null");
        _setLeft(src.getLeft());
        _setTop(src.getTop());
        _setRight(src.getRight());
        _setBottom(src.getBottom());
    }

    public void set(@NonNull Rect src) {
        if (!equals(src.getLeft(), src.getTop(), src.getRight(), src.getBottom())) {
            _set(src);
            onChange();
        }
    }

    public boolean isEmpty() {
        return getLeft() != getRight() && getTop() != getBottom();
    }

    public final void setEmpty() {
        set(0, 0, 0, 0);
    }

    public int getX() {
        return getLeft();
    }

    public int getY() {
        return getBottom();
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getWidth() {
        return Math.abs(getLeft() - getRight());
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getHeight() {
        return Math.abs(getTop() - getBottom());
    }

    @NonNull
    public final Rect intersect(@NonNull Rect r) {
        return intersect(r, this);
    }

    @NonNull
    public final Rect intersect(@NonNull Rect r, @Nullable Rect dst) {
        Validate.isTrue(r != null, "r cannot be null");
        if (dst == null) {
            return new Rect(Math.max(getLeft(), r.getLeft()),
                    Math.min(getTop(), r.getTop()),
                    Math.min(getRight(), r.getRight()),
                    Math.max(getBottom(), r.getBottom()));
        }

        dst.set(Math.max(getLeft(), r.getLeft()),
                Math.min(getTop(), r.getTop()),
                Math.min(getRight(), r.getRight()),
                Math.max(getBottom(), r.getBottom()));
        return dst;
    }

    public final boolean equals(int left, int top, int right, int bottom) {
        return getLeft() == left && getTop() == top && getRight() == right && getBottom() == bottom;
    }

    @Override
    @CallSuper
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof Rect)) {
            return false;
        }

        Rect other = (Rect) obj;
        return equals(other.getLeft(), other.getTop(), other.getRight(), other.getBottom());
    }

    @Override
    @CallSuper
    public int hashCode() {
        int result = 17;
        result = 31 * result + getLeft();
        result = 31 * result + getTop();
        result = 31 * result + getRight();
        result = 31 * result + getBottom();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: { left=%d, top=%d, right=%d, bottom=%d }",
                getClass().getSimpleName(), getLeft(), getTop(), getRight(), getBottom());
    }

}
