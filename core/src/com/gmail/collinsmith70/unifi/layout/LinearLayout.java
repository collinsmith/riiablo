package com.gmail.collinsmith70.unifi.layout;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;

public class LinearLayout extends WidgetGroup {

public enum Orientation {
    HORIZONTAL {
        @Override
        void layout(LinearLayout linearLayout) {
            int left = linearLayout.getLeft();
            for (Widget child : linearLayout.getChildren()) {
                child.setTop(linearLayout.getTop());
                child.setLeft(left);
                left += child.getWidth();
            }
        }
    },
    VERTICAL {
        @Override
        void layout(LinearLayout linearLayout) {int top = linearLayout.getTop();
            for (Widget child : linearLayout.getChildren()) {
                child.setTop(top);
                child.setLeft(linearLayout.getLeft());
                top += child.getHeight();
            }
        }
    };

    abstract void layout(LinearLayout linearLayout);
}

@NonNull private Orientation orientation;

@NonNull public Orientation getOrientation() {
    return orientation;
}
public void setOrientation(@NonNull Orientation orientation) {
    if (orientation == null) {
        throw new IllegalArgumentException("orientation cannot be null");
    }

    this.orientation = orientation;
}

public LinearLayout() {
    this(Orientation.HORIZONTAL);
}

public LinearLayout(Orientation orientation) {
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
