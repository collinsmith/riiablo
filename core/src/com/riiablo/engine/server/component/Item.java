package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.codec.DC6;

@Transient
@PooledWeaver
public class Item extends Component {
  public com.riiablo.item.Item item;
  public AssetDescriptor<DC6> flippyDescriptor;

  public Item set(com.riiablo.item.Item item) {
    this.item = item;
    this.flippyDescriptor = new AssetDescriptor<>(Class.Type.ITM.PATH + '\\' + item.getFlippyFile() + ".dc6", DC6.class);
    return this;
  }
}
