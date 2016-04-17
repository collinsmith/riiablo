package com.gmail.collinsmith70.unifi2;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi2.math.Boundary;

public interface Paddable {

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getPaddingBottom();

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getPaddingLeft();

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getPaddingRight();

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getPaddingTop();

  void setPaddingBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom);

  void setPaddingLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left);

  void setPaddingRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right);

  void setPaddingTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top);

  void setPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom);

  void setPadding(@NonNull final Boundary src);

  @NonNull
  Boundary getPadding();

  @NonNull
  Boundary getPadding(@Nullable Boundary dst);

  boolean hasPadding();

}
