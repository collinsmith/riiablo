package com.gmail.collinsmith70.unifi2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi2.math.Dimension2D;

public interface Sized {

  @NonNull
  Dimension2D getSize();

  @NonNull
  Dimension2D getSize(@Nullable Dimension2D dst);

  boolean hasSize();

}
