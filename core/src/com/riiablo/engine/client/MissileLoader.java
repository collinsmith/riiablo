package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Exclude;
import com.artemis.systems.IteratingSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.BBoxWrapper;
import com.riiablo.engine.server.component.Missile;
import com.riiablo.graphics.BlendMode;

@All(Missile.class)
@Exclude({AnimationWrapper.class, BBoxWrapper.class})
public class MissileLoader extends IteratingSystem {
  protected ComponentMapper<Missile> mMissile;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<BBoxWrapper> mBBoxWrapper;

  @Override
  protected void process(int entityId) {
    Missile missile = mMissile.get(entityId);
    if (!Riiablo.assets.isLoaded(missile.missileDescriptor)) return;
    DCC celFile = Riiablo.assets.get(missile.missileDescriptor);

    Missiles.Entry entry = missile.missile;

    int blendMode;
    switch (entry.Trans) {
      case 0:  blendMode = BlendMode.ID; break;
      case 1:  blendMode = BlendMode.LUMINOSITY; break;
      default: blendMode = BlendMode.ID; break;
    }

    Animation animation = mAnimationWrapper.create(entityId).animation;
    animation.edit()
        .layer(celFile, blendMode)
        .build();
    animation.setMode(entry.LoopAnim > 0 ? Animation.Mode.LOOP : Animation.Mode.CLAMP); // TODO: Some are 2 -- special case?
    animation.setFrame(entry.RandStart);
    animation.setFrameDelta(entry.animrate);
    animation.setDirection(0);

    mBBoxWrapper.create(entityId).box = animation.getBox();
  }
}
