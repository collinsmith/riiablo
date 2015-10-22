package com.google.collinsmith70.old2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.collinsmith70.old2.asset.AtlasedBitmapFont;
import com.google.collinsmith70.old2.asset.loader.AtlasedBitmapFontLoader;
import com.google.collinsmith70.old2.scene.AbstractScene;
import com.google.collinsmith70.old2.scene.SplashScene;
import com.google.collinsmith70.old2.widget.panel.ConsolePanel;
import com.google.collinsmith70.util.EffectivelyFinal;

public class Client implements ApplicationListener {
    public static final String CLIENT_PREFS = Client.class.getCanonicalName();

    private final int VIRTUAL_WIDTH;
    private final int VIRTUAL_HEIGHT;

    @EffectivelyFinal
    private AssetManager ASSET_MANAGER;

    @EffectivelyFinal
    private SettingManager SETTING_MANAGER;

    @EffectivelyFinal
    private Stage STAGE;

    @EffectivelyFinal
    private ConsolePanel CONSOLE;

    @EffectivelyFinal
    private BitmapFont CONSOLE_FONT;

    private AbstractScene scene;

    public Client(int virtualWidth, int virtualHeight) {
        this.VIRTUAL_WIDTH = virtualWidth;
        this.VIRTUAL_HEIGHT = virtualHeight;
    }

    public AssetManager getAssetManager() {
        return ASSET_MANAGER;
    }

    private BitmapFont getConsoleFont() {
        return CONSOLE_FONT;
    }

    public ConsolePanel getConsole() {
        return CONSOLE;
    }

    public SettingManager getSettingManager() {
       return SETTING_MANAGER;
    }

    public int getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public int getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }

    public void setScene(AbstractScene scene) {
        AbstractScene oldScene = this.scene;
        this.scene = scene;
        STAGE.clear();
        STAGE.addActor(this.scene);
        STAGE.setKeyboardFocus(this.scene);
        Gdx.input.setInputProcessor(STAGE);
        this.scene.loadAssets();
        this.scene.show();
        this.scene.addActor(CONSOLE);
        if (oldScene != null) {
            oldScene.dispose();
        }
    }

    public AbstractScene getScene() {
        return scene;
    }

    public boolean process(String command) {
        if (command.equals("exit")) {
            Gdx.app.exit();
            return true;
        } else if (command.equals("help")) {
            return true;
        }

        return false;
    }

    @Override
    public void create() {
        Preferences PREFERENCES = Gdx.app.getPreferences(CLIENT_PREFS);
        this.SETTING_MANAGER = new SettingManager(PREFERENCES);

        String defaultConsoleFont = "default.fnt";
        ASSET_MANAGER = new AssetManager();
        ASSET_MANAGER.setLoader(AtlasedBitmapFont.class, new AtlasedBitmapFontLoader(new InternalFileHandleResolver()));
        ASSET_MANAGER.load(defaultConsoleFont, BitmapFont.class);
        ASSET_MANAGER.finishLoading();
        CONSOLE_FONT = ASSET_MANAGER.get(defaultConsoleFont, BitmapFont.class);
        CONSOLE_FONT.setColor(new Color(
                1.0f,
                1.0f,
                1.0f,
                1.0f));

        Gdx.graphics.setDisplayMode(1280, 720, false);

        this.STAGE = new Stage();
        STAGE.setViewport(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

        CONSOLE = new ConsolePanel(this);
        CONSOLE.loadAssets();
        CONSOLE.show();
        CONSOLE.setVisible(false);

        setScene(new SplashScene(this));

        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
    }

    @Override
    public void resize(int width, int height) {
        STAGE.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        STAGE.act(Gdx.graphics.getDeltaTime());
        STAGE.draw();
        Batch b = STAGE.getBatch();
        b.begin(); {
            if (!CONSOLE.isVisible()) {
                CONSOLE_FONT.draw(b, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, VIRTUAL_HEIGHT);
            }
        } b.end();
    }

    @Override
    public void pause() {
        if (scene != null) {
            scene.pause();
        }
    }

    @Override
    public void resume() {
        if (scene != null) {
            scene.resume();
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("Application.State", "Disposing resources");
        scene.dispose();
        STAGE.dispose();
        ASSET_MANAGER.dispose();
    }
}
