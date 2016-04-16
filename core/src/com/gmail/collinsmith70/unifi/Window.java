package com.gmail.collinsmith70.unifi;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class Window
        implements WidgetParent, Disposable {

  @NonNull
  private final com.gmail.collinsmith70.unifi.math.Dimension2D dimension;

  @NonNull
  private final Collection<Widget> children;

  private boolean debugging;

  @Nullable
  private Texture texture;

  @Nullable
  private com.gmail.collinsmith70.unifi.graphics.Canvas canvas;

  private boolean valid;

  public Window(int width, int height) {
    this.valid = false;
    this.dimension = new com.gmail.collinsmith70.unifi.math.Dimension2D(width, height);
    this.children = new ArrayList<Widget>();
    this.debugging = Boolean.parseBoolean(System.getProperty(Window.class.getName() + "debugMode"));
  }

  public void validate() {
    this.valid = true;
  }

  public void invalidate() {
    this.valid = false;
    for (Widget child : this) {
      child.invalidate();
    }
  }

  private boolean isValid() {
    return valid;
  }

  public void draw(@NonNull final Batch batch) {
    assert batch != null : "batch should not be null";
    if (isValid()) {
      batch.draw(texture,
              0, 0, getWidth(), getHeight(),
              0, 0, getWidth(), getHeight(),
              false, true);
      return;
    }

    if (canvas != null) {
      canvas.dispose();
      this.canvas = null;
    }

    this.canvas = new com.gmail.collinsmith70.unifi.graphics.Canvas(getWidth(), getHeight());
    for (Widget child : this) {
      child.draw(canvas);
    }

    texture = canvas.toTexture();
    batch.draw(texture,
            0, 0, getWidth(), getHeight(),
            0, 0, getWidth(), getHeight(),
            false, true);
    validate();
  }

  @Override
  public void dispose() {
    if (texture != null) {
      texture.dispose();
      this.texture = null;
    }

    if (canvas != null) {
      canvas.dispose();
      this.canvas = null;
    }

    for (Widget child : this) {
      child.dispose();
    }
  }

  public void resize(int width, int height) {
    dimension.set(width, height);
    invalidate();
  }

  @Override
  public void requestLayout() {
    System.out.println("Window#requestLayout();");
    layout();
  }

  private void layout() {
    for (Widget child : this) {
      if (!(child instanceof WidgetParent)) {
        continue;
      }

      ((WidgetParent) child).requestLayout();
    }

    invalidate();
  }

  public void setDebugging(boolean debugging) {
    if (debugging == isDebugging()) {
      return;
    }

    this.debugging = debugging;
    invalidate();
  }

  public boolean isDebugging() {
    return debugging;
  }

  @Nullable
  @Override
  public WidgetParent getParent() {
    return null;
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public int getBottom() {
    return 0;
  }

  @Override
  public void setBottom(final int bottom) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getLeft() {
    return 0;
  }

  @Override
  public void setLeft(final int left) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getRight() {
    return dimension.getWidth();
  }

  @Override
  public void setRight(final int right) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getTop() {
    return dimension.getHeight();
  }

  @Override
  public void setTop(final int top) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Boundary getBoundary() {
    return new com.gmail.collinsmith70.unifi.math.Boundary(getLeft(), getTop(), getRight(), getBottom());
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Boundary getBoundary(@Nullable final com.gmail.collinsmith70.unifi.math.Boundary dst) {
    if (dst == null) {
      return getBoundary();
    }

    dst.set(getLeft(), getTop(), getRight(), getBottom());
    return dst;
  }

  @Override
  public void setBoundary(final int left, final int top, final int right, final int bottom) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public void setBoundary(@NonNull final com.gmail.collinsmith70.unifi.math.Boundary src) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Dimension2D getSize() {
    return new com.gmail.collinsmith70.unifi.math.Dimension2D(dimension);
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Dimension2D getSize(@Nullable final com.gmail.collinsmith70.unifi.math.Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(dimension);
    return dst;
  }

  @Override
  public boolean hasSize() {
    return getWidth() > 0 && getHeight() > 0;
  }

  @Override
  public int getX() {
    return 0;
  }

  @Override
  public void setX(final int x) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getY() {
    return 0;
  }

  @Override
  public void setY(final int y) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Point2D getPosition() {
    return new com.gmail.collinsmith70.unifi.math.Point2D(getX(), getY());
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi.math.Point2D getPosition(@Nullable final com.gmail.collinsmith70.unifi.math.Point2D dst) {
    if (dst == null) {
      return getPosition();
    }

    dst.set(getX(), getY());
    return dst;
  }

  @Override
  public void setPosition(final int x, final int y) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public void setPosition(@NonNull final com.gmail.collinsmith70.unifi.math.Point2D src) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return dimension.getWidth();
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return dimension.getHeight();
  }

  @Override
  public boolean contains(final int x, final int y) {
    return getLeft() <= x && x <= getRight()
            && getBottom() <= y && y <= getTop();
  }

  @Override
  public boolean contains(@NonNull final com.gmail.collinsmith70.unifi.math.Point2D point) {
    if (point == null) {
      throw new IllegalArgumentException("point cannot be null");
    }

    return contains(point.getX(), point.getY());
  }

  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  @Override
  @CallSuper
  public void addWidget(@NonNull final Widget widget) {
    if (widget == null) {
      throw new IllegalArgumentException("child widget cannot be null");
    }

    children.add(widget);
    widget.setParent(this);
    if (widget instanceof WidgetParent) {
      widget.setBoundary(getLeft(), getTop(), getRight(), getBottom());
    }

    requestLayout();
  }

  @Override
  @CallSuper
  public boolean containsWidget(@Nullable Widget widget) {
    assert !children.contains(widget) || widget.getParent() == this;
    return widget != null && children.contains(widget);
  }

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
    requestLayout();
    assert removed : "widget parent was this WidgetGroup but was not a child";
    return removed;
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
