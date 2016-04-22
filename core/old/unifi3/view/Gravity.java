package com.gmail.collinsmith70.unifi3.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi3.math.Rectangle;

public class Gravity {

  public static final int NO_GRAVITY       = 0;
  public static final int AXIS_SPECIFIED   = 1 << 0;
  public static final int AXIS_PULL_BEFORE = 1 << 1;
  public static final int AXIS_PULL_AFTER  = 1 << 2;
  public static final int AXIS_CLIP        = 1 << 3;

  public static final int AXIS_X_SHIFT = 0;
  public static final int AXIS_Y_SHIFT = 4;

  public static final int NONE = NO_GRAVITY;
  public static final int LEFT = (AXIS_PULL_BEFORE | AXIS_SPECIFIED) << AXIS_X_SHIFT;
  public static final int RIGHT = (AXIS_PULL_AFTER | AXIS_SPECIFIED) << AXIS_X_SHIFT;
  public static final int TOP = (AXIS_PULL_BEFORE | AXIS_SPECIFIED) << AXIS_Y_SHIFT;
  public static final int BOTTOM = (AXIS_PULL_AFTER | AXIS_SPECIFIED) << AXIS_Y_SHIFT;

  public static final int CENTER_HORIZONTAL = AXIS_SPECIFIED << AXIS_X_SHIFT;
  public static final int CENTER_VERTICAL = AXIS_SPECIFIED << AXIS_Y_SHIFT;
  public static final int CENTER = CENTER_VERTICAL | CENTER_HORIZONTAL;

  public static final int FILL_HORIZONTAL = LEFT | RIGHT;
  public static final int FILL_VERTICAL = TOP | BOTTOM;
  public static final int FILL = FILL_VERTICAL | FILL_HORIZONTAL;

  public static final int CLIP_VERTICAL = AXIS_CLIP << AXIS_Y_SHIFT;
  public static final int CLIP_HORIZONTAL = AXIS_CLIP << AXIS_X_SHIFT;

  public static final int HORIZONTAL_GRAVITY_MASK
          = (AXIS_SPECIFIED | AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_X_SHIFT;
  public static final int VERTICAL_GRAVITY_MASK
          = (AXIS_SPECIFIED | AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_Y_SHIFT;

  public static void apply(int gravity,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int height,
                           @NonNull Rectangle container,
                           @NonNull Rectangle dst) {
    apply(gravity, width, height, container, 0, 0, dst);
  }

  public static void apply(int gravity,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int height,
                           @NonNull Rectangle container,
                           int xAdj,
                           int yAdj,
                           @NonNull Rectangle dst) {
    switch (gravity & ((AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_X_SHIFT)) {
      case NO_GRAVITY:
        dst.setLeft(container.getLeft()
                + ((container.getRight() - container.getLeft() - width) / 2) + xAdj);
        dst.setRight(dst.getLeft() + width);
        if ((gravity & (AXIS_CLIP << AXIS_X_SHIFT)) == (AXIS_CLIP << AXIS_X_SHIFT)) {
          if (dst.getLeft() < container.getLeft()) {
            dst.setLeft(container.getLeft());
          }

          if (dst.getRight() > container.getRight()) {
            dst.setRight(container.getRight());
          }
        }

        break;
      case AXIS_PULL_BEFORE << AXIS_X_SHIFT:
        dst.setLeft(container.getLeft() + xAdj);
        dst.setRight(dst.getLeft() + width);
        if ((gravity & (AXIS_CLIP << AXIS_X_SHIFT)) == (AXIS_CLIP << AXIS_X_SHIFT)) {
          if (dst.getRight() > container.getRight()) {
            dst.setRight(container.getRight());
          }
        }

        break;
      case AXIS_PULL_AFTER << AXIS_X_SHIFT:
        dst.setRight(container.getRight() - xAdj);
        dst.setLeft(dst.getRight() - width);
        if ((gravity & (AXIS_CLIP << AXIS_X_SHIFT)) == (AXIS_CLIP << AXIS_X_SHIFT)) {
          if (dst.getLeft() < container.getLeft()) {
            dst.setLeft(container.getLeft());
          }
        }

        break;
      default:
        dst.setLeft(container.getLeft() + xAdj);
        dst.setRight(container.getRight() + xAdj);
        break;
    }

    switch (gravity & ((AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_Y_SHIFT)) {
      case NO_GRAVITY:
        dst.setBottom(container.getBottom()
                + ((container.getTop() - container.getBottom() - height) / 2) + yAdj);
        dst.setTop(dst.getBottom() + height);
        if ((gravity & (AXIS_CLIP << AXIS_Y_SHIFT)) == (AXIS_CLIP << AXIS_Y_SHIFT)) {
          if (dst.getBottom() < container.getBottom()) {
            dst.setBottom(container.getBottom());
          }

          if (dst.getTop() > container.getTop()) {
            dst.setTop(container.getTop());
          }
        }

        break;
      case AXIS_PULL_BEFORE << AXIS_Y_SHIFT:
        dst.setBottom(container.getBottom() + yAdj);
        dst.setTop(dst.getBottom() + height);
        if ((gravity & (AXIS_CLIP << AXIS_Y_SHIFT)) == (AXIS_CLIP << AXIS_Y_SHIFT)) {
          if (dst.getTop() > container.getTop()) {
            dst.setTop(container.getTop());
          }
        }

        break;
      case AXIS_PULL_AFTER << AXIS_Y_SHIFT:
        dst.setTop(container.getTop() - yAdj);
        dst.setBottom(dst.getTop() - height);
        if ((gravity & (AXIS_CLIP << AXIS_Y_SHIFT)) == (AXIS_CLIP << AXIS_Y_SHIFT)) {
          if (dst.getBottom() < container.getBottom()) {
            dst.setBottom(container.getBottom());
          }
        }

        break;
      default:
        dst.setBottom(container.getBottom() + yAdj);
        dst.setTop(container.getTop() + yAdj);
        break;
    }
  }

  public static boolean isVertical(int gravity) {
    return gravity > 0 && (gravity & VERTICAL_GRAVITY_MASK) != 0;
  }

  public static boolean isHorizontal(int gravity) {
    return gravity > 0 && (gravity & HORIZONTAL_GRAVITY_MASK) != 0;
  }

}
