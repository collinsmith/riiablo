package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.InputProcessor;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class Window implements WidgetParent, WidgetManager, InputProcessor {

private Dimension2D DIMENSION;
private final Collection<Widget> CHILDREN;

public Window(int width, int height) {
    this.DIMENSION = new Dimension(width, height);
    this.CHILDREN = new ConcurrentSkipListSet<Widget>();
}

@Override public boolean keyDown(int keycode) { return false; }
@Override public boolean keyUp(int keycode) { return false; }
@Override public boolean keyTyped(char character) { return false; }
@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
@Override public boolean scrolled(int amount) { return false; }

@Override public void requestLayout() { throw new UnsupportedOperationException(); }

@NonNull
@Override
public WidgetManager addWidget(@NonNull Widget child) {
    if (child == null) {
        throw new IllegalArgumentException("child widget cannot be null");
    }

    CHILDREN.add(child);
    return this;
}

@Override
public boolean containsWidget(@Nullable Widget child) {
    return child != null && CHILDREN.contains(child);
}

@Override
public boolean removeWidget(@Nullable Widget child) {
    return child != null && CHILDREN.remove(child);
}

@Override
public int getNumWidgets() {
    return CHILDREN.size();
}

}
