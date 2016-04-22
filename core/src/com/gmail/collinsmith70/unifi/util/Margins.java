package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.math.Rect;

import org.apache.commons.lang3.Validate;

public class Margins extends Rect {

    public Margins() {
        this(0, 0, 0, 0);
    }

    public Margins(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                   @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                   @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                   @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        _set(left, top, right, bottom);
    }

    public Margins(@NonNull Rect src) {
        _set(src);
    }

    public Margins(@NonNull Margins src) {
        _set(src);
    }

    protected void onChange() {
    }

    private void _setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
        Validate.isTrue(left >= 0, "left must be greater than or equal to 0");
        super.setLeft(left);
    }

    @Override
    public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
        if (getLeft() != left) {
            _setLeft(left);
            onChange();
        }
    }

    private void _setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
        Validate.isTrue(top >= 0, "top must be greater than or equal to 0");
        super.setTop(top);
    }

    @Override
    public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
        if (getTop() != top) {
            _setTop(top);
            onChange();
        }
    }

    private void _setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
        Validate.isTrue(right >= 0, "right must be greater than or equal to 0");
        super.setRight(right);
    }

    @Override
    public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
        if (getRight() != right) {
            _setRight(right);
            onChange();
        }
    }

    private void _setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        Validate.isTrue(bottom >= 0, "bottom must be greater than or equal to 0");
        super.setBottom(bottom);
    }

    @Override
    public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        if (getBottom() != bottom) {
            _setBottom(bottom);
            onChange();
        }
    }

    private void _set(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        boolean changed = !equals(left, top, right, bottom);
        if (changed) {
            _setLeft(left);
            _setTop(top);
            _setRight(right);
            _setBottom(bottom);
            onChange();
        }
    }

    @Override
    public void set(int left, int top, int right, int bottom) {
        _set(left, top, right, bottom);
    }

    private void _set(@NonNull Rect src) {
        Validate.isTrue(src != null, "source Rect cannot be null");
        _set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
    }

    @Override
    public void set(@NonNull Rect src) {
        _set(src);
    }

    private void _set(@NonNull Margins src) {
        Validate.isTrue(src != null, "source Margins cannot be null");
        _set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
    }

    public void set(@NonNull Margins src) {
        _set(src);
    }

    @Override
    public boolean isEmpty() {
        return getLeft() != 0 || getTop() != 0 || getRight() != 0 || getBottom() != 0;
    }

    @Deprecated
    @Override
    public int getX() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public int getY() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public int getWidth() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public int getHeight() {
        throw new UnsupportedOperationException();
    }

}
