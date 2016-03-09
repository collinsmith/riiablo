package com.gmail.collinsmith70.unifi.unifi;

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

  @NonNull
  private final Boundary margin;

  public WidgetGroup() {
    this.children = new ArrayList<Widget>();
    this.margin = new Boundary();
  }

  @Override
  public int getMarginBottom() {
    return margin.getBottom();
  }

  @Override
  public int getMarginLeft() {
    return margin.getLeft();
  }

  @Override
  public int getMarginRight() {
    return margin.getRight();
  }

  @Override
  public int getMarginTop() {
    return margin.getTop();
  }

  @Override
  public void setMarginBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    if (bottom < 0) {
      throw new IllegalArgumentException("bottom margin should be a natural number");
    }

    margin.setBottom(bottom);
  }

  @Override
  public void setMarginLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    if (left < 0) {
      throw new IllegalArgumentException("left margin should be a natural number");
    }

    margin.setLeft(left);
  }

  @Override
  public void setMarginRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    if (right < 0) {
      throw new IllegalArgumentException("right margin should be a natural number");
    }

    margin.setRight(right);
  }

  @Override
  public void setMarginTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    if (top < 0) {
      throw new IllegalArgumentException("top margin should be a natural number");
    }

    margin.setTop(top);
  }

  @Override
  public void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                        @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    setMarginBottom(bottom);
    setMarginLeft(left);
    setMarginRight(right);
    setMarginTop(top);
  }

  @Override
  public void setMargin(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src margin cannot be null");
    }

    setMargin(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @Override
  @NonNull
  public Boundary getMargin() {
    return new Boundary(margin);
  }

  @NonNull
  @Override
  public Boundary getMargin(@Nullable Boundary dst) {
    if (dst == null) {
      return getMargin();
    }

    dst.set(margin);
    return dst;
  }

  @Override
  public boolean hasMargin() {
    return getMarginLeft() > 0 || getMarginRight() > 0
            || getMarginBottom() > 0 || getMarginTop() > 0;
  }

  @Override
  public void setTranslationX(int x) {
    super.setTranslationX(x);

    x = getX();
    for (Widget child : this) {
      child.setTranslationX(x);
    }
  }

  @Override
  public void setTranslationY(int y) {
    super.setTranslationY(y);

    y = getY();
    for (Widget child : this) {
      child.setTranslationY(y);
    }
  }

  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  @Override
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
  public boolean containsWidget(@Nullable Widget child) {
    return child != null && children.contains(child);
  }

  @Override
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
  public int getNumWidgets() {
    return children.size();
  }

  @NonNull
  @Override
  public Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
  }

}
