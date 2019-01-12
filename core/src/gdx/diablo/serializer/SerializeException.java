package gdx.diablo.serializer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SerializeException extends RuntimeException {
  public SerializeException() {
    super();
  }

  public SerializeException(@Nullable String message) {
    super(message);
  }

  public SerializeException(@NonNull Throwable cause) {
    super(cause);
  }

  public SerializeException(@Nullable String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
