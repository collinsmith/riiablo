package com.gmail.collinsmith70.unifi.layout;

import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;

public class LinearLayout extends WidgetGroup {

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
    int top = 0;
    for (Widget child : getChildren()) {
        child.setBounds(top, getLeft(), getRight(), top);
    }
}

}
