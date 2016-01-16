package com.gmail.collinsmith70.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Console extends PrintStream {

private static final int DEFAULT_BUFFER_CAPACITY = 128;

private final int INITIAL_BUFFER_CAPACITY;
private final Set<BufferListener> BUFFER_LISTENERS;

private StringBuffer buffer;

public Console() {
    this(DEFAULT_BUFFER_CAPACITY);
}

public Console(int bufferLength) {
    this(bufferLength, System.out);
}

public Console(int initialBufferCapacity, OutputStream out) {
    super(out, true);
    if (initialBufferCapacity < 0) {
        throw new IllegalArgumentException(String.format(
                "Invalid console initial buffer capacity given (%d). " +
                "Console buffer size should be >= 0",
                initialBufferCapacity));
    }

    this.INITIAL_BUFFER_CAPACITY = initialBufferCapacity;
    this.BUFFER_LISTENERS = new CopyOnWriteArraySet<BufferListener>();
}

@Override
public void flush() {
    super.flush();
    this.buffer = new StringBuffer(INITIAL_BUFFER_CAPACITY);
}

public StringBuffer getBuffer() {
    return buffer;
}

public String getBufferContents() {
    return buffer.toString();
}

public void addBufferListener(BufferListener l) {
    BUFFER_LISTENERS.add(l);
}

public boolean removeBufferListener(BufferListener l) {
    return BUFFER_LISTENERS.remove(l);
}

public boolean containsBufferListener(BufferListener l) {
    return BUFFER_LISTENERS.contains(l);
}

}
