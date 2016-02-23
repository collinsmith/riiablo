package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarStateAdapter;
import com.gmail.collinsmith70.diablo.scene.HudedScene;
import com.gmail.collinsmith70.diablo.widget.ClientConsoleWidget;
import com.gmail.collinsmith70.key.Key;
import com.gmail.collinsmith70.key.KeyStateAdapter;
import com.gmail.collinsmith70.unifi.layout.LinearLayout;
import com.gmail.collinsmith70.unifi.widget.Button;
import com.gmail.collinsmith70.unifi.widget.Widget;
import com.gmail.collinsmith70.unifi.widget.WidgetGroup;
import com.gmail.collinsmith70.unifi.widget.Window;
import com.gmail.collinsmith70.util.ImmutableDimension;
import com.gmail.collinsmith70.util.serializer.LocaleStringSerializer;

import java.awt.Dimension;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class Client implements ApplicationListener {

private static final String TAG = Client.class.getSimpleName();

private final Dimension RESOLUTION;
private final PrintStream STDOUT;
private final PrintStream STDERR;

private final boolean FORCE_WINDOWED;

private ClientConsole CONSOLE;
private ClientConsoleWidget CONSOLE_WIDGET;

private ClientInputProcessor INPUT_PROCESSOR;

private CommandManager COMMAND_MANAGER;
private GdxCvarManager CVAR_MANAGER;
private GdxKeyManager KEY_MANAGER;
private AssetManager ASSET_MANAGER;

//private Stage STAGE;

private Window WINDOW;
private Batch BATCH;

private HudedScene scene;

private byte pCvar_showFps;
private boolean pCvar_Windowed;

private BitmapFont font;

public Client(int width, int height) {
    this(width, height, false);
}

public Client(int width, int height, boolean forceWindowed) {
    this.RESOLUTION = new ImmutableDimension(width, height);
    this.FORCE_WINDOWED = forceWindowed;

    this.STDOUT = System.out;
    this.STDERR = System.err;
}

@Override
public void create() {
    //FileHandle consoleFileHandle = Gdx.files.local("console.out");
    //OutputStream consoleOut = consoleFileHandle.write(false);
    OutputStream consoleOut = System.out;

    this.CONSOLE = new ClientConsole(this, consoleOut);
    System.setOut(CONSOLE);
    System.setErr(CONSOLE);

    this.CVAR_MANAGER = new GdxCvarManager();
    setSerializers();
    Cvars.addTo(CVAR_MANAGER);

    this.COMMAND_MANAGER = new CommandManager();
    Commands.addTo(COMMAND_MANAGER);

    this.KEY_MANAGER = new GdxKeyManager();
    Keys.addTo(KEY_MANAGER);

    FileHandleResolver fhResolver = new InternalFileHandleResolver();
    this.ASSET_MANAGER = new AssetManager(fhResolver);
    setAssetLoaders(fhResolver);
    initPCvars();
    loadCommonAssets();

    this.CONSOLE_WIDGET = new ClientConsoleWidget(CONSOLE);

    //this.STAGE = new Stage();
    //STAGE.setViewport(new FitViewport(RESOLUTION.width, RESOLUTION.height));
    //STAGE.setDebugAll(true);

    Widget button1 = new Button();
    button1.setSize(128, 128);
    Widget button2 = new Button();
    button2.setSize(256, 256);
    Widget button3 = new Button();
    button3.setSize(256, 128);

    WidgetGroup ll1 = new LinearLayout(LinearLayout.Orientation.VERTICAL);
    ll1.setSize(512, 512);
    ll1.addWidget(button1).addWidget(button2);
    ll1.setLayoutParams(new WidgetGroup.LayoutParams(
            WidgetGroup.LayoutParams.WRAP_CONTENT, WidgetGroup.LayoutParams.WRAP_CONTENT));

    WidgetGroup ll2 = new LinearLayout(LinearLayout.Orientation.HORIZONTAL);
    ll2.addWidget(ll1).addWidget(button3);

    this.BATCH = new SpriteBatch(1024);
    this.WINDOW = new Window(RESOLUTION.width, RESOLUTION.height);
    this.WINDOW.addWidget(ll2);
    this.WINDOW.setDebugging(true);

    ll2.setLayoutParams(new WidgetGroup.LayoutParams(
            WidgetGroup.LayoutParams.FILL_PARENT, WidgetGroup.LayoutParams.FILL_PARENT));
    WINDOW.requestLayout();
    ll1.requestLayout();

    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);
    //this.INPUT_PROCESSOR = new ClientInputProcessor(this, STAGE);
    this.INPUT_PROCESSOR = new ClientInputProcessor(this, WINDOW);
    Gdx.input.setInputProcessor(INPUT_PROCESSOR);

    Keys.Console.addStateListener(new KeyStateAdapter<Integer>() {
        @Override
        public void onPressed(Key<Integer> key, Integer binding) {
            CONSOLE_WIDGET.setVisible(!CONSOLE_WIDGET.isVisible());
            Gdx.app.log(TAG, "Console visible = " + CONSOLE_WIDGET.isVisible());
        }
    });

    setScene(new HudedScene(this));
}

