package com.gmail.collinsmith70.unifi.unifi.layout;

import com.gmail.collinsmith70.unifi.unifi.Widget;

public class HorizontalLayout extends LinearLayout {

  @Override
  public void requestLayout() {

    @Gravity final int gravity = getGravity();
    for (Widget child : this) {

      if ((gravity&CENTER_VERTICAL) != 0) {
        child.translateVerticalCenter((getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
      } else if ((gravity&BOTTOM) != 0) {
        child.translateBottom(getPaddingBottom());
      } else {
        child.translateTop(getHeight() - getPaddingTop());
      }

      if ((gravity&CENTER_HORIZONTAL) != 0) {
        child.translateHorizontalCenter((getWidth() - getPaddingLeft() - getPaddingRight()) / 2);
      } else if ((gravity&RIGHT) != 0) {
        child.translateRight(getWidth() - getPaddingRight());
      } else {
        child.translateLeft(getPaddingLeft());
      }

    }
  }

}
