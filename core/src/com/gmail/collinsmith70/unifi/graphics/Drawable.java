package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;

import org.apache.commons.lang3.Validate;

public abstract class Drawable implements Bounded, Padded {

    @NonNull
    private Bounds bounds = new Bounds() {
        @Override
        protected void onChange() {
            invalidate();
            onBoundsChange(this);
        }
    };

    @NonNull
    private Padding padding = new Padding() {
        @Override
        protected void onChange() {
            invalidate();
            onPaddingChange(this);
        }
    };

    public abstract void draw(@NonNull Canvas canvas);

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    public int getIntrinsicWidth() {
        return -1;
    }

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    public int getIntrinsicHeight() {
        return -1;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getMinimumWidth() {
        return Math.max(0, getIntrinsicWidth());
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getMinimumHeight() {
        return Math.max(0, getIntrinsicHeight());
    }

    public void invalidate() {
    }

    protected void onBoundsChange(@NonNull Bounds bounds) {
    }

    @Override
    @NonNull
    public Bounds getBounds() {
        return bounds;
    }

    @NonNull
    @Override
    public Bounds getBounds(@Nullable Bounds dst) {
        if (dst == null) {
            return new Bounds(bounds);
        }

        dst.set(bounds);
        return dst;
    }

    @Override
    public void setBounds(@NonNull Bounds src) {
        Validate.isTrue(src != null, "source Bounds cannot be null");
        bounds.set(src);
    }

    @Override
    public boolean hasBounds() {
        return bounds.isEmpty();
    }

    protected void onPaddingChange(@NonNull Padding padding) {
    }

    @Override
    @NonNull
    public Padding getPadding() {
        return padding;
    }

    @NonNull
    @Override
    public Padding getPadding(@Nullable Padding dst) {
        if (dst == null) {
            return new Padding(padding);
        }

        dst.set(padding);
        return dst;
    }

    @Override
    public void setPadding(@NonNull Padding src) {
        Validate.isTrue(src != null, "source Padding cannot be null");
        padding.set(src);
    }

    @Override
    public boolean hasPadding() {
        return padding.isEmpty();
    }

}
