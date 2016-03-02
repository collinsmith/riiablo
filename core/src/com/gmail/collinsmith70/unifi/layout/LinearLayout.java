package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;
import com.gmail.collinsmith70.unifi.widget.WidgetManager;
import com.gmail.collinsmith70.unifi.widget.WidgetParent;

public class LinearLayout extends WidgetGroup {

public enum Orientation {
    HORIZONTAL {
        @Override
        void layout(LinearLayout linearLayout) {
            final int spacing = linearLayout.getSpacing();
            switch (linearLayout.getDirection()) {
                case START_TO_END:
                    int left = linearLayout.getPaddingLeft();
                    for (Widget child : linearLayout) {
                        if (child.getVisibility().equals(Visibility.GONE)) {
                            continue;
                        } else if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeTop(linearLayout.getHeight());
                        child.moveRelativeLeft(left);
                        left += child.getWidth();
                        left += spacing;
                        linearLayout.setMinHeight(Math.max(linearLayout.getMinHeight(),
                                child.getHeight()));
                    }

                    int width = Math.max(0, left - spacing);
                    linearLayout.setMinWidth(width);
                    break;
                case END_TO_START:
                    int right = linearLayout.getWidth() - linearLayout.getPaddingRight();
                    for (Widget child : linearLayout) {
                        if (child.getVisibility().equals(Visibility.GONE)) {
                            continue;
                        } else if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeTop(linearLayout.getHeight());
                        child.moveRelativeRight(right);
                        right -= child.getWidth();
                        right -= spacing;
                        linearLayout.setMinHeight(Math.max(linearLayout.getMinHeight(),
                                child.getHeight()));
                    }

                    width = Math.max(0, linearLayout.getWidth() - right - spacing);
                    linearLayout.setMinWidth(width);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    },
    VERTICAL {
        @Override
        void layout(LinearLayout linearLayout) {
            final int spacing = linearLayout.getSpacing();
            switch (linearLayout.getDirection()) {
                case START_TO_END:
                    int top = linearLayout.getHeight() - linearLayout.getPaddingTop();
                    for (Widget child : linearLayout) {
                        if (child.getVisibility().equals(Visibility.GONE)) {
                            continue;
                        } else if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeLeft(0);
                        child.moveRelativeTop(top);
                        top -= child.getHeight();
                        top -= spacing;
                        linearLayout.setMinWidth(Math.max(
                                linearLayout.getMinWidth(),
                                child.getWidth()));
                    }

                    int height = Math.max(0, linearLayout.getHeight() - top - spacing);
                    linearLayout.setMinHeight(height);
                    break;
                case END_TO_START:
                    int bottom = linearLayout.getPaddingBottom();
                    for (Widget child : linearLayout) {
                        if (child.getVisibility().equals(Visibility.GONE)) {
                            continue;
                        } else if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeLeft(0);
                        child.moveRelativeBottom(bottom);
                        bottom += child.getHeight();
                        bottom += spacing;
                        linearLayout.setMinWidth(Math.max(
                                linearLayout.getMinWidth(),
                                child.getWidth()));
                    }

                    height = Math.max(0, bottom - spacing);
                    linearLayout.setMinHeight(height);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    };

    abstract void layout(LinearLayout linearLayout);
}
@NonNull private Orientation orientation = Orientation.HORIZONTAL;
@NonNull public Orientation getOrientation() {
    return orientation;
}
public void setOrientation(@NonNull Orientation orientation) {
    if (orientation == null) {
        throw new IllegalArgumentException("orientation cannot be null");
    }

    this.orientation = orientation;
    requestLayout();
}

public enum Direction { START_TO_END, END_TO_START }
@NonNull private Direction direction = Direction.START_TO_END;
@NonNull public Direction getDirection() {
    return direction;
}
public void setDirection(@NonNull Direction direction) {
    if (direction == null) {
        throw new IllegalArgumentException("direction cannot be null");
    }

    this.direction = direction;
    requestLayout();
}

@IntRange(from = 0, to = Integer.MAX_VALUE) private int spacing;
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getSpacing() {
    return spacing;
}
public void setSpacing(@IntRange(from = 0, to = Integer.MAX_VALUE) int spacing) {
    if (spacing < 0) {
        throw new IllegalArgumentException("spacing should be greater than or equal to 0");
    }

    this.spacing = spacing;
}

public LinearLayout() {
    this(Orientation.HORIZONTAL);
}
public LinearLayout(int spacing) {
    this(Orientation.HORIZONTAL, spacing);
}
public LinearLayout(Orientation orientation) {
    this(orientation, Direction.START_TO_END);
}
public LinearLayout(Orientation orientation, int spacing) {
    this(orientation, Direction.START_TO_END, spacing);
}
public LinearLayout(Orientation orientation, Direction direction) {
    this(orientation, direction, 0);
}
public LinearLayout(Orientation orientation, Direction direction, int spacing) {
    setDirection(direction);
    setOrientation(orientation);
    setSpacing(spacing);
}

@NonNull @Override public WidgetManager addWidget(@NonNull Widget child) {
    WidgetManager widgetManager = super.addWidget(child);
    requestLayout();
    return widgetManager;
}

@Override protected void layoutChildren() {
    getOrientation().layout(this);
}

@Override public boolean keyDown(int keycode) {
    return false;
}
@Override public boolean keyUp(int keycode) {
    return false;
}
@Override public boolean keyTyped(char character) {
    return false;
}
@Override public boolean scrolled(int amount) {
    return false;
}

}
