package com.gmail.collinsmith70.unifi.unifi.layout;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.unifi.WidgetGroup;

public abstract class LinearLayout extends WidgetGroup {

  /**
   * Enumeration of all {@code Direction} constants, determining the order which a
   * {@code LinearLayout} is laid out.
   */
  public enum Direction {
    /**
     * Lays out the children of this {@code LinearLayout} starting with the first child and ending
     * with the last.
     */
    START_TO_END,

    /**
     * Lays out the children of this {@code LinearLayout} starting with the last child and ending
     * with the first.
     */
    END_TO_START;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int spacing;

  @NonNull
  private Direction direction;

  /**
   * Spacing between the children of this {@code LinearLayout}.
   *
   * @return Spacing between children of this {@code LinearLayout}, in pixels
   */
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getSpacing() {
    return spacing;
  }

  /**
   * Sets the spacing between the children of this {@code LinearLayout}.
   *
   * @param spacing Spacing between children of this {@code LinearLayout}, in pixels
   */
  public void setSpacing(@IntRange(from = 0, to = Integer.MAX_VALUE) int spacing) {
    if (spacing < 0) {
      throw new IllegalArgumentException("spacing must be greater than or equal to 0");
    }

    this.spacing = spacing;
  }

  /**
   * {@link Direction} which this {@code LinearLayout} is laid out.
   *
   * @return {@code Direction} corresponding to the order which this {@code LinearLayout} is laid
   * out.
   */
  @NonNull
  public Direction getDirection() {
    if (direction == null) {
      return Direction.START_TO_END;
    }

    return direction;
  }

  /**
   * Sets the {@link Direction} which this {@code LinearLayout} is laid out.
   *
   * @param direction {@code Direction} corresponding to the order which this {@code LinearLayout}
   *                  is to be laid out.
   */
  public void setDirection(@NonNull final Direction direction) {
    if (direction == null) {
      throw new IllegalArgumentException("direction cannot be null");
    }

    this.direction = direction;
  }
}
