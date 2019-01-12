package gdx.diablo.validator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ValidationException extends RuntimeException {
  public ValidationException() {
    super();
  }

  public ValidationException(@Nullable String message) {
    super(message);
  }

  public ValidationException(@NonNull Throwable cause) {
    super(cause);
  }

  public ValidationException(@Nullable String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
