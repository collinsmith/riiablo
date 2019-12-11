package com.riiablo.engine.client.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.codec.COF;

@Transient
@PooledWeaver
public class CofWrapper extends Component {
  public COF cof;
}
