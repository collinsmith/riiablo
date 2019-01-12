package gdx.diablo.serializer;

import android.support.annotation.NonNull;

public enum LongStringSerializer implements StringSerializer<Long> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Long obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Long deserialize(@NonNull String string) {
    try {
      return Long.parseLong(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
