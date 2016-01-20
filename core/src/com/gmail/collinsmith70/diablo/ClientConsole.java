package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.gmail.collinsmith70.util.Console;

import java.io.OutputStream;

public class ClientConsole extends Console implements InputProcessor {

private boolean isVisible;
private int position;

public ClientConsole() {
    super();
    setVisible(false);
}

public ClientConsole(OutputStream out) {
    super(out);
    setVisible(false);
}

public boolean isVisible() { return isVisible; }
public void setVisible(boolean b) { this.isVisible = b; }

public int getPosition() { return position; }
public void setPosition(int i) {
    if (i < 0) {
        throw new IllegalArgumentException("position should be positive");
    } else if (i >= getBuffer().length()) {
        throw new IllegalArgumentException("position should less than buffer length: " + getBuffer().length());
    }

    this.position = i;
}

@Override
public void clearBuffer() {
    super.clearBuffer();
    setPosition(0);
}

@Override
public boolean keyTyped(char ch) {
    switch (ch) {
        case '\0':
            return true;
        case '\b':
            if (position > 0) {
                getBuffer().deleteCharAt(position - 1);
                setPosition(position - 1);
            }

            return true;
        case '\r':case '\n':
            if (getBuffer().length() > 0) {
                commitBuffer();
            }

            return true;
        case 127: // DEL
            if (position < getBuffer().length()) {
                getBuffer().deleteCharAt(position);
            }

            return true;
        default:
            getBuffer().insert(position, ch);
            setPosition(position + 1);
            return true;
    }
}

}
