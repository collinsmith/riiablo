package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.DC6;
import com.riiablo.item.Item;

public class ItemComponent implements Component, Pool.Poolable {
  public Item item;
  public AssetDescriptor<DC6> flippyDescriptor;

  @Override
  public void reset() {
    item = null;
    flippyDescriptor = null;
  }
}
