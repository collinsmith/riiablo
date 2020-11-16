package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Exclude;
import com.artemis.systems.IteratingSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC6;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.BBoxWrapper;
import com.riiablo.engine.client.component.Label;
import com.riiablo.engine.client.component.Selectable;
import com.riiablo.engine.server.component.Item;
import com.riiablo.graphics.BlendMode;
import com.riiablo.map.DT1;

@All(Item.class)
@Exclude({AnimationWrapper.class, BBoxWrapper.class, Label.class})
public class ItemLoader extends IteratingSystem {
  protected ComponentMapper<Item> mItem;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<BBoxWrapper> mBBoxWrapper;
  protected ComponentMapper<Label> mLabel;
  protected ComponentMapper<Selectable> mSelectable;

  private final BBox MIN_BBOX = new BBox() {{
    xMin = -DT1.Tile.SUBTILE_WIDTH50;
    yMin = -DT1.Tile.SUBTILE_HEIGHT50;
    width = DT1.Tile.SUBTILE_WIDTH;
    height = DT1.Tile.SUBTILE_HEIGHT;
    xMax = width + xMin;
    yMax = height + yMin;
  }};

  @Override
  protected void process(int entityId) {
    Item item = mItem.get(entityId);
    if (!Riiablo.assets.isLoaded(item.flippyDescriptor)) return;
    DC6 flippy = Riiablo.assets.get(item.flippyDescriptor);

    Animation animation = mAnimationWrapper.create(entityId).animation;
    animation.edit()
        .layer(flippy, BlendMode.ID, (byte) ((item.item.base.Transform << 5) | (item.item.wrapper.charColorIndex & 0x1F)))
        .build();
    animation.setMode(Animation.Mode.CLAMP);

    BBox box = animation.getBox();
    box.set(flippy.getBox(0, flippy.getNumFramesPerDir() - 1));
    if (box.width < MIN_BBOX.width || box.height < MIN_BBOX.height) {
      box.xMin = -MIN_BBOX.width / 2;
      box.height = MIN_BBOX.height;
      box.width = MIN_BBOX.width;
      box.yMax = box.yMin + box.height;
      box.xMax = box.xMin + box.width;
    }
    mBBoxWrapper.create(entityId).box = box;
    mSelectable.create(entityId);

    Label label = mLabel.create(entityId);
    label.offset.set(box.xMin + box.width / 2, -box.yMax + box.height);
    label.actor = item.item.header();
    label.actor.setUserObject(entityId);
  }
}
