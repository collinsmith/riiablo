package gdx.diablo.assets.async;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncResult<T> {
  private final Future<T> future;

  AsyncResult(Future<T> future) {
    this.future = future;
  }

  public boolean isDown() {
    return future.isDone();
  }

  public T get() {
    try {
      return future.get();
    } catch (InterruptedException e) {
      return null;
    } catch (ExecutionException e) {
      throw new GdxRuntimeException(e.getCause());
    }
  }
}
