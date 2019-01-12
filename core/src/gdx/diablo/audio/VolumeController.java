package gdx.diablo.audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public interface VolumeController<T> {

  boolean isEnabled();
  void setEnabled(boolean enabled);

  float getVolume();
  void setVolume(float volume);

  void manage(@NonNull WeakReference<T> sound);
  boolean remove(@Nullable Object obj);

}
