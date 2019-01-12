package gdx.diablo.validator;

import android.support.annotation.Nullable;

public interface RangeValidator<T extends Comparable<? super  T>> extends Validator {
  @Nullable T getMin();
  @Nullable T getMax();
}
