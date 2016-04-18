package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.math.Rectangle;
import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Window implements Bounded, Disposable, WidgetParent {

  private static final String TAG = Window.class.getSimpleName();

  private static final boolean DEBUG_LAYOUT = false;

  @NonNull
  private final Canvas canvas;
  private final boolean ownsCanvas;

  private final Rectangle dirty;

  private boolean first;
  private boolean isLayoutScheduled;
  private boolean isLayoutRequested;
  private boolean isLayingOut;
  private boolean handlingLayoutInLayoutRequest;

  @NonNull
  private final LayoutRunnable layoutRunnable = new LayoutRunnable();

  @NonNull
  private final List<Widget> layoutRequesters;

  @NonNull
  private final Widget.AttachInfo attachInfo;

  @NonNull
  private final Bounds bounds;

  @Nullable
  private Widget widget;

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
    this.first = true;
    this.canvas = canvas;
    this.ownsCanvas = ownsCanvas;
    this.dirty = new Rectangle();
    this.attachInfo = new Widget.AttachInfo(this);
    this.bounds = this.new WindowBounds();
    this.layoutRequesters = new ArrayList<Widget>();
  }

  @NonNull
  private final Canvas getCanvas() {
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

  @Nullable
  final Widget getWidget() {
    return widget;
  }

  final void setWidget(@Nullable Widget widget) {
    if (getWidget() == widget) {
      return;
    }

    this.widget = widget;
    if (widget != null) {
      attachInfo.rootWidget = widget;
      requestLayout();
      widget.setParent(this);
    }
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
    // TODO: if valid (dirty.isEmpty() == false) draw cached FrameBuffer, else redraw parts

    canvas.end();
    onDraw();
  }

  protected void onDraw() {

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

  private void scheduleLayout() {
    if (isLayoutScheduled) {
      return;
    }

    isLayoutScheduled = true;
    Gdx.app.postRunnable(layoutRunnable);
  }

  private void unscheduleLayout() {
    if (!isLayoutScheduled) {
      return;
    }

    isLayoutScheduled = false;
  }

  private void doLayout() {
    if (!isLayoutScheduled) {
      return;
    }

    isLayoutScheduled = true;
    performLayout(getWidth(), getHeight());
  }

  private final class LayoutRunnable implements Runnable {

    @Override
    public void run() {
      doLayout();
    }

  }

  @Override
  public void requestLayout() {
    if (handlingLayoutInLayoutRequest) {
      return;
    }

    isLayoutRequested = true;
    scheduleLayout();
  }

  @Override
  public boolean isLayoutRequested() {
    return isLayoutRequested;
  }

  boolean isLayingOut() {
    return isLayingOut;
  }

  boolean requestLayoutDuringLayout(@NonNull Widget widget) {
    Validate.isTrue(widget != null, "widget cannot be null");
    return false;
  }

  private void performLayout(int desiredWindowWidth, int desiredWindowHeight) {
    if (widget == null) {
      throw new RuntimeException("performLayout() called with null widget");
    }

    this.isLayoutRequested = false;
    this.isLayingOut = true;

    if (DEBUG_LAYOUT) {
      Gdx.app.log(TAG, String.format(
              "laying out %s to (%d, %d)",
              widget, widget.getMeasuredWidth(), widget.getMeasuredHeight()));
    }

    widget.layout(0, widget.getMeasuredHeight(), widget.getMeasuredWidth(), 0);
    this.isLayingOut = false;
    if (!layoutRequesters.isEmpty()) {
      List<Widget> validLayoutRequesters = getValidLayoutRequesters(layoutRequesters, false);
      if (!validLayoutRequesters.isEmpty()) {
        this.handlingLayoutInLayoutRequest = true;

        for (Widget validLayoutRequester : validLayoutRequesters) {
          Gdx.app.error(validLayoutRequester.getClass().getSimpleName(),
                  String.format("requestLayout() improperly called by %s during layout: " +
                          "running second layout pass", validLayoutRequester));
          validLayoutRequester.requestLayout();
        }

        int childWidthMeasureSpec = getRootMeasureSpec(desiredWindowWidth, getWidth());
        int childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, getHeight());
        widget.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        this.isLayingOut = true;
        widget.layout(0, widget.getMeasuredHeight(), widget.getMeasuredWidth(), 0);
        this.handlingLayoutInLayoutRequest = false;

        validLayoutRequesters = getValidLayoutRequesters(layoutRequesters, true);
        if (!validLayoutRequesters.isEmpty()) {
          final List<Widget> finalRequesters = validLayoutRequesters;
          Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
              for (Widget finalRequester : finalRequesters) {
                Gdx.app.error(finalRequester.getClass().getSimpleName(),
                        String.format("requestLayout() improperly called by %s during " +
                                "second layout pass: " +
                                "posting in next frame", finalRequester));
                finalRequester.requestLayout();
              }
            }
          });
        }
      }
    }

    this.isLayingOut = false;
  }

  private int getRootMeasureSpec(
          @IntRange(from = Widget.LayoutParams.WRAP_CONTENT, to = Integer.MAX_VALUE)
          int windowSize,
          @IntRange(from = Widget.LayoutParams.WRAP_CONTENT, to = Integer.MAX_VALUE)
          int rootDimension) {
    switch (rootDimension) {
      case Widget.LayoutParams.WRAP_CONTENT:
        return Widget.MeasureSpec.compile(windowSize, Widget.MeasureSpec.Mode.AT_MOST);
      case Widget.LayoutParams.MATCH_PARENT:
        return Widget.MeasureSpec.compile(windowSize, Widget.MeasureSpec.Mode.EXACTLY);
      default:
        return Widget.MeasureSpec.compile(rootDimension, Widget.MeasureSpec.Mode.EXACTLY);
    }
  }

  @NonNull
  private List<Widget> getValidLayoutRequesters(@NonNull List<Widget> layoutRequesters,
                                                boolean secondPass) {
    Validate.isTrue(layoutRequesters != null, "layoutRequesters cannot be null");
    List<Widget> validLayoutRequesters = null;
    for (Widget widget : layoutRequesters) {
      if (widget != null && widget.hasAttachInfo() && widget.hasParent()
              && (secondPass || (widget.getFlags().contains(Widget.Flag.FORCE_LAYOUT)))) {
        boolean gone = false;
        Widget parent = widget;
        while (parent != null) {
          if (parent.getVisibility() == Widget.Visibility.GONE) {
            gone = true;
          }

          if (parent instanceof Widget) {
            parent = (Widget)parent.getParent();
          } else {
            parent = null;
          }
        }

        if (!gone) {
          if (validLayoutRequesters == null) {
            validLayoutRequesters = new ArrayList<Widget>();
          }

          validLayoutRequesters.add(widget);
        }
      }
    }

    if (secondPass) {
      for (Widget widget : layoutRequesters) {
        while (widget != null && widget.getFlags().contains(Widget.Flag.FORCE_LAYOUT)) {
          widget.getFlags().remove(Widget.Flag.FORCE_LAYOUT);
          if (widget.getParent() instanceof Widget) {
            widget = (Widget)getParent();
          } else {
            widget = null;
          }
        }
      }
    }

    layoutRequesters.clear();
    return validLayoutRequesters != null ? validLayoutRequesters : Collections.EMPTY_LIST;
  }

  @NonNull
  @Override
  public Bounds getBounds() {
    return bounds;
  }

  @NonNull
  @Override
  public Bounds getBounds(@Nullable Bounds dst) {
    if (dst == null) {
      return new Bounds(bounds);
    }

    dst.set(bounds);
    return dst;
  }

  @Override
  public void setBounds(@NonNull Bounds bounds) {
    throw new UnsupportedOperationException("Window bounds cannot be changed");
  }

  private final class WindowBounds extends Bounds {

    @Override
    public int getLeft() {
      return 0;
    }

    @Override
    public int getTop() {
      return Window.this.getHeight();
    }

    @Override
    public int getRight() {
      return Window.this.getWidth();
    }

    @Override
    public int getBottom() {
      return 0;
    }

    @Override
    public void setLeft(int left) {
      throw new UnsupportedOperationException("Window bounds cannot be changed");
    }

    @Override
    public void setTop(int top) {
      throw new UnsupportedOperationException("Window bounds cannot be changed");
    }

    @Override
    public void setRight(int right) {
      throw new UnsupportedOperationException("Window bounds cannot be changed");
    }

    @Override
    public void setBottom(int bottom) {
      throw new UnsupportedOperationException("Window bounds cannot be changed");
    }

  }

}
