package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;

import java.util.Objects;

public class ClientInputProcessor implements InputProcessor {
private final Client CLIENT;

public ClientInputProcessor(Client client) {
    this.CLIENT = Objects.requireNonNull(client);
}

public Client getClient() {
    return CLIENT;
}

/**
 * Called when a key was pressed
 *
 * @param keycode one of the constants in {@link Input.Keys}
 *
 * @return whether the input was processed
 */
@Override
public boolean keyDown(int keycode) {
    switch (keycode) {
        case Input.Keys.LEFT:
        case Input.Keys.RIGHT:
        case Input.Keys.UP:
        case Input.Keys.DOWN:
        case Input.Keys.TAB:
            if (CLIENT.getConsole().isVisible()) {
                return CLIENT.getConsole().keyDown(keycode);
            }
        default:
            return false;
    }
}

/**
 * Called when a key was released
 *
 * @param keycode one of the constants in {@link Input.Keys}
 *
 * @return whether the input was processed
 */
@Override
public boolean keyUp(int keycode) {
    return false;
}

/**
 * Called when a key was typed
 *
 * @param ch The character
 *
 * @return whether the input was processed
 */
@Override
public boolean keyTyped(char ch) {
    // TODO: Implement KeyMap structure similar to CVars
    if (ch == '`') {
        CLIENT.getConsole().setVisible(!CLIENT.getConsole().isVisible());
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            Gdx.input.setOnscreenKeyboardVisible(CLIENT.getConsole().isVisible());
        }

        return true;
    } else if (CLIENT.getConsole().isVisible()) {
        CLIENT.getConsole().keyTyped(ch);
        return true;
    }

    return false;
}

/**
 * Called when the screen was touched or a mouse button was pressed. The button parameter will be
 * {@link Buttons#LEFT} on iOS.
 *
 * @param screenX The x coordinate, origin is in the upper left corner
 * @param screenY The y coordinate, origin is in the upper left corner
 * @param pointer the pointer for the event.
 * @param button  the button
 *
 * @return whether the input was processed
 */
@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
}

/**
 * Called when a finger was lifted or a mouse button was released. The button parameter will be
 * {@link Buttons#LEFT} on iOS.
 *
 * @param screenX
 * @param screenY
 * @param pointer the pointer for the event.
 * @param button  the button   @return whether the input was processed
 */
@Override
public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
}

/**
 * Called when a finger or the mouse was dragged.
 *
 * @param screenX
 * @param screenY
 * @param pointer the pointer for the event.  @return whether the input was processed
 */
@Override
public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
}

/**
 * Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
 *
 * @param screenX
 * @param screenY
 *
 * @return whether the input was processed
 */
@Override
public boolean mouseMoved(int screenX, int screenY) {
    return false;
}

/**
 * Called when the mouse wheel was scrolled. Will not be called on iOS.
 *
 * @param amount the scroll amount, -1 or 1 depending on the direction the wheel was scrolled.
 *
 * @return whether the input was processed.
 */
@Override
public boolean scrolled(int amount) {
    return false;
}
}
