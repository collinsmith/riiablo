package com.gmail.collinsmith70.unifi.widget;

import com.badlogic.gdx.InputProcessor;

public class Window implements WidgetParent, WidgetManager, InputProcessor {

@Override public boolean keyDown(int keycode) { throw new UnsupportedOperationException(); }
@Override public boolean keyUp(int keycode) { throw new UnsupportedOperationException(); }
@Override public boolean keyTyped(char character) { throw new UnsupportedOperationException(); }
@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { throw new UnsupportedOperationException(); }
@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { throw new UnsupportedOperationException(); }
@Override public boolean touchDragged(int screenX, int screenY, int pointer) { throw new UnsupportedOperationException(); }
@Override public boolean mouseMoved(int screenX, int screenY) { throw new UnsupportedOperationException(); }
@Override public boolean scrolled(int amount) { throw new UnsupportedOperationException(); }

@Override public void addWidget(Widget child) { throw new UnsupportedOperationException(); }
@Override public boolean containsWidget(Widget child) { throw new UnsupportedOperationException(); }
@Override public boolean removeWidget(Widget child) { throw new UnsupportedOperationException(); }
@Override public int getNumWidgets() { throw new UnsupportedOperationException(); }

@Override public void requestLayout() { throw new UnsupportedOperationException(); }

}
