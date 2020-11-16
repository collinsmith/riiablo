package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.SoundEmitter;

@All({SoundEmitter.class, Position.class})
public class SoundEmitterHandler extends IteratingSystem {
  protected ComponentMapper<SoundEmitter> mSoundEmitter;
  protected ComponentMapper<Position> mPosition;

  @Override
  protected void dispose() {
    IntBag entities = getEntityIds();
    for (int i = 0, size = entities.size(); i < size; i++) {
      int id = entities.get(i);
      unload(id);
    }
  }

  @Override
  protected void process(int entityId) {
    SoundEmitter soundEmitter = mSoundEmitter.get(entityId);
    if (!soundEmitter.sound.isLoaded()) return;
    Vector2 position = mPosition.get(entityId).position;
    Vector2 src = mPosition.get(Riiablo.game.player).position;
    float dst = src.dst(position);
    float volume = 1 - (dst / 20f);
    volume = Math.max(volume, 0);
    volume = soundEmitter.interpolator.apply(volume);
    soundEmitter.sound.setVolume(volume);
    // TODO: volume = volume / sound.initialVolume
  }

  // TODO: ...
  private void unload(int id) {
  }
}
