package com.gmail.collinsmith70.util;

public class AddRemoveAdapter<C, T> implements AddRemoveListener<C, T> {

@Override
public void onAdded(T obj, C instance) {

}

@Override
public void onRemoved(T obj, C instance) {

}

@Override
public void onLoad(T obj, C instance) {
    onAdded(obj, instance);
}

}
