package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.math.Rectangle;
import com.google.common.collect.Iterators;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class Window implements Disposable, WidgetManager, WidgetParent {

  @NonNull
  private final Canvas canvas;
  private final boolean ownsCanvas;

  private final Rectangle dirty;

  private boolean isLayoutRequested;

  @NonNull
  private final Collection<Widget> children;

  @NonNull
  private final Widget.AttachInfo attachInfo;

  public Window(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    this(new Canvas(width, height), true);
  }

  public Window(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int height,
                @NonNull Canvas canvas) {
    this(canvas, false);
    canvas.resize(width, height);
  }

  private Window(@NonNull Canvas canvas, boolean ownsCanvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    this.canvas = canvas;
    this.ownsCanvas = ownsCanvas;
    this.children = new ArrayList<Widget>();
    this.dirty = new Rectangle();
    this.attachInfo = new Widget.AttachInfo(this);
  }

  @NonNull
  public final Canvas getCanvas() {
    return canvas;
  }

  public final int getWidth() {
    return canvas.getWidth();
  }

  public final int getHeight() {
    return canvas.getHeight();
  }

  public final Dimension2D getDimensions() {
    return canvas.getDimensions();
  }

  public final Dimension2D getDimensions(@Nullable Dimension2D dst) {
    return canvas.getDimensions(dst);
  }

  public final void resize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    canvas.resize(width, height);
    invalidate();
    onResize(width, height);
  }

  protected void onResize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                          @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    //...
  }

  public void invalidate() {
    dirty.set(0, 0, getWidth(), getHeight());
  }

  @Override
  public final void dispose() {
    if (ownsCanvas) {
      canvas.dispose();
    }

    onDispose();
  }

  protected void onDispose() {
    //...
  }

  public final void draw() {

    canvas.end();
    onDraw();
  }

  protected void onDraw() {

  }

  @Override
  public final Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  @Override
  public final void addWidget(@NonNull Widget widget) {
    Validate.isTrue(widget != null, "widget cannot be null");
    children.add(widget);
    widget.setParent(this);
    widget.setAttachInfo(attachInfo);
    invalidate();
  }

  @Override
  public final void removeWidget(@Nullable Widget widget) {
    if (widget == null) {
      return;
    }

    children.remove(widget);
    widget.setParent(null);
    widget.setAttachInfo(null);
    invalidate();
  }

  @NonNull
  @Override
  public final Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
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
  public void requestLayout() {
    isLayoutRequested = true;
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public boolean isLayoutRequested() {
    return isLayoutRequested;
  }

}
