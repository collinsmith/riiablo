package gdx.diablo.validator;

import android.support.annotation.Nullable;

public class NumberRangeValidator<T extends Number & Comparable<? super T>> extends NonNullSubclassValidator<T> implements RangeValidator<T> {
  public static <T extends Number & Comparable<? super T>> NumberRangeValidator<T> of(Class<T> type, @Nullable T min, @Nullable T max) {
    return new NumberRangeValidator<>(type, min, max);
  }

  @Nullable private final T MIN, MAX;

  public NumberRangeValidator(Class<T> type, @Nullable T min, @Nullable T max) {
    super(type);
    MIN = min;
    MAX = max;
  }

  @Nullable
  @Override
  public T getMin() {
    return MIN;
  }

  @Nullable
  @Override
  public T getMax() {
    return MAX;
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

  @Override
  public void validate(@Nullable Object obj) {
    super.validate(obj);
    T castedObj = (T) obj;
    if ((MIN != null && MIN.compareTo(castedObj) > 0)
     || (MAX != null && MAX.compareTo(castedObj) < 0)) {
      throw new RangeValidationException(MIN, MAX);
    }
  }
}
