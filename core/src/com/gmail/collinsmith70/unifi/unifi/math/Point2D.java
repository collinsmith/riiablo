package com.gmail.collinsmith70.unifi.unifi.math;

public class Point2D {

public Point2D() {
    this(0, 0);
}
public Point2D(int x, int y) {
    this.x = x;
    this.y = y;
}
public Point2D(Point2D point) {
    this.x = point.getX();
    this.y = point.getY();
}

private int x;
public int getX() {
    return x;
}
public void setX(int x) {
    this.x = x;
}

private int y;
public int getY() {
    return y;
}
public void setY(int y) {
    this.y = y;
}

public void set(int x, int y) {
    setX(x);
    setY(y);
}

@Override public String toString() {
    return String.format("(%d, %d)", getX(), getY());
}

}
