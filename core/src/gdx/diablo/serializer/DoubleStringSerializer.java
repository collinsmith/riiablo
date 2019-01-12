package gdx.diablo.serializer;

import android.support.annotation.NonNull;

public enum DoubleStringSerializer implements StringSerializer<Double> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Double obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Double deserialize(@NonNull String string) {
    try {
      return Double.parseDouble(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
