package com.gmail.collinsmith70.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Console extends PrintStream {

private static final int INITIAL_BUFFER_CAPACITY = 128;

private final Set<BufferListener> BUFFER_LISTENERS;

private StringBuffer buffer;

public Console() {
    this(null);
}

public Console(OutputStream out) {
    super(out, true);
    this.BUFFER_LISTENERS = new CopyOnWriteArraySet<BufferListener>();
}

@Override
public void flush() {
    super.flush();
    for (BufferListener bufferListener : BUFFER_LISTENERS) {
        bufferListener.flush();
    }
}

public void commitBuffer() {
    String bufferContents = getBufferContents();
    println(bufferContents);
    clearBuffer();

    for (BufferListener bufferListener : BUFFER_LISTENERS) {
        bufferListener.commit(bufferContents);
    }
}

public void clearBuffer() {
    this.buffer = new StringBuffer(INITIAL_BUFFER_CAPACITY);
}

public StringBuffer getBuffer() {
    return buffer;
}

protected void onBufferModified() {
    String bufferContents = getBufferContents();
    for (BufferListener bufferListener : BUFFER_LISTENERS) {
        bufferListener.modified(bufferContents);
    }
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
