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

@IntRange(from = 0, to = Integer.MAX_VALUE) public int getX() {
    return x;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getY() {
    return y;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getWidth() {
    return width;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getHeight() {
    return height;
}

public void setX(@IntRange(from = 0, to = Integer.MAX_VALUE) int x) {
    if (x < 0) {
        throw new IllegalArgumentException(
                "x must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.x = x;
}
public void setY(@IntRange(from = 0, to = Integer.MAX_VALUE) int y) {
    if (y < 0) {
        throw new IllegalArgumentException(
                "y must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.y = y;
}
public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
        throw new IllegalArgumentException(
                "width must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.width = width;
}
public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
        throw new IllegalArgumentException(
                "height must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.height = height;
}

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
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                      @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
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

public boolean hasSize() {
    return getTop() != getBottom()
            && getLeft() != getRight();
}

@IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingBottom;
@IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingLeft;
@IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingRight;
@IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingTop;

@IntRange(from = 0, to = Integer.MAX_VALUE) public int getPaddingBottom() {
    return paddingBottom;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getPaddingLeft() {
    return paddingLeft;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getPaddingRight() {
    return paddingRight;
}
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getPaddingTop() {
    return paddingTop;
}

public void setPaddingBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int paddingBottom) {
    if (paddingBottom < 0) {
        throw new IllegalArgumentException(
                "paddingBottom must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.paddingBottom = paddingBottom;
}
public void setPaddingLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int paddingLeft) {
    if (paddingLeft < 0) {
        throw new IllegalArgumentException(
                "paddingLeft must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.paddingBottom = paddingLeft;
}
public void setPaddingRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int paddingRight) {
    if (paddingRight < 0) {
        throw new IllegalArgumentException(
                "paddingRight must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.paddingBottom = paddingRight;
}
public void setPaddingTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int paddingTop) {
    if (paddingTop < 0) {
        throw new IllegalArgumentException(
                "paddingTop must be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.paddingBottom = paddingTop;
}

public void setPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    setPaddingLeft(left);
    setPaddingRight(right);
    setPaddingTop(top);
    setPaddingBottom(bottom);
}

public boolean hasPadding() {
    return getPaddingBottom() > 0 && getPaddingLeft() > 0
            && getPaddingRight() > 0 && getPaddingTop() > 0;
}

}
