package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.CallSuper;
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

  @NonNull private final Collection<Widget> children;

  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginBottom;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginLeft;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginRight;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int marginTop;

  public WidgetGroup() {
    this.children = new ArrayList<Widget>();
  }

  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginBottom() {
    return marginBottom;
  }

  @Override
  @CallSuper
  public void setMarginBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginBottom) {
    if (marginBottom < 0) {
      throw new IllegalArgumentException("bottom margin should be positive");
    }

    this.marginBottom = marginBottom;
  }

  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginLeft() {
    return marginLeft;
  }

  @Override
  @CallSuper
  public void setMarginLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginLeft) {
    if (marginLeft < 0) {
      throw new IllegalArgumentException("left margin should be positive");
    }

    this.marginLeft = marginLeft;
  }

  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginRight() {
    return marginRight;
  }

  @Override
  @CallSuper
  public void setMarginRight(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginRight) {
    if (marginRight < 0) {
      throw new IllegalArgumentException("right margin should be positive");
    }

    this.marginRight = marginRight;
  }

  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getMarginTop() {
    return marginTop;
  }

  @Override
  @CallSuper
  public void setMarginTop(@IntRange(from = 0, to = Integer.MAX_VALUE) final int marginTop) {
    if (marginTop < 0) {
      throw new IllegalArgumentException("marginTop margin should be positive");
    }

    this.marginTop = marginTop;
  }

  @Override
  @CallSuper
  @NonNull
  public Boundary getMargin() {
    return new Boundary(getMarginLeft(), getMarginTop(), getMarginRight(), getMarginBottom());
  }

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

  @Override
  @CallSuper
  public final void setMargin(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src margin cannot be null");
    }

    setMargin(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @Override
  public final void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) final int margin) {
    setMargin(margin, margin, margin, margin);
  }

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
