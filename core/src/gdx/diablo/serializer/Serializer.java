package gdx.diablo.serializer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Serializer<T1, T2> {
  @Nullable
  T2 serialize(@NonNull T1 obj);
  @Nullable
  T1 deserialize(@NonNull T2 obj);
}
