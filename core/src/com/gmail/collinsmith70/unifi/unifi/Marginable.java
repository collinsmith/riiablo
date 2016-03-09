package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;

public interface Marginable {

  @IntRange(from = 0, to = Integer.MAX_VALUE) int getMarginBottom();
  @IntRange(from = 0, to = Integer.MAX_VALUE) int getMarginLeft();
  @IntRange(from = 0, to = Integer.MAX_VALUE) int getMarginRight();
  @IntRange(from = 0, to = Integer.MAX_VALUE) int getMarginTop();

  void setMarginBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom);
  void setMarginLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left);
  void setMarginRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right);
  void setMarginTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top);

  @NonNull
  Boundary getMargin();

  @NonNull
  Boundary getMargin(@Nullable Boundary dst);

  void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                 @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                 @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                 @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom);

  void setMargin(@NonNull final Boundary src);

  void setMargin(@IntRange(from = 0, to = Integer.MAX_VALUE) int margin);

  boolean hasMargin();

}
