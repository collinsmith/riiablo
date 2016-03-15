package com.gmail.collinsmith70.unifi.unifi.layout;

import android.support.annotation.IntRange;

import com.gmail.collinsmith70.unifi.unifi.WidgetGroup;

public class LinearLayout extends WidgetGroup {

  @IntRange(from = 0, to = Integer.MAX_VALUE) private int spacing;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getSpacing() {
    return spacing;
  }

  public void setSpacing(@IntRange(from = 0, to = Integer.MAX_VALUE) int spacing) {
    if (spacing < 0) {
      throw new IllegalArgumentException("spacing must be greater than or equal to 0");
    }

    this.spacing = spacing;
  }

}
