package com.gmail.collinsmith70.util;

public interface BufferListener {

void modified(String buffer);
void commit(String buffer);
boolean flush();

}
