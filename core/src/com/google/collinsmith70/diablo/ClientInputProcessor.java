package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.google.collinsmith70.diablo.key.Keys;

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
    if (Keys.CONSOLE.containsKey(keycode)) {
        CLIENT.getConsole().setVisible(!CLIENT.getConsole().isVisible());
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            Gdx.input.setOnscreenKeyboardVisible(CLIENT.getConsole().isVisible());
        }

        return true;
    }

    if (CLIENT.getConsole().isVisible()) {
        switch (keycode) {
            case Input.Keys.LEFT:
            case Input.Keys.RIGHT:
            case Input.Keys.UP:
            case Input.Keys.DOWN:
            case Input.Keys.TAB:
                if (CLIENT.getConsole().isVisible()) {
                    return CLIENT.getConsole().keyDown(keycode);
                }

                break;
            case Input.Keys.MENU:
                CLIENT.getConsole().setVisible(!CLIENT.getConsole().isVisible());
                break;
            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
                if (CLIENT.getConsole().isVisible()) {
                    CLIENT.getConsole().setVisible(false);
                    return true;
                }

                break;
            default:
                return true;
        }
    }

    if (getClient().getStage() != null && getClient().getStage().keyDown(keycode)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().keyDown(keycode)) {
        return true;
    }

    return false;
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
    if (CLIENT.getConsole().isVisible()) {
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().keyUp(keycode)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().keyUp(keycode)) {
        return true;
    }

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
    if (Keys.CONSOLE.containsKey(ch)) {
        return true;
    } else if (CLIENT.getConsole().isVisible()) {
        CLIENT.getConsole().keyTyped(ch);
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().keyTyped(ch)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().keyTyped(ch)) {
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
    if (CLIENT.getConsole().isVisible()) {
        Gdx.input.setOnscreenKeyboardVisible(true);
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().touchDown(screenX, screenY, pointer, button)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().touchDown(screenX, screenY, pointer, button)) {
        return true;
    }

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
    if (CLIENT.getConsole().isVisible()) {
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().touchUp(screenX, screenY, pointer, button)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().touchUp(screenX, screenY, pointer, button)) {
        return true;
    }

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
    if (CLIENT.getConsole().isVisible()) {
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().touchDragged(screenX, screenY, pointer)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().touchDragged(screenX, screenY, pointer)) {
        return true;
    }

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
    if (CLIENT.getConsole().isVisible()) {
        return true;
    }

    if (getClient().getStage() != null && getClient().getStage().mouseMoved(screenX, screenY)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().mouseMoved(screenX, screenY)) {
        return true;
    }

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
    if (CLIENT.getConsole().isVisible()) {
        return CLIENT.getConsole().scrolled(amount);
    }

    if (getClient().getStage() != null && getClient().getStage().scrolled(amount)) {
        return true;
    } else if (getClient().getScene() != null && getClient().getScene().scrolled(amount)) {
        return true;
    }

    return false;
}

}
