package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.Nullable;

public interface WidgetParent {

  @Nullable
  WidgetParent getParent();

  boolean hasParent();

  void requestLayout();

  boolean isLayoutRequested();

}
