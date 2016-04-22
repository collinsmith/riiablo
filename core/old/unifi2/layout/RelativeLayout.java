package com.gmail.collinsmith70.unifi2.layout;

import com.gmail.collinsmith70.unifi2.Widget;
import com.gmail.collinsmith70.unifi2.WidgetGroup;

import java.util.Set;

/**
 * A {@code WidgetGroup} that lays out child {@link Widget} instances relative to itself.
 */
public class RelativeLayout extends WidgetGroup {

  public static final class LayoutParams {

    /**
     * Determines where to lay out a {@code Widget} within its parent. Should be given as a
     * {@link Set} of {@link Gravity} constants. Components laid out under a
     * {@code RelativeLayout} may overlap with one another.
     */
    @LayoutParam
    public static final String relativeTo = "relativeTo";

  }

  @Override
  public void layoutChildren() {
    super.layoutChildren();
    for (Widget child : this) {

      Set<Gravity> relativeTo = child.get(RelativeLayout.LayoutParams.relativeTo);

      if (relativeTo.contains(Gravity.CENTER_VERTICAL)) {
        child.translateVerticalCenter((getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
      } else if (relativeTo.contains(Gravity.BOTTOM)) {
        child.translateBottom(getPaddingBottom());
      } else {
        child.translateTop(getHeight() - getPaddingTop());
      }

      if (relativeTo.contains(Gravity.CENTER_HORIZONTAL)) {
        child.translateHorizontalCenter((getWidth() - getPaddingLeft() - getPaddingRight()) / 2);
      } else if (relativeTo.contains(Gravity.RIGHT)) {
        child.translateRight(getWidth() - getPaddingRight());
      } else {
        child.translateLeft(getPaddingLeft());
      }

    }
  }

}
