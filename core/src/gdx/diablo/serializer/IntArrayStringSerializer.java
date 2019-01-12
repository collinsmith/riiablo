package gdx.diablo.serializer;

import com.google.common.primitives.Ints;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum IntArrayStringSerializer implements StringSerializer<int[]> {
  INSTANCE;

  @NonNull
  @Override
  public String serialize(@NonNull int[] ints) {
    return Arrays.toString(ints);
  }

  @NonNull
  @Override
  public int[] deserialize(@NonNull String string) {
    if (!string.matches("\\[(\\d+,\\s)*\\d+\\]")) {
      throw new SerializeException(String.format("Invalid serialized format: \"%s\"", string));
    }

    List<Integer> ints = new ArrayList<>(2);
    string = string.substring(1, string.length() - 1);
    for (String integer : string.split(", ")) {
      ints.add(Integer.parseInt(integer));
    }

    return Ints.toArray(ints);
  }
}
