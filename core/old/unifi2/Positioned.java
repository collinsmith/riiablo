package com.gmail.collinsmith70.unifi2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi2.math.Point2D;

public interface Positioned {

  void setPosition(int x, int y);

  void setPosition(@NonNull final Point2D src);

  @NonNull
  Point2D getPosition();

  @NonNull
  Point2D getPosition(@Nullable Point2D dst);

}
