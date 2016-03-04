package com.gmail.collinsmith70.unifi.unifi.math;

public class Boundary {

  private int bottom;
  private int left;
  private int right;
  private int top;

  public Boundary() {
    this(0, 0, 0, 0);
  }

  public Boundary(int left, int top, int right, int bottom) {
    this.bottom = bottom;
    this.left = left;
    this.right = right;
    this.top = top;
  }

  public Boundary(Boundary boundary) {
    this.bottom = boundary.getBottom();
    this.left = boundary.getLeft();
    this.right = boundary.getRight();
    this.top = boundary.getTop();
  }

  public int getBottom() {
    return bottom;
  }

  public int getLeft() {
    return left;
  }

  public int getRight() {
    return right;
  }

  public int getTop() {
    return top;
  }

  public void setBottom(int bottom) {
    this.bottom = bottom;
  }

  public void setLeft(int left) {
    this.left = left;
  }

  public void setRight(int right) {
    this.right = right;
  }

  public void setTop(int top) {
    this.top = top;
  }

  public void set(int left, int top, int right, int bottom) {
    setBottom(bottom);
    setLeft(left);
    setRight(right);
    setTop(top);
  }

  public ImmutableDimension2D getSize() {
    return new ImmutableDimension2D(Math.abs(getTop() - getBottom()),
            Math.abs(getLeft() - getRight()));
  }

  public boolean isEmpty() {
    return getLeft() != getRight() && getBottom() != getTop();
  }

  @Override
  public String toString() {
    return String.format("(%d, %d, %d, %d)", getLeft(), getTop(), getRight(), getBottom());
  }

}
