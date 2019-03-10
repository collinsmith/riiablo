package com.riiablo.entity;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
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
    animation = Animation.builder()
        .layer(flippy)
        .build();
    animation.setLooping(false);
    animation.updateBox();
    animation.addAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onFinished(Animation animation) {
        Riiablo.audio.play(item.base.dropsound, true);
        animation.removeAnimationListener(this);
      }
    });

    Riiablo.audio.play("item_flippy", true);
    dirty = Dirty.NONE;
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {}
}
