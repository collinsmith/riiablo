package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.InputProcessor;
import com.gmail.collinsmith70.util.Console;
import com.google.common.base.Preconditions;

import java.io.OutputStream;

public class ClientConsole extends Console implements InputProcessor {

  private final Client CLIENT;

  private int position;

  public ClientConsole(Client client) {
    super();
    this.CLIENT = Preconditions.checkNotNull(client);
  }

  public ClientConsole(Client client, OutputStream out) {
    super(out);
    this.CLIENT = Preconditions.checkNotNull(client);
  }

  public Client getClient() {
    return CLIENT;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int i) {
    if (i < 0) {
      throw new IllegalArgumentException("position should be positive");
    } else if (i > getBuffer().length()) {
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
          onBufferModified();
          setPosition(position - 1);
        }

        return true;
      case '\r':
      case '\n':
        if (getBuffer().length() > 0) {
          commitBuffer();
        }

        return true;
      case 127: // DEL
        if (position < getBuffer().length()) {
          getBuffer().deleteCharAt(position);
          onBufferModified();
        }

        return true;
      default:
        getBuffer().insert(position, ch);
        setPosition(position + 1);
        onBufferModified();
        return true;
    }
  }

  @Override
  public boolean keyDown(int keycode) {
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }

}
