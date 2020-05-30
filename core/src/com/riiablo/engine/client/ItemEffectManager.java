package com.riiablo.engine.client;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.server.component.Item;

@All({Item.class, AnimationWrapper.class})
public class ItemEffectManager extends BaseEntitySystem {
  protected ComponentMapper<Item> mItem;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;

  @Override
  protected void inserted(int entityId) {
    com.riiablo.item.Item item = mItem.get(entityId).item;
    final String sound = item.getDropSound();
    mAnimationWrapper.get(entityId).animation.addAnimationListener(item.getDropFxFrame(), new Animation.AnimationListener() {
      @Override
      public void onTrigger(Animation animation, int frame) {
        Riiablo.audio.play(sound, true);
        animation.removeAnimationListener(frame, this);
      }
    });

    Riiablo.audio.play("item_flippy", true);
  }

  @Override
  protected void processSystem() {}
}
