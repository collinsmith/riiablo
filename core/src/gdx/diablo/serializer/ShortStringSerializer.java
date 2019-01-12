package gdx.diablo.serializer;

import android.support.annotation.NonNull;

public enum ShortStringSerializer implements StringSerializer<Short> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Short obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Short deserialize(@NonNull String string) {
    try {
      return Short.parseShort(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
