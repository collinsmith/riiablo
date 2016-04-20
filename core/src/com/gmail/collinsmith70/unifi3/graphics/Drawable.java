package com.gmail.collinsmith70.unifi3.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi3.util.Bounded;
import com.gmail.collinsmith70.unifi3.util.Padded;

public interface Drawable extends Bounded, Padded {

  void draw(@NonNull Canvas canvas);

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getMinimumWidth();
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getMinimumHeight();

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getIntrinsicWidth();
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getIntrinsicHeight();

}
