package gdx.diablo.validator;

import android.support.annotation.Nullable;

public abstract class SimpleValidator implements Validator {
  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }
}
