package com.google.collinsmith70.diablo.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureAtlasedBitmapFont extends BitmapFont {

private TextureAtlasedBitmapFont() {
    //...
}

private TextureAtlasedBitmapFont(FileHandle fontFileHandle, TextureRegion region) {
    super(fontFileHandle, region);
}

public static TextureAtlasedBitmapFont create(FileHandle fontFileHandle, TextureAtlas textureAtlas, String regionName) {
    TextureRegion region = textureAtlas.findRegion(regionName);
    return new TextureAtlasedBitmapFont(fontFileHandle, region);
}

}
