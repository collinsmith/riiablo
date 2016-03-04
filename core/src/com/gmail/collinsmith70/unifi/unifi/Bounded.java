package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.unifi.math.ImmutableBoundary;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutableDimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.ImmutablePoint2D;

public interface Bounded {

int getBottom();
int getLeft();
int getRight();
int getTop();

void setBottom(int bottom);
void setLeft(int left);
void setRight(int right);
void setTop(int top);

void setBounds(int left, int top, int right, int bottom);
@NonNull ImmutableBoundary getBounds();
boolean contains(int x, int y);

void setPosition(int x, int y);
@NonNull ImmutablePoint2D getPosition();

void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
             @IntRange(from = 0, to = Integer.MAX_VALUE) int height);
@NonNull ImmutableDimension2D getSize();
boolean hasSize();

int getX();
int getY();
@IntRange(from = 0, to = Integer.MAX_VALUE) int getWidth();
@IntRange(from = 0, to = Integer.MAX_VALUE) int getHeight();

void setX(int x);
void setY(int y);
void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width);
void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height);

}
