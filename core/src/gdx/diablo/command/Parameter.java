package gdx.diablo.command;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import gdx.diablo.console.Console;
import gdx.diablo.serializer.StringSerializer;
import gdx.diablo.validator.Validator;

public class Parameter<T> implements StringSerializer<T>, Validator, Console.SuggestionProvider {

  public static <T> Parameter<T> of (Class<T> type) {
    return new Parameter<>(type);
  }

  final Class<T> TYPE;
  private StringSerializer<T> serializer;
  private Validator validator;
  private Console.SuggestionProvider suggestionProvider;

  Parameter(Class<T> type) {
    Preconditions.checkArgument(type != null, "type cannot be null");
    this.TYPE = type;
  }

  public Parameter<T> serializer(StringSerializer<T> serializer) {
    Preconditions.checkArgument(serializer != null, "string serializer cannot be null");
    this.serializer = serializer;
    return this;
  }


  public Parameter<T> validator(Validator validator) {
    Preconditions.checkArgument(validator != null, "validator cannot be null");
    this.validator = validator;
    return this;
  }

  public Parameter<T> suggester(Console.SuggestionProvider suggestionProvider) {
    Preconditions.checkArgument(suggestionProvider != null, "suggestion provider cannot be null");
    this.suggestionProvider = suggestionProvider;
    return this;
  }

  public boolean canSerialize() {
    return serializer != null;
  }

  public boolean canValidate() {
    return validator != null;
  }

  public boolean canSuggest() {
    return suggestionProvider != null;
  }

  public Class<T> getType() {
    return TYPE;
  }

  @Override
  public String toString() {
    return "<" + TYPE.getSimpleName() + ">";
  }

  @NonNull
  @Override
  public String serialize(@NonNull T obj) {
    if (serializer == null) {
      throw new UnsupportedOperationException(this + " is not serializable");
    }

    return serializer.serialize(obj);
  }

  @NonNull
  @Override
  public T deserialize(@NonNull String str) {
    if (serializer == null) {
      throw new UnsupportedOperationException(this + " is not deserializable");
    }

    return serializer.deserialize(str);
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (validator == null) {
      throw new UnsupportedOperationException(this + " is not validatable");
    }

    validator.validate(obj);
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    if (validator == null) {
      throw new UnsupportedOperationException(this + " is not validatable");
    }

    return validator.isValid(obj);
  }

  @Override
  public int suggest(Console console, CharSequence buffer, String[] args, int arg) {
    if (suggestionProvider == null) {
      throw new UnsupportedOperationException(this + " cannot process console input");
    }

    return suggestionProvider.suggest(console, buffer, args, arg);
  }
}
