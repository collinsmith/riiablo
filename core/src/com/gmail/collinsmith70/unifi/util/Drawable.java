package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 *
 */
public abstract class Drawable {

public void draw(Batch batch) {

}

@IntRange(from = 0, to = Integer.MAX_VALUE) private int x;
@IntRange(from = 0, to = Integer.MAX_VALUE) private int y;

@IntRange(from = 0, to = Integer.MAX_VALUE) private int width;
@IntRange(from = 0, to = Integer.MAX_VALUE) private int height;

@IntRange(from = 0, to = Integer.MAX_VALUE) public int getX() { throw new UnsupportedOperationException(); }
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getY() { throw new UnsupportedOperationException(); }
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getWidth() { throw new UnsupportedOperationException(); }
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getHeight() { throw new UnsupportedOperationException(); }

public void setX(@IntRange(from = 0, to = Integer.MAX_VALUE) int x) { throw new UnsupportedOperationException(); }
public void setY(@IntRange(from = 0, to = Integer.MAX_VALUE) int y) { throw new UnsupportedOperationException(); }
public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) { throw new UnsupportedOperationException(); }
public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) { throw new UnsupportedOperationException(); }

@IntRange(from = 0, to = Integer.MAX_VALUE) public int getBottom() {
    return getTop() + getHeight();
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getLeft() {
    return getX();
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getRight() {
    return getLeft() + getWidth();
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getTop() {
    return getY();
}

public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    if (bottom < 0) {
        throw new IllegalArgumentException(
                "bottom must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    if (getY() < bottom) {
        setY(bottom);
    }

    setHeight(bottom - getY());
}
public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    if (left < 0) {
        throw new IllegalArgumentException(
                "left must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    if (getRight() < left) {
        setRight(left);
    }

    setX(left);
}
public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    if (right < 0) {
        throw new IllegalArgumentException(
                "right must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    if (getX() < right) {
        setX(right);
    }

    setWidth(right - getX());
}
public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    if (top < 0) {
        throw new IllegalArgumentException(
                "top must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    if (getBottom() < top) {
        setBottom(top);
    }

    setY(top);
}

public void setBounds(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    if (right < left) {
        throw new IllegalArgumentException("right edge should be larger than the left edge");
    } else if (bottom < top) {
        throw new IllegalArgumentException("bottom edge should be larger than the top edge");
    }

    setX(left);
    setY(top);
    setWidth(right - left);
    setHeight(bottom - top);
}

}
