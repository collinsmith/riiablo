package com.google.collinsmith70.diablo.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AtlasedBitmapFont extends BitmapFont {
	private AtlasedBitmapFont() {
		//...
	}
	
	private AtlasedBitmapFont(FileHandle fontFile, TextureRegion region) {
		super(fontFile, region);
	}
	
	public static AtlasedBitmapFont create(FileHandle fontFile, TextureAtlas atlas, String regionName) {
		TextureRegion region = atlas.findRegion(regionName);
		return new AtlasedBitmapFont(fontFile, region);
	}
}
