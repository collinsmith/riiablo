package com.gmail.collinsmith70.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

  void setPadding(@NonNull final com.gmail.collinsmith70.unifi.math.Boundary src);

  @NonNull
  com.gmail.collinsmith70.unifi.math.Boundary getPadding();

  @NonNull
  com.gmail.collinsmith70.unifi.math.Boundary getPadding(@Nullable com.gmail.collinsmith70.unifi.math.Boundary dst);

  boolean hasPadding();

}
