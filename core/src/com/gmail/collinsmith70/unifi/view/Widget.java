package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;

import org.apache.commons.lang3.Validate;

public class Widget implements Bounded, Padded {

  @Nullable
  private WidgetParent parent;

  @Nullable
  private AttachInfo attachInfo;

  @NonNull
  private Bounds bounds;

  @NonNull
  private Padding padding;

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

  @Nullable
  AttachInfo getAttachInfo() {
    return attachInfo;
  }

  void setAttachInfo(@Nullable AttachInfo attachInfo) {
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

  @Nullable Window getWindow() {
    if (attachInfo == null) {
      return null;
    }

    return attachInfo.window;
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
    Validate.isTrue(bounds != null, "bounds cannot be null");
    this.bounds = bounds;
  }

  @NonNull
  @Override
  public Padding getPadding() {
    return padding;
  }

  @NonNull
  @Override
  public Padding getPadding(@Nullable Padding dst) {
    if (dst == null) {
      return new Padding(padding);
    }

    dst.set(padding);
    return dst;
  }

  @Override
  public void setPadding(@NonNull Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    this.padding = padding;
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
