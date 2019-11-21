package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.ItemComponent;
import com.riiablo.engine.component.LabelComponent;
import com.riiablo.graphics.BlendMode;

public class ItemLoaderSystem extends IteratingSystem {
  private final ComponentMapper<ItemComponent> itemComponent = ComponentMapper.getFor(ItemComponent.class);

  public ItemLoaderSystem() {
    super(Family.all(ItemComponent.class).exclude(AnimationComponent.class, BBoxComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    ItemComponent itemComponent = this.itemComponent.get(entity);
    if (!Riiablo.assets.isLoaded(itemComponent.flippyDescriptor)) return;

    DC6 flippy = Riiablo.assets.get(itemComponent.flippyDescriptor);

    Animation animation = Animation.builder()
        .layer(flippy, BlendMode.ID, (byte) ((itemComponent.item.base.Transform << 5) | (itemComponent.item.charColorIndex & 0x1F)))
        .build();
    animation.setMode(Animation.Mode.CLAMP);

    AnimationComponent animationComponent = getEngine().createComponent(AnimationComponent.class);
    animationComponent.animation = animation;
    entity.add(animationComponent);

    BBox box = flippy.getBox(0, flippy.getNumFramesPerDir() - 1);
    BBoxComponent boxComponent = getEngine().createComponent(BBoxComponent.class);
    boxComponent.box = box;
    entity.add(boxComponent);

    LabelComponent labelComponent = getEngine().createComponent(LabelComponent.class);
    labelComponent.actor = itemComponent.item.details().header;
    labelComponent.offset.set(box.xMin + box.width / 2, -box.yMax + box.height + labelComponent.actor.getHeight() / 2);
    entity.add(labelComponent);
  }
}
