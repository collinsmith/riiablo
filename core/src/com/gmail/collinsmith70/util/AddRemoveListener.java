package com.gmail.collinsmith70.util;

public interface AddRemoveListener<C, T> {

void onAdded(T obj, C instance);
void onRemoved(T obj, C instance);
void onLoad(T obj, C instance);

}
