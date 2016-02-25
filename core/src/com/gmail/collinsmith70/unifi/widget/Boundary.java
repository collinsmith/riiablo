package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.IntRange;

public interface Boundary {

int getBottom();
int getLeft();
int getRight();
int getTop();

void setBottom(int bottom);
void setLeft(int left);
void setRight(int right);
void setTop(int top);

void moveBottom(int bottom);
void moveLeft(int left);
void moveRight(int right);
void moveTop(int top);

int getX();
int getY();
@IntRange(from = 0, to = Integer.MAX_VALUE) int getWidth();
@IntRange(from = 0, to = Integer.MAX_VALUE) int getHeight();

void setX(int x);
void setY(int y);
void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width);
void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height);

void setBounds(int left, int right, int top, int bottom);
boolean inBounds(int x, int y);
boolean hasSize();
void setPosition(int x, int y);
void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
             @IntRange(from = 0, to = Integer.MAX_VALUE) int height);

}
