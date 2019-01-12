package gdx.diablo.validator;

import android.support.annotation.Nullable;

public final class RejectAllValidator extends SimpleValidator {
  @Override
  public void validate(@Nullable Object obj) {
    throw new ValidationException();
  }
}
