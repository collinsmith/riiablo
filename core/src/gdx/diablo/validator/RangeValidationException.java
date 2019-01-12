package gdx.diablo.validator;

import android.support.annotation.Nullable;

public class RangeValidationException extends ValidationException {
  @Nullable
  private final Object MIN, MAX;

  public RangeValidationException(@Nullable String message) {
    super(message);
    this.MIN = null;
    this.MAX = null;
  }

  public RangeValidationException(@Nullable Object min, @Nullable Object max) {
    super(formatMessage(min, max));
    this.MIN = min;
    this.MAX = max;
  }

  private static String formatMessage(@Nullable Object min, @Nullable Object max) {
    if (min == null && max != null) {
      return String.format("Value must be less than or equal to %s", max);
    } else if (min != null && max == null) {
      return String.format("Value must be greater than or equal to %s", min);
    } else {
      return String.format("Value must be between %s and %s (inclusive)", min, max);
    }
  }

  @Nullable
  public Object getMin() {
    return MIN;
  }

  @Nullable
  public Object getMax() {
    return MAX;
  }
}
