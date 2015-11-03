package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.cvar.Cvar;
import com.google.collinsmith70.diablo.cvar.CvarChangeListener;
import com.google.collinsmith70.diablo.cvar.Cvars;

public class SplashScene extends AbstractScene {

private static final AssetDescriptor<Music> INTRO_MUSIC_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/music/intro.ogg", Music.class);
private Music introMusicAsset;

private static final AssetDescriptor<Sound> SELECT_SOUND_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/cursor/select.wav", Sound.class);
private Sound selectSoundAsset;

public SplashScene(Client client) {
    super(client);
}

@Override
public void create() {
    super.create();
    AssetManager assetManager = getClient().getAssetManager();

    introMusicAsset = assetManager.get(INTRO_MUSIC_ASSET_DESCRIPTOR);
    introMusicAsset.setOnCompletionListener(new Music.OnCompletionListener() {
        @Override
        public void onCompletion(Music music) {
            SplashScene.this.getClient().getAssetManager().unload(INTRO_MUSIC_ASSET_DESCRIPTOR.fileName);
            music.dispose();
        }
    });

    Cvars.Client.Sound.Music.Volume.addCvarChangeListener(new CvarChangeListener<Float>() {
        @Override
        public void onCvarChanged(Cvar<Float> cvar, Float fromValue, Float toValue) {
            SplashScene.this.introMusicAsset.setVolume(getClient().getMusicVolume());
        }
    });

    introMusicAsset.play();

    selectSoundAsset = assetManager.get(SELECT_SOUND_ASSET_DESCRIPTOR);
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(INTRO_MUSIC_ASSET_DESCRIPTOR);
    assetManager.load(SELECT_SOUND_ASSET_DESCRIPTOR);
}

@Override
public void disposeAssets(AssetManager assetManager) {
    assetManager.unload(INTRO_MUSIC_ASSET_DESCRIPTOR.fileName);
    assetManager.unload(SELECT_SOUND_ASSET_DESCRIPTOR.fileName);
}

@Override
public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (super.touchDown(screenX, screenY, pointer, button)) {
        return true;
    }

    selectSoundAsset.play(getClient().getSfxVolume());
    return true;
}

}
