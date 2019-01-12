package gdx.diablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import gdx.diablo.codec.TXT;
import gdx.diablo.codec.excel.Excel;

// TODO: This won't work because I can't return subclasses which makes Excel useful
public class ExcelLoader extends AsynchronousAssetLoader<Excel, ExcelLoader.Parameters> {

  TXT txt;
  Excel excel;

  public ExcelLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, Parameters params) {
  }

  @Override
  public Excel loadSync(AssetManager assets, String fileName, FileHandle file, Parameters params) {
    Excel excel = this.excel;
    if (excel == null) {
      ObjectSet<String> ignore = params != null ? params.ignore : Excel.<String>emptySet();
      txt = TXT.loadFromFile(file);
      excel = Excel.parse(txt, null, ignore);
    } else {
      this.excel = null;
    }

    return excel;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, Parameters params) {
    return null;
  }

  public static class Parameters extends AssetLoaderParameters<Excel> {
    public ObjectSet<String> ignore = Excel.emptySet();
    public Parameters of(ObjectSet<String> ignore) {
      Parameters params = new Parameters();
      params.ignore = ignore;
      return params;
    }
  }

}
