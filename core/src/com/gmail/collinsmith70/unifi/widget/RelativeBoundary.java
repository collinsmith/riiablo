package com.gmail.collinsmith70.unifi.widget;

public interface RelativeBoundary extends Boundary {

int getRelativeBottom();
int getRelativeLeft();
int getRelativeRight();
int getRelativeTop();

void setRelativeBottom(int bottom);
void setRelativeLeft(int left);
void setRelativeRight(int right);
void setRelativeTop(int top);

void moveRelativeBottom(int bottom);
void moveRelativeLeft(int left);
void moveRelativeRight(int right);
void moveRelativeTop(int top);

void setRelativeBounds(int left, int right, int top, int bottom);

}
