package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.asset.Assets;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;
import com.google.collinsmith70.diablo.lang.Langs;
import com.google.collinsmith70.diablo.widget.ActorUtils;
import com.google.collinsmith70.diablo.widget.AnimatedActor;
import com.google.collinsmith70.diablo.widget.ButtonUtils;
import com.google.collinsmith70.diablo.widget.panel.OptionsPanel;

import java.util.Locale;

public class MenuScene extends AbstractScene {

private AnimatedActor logoAnimatedActor;

private static final AssetDescriptor<TextureAtlas> BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR
        = new AssetDescriptor<TextureAtlas>("textures/buttons.pack", TextureAtlas.class);
private TextureAtlas buttonsTextureAtlas;
private Skin buttonsSkin;

private Sound buttonSoundAsset;

private TextButton btnPlaySinglePlayer;
private TextButton btnShowOptions;
private TextButton btnExitGame;

public MenuScene(Client client, AnimatedActor logoAnimatedActor) {
    super(client);
    this.logoAnimatedActor = logoAnimatedActor;
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR);
    assetManager.finishLoading();
}

@Override
public void disposeAssets(AssetManager assetManager) {
    assetManager.unload(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR.fileName);
}

@Override
public void create() {
    super.create();

    AssetManager assetManager =  getClient().getAssetManager();

    addActor(logoAnimatedActor);

    buttonSoundAsset = assetManager.get(Assets.Client.Sound.BUTTON);

    BitmapFont exocetBitmapFont = assetManager.get(Assets.Client.Font.Exocet._16);
    buttonsTextureAtlas = assetManager.get(BUTTONS_TEXTURE_ATLAS_ASSET_DESCRIPTOR);
    buttonsSkin = new Skin(Gdx.files.internal("textures/buttons.json"), buttonsTextureAtlas);
    TextButton.TextButtonStyle textButtonStyle = buttonsSkin.get("wide", TextButton.TextButtonStyle.class);
    textButtonStyle.font = exocetBitmapFont;

    final float xPos = getClient().getVirtualWidth()/2;
    float yPos = getClient().getVirtualHeight()/4;

    btnExitGame = new TextButton(
            Langs.MenuScene.exit_diablo,
            buttonsSkin, "wide");
    ButtonUtils.playSoundOnClicked(btnExitGame, buttonSoundAsset);
    ActorUtils.centerAt(btnExitGame, xPos, yPos);
    btnExitGame.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            MenuScene.this.closeApplication();
        }
    });
    addActor(btnExitGame);
    final float yAdjust = btnExitGame.getHeight()+2;
    yPos += yAdjust;

    btnShowOptions = new TextButton(
            Langs.MenuScene.display_options,
            buttonsSkin, "wide");
    btnShowOptions.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            OptionsPanel optionsPanel = new OptionsPanel(MenuScene.this.getClient());
            optionsPanel.create();

            MenuScene.this.addActor(optionsPanel);
        }
    });
    ButtonUtils.playSoundOnClicked(btnShowOptions, buttonSoundAsset);
    ActorUtils.centerAt(btnShowOptions, xPos, yPos);
    addActor(btnShowOptions);
    yPos += yAdjust;

    btnPlaySinglePlayer = new TextButton(
            Langs.MenuScene.play_single_player,
            buttonsSkin, "wide");
    ButtonUtils.playSoundOnClicked(btnPlaySinglePlayer, buttonSoundAsset);
    ActorUtils.centerAt(btnPlaySinglePlayer, xPos, yPos);
    addActor(btnPlaySinglePlayer);

    Cvars.Client.Locale.addCvarChangeListener(new CvarChangeListener<Locale>() {
        @Override
        public void onCvarChanged(Cvar<Locale> cvar, Locale fromValue, Locale toValue) {
            FileHandle i18nBundleFileHandle = Gdx.files.internal("lang/MenuScene");
            I18NBundle i18nBundle = I18NBundle.createBundle(i18nBundleFileHandle, toValue);

            MenuScene.this.btnPlaySinglePlayer.setText(i18nBundle.get(Langs.MenuScene.play_single_player));
            MenuScene.this.btnShowOptions.setText(i18nBundle.get(Langs.MenuScene.display_options));
            MenuScene.this.btnExitGame.setText(i18nBundle.get(Langs.MenuScene.exit_diablo));
        }
    });
}

@Override
public boolean keyDown(int keycode) {
    if (super.keyDown(keycode)) {
        return true;
    }

    switch (keycode) {
        case Input.Keys.ESCAPE:
            closeApplication();
            return true;
        default:
            return false;
    }
}

private void closeApplication() {
    Gdx.app.exit();
}

}
