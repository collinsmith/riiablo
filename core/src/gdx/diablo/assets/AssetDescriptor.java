package gdx.diablo.assets;

public class AssetDescriptor<T> {
  public static <T> AssetDescriptor<T> of(String fileName, Class<T> type) {
    return of(fileName, type, null);
  }

  public static <T> AssetDescriptor<T> of(String fileName, Class<T> type, AssetLoader.Parameters<T> params) {
    return new AssetDescriptor<>(fileName, type, params);
  }

  public final String   fileName;
  public final Class<T> type;
  public final AssetLoader.Parameters params;

  public AssetDescriptor(String fileName, Class<T> type, AssetLoader.Parameters<T> params) {
    this.fileName = fileName.replace('\\', '/');
    this.type = type;
    this.params = params;
  }

  @Override
  public String toString() {
    return fileName + ", " + type.getName();
  }
}
