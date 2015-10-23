package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
private AssetManager ASSET_MANAGER;

@EffectivelyFinal
private BitmapFont CONSOLE_FONT;

@EffectivelyFinal
private Stage STAGE;

private boolean showFps;

public Client(int virtualWidth, int virtualHeight) {
    this.VIRTUAL_WIDTH = virtualWidth;
    this.VIRTUAL_HEIGHT = virtualHeight;
    this.showFps = false;
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

    AssetDescriptor<BitmapFont> consoleFont = Cvars.Client.Overlay.ConsoleFont.getValue();

    ASSET_MANAGER = new AssetManager();
    ASSET_MANAGER.load(consoleFont);
    ASSET_MANAGER.finishLoading();

    CONSOLE_FONT = ASSET_MANAGER.get(consoleFont);
    CONSOLE_FONT.setColor(new Color(
            Cvars.Client.Overlay.ConsoleFontColor.r.getValue(),
            Cvars.Client.Overlay.ConsoleFontColor.g.getValue(),
            Cvars.Client.Overlay.ConsoleFontColor.b.getValue(),
            Cvars.Client.Overlay.ConsoleFontColor.a.getValue()));

    this.STAGE = new Stage();
    STAGE.setViewport(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    Cvars.Client.Overlay.ShowFps.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            Client.this.showFps = toValue;
        }
    });
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
        if (showFps) {
            CONSOLE_FONT.draw(b, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, VIRTUAL_HEIGHT);
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
    ASSET_MANAGER.dispose();
}
}
