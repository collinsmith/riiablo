package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;

import org.apache.commons.lang3.Validate;

public class Widget implements Bounded, Drawable, Padded {

  @Nullable
  private WidgetParent parent;

  @Nullable
  private AttachInfo attachInfo;

  @NonNull
  private Bounds bounds;

  @NonNull
  private Padding padding;

  @Nullable
  private Drawable background;

  @Nullable
  private Drawable foreground;

  @Nullable
  private Drawable overlay;

  @Nullable
  private Drawable debug;

  public Widget() {
    this.bounds = new Bounds() {
      @Override
      protected void onChange() {
        invalidate();
      }
    };

    this.padding = new Padding() {
      @Override
      protected void onChange() {
        invalidate();
      }
    };
  }

  public void invalidate() {

  }

  @Override
  public final void draw(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    drawBackground(canvas);
    drawForeground(canvas);
    drawOverlay(canvas);
    drawDebug(canvas);
  }

  protected void drawBackground(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable background = getBackground();
    if (background != null) {
      background.draw(canvas);
    }
  }

  protected void drawForeground(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable foreground = getForeground();
    if (foreground != null) {
      foreground.draw(canvas);
    }
  }

  protected void drawOverlay(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable overlay = getOverlay();
    if (overlay != null) {
      overlay.draw(canvas);
    }
  }

  protected void drawDebug(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable debug = getDebug();
    if (debug != null) {
      debug.draw(canvas);
    }
  }

  @Nullable
  final AttachInfo getAttachInfo() {
    return attachInfo;
  }

  final void setAttachInfo(@Nullable AttachInfo attachInfo) {
    this.attachInfo = attachInfo;
  }

  @Nullable
  final WidgetParent getParent() {
    return parent;
  }

  final void setParent(@Nullable WidgetParent parent) {
    this.parent = parent;
  }

  final boolean hasParent() {
    return getParent() != null;
  }

  @Nullable
  public final Window getWindow() {
    if (attachInfo == null) {
      return null;
    }

    return attachInfo.window;
  }

  @NonNull
  @Override
  public final Bounds getBounds() {
    return bounds;
  }

  @NonNull
  @Override
  public final Bounds getBounds(@Nullable Bounds dst) {
    if (dst == null) {
      return new Bounds(bounds);
    }

    dst.set(bounds);
    return dst;
  }

  @Override
  public final void setBounds(@NonNull Bounds bounds) {
    Validate.isTrue(bounds != null, "bounds cannot be null");
    this.bounds = bounds;
  }

  @NonNull
  @Override
  public final Padding getPadding() {
    return padding;
  }

  @NonNull
  @Override
  public final Padding getPadding(@Nullable Padding dst) {
    if (dst == null) {
      return new Padding(padding);
    }

    dst.set(padding);
    return dst;
  }

  @Override
  public final void setPadding(@NonNull Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    this.padding = padding;
  }

  @Nullable
  public Drawable getBackground() {
    return background;
  }

  public void setBackground(@Nullable Drawable background) {
    if (getBackground() != background) {
      this.background = background;
      invalidate();
    }
  }

  @Nullable
  public Drawable getForeground() {
    return foreground;
  }

  public void setForeground(@Nullable Drawable foreground) {
    if (getForeground() != foreground) {
      this.foreground = foreground;
      invalidate();
    }
  }

  @Nullable
  public Drawable getOverlay() {
    return overlay;
  }

  public void setOverlay(@Nullable Drawable overlay) {
    if (getOverlay() != overlay) {
      this.overlay = overlay;
      invalidate();
    }
  }

  @Nullable
  public Drawable getDebug() {
    return debug;
  }

  public void setDebug(@Nullable Drawable debug) {
    if (getDebug() != debug) {
      this.debug = debug;
      invalidate();
    }
  }

  final static class AttachInfo {

    @NonNull
    final Window window;

    AttachInfo(@NonNull Window window) {
      Validate.isTrue(window != null, "window cannot be null");
      this.window = window;
    }

  }

}
