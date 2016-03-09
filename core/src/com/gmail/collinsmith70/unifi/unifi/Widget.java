package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutableBoundary;
import com.gmail.collinsmith70.unifi.unifi.math.Point2D;

public class Widget
        implements Bounded, Paddable, Parentable<WidgetParent>, Translateable {

  @Nullable private WidgetParent parent;

  private int bottom;
  private int left;
  private int right;
  private int top;

  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingBottom;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingLeft;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingRight;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingTop;

  public Widget() {
  }

  /**
   * Bottom position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Bottom of this {@code Widget}, in pixels
   */
  @Override
  public final int getBottom() {
    return bottom;
  }

  /**
   * Sets the bottom position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified bottom is greater than the current {@linkplain #getTop top} of this
   *         {@code Widget}, then the top is set to the same value as the specified bottom.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param bottom Bottom of this {@code Widget}, in pixels.
   */
  @Override
  public final void setBottom(final int bottom) {
    if (getTop() < bottom) {
      this.top = bottom;
    }

    this.bottom = bottom;
  }

  /**
   * Left position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Left of this {@code Widget}, in pixels
   */
  @Override
  public final int getLeft() {
    return left;
  }

  /**
   * Sets the left position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified left is greater than the current {@linkplain #getRight right} of this
   *         {@code Widget}, then the right is set to the same value as the specified left.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param left Left of this {@code Widget}, in pixels.
   */
  @Override
  public final void setLeft(final int left) {
    if (getRight() < left) {
      this.right = left;
    }

    this.left = left;
  }

  /**
   * Right position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Right of this {@code Widget}, in pixels
   */
  @Override
  public final int getRight() {
    return right;
  }

  /**
   * Sets the right position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified right is less than the current {@linkplain #getLeft left} of this
   *         {@code Widget}, then the left is set to the same value as the specified right.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param right Right of this {@code Widget}, in pixels.
   */
  @Override
  public final void setRight(final int right) {
    if (right < getLeft()) {
      this.left = right;
    }

    this.right = right;
  }

  /**
   * Top position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Top of this {@code Widget}, in pixels
   */
  @Override
  public final int getTop() {
    return top;
  }

  /**
   * Sets the top position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified top is less than the current {@linkplain #getBottom bottom} of this
   *         {@code Widget}, then the bottom is set to the same value as the specified top.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param top Top of this {@code Widget}, in pixels.
   */
  @Override
  public final void setTop(final int top) {
    if (top < getBottom()) {
      this.bottom = top;
    }

    this.top = top;
  }

  /**
   * {@link Boundary} containing of the positions of this {@code Widget} relative to its
   * {@linkplain #getParent parent}.
   * <p>
   *   Note: Changing the sides of the returned {@code Boundary} instance will not be reflected
   *         within this {@code Widget}.
   * </p>
   *
   * @return {@code Boundary} containing the positions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Boundary getBoundary() {
    return new Boundary(getLeft(), getTop(), getRight(), getBottom());
  }

  /**
   * Populates the passed {@link Boundary} instance with the positions of this {@code Widget}
   * relative to its {@linkplain #getParent parent}.
   *
   * @param dst {@code Boundary} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getBoundary} were
   *            called.
   *
   * @return {@code Boundary} containing the positions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Boundary getBoundary(@Nullable final Boundary dst) {
    if (dst == null) {
      return getBoundary();
    }

    dst.set(getLeft(), getTop(), getRight(), getBottom());
    return dst;
  }

  /**
   * Sets the positions of all sides of this {@code Widget} relative to its
   * {@linkplain #getParent parent}.
   * <p>
   *   Precondition: {@code left <= right AND bottom <= top}
   * </p>
   *
   * @param left   Left of this {@code Widget}, in pixels.
   * @param top    Top of this {@code Widget}, in pixels.
   * @param right  Right of this {@code Widget}, in pixels.
   * @param bottom Bottom of this {@code Widget}, in pixels.
   */
  @Override
  public final void setBoundary(final int left, final int top, final int right, final int bottom) {
    if (right < left) {
      throw new IllegalArgumentException("left <= right");
    } else if (top < bottom) {
      throw new IllegalArgumentException("bottom <= top");
    }

    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  /**
   * Sets the positions of all sides of this {@code Widget} relative to its
   * {@linkplain #getParent parent} from the specified source {@link Boundary}.
   * <p>
   *   Precondition: {@code src.getLeft() <= src.getRight() AND src.getBottom() <= src.getTop()}
   * </p>
   *
   * @param src {@code Boundary} to set the side positions of this {@code Widget} to
   */
  @Override
  public final void setBoundary(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src cannot be null");
    }

    setBoundary(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  /**
   * Width of this {@code Widget}
   *
   * @return Width of this {@code Widget}, in pixels
   */
  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getWidth() {
    return getRight() - getLeft();
  }

  /**
   * Height of this {@code Widget}
   *
   * @return Height of this {@code Widget}, in pixels
   */
  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getHeight() {
    return getTop() - getBottom();
  }

  /**
   * {@link Dimension2D} containing the {@linkplain #getWidth width} and
   * {@linkplain #getHeight height} dimensions of this {@code Widget}.
   *
   * @return {@code Dimension2D} containing the dimensions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Dimension2D getSize() {
    return new Dimension2D(getWidth(), getHeight());
  }

  /**
   * Populates the passed {@link Dimension2D} instance with the {@linkplain #getWidth width} and
   * {@linkplain #getHeight height} dimensions of this {@code Widget}.
   *
   * @param dst {@code Dimension2D} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getSize} were
   *            called.
   *
   * @return {@code Dimension2D} containing the dimensions of this {@code Widget}
   */
  @NonNull
  @Override
  public Dimension2D getSize(@Nullable final Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(getWidth(), getHeight());
    return dst;
  }

}
