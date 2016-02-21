package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class Window implements WidgetParent, WidgetManager, InputProcessor, Disposable {

private final Collection<Widget> CHILDREN;

private int width;
private int height;

public Window(int width, int height) {
    setWidth(width);
    setHeight(height);
    this.CHILDREN = new ConcurrentSkipListSet<Widget>();
}

@Override
public WidgetParent getParent() {
    return null;
}

@Override
public boolean hasParent() {
    return false;
}

public void setDebugging(boolean debugging) {
    for (Widget child : CHILDREN) {
        child.setDebugging(true);
    }
}

@Override public int getBottom() {
    return 0;
}
@Override public int getLeft() {
    return 0;
}
@Override public int getRight() {
    return getWidth();
}
@Override public int getTop() {
    return getHeight();
}

@Override public int getX() {
    return 0;
}
@Override public int getY() {
    return 0;
}

public int getWidth() { return width; }
public void setWidth(int width) {
    if (width < 0) {
        throw new IllegalArgumentException(
                "width should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.width = width;
}

public int getHeight() { return height; }
public void setHeight(int height) {
    if (height < 0) {
        throw new IllegalArgumentException(
                "height should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    this.height = height;
}

@Override public boolean keyDown(int keycode) { return false; }
@Override public boolean keyUp(int keycode) { return false; }
@Override public boolean keyTyped(char character) { return false; }
@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
@Override public boolean mouseMoved(int screenX, int screenY) {
    // LibGDX is dumb and uses two different coordinate systems for rendering and input, so screenY
    // needs to be inverted in order for the correct screenY to be used for graphics
    for (Widget child : CHILDREN) {
        if (child.mouseMoved(screenX, getHeight()-screenY)) {
            return true;
        }
    }

    return false;
}
@Override public boolean scrolled(int amount) { return false; }

@Override public void requestLayout() {
    for (Widget child : CHILDREN) {
        if (child instanceof WidgetParent) {
            ((WidgetParent)child).requestLayout();
        }
    }
}

@NonNull
@Override
public WidgetManager addWidget(@NonNull Widget child) {
    if (child == null) {
        throw new IllegalArgumentException("child widget cannot be null");
    }

    CHILDREN.add(child);
    child.setParent(this);
    return this;
}

@Override
public boolean containsWidget(@Nullable Widget child) {
    return child != null && CHILDREN.contains(child);
}

@Override
public boolean removeWidget(@Nullable Widget child) {
    if (child == null) {
        return false;
    }

    if (child.getParent() == this) {
        child.setParent(null);
    }

    return CHILDREN.remove(child);
}

@Override
public int getNumWidgets() {
    return CHILDREN.size();
}

public void clear() {
    CHILDREN.clear();
}

@Override
public void dispose() {
    clear();
}

public void draw(Batch batch) {
    drawChildren(batch);
}

public void drawChildren(Batch batch) {
    for (Widget child : CHILDREN) {
        child.draw(batch);
    }
}

}
