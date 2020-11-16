package com.riiablo.engine.client.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.codec.COF;

@Transient
@PooledWeaver
public class CofDescriptor extends Component {
  public AssetDescriptor<COF> descriptor;
}
