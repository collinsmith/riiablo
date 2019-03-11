package com.riiablo.entity;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.item.Item;
import com.riiablo.map.DT1;

public class ItemHolder extends Entity {

  Item item;

  AssetDescriptor<DC6> flippyDescriptor;
  DC flippy;

  public ItemHolder(Item item) {
    super(Type.ITM, "item", null);
    this.item = item;
    name(item.getName());

    flippyDescriptor = new AssetDescriptor<>(Type.ITM.PATH + "\\" + item.base.flippyfile + ".dc6", DC6.class);
  }

  @Override
  public boolean isSelectable() {
    return animation.isFinished();
  }

  @Override
  public float getLabelOffset() {
    return DT1.Tile.SUBTILE_HEIGHT;
  }

  @Override
  protected void updateCOF() {
    Riiablo.assets.load(flippyDescriptor);
    Riiablo.assets.finishLoadingAsset(flippyDescriptor);
    flippy = Riiablo.assets.get(flippyDescriptor);
    byte packedTransform = (byte) ((item.base.Transform << 5) | (item.charColorIndex & 0x1F));
    animation = Animation.builder()
        .layer(flippy, BlendMode.ID, packedTransform)
        .build();
    animation.setLooping(false);
    animation.updateBox();
    animation.addAnimationListener(item.base.dropsfxframe, new Animation.AnimationListener() {
      @Override
      public void onTrigger(Animation animation, int frame) {
        Riiablo.audio.play(item.base.dropsound, true);
        animation.removeAnimationListener(frame, this);
      }
    });

    Riiablo.audio.play("item_flippy", true);
    dirty = Dirty.NONE;
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {}
}
