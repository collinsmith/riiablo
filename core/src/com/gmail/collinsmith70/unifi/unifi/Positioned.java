package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.IntPoint2D;

public interface Positioned {

  void setPosition(int x, int y);

  void setPosition(@NonNull final IntPoint2D src);

  @NonNull
  IntPoint2D getPosition();

  @NonNull
  IntPoint2D getPosition(@Nullable IntPoint2D dst);

}
