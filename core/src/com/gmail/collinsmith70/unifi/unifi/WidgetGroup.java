package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class WidgetGroup extends Widget
        implements WidgetParent, Marginable {

  /**
   * Gravity flag for top/up on the {@code y}-axis
   */
  public static final int TOP = 1 << 0;

  /**
   * Gravity flag for bottom/down on the {@code y}-axis
   */
  public static final int BOTTOM = 1 << 1;

  /**
   * Gravity flag for left on the {@code x}-axis
   */
  public static final int LEFT = 1 << 2;

  /**
   * Gravity flag for right on the {@code x}-axis
   */
  public static final int RIGHT = 1 << 3;

  /**
   * Gravity flag for vertical center (middle of {@linkplain #TOP top} and
   * {@linkplain #BOTTOM bottom) on the {@code y}-axis
   */
  public static final int CENTER_VERTICAL = 1 << 4;

  /**
   * Gravity flag for horizontal center (middle of {@linkplain #LEFT left} and
   * {@linkplain #RIGHT right)) on the {@code x}-axis
   */
  public static final int CENTER_HORIZONTAL = 1 << 5;

  /**
   * Gravity constant representing both {@linkplain #CENTER_VERTICAL vertical} and
   * {@linkplain #CENTER_HORIZONTAL horizontal} centers on the {@code x}- and {@code y}-axes
   */
  public static final int CENTER = CENTER_HORIZONTAL | CENTER_VERTICAL;

  /**
   * Annotation used to mark integers which are {@code WidgetGroup} gravity constants.
   */
  @IntDef(flag = true, value = { TOP, BOTTOM, LEFT, RIGHT, CENTER_VERTICAL, CENTER_HORIZONTAL })
  public @interface Gravity {}

  @NonNull private final Collection<Widget> children;

  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginBottom;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginLeft;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginRight;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginTop;

  @Gravity private int gravity;

  public WidgetGroup() {
    this.children = new ArrayList<Widget>();
  }

  /**
   * Gravity of this {@code WidgetGroup}, controlling how children are aligned.
   *
   * @return Gravity of this {@code WidgetGroup}
   */
  @Gravity
  public int getGravity() {
    return gravity;
  }

  /**
   * Sets the gravity of this {@code WidgetGroup} which controls how child {@link Widget} instances
   * are aligned.
   *
   * @param gravity Gravity of this {@code WidgetGroup}
   */
  public void setGravity(@Gravity final int gravity) {
    this.gravity = gravity;
  }

  /**
   * Bottom margin of this {@code WidgetGroup}. Margin is defined as the space outside of this
   * {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @return Bottom margin, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginBottom() {
    return marginBottom;
  }

  /**
   * Sets the bottom margin of this {@code WidgetGroup}. Margin is defined as the space outside of
   * this {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside
   * edge).
   *
   * @param marginBottom Bottom margin, in pixels
   */
  @Override
  @CallSuper
  public void setMarginBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginBottom) {
    if (marginBottom < 0) {
      throw new IllegalArgumentException("bottom margin should be positive");
    }

    this.marginBottom = marginBottom;
  }

  /**
   * Left margin of this {@code WidgetGroup}. Margin is defined as the space outside of this
   * {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @return Left margin, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginLeft() {
    return marginLeft;
  }

  /**
   * Sets the left margin of this {@code WidgetGroup}. Margin is defined as the space outside of
   * this {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside
   * edge).
   *
   * @param marginLeft Left margin, in pixels
   */
  @Override
  @CallSuper
  public void setMarginLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginLeft) {
    if (marginLeft < 0) {
      throw new IllegalArgumentException("left margin should be positive");
    }

    this.marginLeft = marginLeft;
  }

  /**
   * Right margin of this {@code WidgetGroup}. Margin is defined as the space outside of this
   * {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @return Right margin, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginRight() {
    return marginRight;
  }

  /**
   * Sets the right margin of this {@code WidgetGroup}. Margin is defined as the space outside of
   * this {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside
   * edge).
   *
   * @param marginRight Right margin, in pixels
   */
  @Override
  @CallSuper
  public void setMarginRight(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginRight) {
    if (marginRight < 0) {
      throw new IllegalArgumentException("right margin should be positive");
    }

    this.marginRight = marginRight;
  }

  /**
   * Top margin of this {@code WidgetGroup}. Margin is defined as the space outside of this
   * {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @return Top margin, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginTop() {
    return marginTop;
  }

  /**
   * Sets the top margin of this {@code WidgetGroup}. Margin is defined as the space outside of
   * this {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the outside
   * edge).
   *
   * @param marginTop Top margin, in pixels
   */
  @Override
  @CallSuper
  public void setMarginTop(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginTop) {
    if (marginTop < 0) {
      throw new IllegalArgumentException("marginTop margin should be positive");
    }

    this.marginTop = marginTop;
  }

  /**
   * {@link Boundary} containing of the sizes of the margins of this {@code WidgetGroup}. Margin is
   * defined as the space outside of this {@code WidgetGroup}, which no other {@code Widget} may
   * invade (i.e., marks the outside edge).
   * <p>
   *   Note: Changing the sides of the returned {@code Boundary} instance will not be reflected
   *         within this {@code WidgetGroup}.
   * </p>
   *
   * @return {@code Boundary} containing the sizes of the margins of this {@code WidgetGroup}
   */
  @Override
  @CallSuper
  @NonNull
  public Boundary getMargin() {
    return new Boundary(getMarginLeft(), getMarginTop(), getMarginRight(), getMarginBottom());
  }

  /**
   * Populates the passed {@link Boundary} instance with the sizes of the margins of this
   * {@code WidgetGroup}. Margin is defined as the space outside of this {@code WidgetGroup}, which
   * no other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @param dst {@code Boundary} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getMargin} were
   *            called.
   *
   * @return {@code Boundary} containing the sizes of the margins of this {@code WidgetGroup}
   */
  @NonNull
  @CallSuper
  @Override
  public Boundary getMargin(@Nullable final Boundary dst) {
    if (dst == null) {
      return getMargin();
    }

    dst.set(getMarginLeft(), getMarginTop(), getMarginRight(), getMarginBottom());
    return dst;
  }

  /**
   * Sets the margin on all sides of this {@code WidgetGroup}. Margin is defined as the space
   * outside of this {@code WidgetGroup}, which no other {@code Widget} may invade (i.e., marks the
   * outside edge).
   * <p>
   *   Precondition: {@code marginLeft >= 0 AND marginRight >= 0 AND marginBottom >= 0
   *                        AND marginTop >= 0}
   * </p>
   *
   * @param marginLeft   Left margin, in pixels
   * @param marginTop    Top margin, in pixels
   * @param marginRight  Right margin, in pixels
   * @param marginBottom Bottom margin, in pixels
   */
  @Override
  @CallSuper
  public final void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginLeft,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) final int marginTop,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) final int marginRight,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) final int marginBottom) {
    setMarginLeft(marginLeft);
    setMarginTop(marginTop);
    setMarginRight(marginRight);
    setMarginBottom(marginBottom);
  }

  /**
   * Sets the margin on all sides of this {@code WidgetGroup} to those of the source
   * {@link Boundary}. Margin is defined as the space outside of this {@code WidgetGroup}, which no
   * other {@code Widget} may invade (i.e., marks the outside edge).
   * <p>
   *   Precondition: {@code src.getLeft() >= 0 AND src.getRight() >= 0 AND src.getBottom() >= 0
   *                        AND src.getTop() >= 0}
   * </p>
   *
   * @param src {@code Boundary} to copy the margin onto this {@code WidgetGroup}
   */
  @Override
  @CallSuper
  public final void setMargin(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src margin cannot be null");
    }

    setMargin(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  /**
   * Sets the margin on all sides of this {@code WidgetGroup} to the specified value. This method
   * would be the same as calling {@link #setMargin(int, int, int, int)} with all the same
   * parameter. Margin is defined as the space outside of this {@code WidgetGroup}, which no
   * other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @param margin Margin, in pixels
   */
  @Override
  public final void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) final int margin) {
    setMargin(margin, margin, margin, margin);
  }

  /**
   * Checks whether or not at least one side of this {@code WidgetGroup} has a positive margin
   * value. Margin is defined as the space outside of this {@code WidgetGroup}, which no
   * other {@code Widget} may invade (i.e., marks the outside edge).
   *
   * @return {@code true} if at least one side of this {@code WidgetGroup} has a positive margin
   *         value, otherwise {@code false}
   */
  @Override
  @CallSuper
  public boolean hasMargin() {
    return getMarginLeft() > 0 || getMarginRight() > 0
            || getMarginBottom() > 0 || getMarginTop() > 0;
  }

  /**
   * Immutable {@link Iterator} which iterates through each child {@link Widget} belonging to this
   * {@code WidgetGroup}.
   *
   * @return Immutable {@code Iterator} which iterates through each child {@link Widget}
   */
  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  /**
   * Adds the given {@link Widget} to this {@code WidgetGroup}, setting the {@linkplain #getParent
   * parent} of that {@code Widget} to this and removing it from its current parent (if any).
   *
   * @param widget {@code Widget} to add to this {@code WidgetGroup} container
   */
  @Override
  @CallSuper
  public void addWidget(@NonNull final Widget widget) {
    if (widget == null) {
      throw new IllegalArgumentException("child widget cannot be null");
    }

    children.add(widget);
    widget.setParent(this);
  }

  /**
   * Checks whether or not the given {@link Widget} belongs to this {@code WidgetGroup}.
   *
   * @param widget {@code Widget} to check
   *
   * @return {@code true} if the given {@link Widget} belongs to this {@code WidgetGroup},
   *         otherwise {@code false}
   */
  @Override
  @CallSuper
  public boolean containsWidget(@Nullable Widget widget) {
    assert !children.contains(widget) || widget.getParent() == this;
    return widget != null && children.contains(widget);
  }

  /**
   * Removes the given {@link Widget} from this {@code WidgetGroup} if it belongs to it.
   *
   * @param widget {@code Widget} to remove from this {@code WidgetGroup} container
   *
   * @return {@code true} if the {@code Widget} was successfully removed, otherwise {@code false}
   */
  @Override
  @CallSuper
  public boolean removeWidget(@Nullable Widget widget) {
    if (widget == null) {
      return false;
    } else if (widget.getParent() != this) {
      assert !containsWidget(widget)
              : "widget parent is not this WidgetGroup, so this WidgetGroup should not contain it";
      return false;
    }

    widget.setParent(null);
    boolean removed = children.remove(widget);
    assert removed : "widget parent was this WidgetGroup but was not a child";
    return removed;
  }

  /**
   * Total number of {@link Widget} instances contained by this {@code WidgetGroup}.
   *
   * @return Number of child {@code Widget} instances contained by this {@code WidgetGroup}
   */
  @Override
  @CallSuper
  public int getNumWidgets() {
    return children.size();
  }

  /**
   * Immutable view of all children {@link Widget} instances belonging to this {@code WidgetGroup}
   * container.
   *
   * @return Immutable view of all children {@link Widget} instances
   */
  @NonNull
  @CallSuper
  @Override
  public Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
  }

}
