package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;
import com.gmail.collinsmith70.unifi.widget.WidgetManager;

public class LinearLayout extends WidgetGroup {

public enum Direction { START_TO_END, END_TO_START }

public enum Orientation {
    HORIZONTAL {
        @Override
        void layout(LinearLayout linearLayout) {
            switch (linearLayout.getDirection()) {
                case START_TO_END:
                    int left = linearLayout.getLeft();
                    for (Widget child : linearLayout.getChildren()) {
                        child.moveTop(linearLayout.getTop());
                        child.moveLeft(left);
                        left += child.getWidth();
                    }
                    break;
                case END_TO_START:
                    int right = linearLayout.getRight();
                    for (Widget child : linearLayout.getChildren()) {
                        child.moveTop(linearLayout.getTop());
                        child.moveRight(right);
                        right -= child.getWidth();
                    }
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
                    int top = linearLayout.getTop();
                    for (Widget child : linearLayout.getChildren()) {
                        child.moveLeft(linearLayout.getLeft());
                        child.moveTop(top);
                        top -= child.getHeight();
                    }
                    break;
                case END_TO_START:
                    int bottom = linearLayout.getBottom();
                    for (Widget child : linearLayout.getChildren()) {
                        child.moveLeft(linearLayout.getLeft());
                        child.moveBottom(bottom);
                        bottom += child.getHeight();
                    }
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

@NonNull
@Override
public WidgetManager addWidget(@NonNull Widget child) {
    WidgetManager widgetManager = super.addWidget(child);
    requestLayout();
    return widgetManager;
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

@Override
public boolean keyDown(int keycode) {
    return false;
}

@Override
public boolean keyUp(int keycode) {
    return false;
}

@Override
public boolean keyTyped(char character) {
    return false;
}

@Override
public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
}

@Override
public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
}

@Override
public boolean scrolled(int amount) {
    return false;
}

@Override
public void requestLayout() {
    getOrientation().layout(this);
}

}
