package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.widget.AnimatedActor;

public class SplashScene extends AbstractScene {

private static final AssetDescriptor<Music> INTRO_MUSIC_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/music/intro.ogg", Music.class);

private static final AssetDescriptor<Sound> SELECT_SOUND_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/cursor/select.wav", Sound.class);
private Sound selectSoundAsset;

private static final AssetDescriptor<Texture> LOGO_ANIMATION_TEXTURE_DESCRIPTOR
        = new AssetDescriptor<Texture>("animations/logo.png", Texture.class);
private Texture logoAnimationTextureAsset;
private AnimatedActor logoAnimatedActor;

public SplashScene(Client client) {
    super(client);
}

@Override
public void create() {
    super.create();
    AssetManager assetManager = getClient().getAssetManager();

    getClient().getMusicController().enqueue(INTRO_MUSIC_ASSET_DESCRIPTOR);

    selectSoundAsset = assetManager.get(SELECT_SOUND_ASSET_DESCRIPTOR);

    logoAnimationTextureAsset = assetManager.get(LOGO_ANIMATION_TEXTURE_DESCRIPTOR);
    logoAnimatedActor = new AnimatedActor(logoAnimationTextureAsset, 30, 374, 170, 1/30.0f);
    logoAnimatedActor.setScale(
            getClient().getVirtualWidth() / 1280.0f,
            getClient().getVirtualHeight() / 720.0f);
    logoAnimatedActor.setPosition((getClient().getVirtualWidth() - logoAnimatedActor.getWidth() * logoAnimatedActor.getScaleX()) / 2 - 3, (getClient().getVirtualHeight() - logoAnimatedActor.getHeight() * logoAnimatedActor.getScaleY()));
    addActor(logoAnimatedActor);
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(SELECT_SOUND_ASSET_DESCRIPTOR);
    assetManager.load(LOGO_ANIMATION_TEXTURE_DESCRIPTOR);
    assetManager.finishLoading();
}

@Override
public void disposeAssets(AssetManager assetManager) {
    assetManager.unload(SELECT_SOUND_ASSET_DESCRIPTOR.fileName);
}

@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (super.touchDown(screenX, screenY, pointer, button)) {
        return true;
    }

    selectSoundAsset.play();
    return true;
}

}
