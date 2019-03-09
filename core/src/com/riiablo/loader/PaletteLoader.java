package com.riiablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.Palette;

public class PaletteLoader extends AsynchronousAssetLoader<Palette, PaletteLoader.PaletteParameter> {

  private static class PaletteLoaderInfo {
    Palette palette;
  }

  private final PaletteLoaderInfo info = new PaletteLoaderInfo();

  public PaletteLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, PaletteParameter params) {
    info.palette = Palette.loadFromFile(file);
  }

  @Override
  public Palette loadSync(AssetManager assets, String fileName, FileHandle file, PaletteParameter params) {
    if (info == null) return null;
    Palette palette = info.palette;
    if (palette == null) {
      palette = info.palette = Palette.loadFromFile(file);
    }

    return palette;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PaletteParameter params) {
    return null;
  }

  public static class PaletteParameter extends AssetLoaderParameters<Palette> {}

}
