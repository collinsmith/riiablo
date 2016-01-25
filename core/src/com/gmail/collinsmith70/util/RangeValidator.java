package com.gmail.collinsmith70.util;

public interface RangeValidator<T extends Comparable<? super T>> extends Validator<T> {

T getMin();
T getMax();

}
