package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.IntPoint2D;

public interface Translateable {

  int getTranslationX();

  void setTranslationX(int x);

  int getTranslationY();

  void setTranslationY(int y);

  void setTranslation(int x, int y);

  void setTranslation(@NonNull final IntPoint2D src);

  @NonNull
  IntPoint2D getTranslation();

  @NonNull
  IntPoint2D getTranslation(@Nullable IntPoint2D dst);

}
