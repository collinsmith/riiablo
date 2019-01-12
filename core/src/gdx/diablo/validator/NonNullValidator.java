package gdx.diablo.validator;

import android.support.annotation.Nullable;

public class NonNullValidator extends SimpleValidator {
  @Override
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    }
  }
}
