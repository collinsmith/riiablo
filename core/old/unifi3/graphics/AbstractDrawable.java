package com.gmail.collinsmith70.unifi3.graphics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi3.util.Bounds;

import org.apache.commons.lang3.Validate;

public abstract class AbstractDrawable implements Drawable {

  private Bounds bounds;
  private com.gmail.collinsmith70.unifi3.util.Padding padding;

  @Override
  public int getMinimumWidth() {
    final int intrinsicWidth = getIntrinsicWidth();
    return intrinsicWidth > 0 ? intrinsicWidth : 0;
  }

  @Override
  public int getMinimumHeight() {
    final int intrinsicHeight = getIntrinsicHeight();
    return intrinsicHeight > 0 ? intrinsicHeight : 0;
  }

  @Override
  public int getIntrinsicWidth() {
    return -1;
  }

  @Override
  public int getIntrinsicHeight() {
    return -1;
  }

  @NonNull
  @Override
  public Bounds getBounds() {
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
  public void setBounds(@NonNull Bounds bounds) {
    Validate.isTrue(bounds != null, "bounds cannot be null");
    this.bounds = bounds;
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi3.util.Padding getPadding() {
    return padding;
  }

  @NonNull
  @Override
  public com.gmail.collinsmith70.unifi3.util.Padding getPadding(@Nullable com.gmail.collinsmith70.unifi3.util.Padding dst) {
    if (dst == null) {
      return new com.gmail.collinsmith70.unifi3.util.Padding(padding);
    }

    dst.set(padding);
    return dst;
  }

  @Override
  public void setPadding(@NonNull com.gmail.collinsmith70.unifi3.util.Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    this.padding = padding;
  }

}
