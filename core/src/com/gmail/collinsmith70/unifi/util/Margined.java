package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Margined {

  @NonNull
  Margin getMargin();

  @NonNull
  Margin getMargin(@Nullable Margin dst);

  void setMargin(@NonNull Margin margin);

}
