package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;

/**
 * Interface representing methods required to form a {@code Bounded}. A {@code Bounded} is an
 * object who has well-defined {@linkplain #getLeft() left}, {@linkplain #getRight() right},
 * {@linkplain #getTop() top} and {@linkplain #getBottom() bottom} sides.
 */
public interface Bounded {

  /**
   * @return distance from the bottom of the screen to the bottom side of this {@code Bounded}
   * @see #setBottom(int)
   * @see #setBounds(int, int, int, int)
   * @see #getY()
   * @see #setY(int)
   */
  int getBottom();

  /**
   * @return distance from the left of the screen to the left side of this {@code Bounded}
   * @see #setLeft(int)
   * @see #setBounds(int, int, int, int)
   * @see #getX()
   * @see #setX(int)
   */
  int getLeft();

  /**
   * @return distance from the left of the screen to the right side of this {@code Bounded}
   * @see #setRight(int)
   * @see #setBounds(int, int, int, int)
   * @see #setWidth(int)
   */
  int getRight();

  /**
   * @return distance from the bottom of the screen to the top side of this {@code Bounded}
   * @see #setTop(int)
   * @see #setBounds(int, int, int, int)
   * @see #setHeight(int)
   */
  int getTop();

  /**
   * @param bottom distance from the bottom of the screen to the bottom side of this {@code Bounded}
   * @see #getBottom()
   * @see #setBounds(int, int, int, int)
   * @see #getY()
   * @see #setY(int)
   */
  void setBottom(int bottom);

  /**
   * @param left distance from the left of the screen to the left side of this {@code Bounded}
   * @see #getLeft()
   * @see #setBounds(int, int, int, int)
   * @see #getX()
   * @see #setX(int)
   */
  void setLeft(int left);

  /**
   * @param right distance from the left of the screen to the right side of this {@code Bounded}
   * @see #getRight()
   * @see #setBounds(int, int, int, int)
   * @see #setWidth(int)
   */
  void setRight(int right);

  /**
   * @param top distance from the bottom of the screen to the top side of this {@code Bounded}
   * @see #getTop()
   * @see #setBounds(int, int, int, int)
   * @see #setHeight(int)
   */
  void setTop(int top);

  /**
   * Changes the position (i.e., translates) of this {@code Bounded} such that its area does not
   * change and its bottom side is located at the given {@linkplain #getBottom() bottom}.
   * <p>
   * Note: This operation only translates this {@code Bounded} on the y-axis
   * </p>
   *
   * @param bottom distance from the bottom of the screen to the bottom side of this {@code Bounded}
   * @see #setY(int)
   */
  void moveBottom(int bottom);

  /**
   * Changes the position (i.e., translates) of this {@code Bounded} such that its area does not
   * change and its left side is located at the given {@linkplain #getLeft() left}.
   * <p>
   * Note: This operation only translates this {@code Bounded} on the x-axis
   * </p>
   *
   * @param left distance from the left of the screen to the left side of this {@code Bounded}
   * @see #setX(int)
   */
  void moveLeft(int left);

  /**
   * Changes the position (i.e., translates) of this {@code Bounded} such that its area does not
   * change and its right side is located at the given {@linkplain #getRight() right}.
   * <p>
   * Note: This operation only translates this {@code Bounded} on the x-axis
   * </p>
   *
   * @param right distance from the left of the screen to the right side of this {@code Bounded}
   */
  void moveRight(int right);

  /**
   * Changes the position (i.e., translates) of this {@code Bounded} such that its area does not
   * change and its top side is located at the given {@linkplain #getTop() top}.
   * <p>
   * Note: This operation only translates this {@code Bounded} on the y-axis
   * </p>
   *
   * @param top distance from the bottom of the screen to the top side of this {@code Bounded}
   */
  void moveTop(int top);

  /**
   * @return x-axis location of this {@code Bounded} (in pixels), given as the bottom left coordinate
   * @see #setX(int)
   * @see #getLeft()
   * @see #setLeft(int)
   * @see #setPosition(int, int)
   */
  int getX();

