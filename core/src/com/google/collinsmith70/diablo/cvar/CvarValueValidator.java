package com.google.collinsmith70.diablo.cvar;

public interface CvarValueValidator<T> {

T onValidateValue(Cvar<T> cvar, T fromValue, T toValue);

}
