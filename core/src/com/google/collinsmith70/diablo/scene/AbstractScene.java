package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.google.collinsmith70.diablo.Client;

public abstract class AbstractScene extends WidgetGroup implements InputProcessor, ApplicationListener {

private final Client CLIENT;

public AbstractScene(Client client) {
    if (client == null) {
        throw new IllegalArgumentException("Client cannot be null");
    }

    this.CLIENT = client;

    setFillParent(true);
    addListener(new PropagatingEventHandler(AbstractScene.this));
}

public Client getClient() {
    return CLIENT;
}

@Override
public void draw(Batch batch, float parentAlpha) {
    validate();
    drawBackground(batch);
    super.draw(batch, parentAlpha);
}

protected void drawBackground(Batch batch) {
    //...
}

/**
 * Called when the {@link Application} is first created.
 */
@Override
public void create() {
    AssetManager assetManager = getClient().getAssetManager();
    loadAssets(assetManager);
    assetManager.finishLoading();
}

public void loadAssets(AssetManager assetManager) {

}

/**
 * Called when the {@link Application} is resized. This can happen at any point during a non-paused
 * state but will never happen before a call to {@link #create()}.
 *
 * @param width  the new width in pixels
 * @param height the new height in pixels
 */
@Override
public void resize(int width, int height) {
    //...
}

/**
 * Called when the {@link Application} should render itself.
 */
@Override
public void render() {
    //...
}

/**
 * Called when the {@link Application} is paused, usually when it's not active or visible on screen.
 * An Application is also paused before it is destroyed.
 */
@Override
public void pause() {
    //...
}

/**
 * Called when the {@link Application} is resumed from a paused state, usually when it regains
 * focus.
 */
@Override
public void resume() {
    //...
}

/**
 * Called when the {@link Application} is destroyed. Preceded by a call to {@link #pause()}.
 */
@Override
public void dispose() {
    //...
}

/**
 * Called when a key was pressed
 *
 * @param keycode one of the constants in {@link Input.Keys}
 *
 * @return whether the input was processed
 */
@Override
public boolean keyDown (int keycode) {
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
public boolean keyUp (int keycode) {
    return false;
}

/**
 * Called when a key was typed
 *
 * @param character The character
 *
 * @return whether the input was processed
 * */
@Override
public boolean keyTyped(char character) {
    return false;
}

/**
 * Called when the screen was touched or a mouse button was pressed. The button parameter will be
 * {@link Input.Buttons#LEFT} on iOS.
 *
 * @param screenX x coordinate, origin is in the upper left corner
 * @param screenY y coordinate, origin is in the upper left corner
 * @param pointer pointer for the event.
 * @param button  button calling event
 *
 * @return whether the input was processed
 */
@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
}

/**
 * Called when a finger was lifted or a mouse button was released. The button parameter will be
 * {@link Input.Buttons#LEFT} on iOS.
 *
 * @param pointer pointer for the event.
 * @param button  button calling event
 *
 * @return whether the input was processed
 */
@Override
public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
}

/**
 * Called when a finger or the mouse was dragged.
 *
 * @param pointer pointer for the event.
 *
 * @return whether the input was processed
 */
@Override
public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
}

/**
 * Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
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
