package gdx.diablo.validator;

import android.support.annotation.Nullable;

public class NonNullNonEmptyStringValidator extends NonNullValidator {
  @Override
  public void validate(@Nullable Object obj) {
    super.validate(obj);
    if (obj.getClass() != String.class) {
      throw new ValidationException("obj is not a string");
    }

    String str = (String) obj;
    if (str.isEmpty()) {
      throw new ValidationException("passed string is empty");
    }
  }
}
