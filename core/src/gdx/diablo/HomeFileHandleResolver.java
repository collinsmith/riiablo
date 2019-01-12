package gdx.diablo;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class HomeFileHandleResolver implements FileHandleResolver {
  private final FileHandle home;
  public HomeFileHandleResolver(FileHandle home) {
    this.home = home;
  }

  @Override
  public FileHandle resolve(String fileName) {
    return home.child(fileName);
  }
}
