package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.Nullable;

public interface Parentable<E> {

  @Nullable
  E getParent();

  //void setParent(@Nullable final E parent);

  boolean hasParent();

}
