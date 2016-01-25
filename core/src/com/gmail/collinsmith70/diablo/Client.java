package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarChangeAdapter;
import com.gmail.collinsmith70.diablo.scene.HudedScene;
import com.gmail.collinsmith70.diablo.serializer.AssetDescriptorStringSerializer;
import com.gmail.collinsmith70.diablo.widget.ClientConsoleWidget;
import com.gmail.collinsmith70.util.ImmutableDimension;

import java.awt.Dimension;
import java.io.OutputStream;
import java.io.PrintStream;

public class Client implements ApplicationListener {

private static final String TAG = Client.class.getSimpleName();

private final Dimension RESOLUTION;
private final PrintStream STDOUT;
private final PrintStream STDERR;

private ClientConsole CONSOLE;
private ClientConsoleWidget CONSOLE_WIDGET;

private CommandManager COMMAND_MANAGER;
private GdxCvarManager CVAR_MANAGER;
private GdxKeyManager KEY_MANAGER;
private AssetManager ASSET_MANAGER;

private Stage STAGE;

private HudedScene scene;

private byte pCvar_showFps;

private BitmapFont font;

public Client(int width, int height) {
    this.RESOLUTION = new ImmutableDimension(width, height);

    this.STDOUT = System.out;
    this.STDERR = System.err;

    FileHandle consoleFileHandle = Gdx.files.local("console.out");
    OutputStream consoleOut = consoleFileHandle.write(false);

    this.CONSOLE = new ClientConsole(consoleOut);
    System.setOut(CONSOLE);
    System.setErr(CONSOLE);
}

@Override
public void create() {
    this.CVAR_MANAGER = new GdxCvarManager();
    setSerializers();
    Cvars.addTo(CVAR_MANAGER);
    initPCvars();

    this.COMMAND_MANAGER = new CommandManager();
    Commands.addTo(COMMAND_MANAGER);

    this.KEY_MANAGER = new GdxKeyManager();
    Keys.addTo(KEY_MANAGER);

    FileHandleResolver fhResolver = new InternalFileHandleResolver();
    this.ASSET_MANAGER = new AssetManager(fhResolver);
    setAssetLoaders(fhResolver);
    loadCommonAssets();

    this.CONSOLE_WIDGET = new ClientConsoleWidget(CONSOLE);

    this.STAGE = new Stage();
    STAGE.setViewport(new FitViewport(RESOLUTION.width, RESOLUTION.height));

}

private void initPCvars() {
    Cvars.Client.Render.ShowFPS.addCvarChangeListener(new CvarChangeAdapter<Byte>() {
        @Override
        public void afterChanged(Cvar<Byte> cvar, Byte from, Byte to) {
            Client.this.pCvar_showFps = to;
        }
    });
}

private void setSerializers() {
    Gdx.app.log(TAG, "Setting serializers...");

    CVAR_MANAGER.putAssetSerializer(
            BitmapFont.class,
            new AssetDescriptorStringSerializer<BitmapFont>(BitmapFont.class));
}


private void setAssetLoaders(FileHandleResolver resolver) {
    Gdx.app.log(TAG, "Setting asset loaders...");

    CVAR_MANAGER.putAssetSerializer(
            BitmapFont.class,
            new AssetDescriptorStringSerializer<BitmapFont>(BitmapFont.class));
}

private void loadCommonAssets() {
    Gdx.app.log(TAG, "Loading common assets...");



    ASSET_MANAGER.finishLoading();
}

public Dimension getResolution() { return RESOLUTION; }
public Stage getStage() { return STAGE; }
public ClientConsole getConsole() { return CONSOLE; }
public AssetManager getAssetManager() { return ASSET_MANAGER; }
public CommandManager getCommandManager() { return COMMAND_MANAGER; }
public GdxCvarManager getCvarManager() { return CVAR_MANAGER; }
public GdxKeyManager getKeyManager() { return KEY_MANAGER; }

public void setScene() {

}

@Override
public void resize(int width, int height) {

}

@Override
public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    STAGE.act(Gdx.graphics.getDeltaTime());
    STAGE.draw();
    Batch b = STAGE.getBatch();
    b.begin(); {
        if (pCvar_showFps > 0) {
            //CONSOLE.getFont()
            //        .draw(b, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, VIRTUAL_HEIGHT);
        }
    } b.end();
}

@Override
public void pause() {

}

@Override
public void resume() {

}

@Override
public void dispose() {
    Gdx.app.log(TAG, "Disposing scene...");
    scene.dispose();
    this.scene = null;
    Gdx.app.log(TAG, "Disposing stage...");
    STAGE.dispose();
    this.STAGE = null;

    Gdx.app.log(TAG, "Saving cvars...");
    CVAR_MANAGER.saveAll();
    Gdx.app.log(TAG, "Disposing cvar manager...");
    this.CVAR_MANAGER = null;

    Gdx.app.log(TAG, "Saving key bindings...");
    KEY_MANAGER.saveAll();
    Gdx.app.log(TAG, "Disposing key manager...");
    this.KEY_MANAGER = null;

    Gdx.app.log(TAG, "Disposing command manager...");
    this.COMMAND_MANAGER = null;

    Gdx.app.log(TAG, "Resetting stdout...");
    System.setOut(STDOUT);
    Gdx.app.log(TAG, "Resetting stderr...");
    System.setErr(STDERR);
    Gdx.app.log(TAG, "Flushing console...");
    CONSOLE.flush();
    CONSOLE.close();

    Gdx.app.log(TAG, "Disposing console...");
    this.CONSOLE = null;
    this.CONSOLE_WIDGET = null;

    Gdx.app.log(TAG, "Disposing assets...");
    ASSET_MANAGER.dispose();
    this.ASSET_MANAGER = null;
}

}
