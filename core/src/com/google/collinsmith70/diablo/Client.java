package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.util.EffectivelyFinal;

public class Client implements ApplicationListener {
private final int VIRTUAL_WIDTH;
private final int VIRTUAL_HEIGHT;

@EffectivelyFinal
private ClientInputProcessor INPUT_PROCESSOR;

@EffectivelyFinal
private ClientCommandProcessor COMMAND_PROCESSOR;

@EffectivelyFinal
private AssetManager ASSET_MANAGER;

@EffectivelyFinal
private Stage STAGE;

@EffectivelyFinal
private Console CONSOLE;

private boolean showFps;

public Client(int virtualWidth, int virtualHeight) {
    this.VIRTUAL_WIDTH = virtualWidth;
    this.VIRTUAL_HEIGHT = virtualHeight;
    this.showFps = false;
}

public int getVirtualWidth() {
    return VIRTUAL_WIDTH;
}

public int getVirtualHeight() {
    return VIRTUAL_HEIGHT;
}

public ClientInputProcessor getInputProcessor() {
    return INPUT_PROCESSOR;
}

public AssetManager getAssetManager() {
    return ASSET_MANAGER;
}

public Console getConsole() {
    return CONSOLE;
}

/**
 * Called when the {@link Application} is first created.
 */
@Override
public void create() {
    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
        Timer.schedule(new ControllerDetectorTask(), 1.0f, 1.0f)
                .run();
    }

    this.STAGE = new Stage();
    STAGE.setViewport(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    Cvars.Client.Overlay.ShowFps.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            Client.this.showFps = toValue;
        }
    });

    this.COMMAND_PROCESSOR = new ClientCommandProcessor(this);

    this.ASSET_MANAGER = new AssetManager();
    this.CONSOLE = new Console(this);
    CONSOLE.addCommandProcessor(COMMAND_PROCESSOR);

    this.INPUT_PROCESSOR = new ClientInputProcessor(this);
    Gdx.input.setInputProcessor(INPUT_PROCESSOR);
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
    STAGE.getViewport().update(width, height, true);
}

/**
 * Called when the {@link Application} should render itself.
 */
@Override
public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    STAGE.act(Gdx.graphics.getDeltaTime());
    STAGE.draw();
    Batch b = STAGE.getBatch();
    b.begin(); {
        if (CONSOLE.isVisible()) {
            CONSOLE.render(b);
        } else if (showFps) {
            CONSOLE.getFont()
                    .draw(b, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, VIRTUAL_HEIGHT);
        }
    } b.end();
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
    STAGE.dispose();
    CONSOLE.dispose();
    ASSET_MANAGER.dispose();
}
}
