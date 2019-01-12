package gdx.diablo.assets;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public abstract class AssetLoader<T, P extends AssetLoader.Parameters<T>> {
  private FileHandleResolver resolver;

  public AssetLoader(FileHandleResolver resolver) {
    this.resolver = resolver;
  }

  public FileHandle resolve(String fileName) {
    return resolver.resolve(fileName);
  }

  public interface Parameters<T> {}
}
