package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;

public interface Sized {

  @NonNull
  Dimension2D getSize();

  @NonNull
  Dimension2D getSize(@Nullable Dimension2D dst);

  boolean hasSize();

}
