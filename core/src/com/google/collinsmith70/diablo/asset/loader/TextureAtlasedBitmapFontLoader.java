package com.google.collinsmith70.diablo.asset.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.google.collinsmith70.diablo.asset.TextureAtlasedBitmapFont;

public class TextureAtlasedBitmapFontLoader
        extends AsynchronousAssetLoader<
        TextureAtlasedBitmapFont,
        TextureAtlasedBitmapFontLoader.TextureAtlasedBitmapFontParameter> {

private TextureAtlasedBitmapFont textureAtlasBitmapFont;

public TextureAtlasedBitmapFontLoader(FileHandleResolver resolver) {
    super(resolver);
}

@Override
public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TextureAtlasedBitmapFontParameter parameter) {
    Array<AssetDescriptor> dependancies = new Array<AssetDescriptor>();
    AssetDescriptor descriptor = new AssetDescriptor(resolve(parameter.atlasPath), TextureAtlas.class);
    dependancies.add(descriptor);
    return dependancies;
}

@Override
public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, TextureAtlasedBitmapFontParameter parameter) {
    TextureAtlas textureAtlas = manager.get(parameter.atlasPath, TextureAtlas.class);
    textureAtlasBitmapFont = TextureAtlasedBitmapFont.create(fileHandle, textureAtlas, parameter.regionName);
}

@Override
public TextureAtlasedBitmapFont loadSync(AssetManager manager, String fileName, FileHandle file, TextureAtlasedBitmapFontParameter parameter) {
    TextureAtlasedBitmapFont textureAtlasBitmapFont = this.textureAtlasBitmapFont;
    this.textureAtlasBitmapFont = null;
    return textureAtlasBitmapFont;
}

public static class TextureAtlasedBitmapFontParameter extends AssetLoaderParameters<TextureAtlasedBitmapFont> {
    public String regionName = null;
    public String atlasPath = null;
}

}
