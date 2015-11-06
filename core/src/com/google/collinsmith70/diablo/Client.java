package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.collinsmith70.diablo.audio.*;
import com.google.collinsmith70.diablo.audio.SoundVolumeController;
import com.google.collinsmith70.diablo.command.Commands;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.loader.VolumeControlledMusicLoader;
import com.google.collinsmith70.diablo.loader.VolumeControlledSoundLoader;
import com.google.collinsmith70.diablo.scene.AbstractScene;
import com.google.collinsmith70.diablo.scene.SplashScene;
import com.google.collinsmith70.util.EffectivelyFinal;

public class Client implements ApplicationListener {
private static final String TAG = Client.class.getSimpleName();

private final int VIRTUAL_WIDTH;
private final int VIRTUAL_HEIGHT;

private final boolean FORCE_WINDOWED;

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

private AbstractScene scene;

private boolean showFps;

private VolumeController<Sound> soundVolumeController;
private VolumeController<Music> musicVolumeController;

public Client(int virtualWidth, int virtualHeight) {
    this(virtualWidth, virtualHeight, false);
}

public Client(int virtualWidth, int virtualHeight, boolean forceWindowed) {
    this.VIRTUAL_WIDTH = virtualWidth;
    this.VIRTUAL_HEIGHT = virtualHeight;
    this.FORCE_WINDOWED = forceWindowed;
    this.showFps = false;
}

public int getVirtualWidth() {
    return VIRTUAL_WIDTH;
}

public int getVirtualHeight() {
    return VIRTUAL_HEIGHT;
}

public VolumeController<Sound> getSoundVolumeController() {
    return soundVolumeController;
}

public VolumeController<Music> getMusicVolumeController() {
    return musicVolumeController;
}

public void setScene(AbstractScene scene) {
    scene.create();

    STAGE.clear();
    STAGE.addActor(scene);

    AbstractScene oldScene = this.scene;
    if (oldScene != null) {
        oldScene.dispose();
    }

    this.scene = scene;
}

public AbstractScene getScene() {
    return scene;
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
    Commands.loadAll();
    Cvars.loadAll();
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

    if (Gdx.app.getType() == Application.ApplicationType.Desktop && !FORCE_WINDOWED) {
        Cvars.Client.Render.Windowed.addCvarChangeListener(new CvarChangeListener<Boolean>() {
            @Override
            public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
                Gdx.graphics.setDisplayMode(
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight(),
                        toValue);
            }
        });
    }

    this.COMMAND_PROCESSOR = new ClientCommandProcessor(this);

    FileHandleResolver resolver = new InternalFileHandleResolver();
    this.ASSET_MANAGER = new AssetManager(resolver);

    soundVolumeController = new SoundVolumeController();
    ASSET_MANAGER.setLoader(
            Sound.class,
            new VolumeControlledSoundLoader(resolver, soundVolumeController));

    musicVolumeController = new com.google.collinsmith70.diablo.audio.MusicVolumeController();
    ASSET_MANAGER.setLoader(
            Music.class,
            new VolumeControlledMusicLoader(resolver, musicVolumeController));

    this.CONSOLE = new Console(this);
    CONSOLE.addCommandProcessor(COMMAND_PROCESSOR);

    Gdx.input.setCatchMenuKey(true);
    Gdx.input.setCatchBackKey(true);
    this.INPUT_PROCESSOR = new ClientInputProcessor(this);
    Gdx.input.setInputProcessor(INPUT_PROCESSOR);

    setScene(new SplashScene(this));
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
    if (getScene() != null) {
        getScene().resize(width, height);
    }
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
    if (getScene() != null) {
        getScene().pause();
    }
}

/**
 * Called when the {@link Application} is resumed from a paused state, usually when it regains
 * focus.
 */
@Override
public void resume() {
    if (getScene() != null) {
        getScene().resume();
    }
}

/**
 * Called when the {@link Application} is destroyed. Preceded by a call to {@link #pause()}.
 */
@Override
public void dispose() {
    Cvar.saveAll();
    Gdx.app.log(TAG, "Disposing scene...");
    getScene().dispose();
    Gdx.app.log(TAG, "Disposing stage...");
    STAGE.dispose();
    Gdx.app.log(TAG, "Disposing console...");
    CONSOLE.dispose();
    Gdx.app.log(TAG, "Disposing assets...");
    ASSET_MANAGER.dispose();
}
}
