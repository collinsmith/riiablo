package com.gmail.collinsmith70.key;

public interface KeyStateListener<T> {

void onPressed(Key<T> key, T binding);
void onDepressed(Key<T> key, T binding);

}
