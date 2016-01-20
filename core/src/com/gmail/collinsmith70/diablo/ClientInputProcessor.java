package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.InputProcessor;
import com.gmail.collinsmith70.key.Key;
import com.google.common.base.Preconditions;

public class ClientInputProcessor implements InputProcessor {

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

public Client getClient() { return CLIENT; }

@Override
public boolean keyDown(int keycode) {
    Key<Integer> key = KEY_MANAGER.get(keycode);
    if (key != null) {
        key.setPressed(true, keycode);
    }

    return false;
}

@Override
public boolean keyUp(int keycode) {
    Key<Integer> key = KEY_MANAGER.get(keycode);
    if (key != null) {
        key.setPressed(false, keycode);
    }

    return false;
}

@Override
public boolean keyTyped(char character) {
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