private void setSerializers() {
    Gdx.app.log(TAG, "Setting serializers...");

    CVAR_MANAGER.putSerializer(Locale.class, LocaleStringSerializer.INSTANCE);
}

private void initPCvars() {
    Cvars.Client.Windowed.addStateListener(new CvarStateAdapter<Boolean>() {
        @Override
        public void onChanged(Cvar<Boolean> cvar, Boolean from, Boolean to) {
            Client.this.pCvar_Windowed = to;
            if (Gdx.app.getType() == Application.ApplicationType.Desktop
                    && !Client.this.FORCE_WINDOWED) {
                Gdx.graphics.setDisplayMode(
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight(),
                        !to);
            }
        }
    });

    Cvars.Client.Render.ShowFPS.addStateListener(new CvarStateAdapter<Byte>() {
        @Override
        public void onChanged(Cvar<Byte> cvar, Byte from, Byte to) {
            Client.this.pCvar_showFps = to;
        }
    });

    Cvars.Client.Console.Font.addStateListener(new CvarStateAdapter<String>() {
        @Override
        public void onChanged(Cvar<String> cvar, String from, String to) {
            Client.this.ASSET_MANAGER.load(to, BitmapFont.class);
            ASSET_MANAGER.finishLoading();
            Client.this.font = Client.this.ASSET_MANAGER.get(to);
        }
    });
}

private void setAssetLoaders(FileHandleResolver resolver) {
    Gdx.app.log(TAG, "Setting asset loaders...");
}

private void loadCommonAssets() {
    Gdx.app.log(TAG, "Loading common assets...");


    ASSET_MANAGER.finishLoading();
}

public Dimension getResolution() { return RESOLUTION; }
//public Stage getStage() { return STAGE; }
public Window getWindow() { return WINDOW; }
public ClientConsole getConsole() { return CONSOLE; }
public AssetManager getAssetManager() { return ASSET_MANAGER; }
public CommandManager getCommandManager() { return COMMAND_MANAGER; }
public GdxCvarManager getCvarManager() { return CVAR_MANAGER; }
public GdxKeyManager getKeyManager() { return KEY_MANAGER; }
public BitmapFont getDefaultFont() { return font; }

public boolean isFullscreen() {
    return !pCvar_Windowed && !FORCE_WINDOWED;
}

public void setScene(HudedScene scene) {
    //STAGE.clear();
    //STAGE.addActor(scene);
    //STAGE.addActor(CONSOLE_WIDGET);

    HudedScene oldScene = this.scene;
    if (oldScene != null) {
        oldScene.dispose();
    }

    this.scene = scene;
}

@Override
public void resize(int width, int height) {
    //STAGE.getViewport().update(width, height, true);
}

@Override
public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    //STAGE.act(Gdx.graphics.getDeltaTime());
    //STAGE.draw();
    //Batch b = STAGE.getBatch();
    Batch b = BATCH;
    b.begin(); {
        WINDOW.draw(b);
        if (pCvar_showFps > 0) {
            GlyphLayout fps = new GlyphLayout(
                    font,
                    Integer.toString(Gdx.graphics.getFramesPerSecond()));

            float x = 0;
            float y = 0;
            switch (pCvar_showFps) {
                case 1:
                    x = 0;
                    y = RESOLUTION.height;
                    break;
                case 2:
                    x = RESOLUTION.width - fps.width;
                    y = RESOLUTION.height;
                    break;
                case 3:
                    x = 0;
                    y = 0;
                    break;
                case 4:
                    x = RESOLUTION.width - fps.width;
                    y = 0;
                    break;
            }

            font.draw(b, fps, x ,y);
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
    if (scene != null) {
        scene.dispose();
        this.scene = null;
    }

    Gdx.app.log(TAG, "Disposing stage...");
    //STAGE.dispose();
    //this.STAGE = null;
    getWindow().dispose();
    this.WINDOW = null;
    this.BATCH.dispose();
    this.BATCH = null;

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
