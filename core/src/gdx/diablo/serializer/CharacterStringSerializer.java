package gdx.diablo.serializer;

import android.support.annotation.NonNull;

public enum CharacterStringSerializer implements StringSerializer<Character> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Character obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Character deserialize(@NonNull String string) {
    if (string.length() != 1) {
      throw new SerializeException("character serializations should have a length of 1");
    }

    return string.charAt(0);
  }
}
