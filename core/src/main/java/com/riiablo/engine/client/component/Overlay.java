package com.riiablo.engine.client.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

import com.badlogic.gdx.assets.AssetDescriptor;

import com.riiablo.codec.Animation;
import com.riiablo.codec.DC;
import com.riiablo.codec.DCC;

@Transient
@PooledWeaver
public class Overlay extends PooledComponent {
  public final Animation animation = Animation.newAnimation();
  public AssetDescriptor<? extends DC> assetDescriptor;
  public com.riiablo.codec.excel.Overlay.Entry entry;
  public boolean isLoaded;

  public Overlay set(com.riiablo.codec.excel.Overlay.Entry overlay) {
    this.entry = overlay;
    this.assetDescriptor = new AssetDescriptor<>("data\\global\\overlays\\" + overlay.Filename + ".dcc", DCC.class);
    this.isLoaded = false;
    return this;
  }

  @Override
  protected void reset() {
    animation.reset();
    assetDescriptor = null;
    entry = null;
    isLoaded = false;
  }
}
