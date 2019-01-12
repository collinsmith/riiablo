package gdx.diablo.cvar;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.serializer.SerializeException;
import gdx.diablo.serializer.StringSerializer;
import gdx.diablo.validator.Validator;

public class Cvar<T> implements SuggestionProvider {
  @NonNull
  final String ALIAS;
  @NonNull
  final String DESCRIPTION;
  @NonNull
  final Class<T> TYPE;
  @Nullable
  final T DEFAULT_VALUE;
  @Nullable
  final Validator VALIDATOR;
  @Nullable
  final SuggestionProvider SUGGESTIONS;
  @Nullable
  final StringSerializer<T> SERIALIZER;
  final boolean REQUIRES_RESTART;

  @NonNull
  final Set<StateListener<T>> STATE_LISTENERS = new CopyOnWriteArraySet<>();

  @Nullable
  private T value;
  private boolean isLoaded;

  private Cvar(Cvar.Builder<T> builder) {
    ALIAS            = Strings.nullToEmpty(builder.alias);
    DESCRIPTION      = Strings.nullToEmpty(builder.description);
    TYPE             = builder.TYPE;
    DEFAULT_VALUE    = builder.defaultValue;
    VALIDATOR        = builder.validator;
    SUGGESTIONS      = builder.suggestions;
    SERIALIZER       = builder.serializer;
    REQUIRES_RESTART = builder.requiresRestart;

    value = DEFAULT_VALUE;
    isLoaded = false;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(value);
  }

  @NonNull
  public String getAlias() {
    return ALIAS;
  }

  @NonNull
  public String getDescription() {
    return DESCRIPTION;
  }

  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  @Nullable
  public T getDefault() {
    return DEFAULT_VALUE;
  }

  public boolean requiresRestart() {
    return REQUIRES_RESTART;
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  public boolean hasSerializer() {
    return SERIALIZER != null;
  }

  public StringSerializer<T> getSerializer() {
    return SERIALIZER;
  }

  @Nullable
  public T get() {
    return value;
  }

  public void set(@Nullable T value) {
    final T prev = this.value;
    if (ObjectUtils.equals(prev, value)) {
      return;
    }

    if (VALIDATOR != null) VALIDATOR.validate(value);

    this.value = value;
    if (isLoaded) {
      for (StateListener<T> l : STATE_LISTENERS) l.onChanged(this, prev, value);
    } else {
      isLoaded = true;
      for (StateListener<T> l : STATE_LISTENERS) l.onLoaded(this, value);
    }
  }

  public void set(@NonNull String str) {
    try {
      if (SERIALIZER == null) {
        throw new SerializeException(ALIAS + " does not have a serializer attached.");
      }

      T value = SERIALIZER.deserialize(str);
      set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, RuntimeException.class);
      throw new RuntimeException(t);
    }
  }

  public void set(@NonNull String str, @NonNull StringSerializer deserializer) {
    try {
      T value = ((StringSerializer<T>) deserializer).deserialize(str);
      set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, RuntimeException.class);
      throw new RuntimeException(t);
    }
  }

  public void reset() {
    if (!ObjectUtils.equals(value, DEFAULT_VALUE)) {
      T prev = value;
      value = DEFAULT_VALUE;
      for (StateListener<T> l : STATE_LISTENERS) l.onChanged(this, prev, value);
    }
  }

  @Override
  public Collection<String> suggest(@NonNull String str) {
    if (SUGGESTIONS == null) return Collections.emptyList();
    return SUGGESTIONS.suggest(str);
  }

  public boolean addStateListener(StateListener<T> l) {
    boolean added = STATE_LISTENERS.add(l);
    l.onLoaded(this, value);
    return added;
  }

  public boolean containsStateListener(Object o) {
    return o != null && STATE_LISTENERS.contains(o);
  }

  public boolean removeStateListener(Object o) {
    return o != null && STATE_LISTENERS.remove(o);
  }

  public boolean clearStateListeners() {
    boolean empty = STATE_LISTENERS.isEmpty();
    STATE_LISTENERS.clear();
    return !empty;
  }

  public interface StateListener<T> {
    void onChanged(Cvar<T> cvar, T from, T to);
    void onLoaded(Cvar<T> cvar, T to);
  }

  public static <T> Builder<T> builder(Class<T> type) {
    return new Builder<>(type);
  }

  public static class Builder<T> {
    @NonNull
    final Class<T> TYPE;
    @Nullable
    String alias;
    @Nullable
    String description;
    @Nullable
    T defaultValue;
    @Nullable
    Validator validator;
    @Nullable
    SuggestionProvider suggestions;
    @Nullable
    StringSerializer<T> serializer;
    boolean requiresRestart = false;

    Builder(@NonNull Class<T> type) {
      TYPE = type;
    }

    public Builder<T> alias(@NonNull String alias) {
      Preconditions.checkNotNull(alias, "alias cannot be null");
      this.alias = alias;
      return this;
    }

    public Builder<T> description(@NonNull String description) {
      Preconditions.checkNotNull(description, "description cannot be null");
      this.description = description;
      return this;
    }

    public Builder<T> defaultValue(T defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public Builder<T> validator(@NonNull Validator validator) {
      Preconditions.checkNotNull(validator, "validator cannot be null");
      this.validator = validator;
      return this;
    }

    public Builder<T> suggestions(@NonNull SuggestionProvider suggestions) {
      Preconditions.checkNotNull(suggestions, "suggestion provider cannot be null");
      this.suggestions = suggestions;
      return this;
    }

    public Builder<T> serializer(@NonNull StringSerializer<T> serializer) {
      Preconditions.checkNotNull(serializer, "serializer cannot be null");
      this.serializer = serializer;
      return this;
    }

    public Builder<T> requiresRestart() {
      this.requiresRestart = true;
      return this;
    }

    public Cvar<T> build() {
      return new Cvar<>(this);
    }
  }
}
