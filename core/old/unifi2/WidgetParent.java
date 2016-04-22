package com.gmail.collinsmith70.unifi2;

import android.support.annotation.Nullable;

public interface WidgetParent extends Bounded, WidgetManager {

  @Nullable
  WidgetParent getParent();

  boolean hasParent();

  void requestLayout();

}
