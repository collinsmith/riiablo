package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.MissileComponent;
import com.riiablo.graphics.BlendMode;

public class MissileLoaderSystem extends IteratingSystem {
  private final ComponentMapper<MissileComponent> missileComponent = ComponentMapper.getFor(MissileComponent.class);

  public MissileLoaderSystem() {
    super(Family.all(MissileComponent.class).exclude(AnimationComponent.class, BBoxComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    MissileComponent missileComponent = this.missileComponent.get(entity);
    if (!Riiablo.assets.isLoaded(missileComponent.missileDescriptor)) return;

    Missiles.Entry missile = missileComponent.missile;
    DCC celFile = Riiablo.assets.get(missileComponent.missileDescriptor);

    int blendMode;
    switch (missile.Trans) {
      case 0:  blendMode = BlendMode.ID; break;
      case 1:  blendMode = BlendMode.LUMINOSITY; break;
      default: blendMode = BlendMode.ID; break;
    }

    Animation animation = Animation.builder()
        .layer(celFile, blendMode)
        .build();
    animation.setMode(missile.LoopAnim > 0 ? Animation.Mode.LOOP : Animation.Mode.CLAMP); // TODO: Some are 2 -- special case?
    animation.setFrame(missile.RandStart);
    animation.setFrameDelta(missile.animrate);

    AnimationComponent animationComponent = getEngine().createComponent(AnimationComponent.class);
    animationComponent.animation = animation;
    entity.add(animationComponent);

    BBoxComponent boxComponent = getEngine().createComponent(BBoxComponent.class);
    boxComponent.box = animation.getBox();
    entity.add(boxComponent);
  }
}
