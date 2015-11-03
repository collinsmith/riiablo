package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.google.collinsmith70.diablo.Client;

public class SplashScene extends AbstractScene {

private static final AssetDescriptor<Music> INTRO_ASSET_DESCRIPTOR
        = new AssetDescriptor("audio/music/intro.ogg", Music.class);
private Music introAsset;

public SplashScene(Client client) {
    super(client);
}

@Override
public void create() {
    super.create();

    AssetManager assetManager = getClient().getAssetManager();
    introAsset = assetManager.get(INTRO_ASSET_DESCRIPTOR);
    introAsset.setOnCompletionListener(new Music.OnCompletionListener() {
        @Override
        public void onCompletion(Music music) {
            SplashScene.this.getClient()
                    .getAssetManager().unload(INTRO_ASSET_DESCRIPTOR.fileName);
            music.dispose();
        }
    });

    introAsset.play();
}

@Override
public void loadAssets(AssetManager assetManager) {
    assetManager.load(INTRO_ASSET_DESCRIPTOR);
}

}
