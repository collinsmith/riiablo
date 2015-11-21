package com.google.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.collinsmith70.diablo.asset.Assets;
import com.google.collinsmith70.diablo.asset.loader.VolumeControlledMusicLoader;
import com.google.collinsmith70.diablo.asset.loader.VolumeControlledSoundLoader;
import com.google.collinsmith70.diablo.audio.SoundVolumeController;
import com.google.collinsmith70.diablo.audio.VolumeController;
import com.google.collinsmith70.diablo.command.Commands;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.key.Keys;
import com.google.collinsmith70.diablo.lang.Langs;
import com.google.collinsmith70.diablo.scene.AbstractScene;
import com.google.collinsmith70.diablo.scene.SplashScene;

import java.util.Locale;

public class Client implements ApplicationListener {
private static final String TAG = Client.class.getSimpleName();

private final int VIRTUAL_WIDTH;
private final int VIRTUAL_HEIGHT;

private final boolean FORCE_WINDOWED;

private ClientInputProcessor INPUT_PROCESSOR;
private ClientCommandProcessor COMMAND_PROCESSOR;
private AssetManager ASSET_MANAGER;
private Stage STAGE;
private Console CONSOLE;
private MusicController MUSIC_CONTROLLER;

private AbstractScene scene;

private boolean vibratingEnabled;
private boolean showFps;
private I18NBundle i18nBundle;

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
    this.vibratingEnabled = true;
}

public int getVirtualWidth() {
    return VIRTUAL_WIDTH;
}

public int getVirtualHeight() {
    return VIRTUAL_HEIGHT;
}

public Stage getStage() {
    return STAGE;
}

public VolumeController<Sound> getSoundVolumeController() {
    return soundVolumeController;
}

public VolumeController<Music> getMusicVolumeController() {
    return musicVolumeController;
}

public MusicController getMusicController() {
    return MUSIC_CONTROLLER;
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

public I18NBundle getI18NBundle() {
    return i18nBundle;
}

public boolean isVibratingEnabled() {
    return vibratingEnabled;
}

/**
 * Called when the {@link Application} is first created.
 */
@Override
public void create() {
    Commands.loadAll();
    Cvars.loadAll();
    Keys.loadAll();
    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
        //Timer.schedule(new ControllerDetectorTask(), 1.0f, 1.0f)
        //        .run();
    }

    this.STAGE = new Stage();
    STAGE.setViewport(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    Cvars.Client.Overlay.ShowFps.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            Client.this.showFps = toValue;
        }
    });

    Cvars.Client.Input.Vibrations.addCvarChangeListener(new CvarChangeListener<Boolean>() {
        @Override
        public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
            Client.this.vibratingEnabled = toValue;
        }
    });

    if (Gdx.app.getType() == Application.ApplicationType.Desktop && !FORCE_WINDOWED) {
        Cvars.Client.Render.Windowed.addCvarChangeListener(new CvarChangeListener<Boolean>() {
            @Override
            public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
                Gdx.graphics.setDisplayMode(
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight(),
                        !toValue);
            }
        });
    }

    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
        Cvars.Client.Render.VSync.addCvarChangeListener(new CvarChangeListener<Boolean>() {
            @Override
            public void onCvarChanged(Cvar<Boolean> cvar, Boolean fromValue, Boolean toValue) {
                Gdx.graphics.setVSync(toValue);
            }
        });
    }

    Cvars.Client.Locale.addCvarChangeListener(new CvarChangeListener<Locale>() {
        @Override
        public void onCvarChanged(Cvar<Locale> cvar, Locale fromValue, Locale toValue) {
            Gdx.app.log(TAG, "Client language changed to " + cvar.getStringValue());
            FileHandle i18nBundleFileHandle = Gdx.files.internal("lang/Client");
            Client.this.i18nBundle = I18NBundle.createBundle(i18nBundleFileHandle, toValue);
            Gdx.graphics.setTitle(Client.this.getI18NBundle().get(Langs.Client.diablo));
        }
    });

    Cvars.Client.Render.Brightness.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            //...
        }
    });

    Cvars.Client.Render.Gamma.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            //...
        }
    });

    this.COMMAND_PROCESSOR = new ClientCommandProcessor(this);

    FileHandleResolver resolver = new InternalFileHandleResolver();
    this.ASSET_MANAGER = new AssetManager(resolver);
    setAssetLoaders(resolver);
    loadCommonAssets();

    this.CONSOLE = new Console(this);
    CONSOLE.addCommandProcessor(COMMAND_PROCESSOR);

    this.MUSIC_CONTROLLER = new MusicController(getAssetManager());

    Gdx.input.setCatchMenuKey(true);
    Gdx.input.setCatchBackKey(true);
    this.INPUT_PROCESSOR = new ClientInputProcessor(this);
    Gdx.input.setInputProcessor(INPUT_PROCESSOR);

    setScene(new SplashScene(this));
}

private void setAssetLoaders(FileHandleResolver resolver) {
    Gdx.app.log(TAG, "Setting asset loaders...");

    soundVolumeController = new SoundVolumeController();
    Gdx.app.log(TAG, String.format("%s loader set to %s",
            Sound.class.getSimpleName(),
            VolumeControlledSoundLoader.class.getSimpleName()));
    ASSET_MANAGER.setLoader(Sound.class, new VolumeControlledSoundLoader(resolver, soundVolumeController));

    musicVolumeController = new com.google.collinsmith70.diablo.audio.MusicVolumeController();
    Gdx.app.log(TAG, String.format("%s loader set to %s",
            Music.class.getSimpleName(),
            VolumeControlledMusicLoader.class.getSimpleName()));
    ASSET_MANAGER.setLoader(
            Music.class,
            new VolumeControlledMusicLoader(resolver, musicVolumeController));
}

private void loadCommonAssets() {
    Gdx.app.log(TAG, "Loading common assets...");

    Gdx.app.log(TAG, "Loading font: " + Assets.Client.Font.Exocet._16.fileName);
    ASSET_MANAGER.load(Assets.Client.Font.Exocet._16);
    Gdx.app.log(TAG, "Loading font: " + Assets.Client.Font.Exocet._24.fileName);
    ASSET_MANAGER.load(Assets.Client.Font.Exocet._24);
    Gdx.app.log(TAG, "Loading font: " + Assets.Client.Font.Exocet._32.fileName);
    ASSET_MANAGER.load(Assets.Client.Font.Exocet._32);
    Gdx.app.log(TAG, "Loading font: " + Assets.Client.Font.Exocet._42.fileName);
    ASSET_MANAGER.load(Assets.Client.Font.Exocet._42);

    Gdx.app.log(TAG, "Loading sound: " + Assets.Client.Sound.SELECT.fileName);
    ASSET_MANAGER.load(Assets.Client.Sound.SELECT);
    Gdx.app.log(TAG, "Loading sound: " + Assets.Client.Sound.BUTTON.fileName);
    ASSET_MANAGER.load(Assets.Client.Sound.BUTTON);

    ASSET_MANAGER.finishLoading();
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