  /**
   * @return y-axis location of this {@code Bounded} (in pixels), given as the bottom left coordinate
   * @see #setY(int)
   * @see #getBottom()
   * @see #setBottom(int)
   * @see #setPosition(int, int)
   */
  int getY();

  /**
   * @return width of this {@code Bounded}
   * @see #setWidth(int)
   * @see #hasSize()
   * @see #setSize(int, int)
   */
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getWidth();

  /**
   * @return height of this {@code Bounded}
   * @see #setHeight(int)
   * @see #hasSize()
   * @see #setSize(int, int)
   */
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getHeight();

  /**
   * Translates the location of this {@code Bounded} so that its bottom left coordinate is at the
   * specified point on the x-axis.
   * <p>
   * Note: This operation does not change the area (i.e., width or height) of this {@code Bounded}
   * </p>
   *
   * @param x x-axis location of this {@code Bounded}
   * @see #getX()
   * @see #setLeft(int)
   * @see #setPosition(int, int)
   */
  void setX(int x);

  /**
   * Translates the location of this {@code Bounded} so that its bottom left coordinate is at the
   * specified point on the y-axis.
   * <p>
   * Note: This operation does not change the area (i.e., width or height) of this {@code Bounded}
   * </p>
   *
   * @param y y-axis location of this {@code Bounded}
   * @see #getY()
   * @see #setBottom(int)
   * @see #setPosition(int, int)
   */
  void setY(int y);

  /**
   * Adjusts the width of this {@code Bounded} to the specified value.
   * <p>
   * Note: This operation will adjust the width by moving the {@linkplain #getRight() right} side of
   * the boundary.
   * </p>
   *
   * @param width width of this {@code Bounded}
   * @see #getWidth()
   * @see #hasSize()
   * @see #setSize(int, int)
   */
  void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width);

  /**
   * Adjusts the height of this {@code Bounded} to the specified value.
   * <p>
   * Note: This operation will adjust the height by moving the {@linkplain #getTop() top} side of
   * the boundary.
   * </p>
   *
   * @param height height of this {@code Bounded}
   * @see #getHeight()
   * @see #hasSize()
   * @see #setSize(int, int)
   */
  void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height);

  /**
   * Sets the bounds of this {@code Bounded} to the specified coordinates.
   * <p>
   * Note: The right side must be greater than or equal to the left side, and the top side must be
   * greater than or equal to the bottom side.
   * </p>
   *
   * @param left   distance from the left of the screen to the left side of this {@code Bounded}
   * @param right  distance from the left of the screen to the right side of this {@code Bounded}
   * @param top    distance from the bottom of the screen to the top side of this {@code Bounded}
   * @param bottom distance from the bottom of the screen to the bottom side of this {@code Bounded}
   * @see #setBottom(int)
   * @see #setLeft(int)
   * @see #setRight(int)
   * @see #setTop(int)
   */
  void setBounds(int left, int right, int top, int bottom);

  /**
   * @param x x-coordinate of the test point
   * @param y y-coordinate of the test point
   * @return {@code true} if the specified point lies within the bounds of this {@code Bounded},
   * otherwise {@code false}
   * @see #setBounds(int, int, int, int)
   */
  boolean inBounds(int x, int y);

  /**
   * @return {@code true} if the area of this {@code Bounded} is non-zero, otherwise {@code false}
   * @see #setSize(int, int)
   */
  boolean hasSize();

  /**
   * Translates the position of this {@code Bounded} to the specified location.
   * <p>
   * Note: The position of this {@code Bounded} should be specified as the bottom left point of its
   * bounds box.
   * </p>
   *
   * @param x x-coordinate of the position
   * @param y y-coordinate of the position
   * @see #setX(int)
   * @see #setY(int)
   */
  void setPosition(int x, int y);

  /**
   * Changes the size of this {@code Bounded} by adjusting its {@linkplain #getWidth() width} and
   * {@linkplain #getHeight() height}.
   *
   * @param width  width of this {@code Bounded}
   * @param height height of this {@code Bounded}
   * @see #setWidth(int)
   * @see #setHeight(int)
   */
  void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
               @IntRange(from = 0, to = Integer.MAX_VALUE) int height);

}
