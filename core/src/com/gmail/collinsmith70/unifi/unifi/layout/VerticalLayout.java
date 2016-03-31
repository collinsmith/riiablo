package com.gmail.collinsmith70.unifi.unifi.layout;

import com.gmail.collinsmith70.unifi.unifi.Widget;

import java.util.Set;

public class VerticalLayout extends LinearLayout {

  @Override
  public void layoutChildren() {

    final int spacing = getSpacing();
    final Set<Gravity> gravity = getGravity();
    final Direction direction = getDirection();
    int childrenHeight = -spacing;
    int preferredWidth = 0;
    for (Widget child : this) {

      if (gravity.contains(Gravity.CENTER_HORIZONTAL)) {
        child.translateHorizontalCenter((getWidth() - getPaddingLeft() - getPaddingRight()) / 2);
      } else if (gravity.contains(Gravity.RIGHT)) {
        child.translateRight(getWidth() - getPaddingRight());
      } else {
        child.translateLeft(getPaddingLeft());
      }

      childrenHeight += child.getHeight() + spacing;
      preferredWidth = Math.max(preferredWidth, child.getWidth());

    }

    setPreferredWidth(preferredWidth);
    setPreferredHeight(childrenHeight);

    int offset = 0;
    for (Widget child : this) {
      switch (direction) {
        case START_TO_END:
          child.translateTop(getHeight() - getPaddingTop() - offset);
          break;
        case END_TO_START:
          child.translateBottom(getHeight() - childrenHeight + offset);
          break;
        default:
      }

      offset += child.getHeight() + spacing;

    }

    if (gravity.contains(Gravity.TOP)) {
      return;
    }

    if (gravity.contains(Gravity.CENTER_VERTICAL)) {
      final int shift = getHeight() / 2 - childrenHeight / 2;
      for (Widget child : this) {
        child.translateTop(child.getTop() - shift);
      }
    } else if (gravity.contains(Gravity.BOTTOM)) {
      final int shift = getHeight() - getPaddingTop() - childrenHeight;
      for (Widget child : this) {
        child.translateBottom(child.getBottom() + shift);
      }
    }

    super.layoutChildren();
  }

}
