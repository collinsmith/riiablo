package com.gmail.collinsmith70.unifi.widget;

import com.gmail.collinsmith70.unifi.util.Focusable;

public interface WidgetParent extends Parentable, Focusable, Boundary {

void requestLayout();

}
