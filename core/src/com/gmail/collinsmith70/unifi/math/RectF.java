package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class RectF {

    public static final RectF ZERO = ImmutableRectF.newImmutableRect();

    private float left;
    private float top;
    private float right;
    private float bottom;

    public RectF() {
        this(0, 0, 0, 0);
    }

    public RectF(float left, float top, float right, float bottom) {
        _set(left, top, right, bottom);
    }

    public RectF(@NonNull RectF src) {
        _set(src);
    }

    public RectF(@NonNull Rect src) {
        _set(src);
    }

    public float getLeft() {
        return left;
    }

    private void _setLeft(float left) {
        this.left = left;
    }

    public void setLeft(float left) {
        _setLeft(left);
    }

    public float getTop() {
        return top;
    }

    private void _setTop(float top) {
        this.top = top;
    }

    public void setTop(float top) {
        _setTop(top);
    }

    public float getRight() {
        return right;
    }

    private void _setRight(float right) {
        this.right = right;
    }

    public void setRight(float right) {
        _setRight(right);
    }

    public float getBottom() {
        return bottom;
    }

    private void _setBottom(float bottom) {
        this.bottom = bottom;
    }

    public void setBottom(float bottom) {
        _setBottom(bottom);
    }

    private void _set(float left, float top, float right, float bottom) {
        _setLeft(left);
        _setTop(top);
        _setRight(right);
        _setBottom(bottom);
    }

    public void set(float left, float top, float right, float bottom) {
        _set(left, top, right, bottom);
    }

    private void _set(@NonNull RectF src) {
        Validate.isTrue(src != null, "source Rect cannot be null");
        _setLeft(src.getLeft());
        _setTop(src.getTop());
        _setRight(src.getRight());
        _setBottom(src.getBottom());
    }

    public void set(@NonNull RectF src) {
        _set(src);
    }

    private void _set(@NonNull Rect src) {
        Validate.isTrue(src != null, "source Rect cannot be null");
        _setLeft(src.getLeft());
        _setTop(src.getTop());
        _setRight(src.getRight());
        _setBottom(src.getBottom());
    }

    public void set(@NonNull Rect src) {
        _set(src);
    }

    public boolean isEmpty() {
        return getLeft() != getRight() && getTop() != getBottom();
    }

    public final void setEmpty() {
        set(0, 0, 0, 0);
    }

    public float getX() {
        return getLeft();
    }

    public float getY() {
        return getBottom();
    }

    @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
    public float getWidth() {
        return Math.abs(getLeft() - getRight());
    }

    @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
    public float getHeight() {
        return Math.abs(getTop() - getBottom());
    }

    @NonNull
    public final RectF intersect(@NonNull RectF r) {
        return intersect(r, this);
    }

    @NonNull
    public final RectF intersect(@NonNull RectF r, @Nullable RectF dst) {
        Validate.isTrue(r != null, "r cannot be null");
        if (dst == null) {
            return new RectF(Math.max(getLeft(), r.getLeft()),
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

    public final boolean equals(float left, float top, float right, float bottom) {
        return getLeft() == left && getTop() == top && getRight() == right && getBottom() == bottom;
    }

    @NonNull
    public final Rect toRect() {
        return new Rect((int)getLeft(), (int)getTop(), (int)getRight(), (int)getBottom());
    }

    @Override
    @CallSuper
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof RectF)) {
            return false;
        }

        RectF other = (RectF) obj;
        return equals(other.getLeft(), other.getTop(), other.getRight(), other.getBottom());
    }

    @Override
    @CallSuper
    public int hashCode() {
        int result = 17;
        result = 31 * result + Float.floatToIntBits(getLeft());
        result = 31 * result + Float.floatToIntBits(getTop());
        result = 31 * result + Float.floatToIntBits(getRight());
        result = 31 * result + Float.floatToIntBits(getBottom());
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: { left=%d, top=%d, right=%d, bottom=%d }",
                getClass().getSimpleName(), getLeft(), getTop(), getRight(), getBottom());
    }

}
