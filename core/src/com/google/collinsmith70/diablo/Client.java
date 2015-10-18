package com.google.collinsmith70.diablo;

import com.badlogic.gdx.ApplicationAdapter;
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
import com.google.collinsmith70.diablo.asset.AtlasedBitmapFont;
import com.google.collinsmith70.diablo.asset.loader.AtlasedBitmapFontLoader;
import com.google.collinsmith70.diablo.scene.AbstractScene;
import com.google.collinsmith70.diablo.scene.SplashScene;

public class Client extends ApplicationAdapter {
	public static final String CLIENT_PREFS = "com.google.collinsmith70.diablo.preferences";

    private final int VIRTUAL_WIDTH;
    private final int VIRTUAL_HEIGHT;

    @EffectivelyFinal
    private AssetManager ASSET_MANAGER;

    @EffectivelyFinal
    private SettingManager SETTING_MANAGER;

    @EffectivelyFinal
    private Stage STAGE;

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
        if (oldScene != null) {
            oldScene.dispose();
        }
    }

    public AbstractScene getScene() {
        return scene;
    }

    @Override
    public void create() {
        Preferences PREFERENCES = Gdx.app.getPreferences(CLIENT_PREFS);
        this.SETTING_MANAGER = new SettingManager(PREFERENCES);

        String defaultConsoleFont = PREFERENCES.getString("cl.glob.consoleFont", "default.fnt");
        ASSET_MANAGER = new AssetManager();
        ASSET_MANAGER.setLoader(AtlasedBitmapFont.class, new AtlasedBitmapFontLoader(new InternalFileHandleResolver()));
        ASSET_MANAGER.load(defaultConsoleFont, BitmapFont.class);
        ASSET_MANAGER.finishLoading();
        CONSOLE_FONT = ASSET_MANAGER.get(defaultConsoleFont, BitmapFont.class);
        CONSOLE_FONT.setColor(new Color(
                PREFERENCES.getFloat("cl.glob.consoleFontColor.r", 1.0f),
                PREFERENCES.getFloat("cl.glob.consoleFontColor.g", 1.0f),
                PREFERENCES.getFloat("cl.glob.consoleFontColor.b", 1.0f),
                PREFERENCES.getFloat("cl.glob.consoleFontColor.a", 1.0f)));

        Gdx.graphics.setDisplayMode(1280, 720, true);

        this.STAGE = new Stage();
        STAGE.setViewport(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

        setScene(new SplashScene(this));

        Gdx.input.setCatchBackKey(true);
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
            CONSOLE_FONT.draw(b, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, VIRTUAL_HEIGHT);
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
        Gdx.app.log("application.state", "Disposing resources");
        scene.dispose();
        STAGE.dispose();
        ASSET_MANAGER.dispose();
        super.dispose();
    }
}
