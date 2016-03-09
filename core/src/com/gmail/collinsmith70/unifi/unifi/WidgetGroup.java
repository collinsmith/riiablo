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

  @NonNull
  private final Collection<Widget> children;

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

  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  @Override
  @CallSuper
  public void addWidget(@NonNull Widget child) {
    if (child == null) {
      throw new IllegalArgumentException("child widget cannot be null");
    }

    if (child.hasParent()) {
      WidgetParent parent = child.getParent();

    }
    child.setParent(this);
    children.add(child);
  }

  @Override
  @CallSuper
  public boolean containsWidget(@Nullable Widget child) {
    return child != null && children.contains(child);
  }

  @Override
  @CallSuper
  public boolean removeWidget(@Nullable Widget child) {
    if (child == null) {
      return false;
    }

    if (child.getParent() == this) {
      child.setParent(null);
    }

    return children.remove(child);
  }

  @Override
  @CallSuper
  public int getNumWidgets() {
    return children.size();
  }

  @NonNull
  @CallSuper
  @Override
  public Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
  }

}
