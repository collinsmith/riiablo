package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.google.collinsmith70.util.EffectivelyFinal;

public class Client implements ApplicationListener {
public static final String CLIENT_PREFS = Client.class.getCanonicalName();

private final int VIRTUAL_WIDTH;
private final int VIRTUAL_HEIGHT;

@EffectivelyFinal
private CvarManager CVAR_MANAGER;

public Client(int virtualWidth, int virtualHeight) {
    this.VIRTUAL_WIDTH = virtualWidth;
    this.VIRTUAL_HEIGHT = virtualHeight;
}

public CvarManager getCvarManager() {
    return CVAR_MANAGER;
}

/**
 * Called when the {@link Application} is first created.
 */
@Override
public void create() {
    final Preferences PREFERENCES = Gdx.app.getPreferences(CLIENT_PREFS);
    CVAR_MANAGER = new CvarManager(PREFERENCES);
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

}

/**
 * Called when the {@link Application} should render itself.
 */
@Override
public void render() {

}

/**
 * Called when the {@link Application} is paused, usually when it's not active or visible on screen.
 * An Application is also paused before it is destroyed.
 */
@Override
public void pause() {

}

/**
 * Called when the {@link Application} is resumed from a paused state, usually when it regains
 * focus.
 */
@Override
public void resume() {

}

/**
 * Called when the {@link Application} is destroyed. Preceded by a call to {@link #pause()}.
 */
@Override
public void dispose() {

}
}
