package com.gmail.collinsmith70.unifi2;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

public interface WidgetManager extends Iterable<Widget> {

  void addWidget(@NonNull Widget child);

  boolean containsWidget(@Nullable Widget child);

  boolean removeWidget(@Nullable Widget child);

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getNumWidgets();

  @NonNull
  Collection<Widget> getChildren();

}
