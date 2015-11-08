package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.I18NBundle;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.asset.Assets;
import com.google.collinsmith70.diablo.lang.Langs;
import com.google.collinsmith70.diablo.widget.ActorUtils;
import com.google.collinsmith70.diablo.widget.AnimatedActor;
import com.google.collinsmith70.diablo.widget.StrobingLabel;

public class SplashScene extends AbstractScene {

private static final AssetDescriptor<Music> INTRO_MUSIC_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/music/intro.ogg", Music.class);

private Sound selectSoundAsset;

private static final AssetDescriptor<Texture> LOGO_ANIMATION_TEXTURE_DESCRIPTOR
        = new AssetDescriptor<Texture>("animations/logo.png", Texture.class);
private Texture logoAnimationTextureAsset;
private AnimatedActor logoAnimatedActor;

private Label labelPressToContinue;
private boolean pressedContinue;

public SplashScene(Client client) {
    super(client);
}

@Override
public void create() {
    super.create();
    AssetManager assetManager = getClient().getAssetManager();

    getClient().getMusicController().enqueue(INTRO_MUSIC_ASSET_DESCRIPTOR);

    selectSoundAsset = assetManager.get(Assets.Client.Sound.SELECT);

    logoAnimationTextureAsset = assetManager.get(LOGO_ANIMATION_TEXTURE_DESCRIPTOR);
    logoAnimatedActor = new AnimatedActor(logoAnimationTextureAsset, 30, 374, 170, 1/30.0f);
    logoAnimatedActor.setScale(
            getClient().getVirtualWidth() / 1280.0f,
            getClient().getVirtualHeight() / 720.0f);
    logoAnimatedActor.setPosition(
            (getClient().getVirtualWidth() - logoAnimatedActor.getWidth() * logoAnimatedActor.getScaleX()) / 2 + 3,
            (getClient().getVirtualHeight() - logoAnimatedActor.getHeight() * logoAnimatedActor.getScaleY()));
    addActor(logoAnimatedActor);

    String press_to_continue;
    I18NBundle bundle = getClient().getI18NBundle();
    if (Controllers.getControllers().size > 0) {
        press_to_continue = bundle.format(Langs.Client.press_any_button);
    } else {
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            press_to_continue = bundle.format(Langs.Client.touch_the_screen);
        } else {
            press_to_continue = bundle.format(Langs.Client.press_any_key);
        }
    }

    BitmapFont exocet = assetManager.get(Assets.Client.Font.Exocet._16);
    Label.LabelStyle labelStyle = new Label.LabelStyle(exocet, Color.WHITE);
    labelPressToContinue = new StrobingLabel(
            press_to_continue,
            labelStyle,
            StrobingLabel.DEFAULT_STROBE_FLOOR,
            StrobingLabel.DEFAULT_STROBE_CEILING,
            StrobingLabel.DEFAULT_STROBE_MOD);
    ActorUtils.centerAt(
            labelPressToContinue,
            getClient().getVirtualWidth() / 2,
            getClient().getVirtualHeight() / 4);
    addActor(labelPressToContinue);
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(LOGO_ANIMATION_TEXTURE_DESCRIPTOR);
    assetManager.finishLoading();
}

@Override
public void disposeAssets(AssetManager assetManager) {

}

private boolean pressToContinue() {
    if (pressedContinue) {
        return false;
    }

    selectSoundAsset.play();
    if (getClient().isVibratingEnabled()) {
        Gdx.input.vibrate(25);
    }

    pressedContinue = true;
    getClient().setScene(new MenuScene(getClient(), logoAnimatedActor));
    return true;
}

@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (super.touchDown(screenX, screenY, pointer, button)) {
        return true;
    }

    return pressToContinue();
}

@Override
public boolean keyDown(int keycode) {
    if (super.keyDown(keycode)) {
        return true;
    } else if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
        return true;
    }

    return pressToContinue();
}

}
