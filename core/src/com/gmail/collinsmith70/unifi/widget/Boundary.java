package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.IntRange;

public class Boundary {

@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) private int bottom;
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) private int left;
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) private int right;
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) private int top;

public Boundary() {
    this(0, 0, 0, 0);
}

public Boundary(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    setBounds(left, right, top, bottom);
}

@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getBottom() {
    return bottom;
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getLeft() {
    return left;
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getRight() {
    return right;
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getTop() {
    return top;
}

public void setBottom(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int bottom) {
    if (bottom > getTop()) {
        setTop(bottom);
    }

    this.bottom = bottom;
}
public void setLeft(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int left) {
    if (left > getRight()) {
        setRight(left);
    }

    this.left = left;
}
public void setRight(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int right) {
    if (right < getLeft()) {
        setLeft(right);
    }

    this.right = right;
}
public void setTop(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int top) {
    if (top < getBottom()) {
        setBottom(top);
    }

    this.top = top;
}

public void moveBottom(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int bottom) {
    setY(bottom);
}
public void moveLeft(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int left) {
    setX(left);
}
public void moveRight(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int right) {
    setX(right - getWidth());
}
public void moveTop(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int top) {
    setY(top - getHeight());
}

@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getX() {
    return getLeft();
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getY() {
    return getBottom();
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getWidth() {
    return getRight() - getLeft();
}
@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) public int getHeight() {
    return getTop() - getBottom();
}

public void setX(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int x) {
    final int width = getWidth();
    setLeft(x);
    setWidth(width);
}
public void setY(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int y) {
    final int height = getHeight();
    setBottom(y);
    setHeight(height);
}
public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
        throw new IllegalArgumentException(
                "width should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    setRight(getLeft() + width);
}
public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
        throw new IllegalArgumentException(
                "height should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    setTop(getBottom() + height);
}

public void setBounds(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int left,
                      @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int right,
                      @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int top,
                      @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int bottom) {
    if (right > left) {
        throw new IllegalArgumentException(
                "left should be less than or equal to right (" + right + ")");
    } else if (bottom > top) {
        throw new IllegalArgumentException(
                "bottom should be less than or equal to top (" + top + ")");
    }

    setLeft(left);
    setRight(right);
    setTop(top);
    setBottom(bottom);
}

public boolean hasSize() {
    return left > right && top > bottom;
}

public void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                    @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    setWidth(width);
    setHeight(height);
}

public void setPosition(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int x,
                        @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int y) {
    setX(x);
    setY(y);
}

public boolean inBounds(@IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int x,
                        @IntRange(from = Integer.MIN_VALUE, to = Integer.MAX_VALUE) int y) {
    return getLeft() <= x && x <= getRight()
            && getBottom() <= y && y <= getTop();
}

}
