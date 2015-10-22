package com.google.collinsmith70.old2.asset.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.google.collinsmith70.old2.asset.AtlasedBitmapFont;

public class AtlasedBitmapFontLoader extends AsynchronousAssetLoader<AtlasedBitmapFont, AtlasedBitmapFontLoader.AtlasedBitmapFontParameter> {
	public AtlasedBitmapFontLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, AtlasedBitmapFontLoader.AtlasedBitmapFontParameter parameter) {
		Array<AssetDescriptor> deps = new Array();
		AssetDescriptor descriptor = new AssetDescriptor(resolve(parameter.atlasPath), TextureAtlas.class);
		deps.add(descriptor);
		return deps;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, AtlasedBitmapFontParameter parameter) {
	}
	
	@Override
	public AtlasedBitmapFont loadSync(AssetManager manager, String fileName, FileHandle file, AtlasedBitmapFontParameter parameter) {
		TextureAtlas atlas = manager.get(parameter.atlasPath, TextureAtlas.class);
		return AtlasedBitmapFont.create(file, atlas, parameter.regionName);
	}
	
	public static class AtlasedBitmapFontParameter extends AssetLoaderParameters<AtlasedBitmapFont> {
		public String regionName = null;
		public String atlasPath = null;
	}
}
