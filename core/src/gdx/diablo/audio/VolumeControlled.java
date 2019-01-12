package gdx.diablo.audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface VolumeControlled<T> {

  void setVolumeController(@NonNull VolumeController<T> controller);

  @Nullable
  VolumeController<T> getVolumeController();

}
