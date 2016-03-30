package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.InputProcessor;
import com.gmail.collinsmith70.key.Key;
import com.google.common.base.Preconditions;

public class ClientInputProcessor implements InputProcessor {

  private static final String TAG = ClientInputProcessor.class.getSimpleName();

  private final Client CLIENT;
  private final GdxKeyManager KEY_MANAGER;
  private final InputProcessor PROPAGATING;

  public ClientInputProcessor(Client client) {
    this(client, null);
  }

  public ClientInputProcessor(Client client, InputProcessor ip) {
    this.CLIENT = Preconditions.checkNotNull(client);
    this.KEY_MANAGER = Preconditions.checkNotNull(CLIENT.getKeyManager());
    this.PROPAGATING = ip;
  }

  public Client getClient() {
    return CLIENT;
  }

  @Override
  public boolean keyDown(int keycode) {
    Key<Integer> key = KEY_MANAGER.get(keycode);
    if (key != null) {
      key.setPressed(true, keycode);
    }

    return PROPAGATING.keyDown(keycode);
  }

  @Override
  public boolean keyUp(int keycode) {
    Key<Integer> key = KEY_MANAGER.get(keycode);
    if (key != null) {
      key.setPressed(false, keycode);
    }

    return PROPAGATING.keyUp(keycode);
  }

  @Override
  public boolean keyTyped(char ch) {
    return PROPAGATING.keyTyped(ch);
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return PROPAGATING.touchDown(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return PROPAGATING.touchUp(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return PROPAGATING.touchDragged(screenX, screenY, pointer);
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return PROPAGATING.mouseMoved(screenX, screenY);
  }

  @Override
  public boolean scrolled(int amount) {
    return PROPAGATING.scrolled(amount);
  }

}
