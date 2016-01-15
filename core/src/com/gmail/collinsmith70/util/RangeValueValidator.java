package com.gmail.collinsmith70.util;

public interface RangeValueValidator<T extends Comparable<? super T>> extends Validator<T> {

public T getMin();
public T getMax();

}
