package gdx.diablo.assets.async;

public interface AsyncTask<T> {
  T call() throws Exception;
}
