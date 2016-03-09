package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Point2D;

public interface Translateable {

  int getTranslationX();

  int getTranslationY();

  @NonNull
  Point2D getTranslation();

  @NonNull
  Point2D getTranslation(@Nullable Point2D dst);

}
