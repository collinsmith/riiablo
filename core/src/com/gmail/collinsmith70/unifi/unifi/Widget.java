package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutableBoundary;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutableDimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutablePoint2D;
import com.gmail.collinsmith70.unifi.unifi.math.Point2D;

public class Widget
        implements Bounded {

@Nullable private WidgetParent parent;
@Nullable public WidgetParent getParent() {
    return parent;
}
public void setParent(@Nullable WidgetParent parent) {
    this.parent = parent;
}
public boolean hasParent() {
    return getParent() != null;
}

private Point2D translation;

public int getTranslationX() {
    return translation.getX();
}
public int getTranslationY() {
    return translation.getY();
}

public void setTranslationX(int x) {
    translation.setX(x);
}
public void setTranslationY(int y) {
    translation.setY(y);
}

private Boundary boundary;

@Override public final int getBottom() {
    return boundary.getBottom();
}
@Override public final int getLeft() {
    return boundary.getLeft();
}
@Override public final int getRight() {
    return boundary.getRight();
}
@Override public final int getTop() {
    return boundary.getTop();
}

@Override public final void setBottom(int bottom) {
    if (getTop() < bottom) {
        boundary.setTop(bottom);
    }

    boundary.setBottom(bottom);
}
@Override public final void setLeft(int left) {
    if (getRight() < left) {
        boundary.setRight(left);
    }

    boundary.setLeft(left);
}
@Override public final void setRight(int right) {
    if (getLeft() > right) {
        boundary.setLeft(right);
    }

    boundary.setRight(right);
}
@Override public final void setTop(int top) {
    if (getBottom() > top) {
        boundary.setBottom(top);
    }

    boundary.setTop(top);
}

@Override public final void setBounds(int left, int right, int top, int bottom) {
    if (right < left) {
        throw new IllegalArgumentException("right should be greater than or equal to left");
    } else if (top < bottom) {
        throw new IllegalArgumentException("top should be greater than or equal to bottom");
    }

    boundary.set(left, right, top, bottom);
}
@Override @NonNull public final ImmutableBoundary getBounds() {
    return new ImmutableBoundary(boundary);
}
@Override public final boolean contains(int x, int y) {
    x -= getX();
    y -= getY();
    return 0 <= x && x <= getWidth()
        && 0 <= y && y <= getHeight();
}

@Override public final void setPosition(int x, int y) {
    setX(x);
    setY(y);
}
@Override @NonNull public final ImmutablePoint2D getPosition() {
    return new ImmutablePoint2D(getX(), getY());
}

@Override public final void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                                    @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    setWidth(width);
    setHeight(height);
}
@Override @NonNull public final ImmutableDimension2D getSize() {
    return new ImmutableDimension2D(getWidth(), getHeight());
}
@Override public final boolean hasSize() {
    return !boundary.isEmpty();
}

@Override public int getX() {
    return getLeft() + getTranslationX();
}
@Override public int getY() {
    return getBottom() + getTranslationY();
}
@Override public int getWidth() {
    return getRight() - getLeft();
}
@Override public int getHeight() {
    return getTop() - getBottom();
}

@Override public void setX(int x) {
    setTranslationX(x - getLeft());
}
@Override public void setY(int y) {
    setTranslationY(y - getBottom());
}
@Override public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
        throw new IllegalArgumentException("width should be greater than or equal to 0");
    }

    boundary.setRight(getLeft() + width);
}
@Override public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
        throw new IllegalArgumentException("height should be greater than or equal to 0");
    }

    boundary.setTop(getBottom() + height);
}

}
