package com.gmail.collinsmith70.unifi.layout;

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
            switch (linearLayout.getDirection()) {
                case START_TO_END:
                    int left = 0;
                    for (Widget child : linearLayout) {
                        if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeTop(linearLayout.getHeight());
                        child.moveRelativeLeft(left);
                        left += child.getWidth();
                        linearLayout.setMinHeight(Math.max(linearLayout.getMinHeight(),
                                child.getHeight()));
                    }

                    linearLayout.setMinWidth(left);
                    break;
                case END_TO_START:
                    int right = linearLayout.getWidth();
                    for (Widget child : linearLayout) {
                        if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeTop(linearLayout.getHeight());
                        child.moveRelativeRight(right);
                        right -= child.getWidth();
                        linearLayout.setMinHeight(Math.max(linearLayout.getMinHeight(),
                                child.getHeight()));
                    }

                    linearLayout.setMinWidth(linearLayout.getWidth() - right);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    },
    VERTICAL {
        @Override
        void layout(LinearLayout linearLayout) {
            switch (linearLayout.getDirection()) {
                case START_TO_END:
                    int top = linearLayout.getHeight();
                    for (Widget child : linearLayout) {
                        if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeLeft(0);
                        child.moveRelativeTop(top);
                        top -= child.getHeight();
                        linearLayout.setMinWidth(Math.max(linearLayout.getMinWidth(),
                                child.getWidth()));
                    }

                    linearLayout.setMinHeight(linearLayout.getHeight() - top);
                    break;
                case END_TO_START:
                    int bottom = 0;
                    for (Widget child : linearLayout) {
                        if (child instanceof WidgetParent) {
                            ((WidgetParent)child).requestLayout();
                        }

                        child.moveRelativeLeft(0);
                        child.moveRelativeBottom(bottom);
                        bottom += child.getHeight();
                        linearLayout.setMinWidth(Math.max(linearLayout.getMinWidth(),
                                child.getWidth()));
                    }

                    linearLayout.setMinHeight(bottom);
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

public LinearLayout() {
    this(Orientation.HORIZONTAL);
}
public LinearLayout(Orientation orientation) {
    this(orientation, Direction.START_TO_END);
}
public LinearLayout(Orientation orientation, Direction direction) {
    setDirection(direction);
    setOrientation(orientation);
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
