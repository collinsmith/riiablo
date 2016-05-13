package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.math.Rect;

import org.apache.commons.lang3.Validate;

public class Bounds extends Rect {

    public static final Bounds ZERO = ImmutableBounds.newImmutableBounds();

    public Bounds() {
        this(0, 0, 0, 0);
    }

    public Bounds(int left, int top, int right, int bottom) {
        _set(left, top, right, bottom);
    }

    public Bounds(@NonNull Rect src) {
        _set(src);
    }

    public Bounds(@NonNull Bounds src) {
        _set(src);
    }

    private void _setLeft(int left) {
        super.setLeft(left);
        if (getRight() < left) {
            super.setRight(left);
        }
    }

    @Override
    public void setLeft(int left) {
        if (getLeft() != left) {
            _setLeft(left);
            onChange();
        }
    }

    private void _setTop(int top) {
        super.setTop(top);
        if (getBottom() > top) {
            super.setBottom(top);
        }
    }

    @Override
    public void setTop(int top) {
        if (getTop() != top) {
            _setTop(top);
            onChange();
        }
    }

    private void _setRight(int right) {
        super.setRight(right);
        if (getLeft() > right) {
            super.setLeft(right);
        }
    }

    @Override
    public void setRight(int right) {
        if (getRight() != right) {
            _setRight(right);
            onChange();
        }
    }

    private void _setBottom(int bottom) {
        super.setBottom(bottom);
        if (getTop() < bottom) {
            super.setTop(bottom);
        }
    }

    @Override
    public void setBottom(int bottom) {
        if (getBottom() != bottom) {
            _setBottom(bottom);
            onChange();
        }
    }

    private void _set(int left, int top, int right, int bottom) {
        Validate.isTrue(right >= left, "right must be greater than or equal to left");
        Validate.isTrue(top >= bottom, "top must be greater than or equal to bottom");
        boolean changed = !equals(left, top, right, bottom);
        if (changed) {
            super.setLeft(left);
            super.setTop(top);
            super.setRight(right);
            super.setBottom(bottom);
            onChange();
        }
    }

    @Override
    public void set(int left, int top, int right, int bottom) {
        _set(left, top, right, bottom);
    }

    private void _set(@NonNull Rect src) {
        Validate.isTrue(src != null, "source Rect cannot be null");
        Validate.isTrue(src.getRight() >= src.getLeft(),
                "source right must be greater than or equal to source left");
        Validate.isTrue(src.getTop() >= src.getBottom(),
                "source top must be greater than or equal to source bottom");
        boolean changed = !equals(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
        if (changed) {
            super.setLeft(src.getLeft());
            super.setTop(src.getTop());
            super.setRight(src.getRight());
            super.setBottom(src.getBottom());
            onChange();
        }
    }

    @Override
    public void set(@NonNull Rect src) {
        _set(src);
    }

    private void _set(@NonNull Bounds src) {
        Validate.isTrue(src != null, "source Bounds cannot be null");
        boolean changed = !equals(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
        if (changed) {
            super.setLeft(src.getLeft());
            super.setTop(src.getTop());
            super.setRight(src.getRight());
            super.setBottom(src.getBottom());
            onChange();
        }
    }

    public void set(@NonNull Bounds src) {
        _set(src);
    }

    @Override
    public boolean isEmpty() {
        return getLeft() < getRight() && getBottom() < getTop();
    }

    @NonNull
    private Bounds _add(@Nullable Bounds dst, @NonNull Rect r) {
        Validate.isTrue(r != null, "r cannot be null");
        if (dst == null) {
            return new Bounds(r.getLeft(), -r.getTop(), -r.getRight(), r.getBottom());
        }

        dst.set(getLeft() + r.getLeft(),
                getTop() - r.getTop(),
                getRight() - r.getRight(),
                getBottom() + r.getBottom());
        return dst;
    }

    @NonNull
    private Bounds _remove(@Nullable Bounds dst, @NonNull Rect r) {
        Validate.isTrue(r != null, "r cannot be null");
        if (dst == null) {
            return new Bounds(-r.getLeft(), r.getTop(), r.getRight(), -r.getBottom());
        }

        dst.set(getLeft() - r.getLeft(),
                getTop() + r.getTop(),
                getRight() + r.getRight(),
                getBottom() - r.getBottom());
        return dst;
    }

    @Override
    public int getX() {
        return getLeft();
    }

    @Override
    public int getY() {
        return getBottom();
    }

    @Override
    public int getWidth() {
        return getRight() - getLeft();
    }

    @Override
    public int getHeight() {
        return getTop() - getBottom();
    }

    @NonNull
    public final Bounds intersect(@NonNull Bounds b) {
        return intersect(b, this);
    }

    @NonNull
    public final Bounds intersect(@NonNull Bounds b, @Nullable Bounds dst) {
        Validate.isTrue(b != null, "b cannot be null");
        if (dst == null) {
            return new Bounds(Math.max(getLeft(), b.getLeft()),
                    Math.min(getTop(), b.getTop()),
                    Math.min(getRight(), b.getRight()),
                    Math.max(getBottom(), b.getBottom()));
        }

        dst.set(Math.max(getLeft(), b.getLeft()),
                Math.min(getTop(), b.getTop()),
                Math.min(getRight(), b.getRight()),
                Math.max(getBottom(), b.getBottom()));
        return dst;
    }

}
