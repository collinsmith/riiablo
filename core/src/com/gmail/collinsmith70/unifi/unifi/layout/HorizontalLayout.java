package com.gmail.collinsmith70.unifi.unifi.layout;

import com.gmail.collinsmith70.unifi.unifi.Widget;

import java.util.Set;

/**
 * A {@link LinearLayout} which lays out child widgets in a horizontal line.
 */
public class HorizontalLayout extends LinearLayout {

  @Override
  public void layoutChildren() {
    super.layoutChildren();
    final int spacing = getSpacing();
    final Set<Gravity> gravity = getGravity();
    final Direction direction = getDirection();
    int childrenWidth = -spacing;
    int preferredHeight = 0;
    for (Widget child : this) {

      if (gravity.contains(Gravity.CENTER_VERTICAL)) {
        child.translateVerticalCenter((getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
      } else if (gravity.contains(Gravity.BOTTOM)) {
        child.translateBottom(getPaddingBottom());
      } else {
        child.translateTop(getHeight() - getPaddingTop());
      }

      childrenWidth += child.getWidth() + spacing;
      preferredHeight = Math.max(preferredHeight, child.getHeight());

    }

    setPreferredWidth(childrenWidth);
    setPreferredHeight(preferredHeight);

    int offset = 0;
    for (Widget child : this) {
      switch (direction) {
        case START_TO_END:
          child.translateLeft(getPaddingLeft() + offset);
          break;
        case END_TO_START:
          child.translateRight(getPaddingLeft() + childrenWidth - offset);
          break;
        default:
      }

      offset += child.getWidth() + spacing;

    }

    if (gravity.contains(Gravity.LEFT)) {
      return;
    }

    if (gravity.contains(Gravity.CENTER_HORIZONTAL)) {
      final int shift = getWidth() / 2 - childrenWidth / 2;
      for (Widget child : this) {
        child.translateLeft(child.getLeft() + shift);
      }
    } else if (gravity.contains(Gravity.RIGHT)) {
      final int shift = getWidth() - getPaddingLeft() - childrenWidth;
      for (Widget child : this) {
        child.translateRight(child.getRight() + shift);
      }
    }
  }

}
