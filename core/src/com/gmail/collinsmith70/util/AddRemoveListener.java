package com.gmail.collinsmith70.util;

public interface AddRemoveListener<C, T> {

void onAdded(T alias, C instance);
void onRemoved(T alias, C instance);

}
