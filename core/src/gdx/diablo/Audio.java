package gdx.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectMap;

import gdx.diablo.codec.excel.Sounds;

public class Audio {
  private static final String TAG = "Audio";
  private static final boolean DEBUG = true;

  private static final String GLOBAL = "data\\global\\sfx\\";
  private static final String LOCAL  = "data\\local\\sfx\\";

  private final AssetManager assets;
  private final ObjectMap<Sounds.Entry, AssetDescriptor<?>> descriptors = new ObjectMap<>();
  private final ObjectFloatMap<Sounds.Entry> playing = new ObjectFloatMap<>();

  public Audio(AssetManager assets) {
    this.assets = assets;
  }

  public synchronized void play(final Sounds.Entry sound, boolean global) {
    if (sound.FileName.isEmpty()) return;
    // TODO: Fix memory leak and dispose sound after playing
    // TODO: Support group size
    // TODO: global vs local sounds


    if (sound.Stream) {
      Music s;
      AssetDescriptor<Music> descriptor = (AssetDescriptor<Music>) descriptors.get(sound);
      if (descriptor == null) {
        descriptor = new AssetDescriptor<>((global ? GLOBAL : LOCAL) + sound.FileName, Music.class);
        descriptors.put(sound, descriptor);
        assets.load(descriptor);
        assets.finishLoadingAsset(descriptor);

        s = assets.get(descriptor);
        s.setVolume(sound.Volume / 255f);
        s.play();
      } else {
        s = assets.get(descriptor);
      }

      if (sound.Defer_Inst && s.isPlaying()) {
        return;
      }

      s.play();
    } else {
      final Sound s;
      AssetDescriptor<Sound> descriptor = (AssetDescriptor<Sound>) descriptors.get(sound);
      if (descriptor == null) {
        descriptor = new AssetDescriptor<>((global ? GLOBAL : LOCAL) + sound.FileName, Sound.class);
        descriptors.put(sound, descriptor);
        assets.load(descriptor);
        assets.finishLoadingAsset(descriptor);

        s = assets.get(descriptor);
      } else {
        s = assets.get(descriptor);
      }

      long id = s.play(sound.Volume / 255f);
      if (id == -1) {
        Gdx.app.postRunnable(new Runnable() {
          @Override
          public void run() {
            s.play(sound.Volume / 255f);
          }
        });
      }
    }
  }

  public void play(int id, boolean global) {
    Sounds.Entry sound = Diablo.files.Sounds.get(id);
    play(sound, global);
  }

  public int play(String id, boolean global) {
    if (id.isEmpty()) return 0;
    Sounds.Entry sound = Diablo.files.Sounds.get(id);
    if (sound == null) return 0;
    play(sound, global);
    return sound.Index;
  }

}
