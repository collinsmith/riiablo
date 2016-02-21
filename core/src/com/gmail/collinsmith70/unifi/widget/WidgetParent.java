package com.gmail.collinsmith70.unifi.widget;

public interface WidgetParent extends Parentable {

int getX();
int getY();
int getWidth();
int getHeight();

int getLeft();
int getRight();
int getTop();
int getBottom();

void requestLayout();

}
