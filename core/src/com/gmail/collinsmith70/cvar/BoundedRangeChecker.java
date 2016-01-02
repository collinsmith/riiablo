package com.gmail.collinsmith70.cvar;

public interface BoundedRangeChecker<T> extends BoundsChecker<T> {

T getMin();
T getMax();

}
