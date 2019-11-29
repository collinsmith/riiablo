package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.component.AudioEmitterComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.map.RenderSystem;

public class AudioEmitterSystem extends IteratingSystem {
  private final ComponentMapper<AudioEmitterComponent> audioEmitterComponent = ComponentMapper.getFor(AudioEmitterComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);

  private RenderSystem renderer;
  private Entity src;

  public AudioEmitterSystem(RenderSystem renderer) {
    super(Family.all(AudioEmitterComponent.class, PositionComponent.class).get());
    this.renderer = renderer;
  }

  @Override
  public void update(float delta) {
    src = renderer.getSrc();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    AudioEmitterComponent audioEmitterComponent = this.audioEmitterComponent.get(entity);
    if (!audioEmitterComponent.sound.isLoaded()) return;
    Vector2 position = this.positionComponent.get(entity).position;
    Vector2 src = this.positionComponent.get(this.src).position;
    float dst = src.dst(position);
    float volume = 1 - (dst / 20f);
    volume = Math.max(volume, 0);
    volume = audioEmitterComponent.interpolator.apply(volume);
    audioEmitterComponent.sound.setVolume(volume);
    // TODO: volume = volume / sound.initialVolume
  }
}
