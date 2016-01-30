package com.gmail.collinsmith70.unifi.widget;

public class Widget {

public boolean isEnabled() { throw new UnsupportedOperationException(); }
public void setEnabled(boolean enabled) { throw new UnsupportedOperationException(); }

public boolean hasFocus() { throw new UnsupportedOperationException(); }
public boolean hasFocusable() { throw new UnsupportedOperationException(); }

public boolean isFocused() { throw new UnsupportedOperationException(); }
public final boolean isFocusable() { throw new UnsupportedOperationException(); }

public Object getTag() { throw new UnsupportedOperationException(); }
public final WidgetParent getParent() { throw new UnsupportedOperationException(); }
public Widget getRootWidget() { throw new UnsupportedOperationException(); }

public float getX() { throw new UnsupportedOperationException(); }
public void setX(float x) { throw new UnsupportedOperationException(); }

public float getY() { throw new UnsupportedOperationException(); }
public void setY(float y) { throw new UnsupportedOperationException(); }

public final int getWidth() { throw new UnsupportedOperationException(); }
public final int getHeight() { throw new UnsupportedOperationException(); }

public final int getBottom() { throw new UnsupportedOperationException(); }
public final int getLeft() { throw new UnsupportedOperationException(); }
public final int getRight() { throw new UnsupportedOperationException(); }
public final int getTop() { throw new UnsupportedOperationException(); }

public int getPaddingBottom() { throw new UnsupportedOperationException(); }
public int getPaddingLeft() { throw new UnsupportedOperationException(); }
public int getPaddingRight() { throw new UnsupportedOperationException(); }
public int getPaddingTop() { throw new UnsupportedOperationException(); }
public void setPadding(int left, int top, int right, int bottom) { throw new UnsupportedOperationException(); }

public Visibility getVisibility() { throw new UnsupportedOperationException(); }
public void setVisibility(Visibility visibility) { throw new UnsupportedOperationException(); }

public enum Visibility {
    VISIBLE,
    INVISIBLE,
    GONE
}

}
