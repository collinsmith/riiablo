package com.gmail.collinsmith70.unifi;

import android.support.annotation.Nullable;

public interface WidgetParent extends Bounded, WidgetManager {

  @Nullable
  WidgetParent getParent();

  boolean hasParent();

  void requestLayout();

}
