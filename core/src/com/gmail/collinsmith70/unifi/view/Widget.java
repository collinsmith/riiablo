package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Widget {

  @Nullable
  private WidgetParent parent;

  @Nullable
  private AttachInfo attachInfo;

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

  final static class AttachInfo {

    @NonNull
    final Window window;

    AttachInfo(@NonNull Window window) {
      Validate.isTrue(window != null, "window cannot be null");
      this.window = window;
    }

  }

}
