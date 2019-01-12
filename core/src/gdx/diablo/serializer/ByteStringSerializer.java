package gdx.diablo.serializer;

import android.support.annotation.NonNull;

public enum ByteStringSerializer implements StringSerializer<Byte> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Byte obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Byte deserialize(@NonNull String string) {
    try {
      return Byte.parseByte(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
