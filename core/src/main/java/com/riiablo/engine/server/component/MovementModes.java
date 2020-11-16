package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class MovementModes extends Component {
  public byte NU;
  public byte WL;
  public byte RN;

  public MovementModes set(byte NU, byte WL, byte RN) {
    this.NU = NU;
    this.WL = WL;
    this.RN = RN;
    return this;
  }
}
