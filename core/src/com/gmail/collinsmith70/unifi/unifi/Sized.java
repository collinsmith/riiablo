package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;

public interface Sized {

  void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
               @IntRange(from = 0, to = Integer.MAX_VALUE) int height);

  void setSize(@NonNull final Dimension2D src);

  @NonNull
  Dimension2D getSize();

  @NonNull
  Dimension2D getSize(@Nullable Dimension2D dst);

  boolean hasSize();

}
