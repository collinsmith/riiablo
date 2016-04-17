package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

public interface WidgetManager extends Iterable<Widget> {

  void addWidget(@NonNull Widget widget);

  void removeWidget(@Nullable Widget widget);

  @NonNull
  Collection<Widget> getChildren();

  void clear();

}
