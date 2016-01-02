package com.gmail.collinsmith70.cvar;

public interface ValueValidator<T> {

boolean isValid(T obj);
T getValidatedValue(T obj);

}
