package com.riiablo.engine.client.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;

import java.util.Arrays;

@Transient
@PooledWeaver
public class CofComponentDescriptors extends PooledComponent {
  @SuppressWarnings("unchecked")
  public final AssetDescriptor<? extends DC>[] descriptors = (AssetDescriptor<DC>[]) new AssetDescriptor[COF.Component.NUM_COMPONENTS];

  @Override
  protected void reset() {
    Arrays.fill(descriptors, null);
  }
}
