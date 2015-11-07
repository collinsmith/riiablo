package com.google.collinsmith70.diablo.scene;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.collinsmith70.diablo.Client;
import com.google.collinsmith70.diablo.widget.AnimatedActor;

public class MenuScene extends AbstractScene {

private AnimatedActor logoAnimatedActor;

private TextureAtlas exocetTextureAtlas;
private BitmapFont exocetBitmapFont;

public MenuScene(Client client, AnimatedActor logoAnimatedActor) {
    super(client);
    this.logoAnimatedActor = logoAnimatedActor;
}

@Override
public void create() {
    super.create();

    AssetManager assetManager =  getClient().getAssetManager();

    addActor(logoAnimatedActor);

}

}
