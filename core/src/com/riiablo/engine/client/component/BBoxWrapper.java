package com.riiablo.engine.client.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.codec.util.BBox;

@Transient
@PooledWeaver
public class BBoxWrapper extends Component {
  public BBox box;
}
