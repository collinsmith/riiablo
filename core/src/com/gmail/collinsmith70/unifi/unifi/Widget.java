package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.IntPoint2D;

public class Widget
        implements Bounded, Paddable, Parentable<WidgetParent>, Translateable {

  @Nullable
  private WidgetParent parent;

  protected int bottom;
  protected int left;
  protected int right;
  protected int top;

  protected int paddingBottom;
  protected int paddingLeft;
  protected int paddingRight;
  protected int paddingTop;

  public Widget() {

  }

  @Override
  @Nullable
  public WidgetParent getParent() {
    return parent;
  }

  final void setParent(@Nullable final WidgetParent parent) {
    this.parent = parent;
  }

  @Override
  public boolean hasParent() {
    return getParent() != null;
  }

  public int getTranslationX() {
    throw new UnsupportedOperationException();
  }

  public void setTranslationX(int x) {
    throw new UnsupportedOperationException();
  }

  public int getTranslationY() {
    throw new UnsupportedOperationException();
  }

  public void setTranslationY(int y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTranslation(int x, int y) {
    throw new UnsupportedOperationException();
  }

  public void setTranslation(@NonNull final IntPoint2D src) {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public IntPoint2D getTranslation() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public IntPoint2D getTranslation(@Nullable IntPoint2D dst) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final int getBottom() {
    return bottom;
  }

  @Override
  public final int getLeft() {
    return left;
  }

  @Override
  public final int getRight() {
    return right;
  }

  @Override
  public final int getTop() {
    return top;
  }

  @Override
  public final void setBottom(int bottom) {
    if (getTop() < bottom) {
      this.top = bottom;
    }

    this.bottom = bottom;
  }

  @Override
  public final void setLeft(int left) {
    if (getRight() < left) {
      this.right = left;
    }

    this.left = left;
  }

  @Override
  public final void setRight(int right) {
    if (getLeft() > right) {
      this.left = right;
    }

    this.right = right;
  }

  @Override
  public final void setTop(int top) {
    if (getBottom() > top) {
      this.bottom = top;
    }

    this.top = top;
  }

  @Override
  public final void setBoundary(int left, int top, int right, int bottom) {
    if (right < left) {
      throw new IllegalArgumentException("right should be greater than or equal to left");
    } else if (top < bottom) {
      throw new IllegalArgumentException("top should be greater than or equal to bottom");
    }

    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  @Override
  public final void setBoundary(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src boundary cannot be null");
    }

    setBoundary(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @Override
  @NonNull
  public final Boundary getBoundary() {
    return new Boundary(left, top, right, bottom);
  }

  @NonNull
  @Override
  public final Boundary getBoundary(@Nullable Boundary dst) {
    if (dst == null) {
      return getBoundary();
    }

    dst.set(left, top, right, bottom);
    return dst;
  }

  @Override
  public final boolean contains(int x, int y) {
    x -= getX();
    y -= getY();
    return 0 <= x && x <= getWidth()
            && 0 <= y && y <= getHeight();
  }

  @Override
  public final void setPosition(int x, int y) {
    setX(x);
    setY(y);
  }

  @Override
  public final void setPosition(@NonNull final IntPoint2D src) {
    if (src == null) {
      throw new IllegalArgumentException("src point cannot be null");
    }

    setPosition(src.getX(), src.getY());
  }

  @Override
  @NonNull
  public final IntPoint2D getPosition() {
    return new IntPoint2D(getX(), getY());
  }

  @NonNull
  @Override
  public final IntPoint2D getPosition(@Nullable IntPoint2D dst) {
    if (dst == null) {
      return getPosition();
    }

    dst.set(getX(), getY());
    return dst;
  }

  @Override
  public final void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    setWidth(width);
    setHeight(height);
  }

  @Override
  public final void setSize(@NonNull final Dimension2D src) {
    if (src == null) {
      throw new IllegalArgumentException("src dimension cannot be null");
    }

    setSize(src.getWidth(), src.getHeight());
  }

  @Override
  @NonNull
  public final Dimension2D getSize() {
    return new Dimension2D(getWidth(), getHeight());
  }

  @NonNull
  @Override
  public final Dimension2D getSize(@Nullable Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(getWidth(), getHeight());
    return dst;
  }

  @Override
  public final boolean hasSize() {
    return left != right && top != bottom;
  }

  @Override
  public int getX() {
    return left + getTranslationX();
  }

  @Override
  public int getY() {
    return bottom + getTranslationY();
  }

  @Override
  public int getWidth() {
    return right - left;
  }

  @Override
  public int getHeight() {
    return top - bottom;
  }

  @Override
  public void setX(int x) {
    setTranslationX(x - left);
  }

  @Override
  public void setY(int y) {
    setTranslationY(y - bottom);
  }

  @Override
  public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
      throw new IllegalArgumentException("width should be greater than or equal to 0");
    }

    this.right = left + width;
  }

  @Override
  public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
      throw new IllegalArgumentException("height should be greater than or equal to 0");
    }

    this.top = bottom + height;
  }

  @Override
  public int getPaddingBottom() {
    return paddingBottom;
  }

  @Override
  public int getPaddingLeft() {
    return paddingLeft;
  }

  @Override
  public int getPaddingRight() {
    return paddingRight;
  }

  @Override
  public int getPaddingTop() {
    return paddingTop;
  }

  @Override
  public void setPaddingBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    if (bottom < 0) {
      throw new IllegalArgumentException("bottom padding should be a natural number");
    }

    this.paddingBottom = bottom;
  }

  @Override
  public void setPaddingLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    if (left < 0) {
      throw new IllegalArgumentException("left padding should be a natural number");
    }

    this.paddingLeft = left;
  }

  @Override
  public void setPaddingRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    if (right < 0) {
      throw new IllegalArgumentException("right padding should be a natural number");
    }

    this.paddingRight = right;
  }

  @Override
  public void setPaddingTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    if (top < 0) {
      throw new IllegalArgumentException("top padding should be a natural number");
    }

    this.paddingTop = top;
  }

  @Override
  public void setPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                         @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                         @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                         @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    setPaddingBottom(bottom);
    setPaddingLeft(left);
    setPaddingRight(right);
    setPaddingTop(top);
  }

  @Override
  public void setPadding(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src padding cannot be null");
    }

    setPadding(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @Override
  @NonNull
  public Boundary getPadding() {
    return new Boundary(paddingLeft, paddingTop, paddingRight, paddingBottom);
  }

  @NonNull
  @Override
  public Boundary getPadding(@Nullable Boundary dst) {
    if (dst == null) {
      return getPadding();
    }

    dst.set(paddingLeft, paddingTop, paddingRight, paddingBottom);
    return dst;
  }

  @Override
  public boolean hasPadding() {
    return paddingLeft > 0 || paddingRight > 0
            || paddingBottom > 0 || paddingTop > 0;
  }

}
