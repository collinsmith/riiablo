package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.Nullable;

public interface Dialog {

  /**
   * @param widget {@link Widget} to center this {@link Dialog} relative to, or {@code null} to center
   *               within the {@link Window} container of this {@link Dialog}.
   */
  void centerAt(@Nullable final Widget widget);

}
