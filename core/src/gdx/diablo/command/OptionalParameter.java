package gdx.diablo.command;

import android.support.annotation.NonNull;

import gdx.diablo.console.Console;
import gdx.diablo.serializer.StringSerializer;
import gdx.diablo.validator.Validator;

public class OptionalParameter<T> extends Parameter<T> {
  public static <T> OptionalParameter<T> of(@NonNull Class<T> type) {
    return new OptionalParameter<>(type);
  }

  OptionalParameter(@NonNull Class<T> type) {
    super(type);
  }

  public OptionalParameter<T> serializer(@NonNull StringSerializer<T> serializer) {
    super.serializer(serializer);
    return this;
  }

  public OptionalParameter<T> validator(@NonNull Validator validator) {
    super.validator(validator);
    return this;
  }

  public OptionalParameter<T> suggester(@NonNull Console.SuggestionProvider suggestionProvider) {
    super.suggester(suggestionProvider);
    return this;
  }

  @Override
  public String toString() {
    return "[" + TYPE.getSimpleName() + "]";
  }
}
