package gdx.diablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gdx.diablo.codec.DS1;

public class DS1Loader extends AsynchronousAssetLoader<DS1, DS1Loader.DS1LoaderParameters> {

  DS1 ds1;

  public DS1Loader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, DS1LoaderParameters params) {
    ds1 = DS1.loadFromStream(file.read());
  }

  @Override
  public DS1 loadSync(AssetManager assets, String fileName, FileHandle file, DS1LoaderParameters params) {
    DS1 ds1 = this.ds1;
    this.ds1 = null;
    return ds1 != null ? ds1 : DS1.loadFromStream(file.read());
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, DS1LoaderParameters params) {
    return null;
  }

  public static class DS1LoaderParameters extends AssetLoaderParameters<DS1> {}

}
