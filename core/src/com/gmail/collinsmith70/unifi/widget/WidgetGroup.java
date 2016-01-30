package com.gmail.collinsmith70.unifi.widget;

public class WidgetGroup extends Widget implements WidgetParent, WidgetManager {

public void addWidget(Widget child) { throw new UnsupportedOperationException(); }
public boolean containsWidget(Widget child) { throw new UnsupportedOperationException(); }
public boolean removeWidget(Widget child) { throw new UnsupportedOperationException(); }

public int getChildCount() { throw new UnsupportedOperationException(); }

public int getMarginBottom() { throw new UnsupportedOperationException(); }
public int getMarginLeft() { throw new UnsupportedOperationException(); }
public int getMarginRight() { throw new UnsupportedOperationException(); }
public int getMarginTop() { throw new UnsupportedOperationException(); }

public void requestLayout() { throw new UnsupportedOperationException(); }

}
