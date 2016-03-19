package com.gmail.collinsmith70.unifi.unifi.layout;

import com.gmail.collinsmith70.unifi.unifi.Widget;

public class HorizontalLayout extends LinearLayout {

  @Override
  public void requestLayout() {

    @Gravity final int gravity = getGravity();
    for (Widget child : this) {

      if ((gravity&CENTER_VERTICAL) != 0) {
        child.translateVerticalCenter(getPaddingLeft());
      } else if ((gravity&BOTTOM) != 0) {
        child.translateBottom(0);
      } else {
        child.translateTop(getHeight());
      }

    }
  }

}
